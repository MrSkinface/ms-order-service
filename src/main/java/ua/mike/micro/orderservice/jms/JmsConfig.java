package ua.mike.micro.orderservice.jms;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MessageConverter;
import ua.mike.micro.jms.JmsConsumerActions;
import ua.mike.micro.jms.JmsMessageConverter;

/**
 * Created by mike on 31.05.2022 15:12
 */
@Configuration
@RequiredArgsConstructor
public class JmsConfig {

    private final ObjectMapper mapper;

    @Bean
    public MessageConverter messageConverter(ObjectMapper mapper) {
        return new JmsMessageConverter(mapper);
    }

    @Bean
    public JmsConsumerActions consumerActions() {
        return new JmsConsumerActions(mapper);
    }
}
