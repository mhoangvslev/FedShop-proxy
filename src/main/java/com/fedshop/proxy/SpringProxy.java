package com.fedshop.proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class SpringProxy {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SpringProxy.class);
        app.run(args);
    }

}
