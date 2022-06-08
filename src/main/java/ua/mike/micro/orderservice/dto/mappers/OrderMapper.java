package ua.mike.micro.orderservice.dto.mappers;

import org.mapstruct.MapMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import ua.mike.micro.orderservice.dto.OrderDto;
import ua.mike.micro.orderservice.models.Order;

/**
 * Created by mike on 31.05.2022 15:28
 */
@Mapper(uses = PositionMapper.class)
public interface OrderMapper {

    @Mappings({
            @Mapping(source = "positions", target = "lines")
    })
    OrderDto toDto(Order order);

    @Mappings({
            @Mapping(source = "lines", target = "positions")
    })
    Order fromDto(OrderDto dto);
}
