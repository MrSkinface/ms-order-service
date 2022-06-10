package ua.mike.micro.orderservice.config;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Created by mike on 09.06.2022 16:43
 */
@Profile("local-discovery")
@Configuration
@EnableDiscoveryClient
public class DiscoveryConfig {
}
