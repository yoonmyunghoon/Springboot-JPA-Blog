package com.cos.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BlogApplication {
	
	static {
	    System.setProperty("spring.config.location", "classpath:/application.yml,classpath:oauth.yml");
	  }

	public static void main(String[] args) {
		SpringApplication.run(BlogApplication.class, args);
	}

}
