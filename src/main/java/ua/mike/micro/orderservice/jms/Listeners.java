package ua.mike.micro.orderservice.jms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import ua.mike.micro.jms.JmsConsumerActions;
import ua.mike.micro.jms.Queue;
import ua.mike.micro.orderservice.dto.PositionDto;
import ua.mike.micro.orderservice.dto.events.AddedBeerNotification;
import ua.mike.micro.orderservice.dto.events.AllocateOrderResult;
import ua.mike.micro.orderservice.dto.events.ValidateOrderRequest;
import ua.mike.micro.orderservice.dto.events.ValidateOrderResult;
import ua.mike.micro.orderservice.models.Order;
import ua.mike.micro.orderservice.models.OrderEvent;
import ua.mike.micro.orderservice.models.OrderStatus;
import ua.mike.micro.orderservice.repo.OrderRepo;
import ua.mike.micro.orderservice.statemachine.OrderStateMachineService;

import java.util.List;

/**
 * Created by mike on 31.05.2022 16:17
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class Listeners {

    private final OrderStateMachineService<OrderStatus, OrderEvent> sm;
    private final OrderRepo repo;
    private final JmsConsumerActions actions;

    @JmsListener(destination = Queue.VALIDATE_ORDER_RESPONSE_QUEUE)
    public void listenValidationResults(String data) {
        actions.consume(data, ValidateOrderResult.class, result -> {
            log.debug("Validation result for {}: valid => {}", result.getOrderUUID(), result.isValid());
            repo.findById(result.getOrderUUID()).ifPresentOrElse(order -> {
                if (result.isValid()) {
                    sm.sendEvent(result.getOrderUUID(), OrderEvent.VALIDATION_SUCCESS);
                    // todo maybe one of these is redundant
                    sm.sendEvent(result.getOrderUUID(), OrderEvent.ALLOCATE);
                } else {
                    sm.sendEvent(result.getOrderUUID(), OrderEvent.VALIDATION_ERROR);
                }
            }, () -> log.error("Order {} not found", result.getOrderUUID()));
        });
    }

    @JmsListener(destination = Queue.ALLOCATE_ORDER_RESPONSE_QUEUE)
    public void listenAllocationResults(String data) {
        actions.consume(data, AllocateOrderResult.class, result -> {
            log.debug("Allocation result for {}: error => {} pending_inventory => {}",
                    result.getOrder(), result.isAllocationError(), result.isPendingInventory());
            final var id = result.getOrder().getId();
            repo.findById(id).ifPresentOrElse(order -> {
                if (!result.isAllocationError() && !result.isPendingInventory()){
                    //allocated normally
                    this.updateAllocatedQty(order, result.getOrder().getLines());
                    sm.sendEvent(id, OrderEvent.ALLOCATION_SUCCESS);
                } else if (!result.isAllocationError() && result.isPendingInventory()) {
                    //pending inventory
                    this.updateAllocatedQty(order, result.getOrder().getLines());
                    sm.sendEvent(id, OrderEvent.ALLOCATION_NO_INVENTORY);
                } else if (result.isAllocationError()){
                    //allocation error
                    sm.sendEvent(id, OrderEvent.ALLOCATION_ERROR);
                }
            }, () -> log.error("Order {} not found", id));
        });
    }

    @JmsListener(destination = Queue.ADDED_BEER_QUEUE)
    public void listenAddedBeerNotifications(String data) {
        actions.consume(data, AddedBeerNotification.class, notification -> {
            log.debug("Got add beer inventory notification");
            repo.findByStatusIn(OrderStatus.INVENTORY_PENDING).stream()
                    .filter(order -> order.getPositions().stream().anyMatch(position -> position.getBeerId().equals(notification.getBeerId())))
                    .forEach(order -> {
                        sm.sendEvent(order.getId(), OrderEvent.ALLOCATE);
                        log.debug("Sent allocation request for: {}", order.getId());
                    });
        });
    }

    private void updateAllocatedQty(Order order, List<PositionDto> lines) {
        order.getPositions().forEach(position -> {
            lines.forEach(line -> {
                if (line.getId().equals(position.getId())) {
                    position.setAllocatedQty(line.getAllocatedQty());
                }
            });
        });
        repo.saveAndFlush(order);
    }
}
