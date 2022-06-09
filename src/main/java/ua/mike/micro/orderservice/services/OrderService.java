package ua.mike.micro.orderservice.services;

import ua.mike.micro.orderservice.dto.OrderDto;
import ua.mike.micro.orderservice.models.OrderStatus;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    UUID newOrder(OrderDto dto);

    List<OrderDto> list();

    List<OrderDto> list(OrderStatus ... status);

    OrderDto getOrder(UUID id);

    void pickupOrder(UUID id);

    void cancelOrder(UUID id);
}
