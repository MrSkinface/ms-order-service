package ua.mike.micro.orderservice.statemachine;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import redis.embedded.RedisServer;
import ua.mike.micro.orderservice.models.Order;
import ua.mike.micro.orderservice.models.OrderEvent;
import ua.mike.micro.orderservice.models.OrderStatus;
import ua.mike.micro.orderservice.models.Position;
import ua.mike.micro.orderservice.repo.OrderRepo;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ua.mike.micro.orderservice.statemachine.OrderStateMachineConfig.ORDER_ID_HEADER;

@SpringBootTest
@SuppressWarnings("all")
@Slf4j
class OrderStateMachineConfigTest {

    private static RedisServer redis;

    @BeforeAll
    static void beforeAll() throws Exception {
        redis = new RedisServer(6379);
        redis.start();
    }

    @AfterAll
    static void afterAll() {
        redis.stop();
    }

    @Autowired
    private OrderStateMachineService<OrderStatus, OrderEvent> service;

    @Autowired
    private OrderRepo repo;

    private Order order;

    @BeforeEach
    void setUp() {
        final var tmp = Order.builder().status(OrderStatus.NEW).build();
        tmp.setPositions(
                List.of(
                        Position.builder()
                                .beerId(UUID.randomUUID())
                                .order(tmp)
                                .orderedQty(100)
                                .build()
                )
        );
        order = repo.save(tmp);
        assertTrue(repo.findById(order.getId()).isPresent());
        assertEquals(OrderStatus.NEW, repo.findById(order.getId()).map(Order::getStatus).get());
    }

    @Test
    void testHappyPath() throws Exception {
        StateMachineTestPlanBuilder.<OrderStatus, OrderEvent>builder()
                .stateMachine(service.acquireStateMachine(order.getId().toString()))
                .defaultAwaitTime(3)
                .step()
                .expectState(OrderStatus.NEW)
                .and().step()
                .sendEvent(MessageBuilder.withPayload(OrderEvent.VALIDATE)
                        .setHeader(ORDER_ID_HEADER, order.getId().toString()).build())
                .expectState(OrderStatus.VALIDATION_PENDING)
                .expectStateChanged(1)
                .expectTransition(1)
                .and().step()
                .sendEvent(MessageBuilder.withPayload(OrderEvent.VALIDATION_SUCCESS)
                        .setHeader(ORDER_ID_HEADER, order.getId().toString()).build())
                .expectState(OrderStatus.VALIDATED)
                .expectStateChanged(1)
                .expectTransition(1)
                .and().step()
                .sendEvent(MessageBuilder.withPayload(OrderEvent.ALLOCATE)
                        .setHeader(ORDER_ID_HEADER, order.getId().toString()).build())
                .expectState(OrderStatus.ALLOCATION_PENDING)
                .expectStateChanged(1)
                .expectTransition(1)
                .and().step()
                .sendEvent(MessageBuilder.withPayload(OrderEvent.ALLOCATION_SUCCESS)
                        .setHeader(ORDER_ID_HEADER, order.getId().toString()).build())
                .expectState(OrderStatus.ALLOCATED)
                .expectStateChanged(1)
                .expectTransition(1)
                .and().step()
                .sendEvent(MessageBuilder.withPayload(OrderEvent.PICK_UP)
                        .setHeader(ORDER_ID_HEADER, order.getId().toString()).build())
                .expectState(OrderStatus.PICKED_UP)
                .expectStateChanged(1)
                .expectTransition(1)
                .and().build().test();
        service.releaseStateMachine(order.getId().toString());
        final var sm = service.acquireStateMachine(order.getId().toString());
        assertEquals(OrderStatus.PICKED_UP, sm.getState().getId());
        service.releaseStateMachine(sm.getId());
        assertTrue(repo.findById(order.getId()).isPresent());
        assertEquals(OrderStatus.PICKED_UP, repo.findById(order.getId()).map(Order::getStatus).get());
    }

    @Test
    void testOrderCancel() throws Exception {
        StateMachineTestPlanBuilder.<OrderStatus, OrderEvent>builder()
                .stateMachine(service.acquireStateMachine(order.getId().toString()))
                .defaultAwaitTime(3)
                .step()
                .expectState(OrderStatus.NEW)
                .and().step()
                .sendEvent(MessageBuilder.withPayload(OrderEvent.VALIDATE)
                        .setHeader(ORDER_ID_HEADER, order.getId().toString()).build())
                .expectState(OrderStatus.VALIDATION_PENDING)
                .expectStateChanged(1)
                .expectTransition(1)
                .and().step()
                .sendEvent(MessageBuilder.withPayload(OrderEvent.VALIDATION_SUCCESS)
                        .setHeader(ORDER_ID_HEADER, order.getId().toString()).build())
                .expectState(OrderStatus.VALIDATED)
                .expectStateChanged(1)
                .expectTransition(1)
                .and().step()
                .sendEvent(MessageBuilder.withPayload(OrderEvent.CANCEL)
                        .setHeader(ORDER_ID_HEADER, order.getId().toString()).build())
                .expectState(OrderStatus.CANCELED)
                .expectStateChanged(1)
                .expectTransition(1)
                .and().build().test();
        service.releaseStateMachine(order.getId().toString());
        final var sm = service.acquireStateMachine(order.getId().toString());
        assertEquals(OrderStatus.CANCELED, sm.getState().getId());
        service.releaseStateMachine(sm.getId());
        assertTrue(repo.findById(order.getId()).isPresent());
        assertEquals(OrderStatus.CANCELED, repo.findById(order.getId()).map(Order::getStatus).get());
    }

    @Test
    void testValidationFailed() throws Exception {
        StateMachineTestPlanBuilder.<OrderStatus, OrderEvent>builder()
                .stateMachine(service.acquireStateMachine(order.getId().toString()))
                .defaultAwaitTime(3)
                .step()
                .expectState(OrderStatus.NEW)
                .and().step()
                .sendEvent(MessageBuilder.withPayload(OrderEvent.VALIDATE)
                        .setHeader(ORDER_ID_HEADER, order.getId().toString()).build())
                .expectState(OrderStatus.VALIDATION_PENDING)
                .expectStateChanged(1)
                .expectTransition(1)
                .and().step()
                .sendEvent(MessageBuilder.withPayload(OrderEvent.VALIDATION_ERROR)
                        .setHeader(ORDER_ID_HEADER, order.getId().toString()).build())
                .expectState(OrderStatus.VALIDATION_ERROR)
                .expectStateChanged(1)
                .expectTransition(1)
                .and().build().test();
        service.releaseStateMachine(order.getId().toString());
        final var sm = service.acquireStateMachine(order.getId().toString());
        assertEquals(OrderStatus.VALIDATION_ERROR, sm.getState().getId());
        service.releaseStateMachine(sm.getId());
        assertTrue(repo.findById(order.getId()).isPresent());
        assertEquals(OrderStatus.VALIDATION_ERROR, repo.findById(order.getId()).map(Order::getStatus).get());
    }

    @Test
    void testAllocationFailed() throws Exception {
        StateMachineTestPlanBuilder.<OrderStatus, OrderEvent>builder()
                .stateMachine(service.acquireStateMachine(order.getId().toString()))
                .defaultAwaitTime(3)
                .step()
                .expectState(OrderStatus.NEW)
                .and().step()
                .sendEvent(MessageBuilder.withPayload(OrderEvent.VALIDATE)
                        .setHeader(ORDER_ID_HEADER, order.getId().toString()).build())
                .expectState(OrderStatus.VALIDATION_PENDING)
                .expectStateChanged(1)
                .expectTransition(1)
                .and().step()
                .sendEvent(MessageBuilder.withPayload(OrderEvent.VALIDATION_SUCCESS)
                        .setHeader(ORDER_ID_HEADER, order.getId().toString()).build())
                .expectState(OrderStatus.VALIDATED)
                .expectStateChanged(1)
                .expectTransition(1)
                .and().step()
                .sendEvent(MessageBuilder.withPayload(OrderEvent.ALLOCATE)
                        .setHeader(ORDER_ID_HEADER, order.getId().toString()).build())
                .expectState(OrderStatus.ALLOCATION_PENDING)
                .expectStateChanged(1)
                .expectTransition(1)
                .and().step()
                .sendEvent(MessageBuilder.withPayload(OrderEvent.ALLOCATION_ERROR)
                        .setHeader(ORDER_ID_HEADER, order.getId().toString()).build())
                .expectState(OrderStatus.ALLOCATION_ERROR)
                .expectStateChanged(1)
                .expectTransition(1)
                .and().build().test();
        service.releaseStateMachine(order.getId().toString());
        final var sm = service.acquireStateMachine(order.getId().toString());
        assertEquals(OrderStatus.ALLOCATION_ERROR, sm.getState().getId());
        service.releaseStateMachine(sm.getId());
        assertTrue(repo.findById(order.getId()).isPresent());
        assertEquals(OrderStatus.ALLOCATION_ERROR, repo.findById(order.getId()).map(Order::getStatus).get());
    }
}