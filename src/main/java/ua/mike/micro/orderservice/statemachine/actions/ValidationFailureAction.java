package ua.mike.micro.orderservice.statemachine.actions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;
import ua.mike.micro.orderservice.models.OrderEvent;
import ua.mike.micro.orderservice.models.OrderStatus;

import java.util.Optional;

import static ua.mike.micro.orderservice.statemachine.OrderStateMachineConfig.ORDER_ID_HEADER;

/**
 * Created by mike on 01.06.2022 20:38
 */
@Component
@Slf4j
public class ValidationFailureAction implements Action<OrderStatus, OrderEvent> {

    @Override
    public void execute(StateContext<OrderStatus, OrderEvent> context) {
        Optional.ofNullable(context.getMessage().getHeaders().get(ORDER_ID_HEADER, String.class)).ifPresent(id -> {
            log.debug("ValidationFailureAction for id: {}", id);
        });
    }
}
