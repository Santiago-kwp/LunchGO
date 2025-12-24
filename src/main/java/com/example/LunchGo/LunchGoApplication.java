package com.example.LunchGo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.LunchGo.review.mapper")
@MapperScan("com.example.LunchGo.member.mapper")
public class LunchGoApplication {

	public static void main(String[] args) {
		SpringApplication.run(LunchGoApplication.class, args);
	}

}
