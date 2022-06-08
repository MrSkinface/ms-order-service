package ua.mike.micro.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MsOrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsOrderServiceApplication.class, args);
    }

}
