package com.newsforright;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsforright.bot.persistence.BotStateRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PoliticalCompassBot {
    public static void main(String[] args) {

        SpringApplication.run(PoliticalCompassBot.class, args);
    }

    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }

    @Bean
    CommandLineRunner commandLineRunner(BotStateRepository botStateRepository){
        return args -> {

        };
    }

}
