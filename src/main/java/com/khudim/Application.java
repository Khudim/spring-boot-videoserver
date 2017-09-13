package com.khudim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by Beaver.
 */
@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories("com.khudim.dao.repository")
@EntityScan("com.khudim.dao.entity")
@ComponentScan("com.khudim.dao.service")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}