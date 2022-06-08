package ua.mike.micro.orderservice.dto.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.mike.micro.orderservice.dto.OrderDto;

/**
 * Created by mike on 01.06.2022 21:09
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AllocateOrderResult {

    private OrderDto order;
    private boolean isAllocationError = false;
    private boolean isPendingInventory = false;
}
