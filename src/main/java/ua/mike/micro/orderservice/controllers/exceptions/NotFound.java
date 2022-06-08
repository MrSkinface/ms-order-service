package ua.mike.micro.orderservice.controllers.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by mike on 03.06.2022 15:59
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NotFound extends RuntimeException {

}
