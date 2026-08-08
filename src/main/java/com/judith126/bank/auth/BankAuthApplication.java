package com.judith126.bank.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BankAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankAuthApplication.class, args);
    }
}
