package com.cos.blog.handler;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;


// 전역에 있는 Controller, RestController에서 발생하는 에러를 여기서 처리해줄 수 있음
@ControllerAdvice
@RestController
public class GlobalExceptionHandler {
	
	@ExceptionHandler(value=Exception.class)
	public String handleException(Exception e) {
		return "<h1>" + e.getMessage() + "</h1>";
		
	}
}
