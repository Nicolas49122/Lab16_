package com.tecsup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = "com.tecsup.model")
public class PrjServiceSpringBootReactApplication {

    public static void main(String[] args)
    {
        SpringApplication.run(PrjServiceSpringBootReactApplication.class, args);
    }

}
