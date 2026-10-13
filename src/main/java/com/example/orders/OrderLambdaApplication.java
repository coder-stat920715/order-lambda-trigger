package com.example.orders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class OrderLambdaApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderLambdaApplication.class, args);
    }
}
