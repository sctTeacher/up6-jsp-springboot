package com.ncmem.up6;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.ncmem")
@SpringBootApplication
public class Up6Application {

	public static void main(String[] args) {
		SpringApplication.run(Up6Application.class, args);
	}
}
