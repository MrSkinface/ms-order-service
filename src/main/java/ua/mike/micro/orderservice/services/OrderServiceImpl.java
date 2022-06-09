package ua.mike.micro.orderservice.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.mike.micro.orderservice.controllers.exceptions.NotFound;
import ua.mike.micro.orderservice.dto.OrderDto;
import ua.mike.micro.orderservice.dto.mappers.OrderMapper;
import ua.mike.micro.orderservice.models.OrderEvent;
import ua.mike.micro.orderservice.models.OrderStatus;
import ua.mike.micro.orderservice.repo.OrderRepo;
import ua.mike.micro.orderservice.statemachine.OrderStateMachineService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepo repo;
    private final OrderStateMachineService<OrderStatus, OrderEvent> sm;
    private final OrderMapper mapper;
    @Override
    public UUID newOrder(OrderDto dto) {
        final var order = mapper.fromDto(dto);
        order.setId(null);
        order.setStatus(OrderStatus.NEW);
        order.getPositions().forEach(position -> position.setOrder(order));
        final var saved = repo.save(order);
        sm.sendEvent(saved.getId(), OrderEvent.VALIDATE);
        return saved.getId();
    }

    @Override
    public List<OrderDto> list() {
        return repo.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDto> list(OrderStatus ... status) {
        return repo.findByStatusIn(status).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDto getOrder(UUID id) {
        return repo.findById(id)
                .map(mapper::toDto)
                .orElseThrow(NotFound::new);
    }

    @Override
    public void pickupOrder(UUID id) {
        repo.findById(id).map(order -> {
                    sm.sendEvent(order.getId(), OrderEvent.PICK_UP);
                    return order;
                }).orElseThrow(NotFound::new);
    }

    @Override
    public void cancelOrder(UUID id) {
        repo.findById(id).map(order -> {
            sm.sendEvent(order.getId(), OrderEvent.CANCEL);
            return order;
        }).orElseThrow(NotFound::new);
    }
}
