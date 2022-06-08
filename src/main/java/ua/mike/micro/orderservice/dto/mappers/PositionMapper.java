package ua.mike.micro.orderservice.dto.mappers;

import org.mapstruct.Mapper;
import ua.mike.micro.orderservice.dto.PositionDto;
import ua.mike.micro.orderservice.models.Position;

/**
 * Created by mike on 31.05.2022 16:49
 */
@Mapper
public interface PositionMapper {

    PositionDto toDto(Position position);

    Position fromDto(PositionDto dto);
}
