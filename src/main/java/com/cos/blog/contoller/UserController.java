package com.cos.blog.contoller;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

// 인증이 안된 사용자들이 출입할 수 있는 경로로 /auth/** 로 허용
// 그냥 주소가 / 이면 index.jsp로 가는 것도 허용
// static 이하에 있는 /js/**, /css/**, /image/** 들도 허용


@Controller
public class UserController {

	@GetMapping("/auth/joinForm")
	public String joinForm() {
		return "user/joinForm";
	}
	
	@GetMapping("/auth/loginForm")
	public String loginForm() {
		return "user/loginForm";
	}
	
	@GetMapping("/user/updateForm")
	public String updateForm() {
		return "user/updateForm";
	}
	
	@GetMapping("/auth/kakao/callback")
	public @ResponseBody String kakaoCallback(String code) throws IOException  { // Data를 리턴해주는 컨트롤러 함수가 됨
		
		// HttpBody에 들어갈 값을 외부파일에서 가져오기
		Properties properties = new Properties();
		String propFileName = "kakao.properties"; 
		String file = getClass().getResource("").getPath()+propFileName; 
		FileInputStream fi = new FileInputStream(file);
		properties.load(fi);
		
		String grant_type = properties.getProperty("grant_type");
		String client_id = properties.getProperty("client_id");
		String redirect_uri = properties.getProperty("redirect_uri");
		
		
		// POST 방식으로 key=value 데이터를 요청해야함(카카오쪽으로)
		// Retrofit2
		// OkHttp
		// RestTemplate
		// 등 여러가지 방법이 있음 
		RestTemplate rt = new RestTemplate();
		// HttpHeader 오브젝트 생성 
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
		// HttpBody 오브젝트 생성 
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("grant_type", grant_type);
		params.add("client_id", client_id);
		params.add("redirect_uri", redirect_uri);
		params.add("code", code);
		// HttpHeader와 HttpBody를 하나의 오브젝트에 담기
		// ResponseEntity가 HttpEntity를 매개변수로 넣어야하기 때문에 만들어줌 
		HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = 
				new HttpEntity<>(params, headers);
		// Http 요청하기 - Post방식으로 요청하고 response 변수의 응답을 받음
		ResponseEntity<String> response = rt.exchange(
				"https://kauth.kakao.com/oauth/token",
				HttpMethod.POST,
				kakaoTokenRequest,
				String.class
				);
		
		return "카카오 토큰 요청 완료 : 토큰 요청에 대한 응답 => " + response;
		
	}
}
