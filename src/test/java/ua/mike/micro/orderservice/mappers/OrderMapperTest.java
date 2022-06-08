package ua.mike.micro.orderservice.mappers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ua.mike.micro.orderservice.dto.OrderDto;
import ua.mike.micro.orderservice.dto.PositionDto;
import ua.mike.micro.orderservice.dto.mappers.OrderMapper;
import ua.mike.micro.orderservice.dto.mappers.OrderMapperImpl;
import ua.mike.micro.orderservice.dto.mappers.PositionMapperImpl;
import ua.mike.micro.orderservice.models.Order;
import ua.mike.micro.orderservice.models.OrderStatus;
import ua.mike.micro.orderservice.models.Position;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by mike on 31.05.2022 16:50
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        OrderMapperImpl.class,
        PositionMapperImpl.class
})
public class OrderMapperTest {

    @Autowired
    private OrderMapper mapper;

    private OrderDto dto;
    private Order order;

    @BeforeEach
    void setUp() {
        dto = OrderDto.builder()
                .id(UUID.randomUUID())
                .lines(List.of(
                        PositionDto.builder().beerId(UUID.randomUUID()).orderedQty(20).build(),
                        PositionDto.builder().beerId(UUID.randomUUID()).orderedQty(30).build()
                ))
                .build();
        order = Order.builder()
                .id(UUID.randomUUID())
                .status(OrderStatus.NEW)
                .createDateTime(OffsetDateTime.now())
                .updateDateTime(OffsetDateTime.now())
                .positions(List.of(
                        Position.builder().beerId(UUID.randomUUID()).orderedQty(40).build(),
                        Position.builder().beerId(UUID.randomUUID()).orderedQty(50).build()
                ))
                .build();
    }

    @Test
    void toDtoTest() {
        final var dto = mapper.toDto(order);
        assertEquals(order.getId(), dto.getId());
        assertEquals(order.getPositions().size(), dto.getLines().size());
        for (int i = 0; i < order.getPositions().size(); i++) {
            final var pos = order.getPositions().get(i);
            final var posDto = dto.getLines().get(i);
            assertEquals(pos.getBeerId(), posDto.getBeerId());
            assertEquals(pos.getOrderedQty(), posDto.getOrderedQty());
        }
    }

    @Test
    void fromDtoTest() {
        final var order = mapper.fromDto(dto);
        assertEquals(order.getId(), dto.getId());
        assertEquals(order.getPositions().size(), dto.getLines().size());
        for (int i = 0; i < order.getPositions().size(); i++) {
            final var pos = order.getPositions().get(i);
            final var posDto = dto.getLines().get(i);
            assertEquals(pos.getBeerId(), posDto.getBeerId());
            assertEquals(pos.getOrderedQty(), posDto.getOrderedQty());
        }
    }
}
