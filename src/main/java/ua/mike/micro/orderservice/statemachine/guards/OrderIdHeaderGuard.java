package ua.mike.micro.orderservice.statemachine.guards;

import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;
import org.springframework.stereotype.Component;
import ua.mike.micro.orderservice.models.OrderEvent;
import ua.mike.micro.orderservice.models.OrderStatus;

import java.util.Optional;

import static ua.mike.micro.orderservice.statemachine.OrderStateMachineConfig.ORDER_ID_HEADER;

/**
 * Created by mike on 31.05.2022 09:57
 */
@Slf4j
@Component
public class OrderIdHeaderGuard implements Guard<OrderStatus, OrderEvent> {

    @Override
    public boolean evaluate(StateContext<OrderStatus, OrderEvent> context) {
        if (Optional.ofNullable(context.getMessageHeader(ORDER_ID_HEADER)).isPresent())
            return true;
        log.warn("Header [{}] is missing, payload: {}", ORDER_ID_HEADER, context.getMessage().getPayload());
        return false;
    }
}
