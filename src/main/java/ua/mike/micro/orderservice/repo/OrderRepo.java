package ua.mike.micro.orderservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.mike.micro.orderservice.models.Order;
import ua.mike.micro.orderservice.models.OrderStatus;

import java.util.List;
import java.util.UUID;

public interface OrderRepo extends JpaRepository<Order, UUID> {

    List<Order> findByStatusIn(OrderStatus ... status);
}
