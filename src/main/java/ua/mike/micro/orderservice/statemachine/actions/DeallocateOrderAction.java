package ua.mike.micro.orderservice.statemachine.actions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;
import ua.mike.micro.jms.Queue;
import ua.mike.micro.orderservice.dto.events.DeallocateOrderRequest;
import ua.mike.micro.orderservice.dto.mappers.OrderMapper;
import ua.mike.micro.orderservice.models.OrderEvent;
import ua.mike.micro.orderservice.models.OrderStatus;
import ua.mike.micro.orderservice.repo.OrderRepo;

import java.util.Optional;
import java.util.UUID;

import static ua.mike.micro.orderservice.statemachine.OrderStateMachineConfig.ORDER_ID_HEADER;

/**
 * Created by mike on 01.06.2022 21:40
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DeallocateOrderAction implements Action<OrderStatus, OrderEvent> {

    private final OrderRepo repo;
    private final OrderMapper mapper;
    private final JmsTemplate jms;

    @Override
    public void execute(StateContext<OrderStatus, OrderEvent> context) {
        Optional.ofNullable(context.getMessage().getHeaders().get(ORDER_ID_HEADER, String.class)).ifPresent(id -> {
            repo.findById(UUID.fromString(id)).ifPresentOrElse(order -> {
                jms.convertAndSend(Queue.DEALLOCATE_ORDER_QUEUE, DeallocateOrderRequest.builder()
                        .order(mapper.toDto(order)).build());
                log.debug("Sent de-allocation request to queue for id: {}", id);
            }, () -> log.error("Order with id {} not found", id));
        });
    }
}
