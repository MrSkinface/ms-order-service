package ua.mike.micro.orderservice.statemachine;

import org.springframework.statemachine.StateMachine;
import ua.mike.micro.orderservice.models.OrderEvent;

import java.util.UUID;

public interface OrderStateMachineService<S, E> {

    StateMachine<S, E> acquireStateMachine(String machineId);

    void releaseStateMachine(String machineId);

    void sendEvent(UUID id, OrderEvent event);
}
