package ua.mike.micro.orderservice.services.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ua.mike.micro.jms.Queue;
import ua.mike.micro.orderservice.dto.events.AllocateOrderRequest;
import ua.mike.micro.orderservice.dto.events.ValidateOrderRequest;
import ua.mike.micro.orderservice.dto.mappers.OrderMapper;
import ua.mike.micro.orderservice.models.OrderStatus;
import ua.mike.micro.orderservice.repo.OrderRepo;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Created by mike on 09.06.2022 14:19
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledReProcessor {

    private final OrderRepo repo;
    private final JmsTemplate jms;
    private final OrderMapper mapper;

    @Scheduled(fixedRate = 10 * 1000)
    public void searchForPendingStates() {
        repo.findByStatusIn(OrderStatus.VALIDATION_PENDING, OrderStatus.ALLOCATION_PENDING).forEach(order -> {
            if (order.getUpdateDateTime().isBefore(OffsetDateTime.now().minus(1, ChronoUnit.MINUTES))) {
                log.debug("Found stuck order {} : status: {} last_updated: {}", order.getId(), order.getStatus(), order.getUpdateDateTime());
                if (OrderStatus.VALIDATION_PENDING.equals(order.getStatus())) {
                    jms.convertAndSend(Queue.VALIDATE_ORDER_QUEUE, ValidateOrderRequest.builder().order(mapper.toDto(order)).build());
                    log.debug("Send for reprocessing validation");
                } else if (OrderStatus.ALLOCATION_PENDING.equals(order.getStatus())) {
                    jms.convertAndSend(Queue.ALLOCATE_ORDER_QUEUE, AllocateOrderRequest.builder().order(mapper.toDto(order)).build());
                    log.debug("Send for reprocessing allocation");
                }
            }
        });
    }
}
