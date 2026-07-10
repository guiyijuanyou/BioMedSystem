package com.cqutcm.biomed;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.cqutcm.biomed.mapper")
@EnableScheduling
public class BiomedDigitalSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(BiomedDigitalSystemApplication.class, args);
    }
}
