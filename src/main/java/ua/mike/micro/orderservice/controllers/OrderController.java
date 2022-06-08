package ua.mike.micro.orderservice.controllers;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ua.mike.micro.orderservice.dto.OrderDto;
import ua.mike.micro.orderservice.services.OrderService;

import javax.validation.Valid;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID placeOrder(@Valid @RequestBody OrderDto dto) {
        return service.newOrder(dto);
    }

    @GetMapping("/{id}")
    public OrderDto getOrder(@PathVariable UUID id) {
        return service.getOrder(id);
    }

    @PutMapping("/{id}/pickup")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void pickupOrder(@PathVariable UUID id) {
        service.pickupOrder(id);
    }

    @PutMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelOrder(@PathVariable UUID id) {
        service.cancelOrder(id);
    }
}
