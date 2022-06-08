package ua.mike.micro.orderservice.dto.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.mike.micro.orderservice.dto.OrderDto;

/**
 * Created by mike on 31.05.2022 15:46
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidateOrderRequest {

    private OrderDto order;
}
