package ua.mike.micro.orderservice.dto.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Created by mike on 08.06.2022 12:11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddedBeerNotification {

    private UUID beerId;
}
