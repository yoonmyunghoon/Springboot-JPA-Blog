package com.cos.blog.test;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TempControllerTest {
	
	@GetMapping("/temp/home")
	public String tempHome() {
		System.out.println("temphome");
		// 파일 리턴 기본 경로 : src/main/resources/static
		// 리턴명 : /home.html
		return "/home.html";
	}
	
	@GetMapping("/temp/jsp")
	public String tempJsp() {
		System.out.println("tempjsptest");
		// application.yml에서 경로 설정
		// prefix : /WEB-INF/views/
		// suffix : .jsp
		return "test";
	}
}
