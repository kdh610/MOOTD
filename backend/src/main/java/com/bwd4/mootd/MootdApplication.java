package com.bwd4.mootd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication
@EnableReactiveMongoRepositories
public class MootdApplication {

    public static void main(String[] args) {
        SpringApplication.run(MootdApplication.class, args);
    }

}
