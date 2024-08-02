package com.dpf.moira;

import com.dpf.moira.test.CarContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    private Moira moira;

    @Bean
    public CommandLineRunner run() {
        return args -> {
            var context = new CarContext(120);
            moira.runAsync("carDecision", context);
        };
    }
}
