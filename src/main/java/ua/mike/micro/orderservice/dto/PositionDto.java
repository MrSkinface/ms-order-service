package ua.mike.micro.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PositionDto {

    @Null
    private UUID id;
    @NotNull
    private UUID beerId;
    @NotNull
    @Min(1)
    private Integer orderedQty;
    @Null
    private Integer allocatedQty;
}
