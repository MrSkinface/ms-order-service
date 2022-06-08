package ua.mike.micro.orderservice.statemachine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.service.StateMachineService;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Service;
import ua.mike.micro.orderservice.models.OrderEvent;
import ua.mike.micro.orderservice.models.OrderStatus;
import ua.mike.micro.orderservice.repo.OrderRepo;

import java.util.Optional;
import java.util.UUID;

import static ua.mike.micro.orderservice.statemachine.OrderStateMachineConfig.ORDER_ID_HEADER;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderStateMachineServiceImpl implements OrderStateMachineService<OrderStatus, OrderEvent> {

    private final StateMachineService<OrderStatus, OrderEvent> service;
    private final OrderRepo repo;

    @Override
    public StateMachine<OrderStatus, OrderEvent> acquireStateMachine(String machineId) {
        final var sm = service.acquireStateMachine(machineId, false);
        sm.getStateMachineAccessor().doWithAllRegions(sma -> sma.addStateMachineInterceptor(new StateMachineInterceptorAdapter<>() {
            @Override
            public void preStateChange(State<OrderStatus, OrderEvent> state, Message<OrderEvent> message,
                                       Transition<OrderStatus, OrderEvent> transition, StateMachine<OrderStatus, OrderEvent> stateMachine,
                                       StateMachine<OrderStatus, OrderEvent> rootStateMachine) {
                log.debug("Pre-State Change");
                Optional.ofNullable(message)
                        .flatMap(msg -> Optional.ofNullable(msg.getHeaders().get(ORDER_ID_HEADER, String.class)))
                        .ifPresent(orderId -> {
                            log.debug("Saving state for order id: " + orderId + " Status: " + state.getId());
                            repo.findById(UUID.fromString(orderId)).ifPresent(order -> {
                                order.setStatus(state.getId());
                                repo.saveAndFlush(order);
                            });
                        });
            }
        }));
        sm.startReactively().block();
        return sm;
    }

    @Override
    public void releaseStateMachine(String machineId) {
        service.releaseStateMachine(machineId);
    }

    @Override
    public void sendEvent(UUID id, OrderEvent event) {
        try {
            final var sm = this.acquireStateMachine(id.toString());
            sm.sendEvent(MessageBuilder.withPayload(event)
                    .setHeader(ORDER_ID_HEADER, id.toString())
                    .build());
        } finally {
            this.releaseStateMachine(id.toString());
        }
    }
}
