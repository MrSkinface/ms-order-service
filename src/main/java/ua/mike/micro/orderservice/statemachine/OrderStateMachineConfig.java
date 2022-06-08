package ua.mike.micro.orderservice.statemachine;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.data.redis.RedisPersistingStateMachineInterceptor;
import org.springframework.statemachine.service.DefaultStateMachineService;
import org.springframework.statemachine.service.StateMachineService;
import ua.mike.micro.orderservice.models.OrderEvent;
import ua.mike.micro.orderservice.models.OrderStatus;
import ua.mike.micro.orderservice.statemachine.actions.*;
import ua.mike.micro.orderservice.statemachine.guards.OrderIdHeaderGuard;
import ua.mike.micro.orderservice.statemachine.listeners.OrderStateMachineListener;

import java.util.EnumSet;

@Configuration
@RequiredArgsConstructor
@EnableStateMachineFactory
public class OrderStateMachineConfig extends StateMachineConfigurerAdapter<OrderStatus, OrderEvent> {

    public static final String ORDER_ID_HEADER = "ORDER_ID_HEADER";

    private final OrderStateMachineListener listener;

    private final OrderRedisStateMachineRepository repository;

    private final OrderIdHeaderGuard orderIdHeaderGuard;

    private final ValidateOrderAction validateOrderAction;
    private final ValidationFailureAction validationFailureAction;
    private final AllocationFailureAction allocationFailureAction;
    private final CancelOrderAction cancelOrderAction;
    private final AllocateOrderAction allocateOrderAction;
    private final DeallocateOrderAction deallocateOrderAction;
    private final PickUpAction pickUpAction;

    @Bean
    @SuppressWarnings("all")
    public StateMachineService<OrderStatus, OrderEvent> stateMachineService(
            StateMachineFactory<OrderStatus, OrderEvent> stateMachineFactory) {
        return new DefaultStateMachineService<>(stateMachineFactory, new RedisPersistingStateMachineInterceptor<>(repository));
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<OrderStatus, OrderEvent> config) throws Exception {
        config.withConfiguration()
                .listener(listener)
                .and()
                .withPersistence()
                .runtimePersister(new RedisPersistingStateMachineInterceptor<>(repository));
    }

    @Override
    public void configure(StateMachineStateConfigurer<OrderStatus, OrderEvent> states) throws Exception {
        states.withStates()
                .initial(OrderStatus.NEW)
                .states(EnumSet.allOf(OrderStatus.class))
                .end(OrderStatus.VALIDATION_ERROR)
                .end(OrderStatus.ALLOCATION_ERROR)
                .end(OrderStatus.PICKED_UP)
                .end(OrderStatus.CANCELED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<OrderStatus, OrderEvent> transitions) throws Exception {
        transitions.withExternal()
                .source(OrderStatus.NEW).target(OrderStatus.VALIDATION_PENDING).event(OrderEvent.VALIDATE)
                .guard(orderIdHeaderGuard)
                .action(validateOrderAction)
                .and().withExternal()
                .source(OrderStatus.NEW).target(OrderStatus.CANCELED).event(OrderEvent.CANCEL)
                .guard(orderIdHeaderGuard)
                .action(cancelOrderAction)
                .and().withExternal()
                .source(OrderStatus.VALIDATION_PENDING).target(OrderStatus.VALIDATED).event(OrderEvent.VALIDATION_SUCCESS)
                .guard(orderIdHeaderGuard)
                .and().withExternal()
                .source(OrderStatus.VALIDATION_PENDING).target(OrderStatus.VALIDATION_ERROR).event(OrderEvent.VALIDATION_ERROR)
                .guard(orderIdHeaderGuard)
                .action(validationFailureAction)
                .and().withExternal()
                .source(OrderStatus.VALIDATION_PENDING).target(OrderStatus.CANCELED).event(OrderEvent.CANCEL)
                .guard(orderIdHeaderGuard)
                .action(cancelOrderAction)
                .and().withExternal()
                .source(OrderStatus.VALIDATED).target(OrderStatus.ALLOCATION_PENDING).event(OrderEvent.ALLOCATE)
                .guard(orderIdHeaderGuard)
                .action(allocateOrderAction)
                .and().withExternal()
                .source(OrderStatus.VALIDATED).target(OrderStatus.CANCELED).event(OrderEvent.CANCEL)
                .guard(orderIdHeaderGuard)
                .action(cancelOrderAction)
                .and().withExternal()
                .source(OrderStatus.ALLOCATION_PENDING).target(OrderStatus.ALLOCATED).event(OrderEvent.ALLOCATION_SUCCESS)
                .guard(orderIdHeaderGuard)
                .and().withExternal()
                .source(OrderStatus.ALLOCATION_PENDING).target(OrderStatus.ALLOCATION_ERROR).event(OrderEvent.ALLOCATION_ERROR)
                .guard(orderIdHeaderGuard)
                .action(allocationFailureAction)
                .and().withExternal()
                .source(OrderStatus.ALLOCATION_PENDING).target(OrderStatus.INVENTORY_PENDING).event(OrderEvent.ALLOCATION_NO_INVENTORY)
                .guard(orderIdHeaderGuard)
                .and().withExternal()
                .source(OrderStatus.ALLOCATION_PENDING).target(OrderStatus.CANCELED).event(OrderEvent.CANCEL)
                .guard(orderIdHeaderGuard)
                .action(cancelOrderAction)
                .and().withExternal()
                .source(OrderStatus.INVENTORY_PENDING).target(OrderStatus.ALLOCATION_PENDING).event(OrderEvent.ALLOCATE)
                .guard(orderIdHeaderGuard)
                .action(allocateOrderAction)
                .and().withExternal()
                .source(OrderStatus.ALLOCATED).target(OrderStatus.PICKED_UP).event(OrderEvent.PICK_UP)
                .guard(orderIdHeaderGuard)
                .action(pickUpAction)
                .and().withExternal()
                .source(OrderStatus.ALLOCATED).target(OrderStatus.CANCELED).event(OrderEvent.CANCEL)
                .guard(orderIdHeaderGuard)
                .action(cancelOrderAction)
                .action(deallocateOrderAction)
                .and().withExternal()
                .source(OrderStatus.INVENTORY_PENDING).target(OrderStatus.CANCELED).event(OrderEvent.CANCEL)
                .guard(orderIdHeaderGuard)
                .action(cancelOrderAction)
                .action(deallocateOrderAction);
    }
}
