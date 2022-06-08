package ua.mike.micro.orderservice.dto.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * Created by mike on 31.05.2022 16:19
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidateOrderResult {

    private UUID orderUUID;
    private boolean isValid;
    private Set<String> errors;
}
