package com.newsforright;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Main spring boot application.
 * Registers telegram bot and puts Jackson ObjectManager to AC
 *
 * @author seaeagle
 */
@SpringBootApplication
public class PoliticalCompassBot {
    public static void main(String[] args) {
        SpringApplication.run(PoliticalCompassBot.class, args);
    }

    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }

}
