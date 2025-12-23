package com.example.learningcheckin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.example.learningcheckin.mapper")
public class LearningCheckinApplication {

    public static void main(String[] args) {
        SpringApplication.run(LearningCheckinApplication.class, args);
    }

}
