package com.anorisno.appDataTool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("com.anorisno.appDataTool.repository")
public class AppDataToolApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppDataToolApplication.class, args);
	}

}
