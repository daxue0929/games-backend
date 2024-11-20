package org.daxue.games;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.daxue.games.mapper")
public class FlappyWordApplication {
    public static void main(String[] args) {
        SpringApplication.run(FlappyWordApplication.class, args);
    }
}