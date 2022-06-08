package ua.mike.micro.orderservice.services;

import ua.mike.micro.orderservice.dto.OrderDto;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    UUID newOrder(OrderDto dto);

    List<OrderDto> list();

    OrderDto getOrder(UUID id);

    void pickupOrder(UUID id);

    void cancelOrder(UUID id);
}
