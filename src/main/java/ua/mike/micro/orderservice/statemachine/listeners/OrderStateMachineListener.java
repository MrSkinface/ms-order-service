package ua.mike.micro.orderservice.statemachine.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;
import ua.mike.micro.orderservice.models.OrderEvent;
import ua.mike.micro.orderservice.models.OrderStatus;
import ua.mike.micro.orderservice.repo.OrderRepo;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderStateMachineListener implements StateMachineListener<OrderStatus, OrderEvent> {

    private final OrderRepo repo;

    @Override
    public void stateChanged(State<OrderStatus, OrderEvent> from, State<OrderStatus, OrderEvent> to) {
        log.debug("Changed state from {} to {}", from != null ? from.getId() : "null", to.getId());
    }

    @Override
    public void stateEntered(State<OrderStatus, OrderEvent> state) {

    }

    @Override
    public void stateExited(State<OrderStatus, OrderEvent> state) {

    }

    @Override
    public void eventNotAccepted(Message<OrderEvent> event) {
        log.debug("Message not accepted: {} {}", event.getPayload(), event.getHeaders());
    }

    @Override
    public void transition(Transition<OrderStatus, OrderEvent> transition) {

    }

    @Override
    public void transitionStarted(Transition<OrderStatus, OrderEvent> transition) {

    }

    @Override
    public void transitionEnded(Transition<OrderStatus, OrderEvent> transition) {

    }

    @Override
    public void stateMachineStarted(StateMachine<OrderStatus, OrderEvent> stateMachine) {
        log.debug("OrderStateMachine started");
    }

    @Override
    public void stateMachineStopped(StateMachine<OrderStatus, OrderEvent> stateMachine) {
        log.debug("OrderStateMachine stopped");
    }

    @Override
    public void stateMachineError(StateMachine<OrderStatus, OrderEvent> stateMachine, Exception exception) {

    }

    @Override
    public void extendedStateChanged(Object key, Object value) {

    }

    @Override
    public void stateContext(StateContext<OrderStatus, OrderEvent> stateContext) {
    }
}
