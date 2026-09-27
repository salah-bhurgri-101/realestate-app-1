package com.realestate.property_service;

import org.springframework.boot.SpringApplication;

public class TestPropertyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(PropertyServiceApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
