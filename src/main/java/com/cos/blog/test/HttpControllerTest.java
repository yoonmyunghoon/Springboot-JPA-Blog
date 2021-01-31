package com.cos.blog.test;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HttpControllerTest {
	
	private static final String TAG = "HttpControllerTest : ";
	
	@GetMapping("/http/lombok")
	public String lombokTest() {
		Member m = Member.builder().username("ssar").password("1234").email("ssar@nate.com").build();
		System.out.println(TAG + "getter : " + m.getId());
		m.setId(5000);
		System.out.println(TAG + "getter : " + m.getId());
		return "lombok test complete!!";
	}
	
	// 매개변수에서 @RequestParam 써서 하나씩 받거나,
	// 객체(Member m)로 한번에 받을 수 있음 // MessageConverter (스프링부트)가 해줌
	@GetMapping("/http/get")
	public String getTest(Member m) {
		return "get!! : " + m.getId() + ", " + m.getUsername() + ", " + m.getPassword() + ", " + m.getEmail(); 
	}
	
	// x-www-form-urlencoded => form태그 요청이랑 똑같음 => 바로 객체(Member m)로 받을 수 있음
	// MIME type이 text/plain이면 @RequestBody String s 로 받음 
	// MIME type이 application/json이면 @RequestBody Member m로 받음 // MessageConverter (스프링부트)가 해줌
	@PostMapping("/http/post")
	public String postTest(@RequestBody Member m) {
		return "post!! : " + m.getId() + ", " + m.getUsername() + ", " + m.getPassword() + ", " + m.getEmail(); 
	}
	
	@PutMapping("/http/put")
	public String putTest(@RequestBody Member m) {
		return "put!! : " + m.getId() + ", " + m.getUsername() + ", " + m.getPassword() + ", " + m.getEmail(); 
	}
	
	@DeleteMapping("/http/delete")
	public String deleteTest() {
		return "delete!!";
	}


	
}
