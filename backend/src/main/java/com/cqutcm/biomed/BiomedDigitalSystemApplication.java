package com.cqutcm.biomed;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.cqutcm.biomed.mapper")
public class BiomedDigitalSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(BiomedDigitalSystemApplication.class, args);
    }
}
