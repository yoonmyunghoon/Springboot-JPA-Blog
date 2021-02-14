package com.cos.blog.contoller;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import com.cos.blog.model.KakaoProfile;
import com.cos.blog.model.OAuthToken;
import com.cos.blog.model.User;
import com.cos.blog.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

// 인증이 안된 사용자들이 출입할 수 있는 경로로 /auth/** 로 허용
// 그냥 주소가 / 이면 index.jsp로 가는 것도 허용
// static 이하에 있는 /js/**, /css/**, /image/** 들도 허용


@Controller
public class UserController {
	
	@Value("${cos.key}")
	private String cosKey;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private AuthenticationManager authenticationManager;

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
	public String kakaoCallback(String code) throws IOException  {
		
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
		
		// Gson, Json Simple, ObjectMapper 등 오브젝트에 json 데이터를 넣는 방법이 있음
		ObjectMapper objectMapper = new ObjectMapper();
		OAuthToken oauthToken = objectMapper.readValue(response.getBody(), OAuthToken.class);
		
		// accessToken을 사용해서 사용자 정보 받기
		RestTemplate rt2 = new RestTemplate();
		HttpHeaders headers2 = new HttpHeaders();
		headers2.add("Authorization", "Bearer "+oauthToken.getAccess_token());
		headers2.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
		
		HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest = 
				new HttpEntity<>(headers2);
		
		ResponseEntity<String> response2 = rt2.exchange(
				"https://kapi.kakao.com/v2/user/me",
				HttpMethod.POST,
				kakaoProfileRequest,
				String.class
				);
		
		ObjectMapper objectMapper2 = new ObjectMapper();
		KakaoProfile kakaoProfile = objectMapper2.readValue(response2.getBody(), KakaoProfile.class);
		
		// 카카오에서 받은 user 정보를 User 객체에 통합해줘야함
		// 필요한 User 오브젝트의 속성에는 username, password, email 이 있음
		// id과 createDate는 자동으로 생성, role은 USER로 해줄거임
		User kakaoUser = User.builder()
				.username(kakaoProfile.getKakao_account().getEmail()+"_"+kakaoProfile.getId())
				.password(cosKey)
				.email(kakaoProfile.getKakao_account().getEmail())
				.oauth("kakao")
				.build();
		
		// 이미 가입된 사용자인지 체크해서 처리
		User originUser = userService.회원찾기(kakaoUser.getUsername());
		
		if (originUser.getUsername() == null) {
			System.out.println("기존 회원이 아니기에 자동 회원가입을 진행합니다.");
			userService.회원가입(kakaoUser);
		}
		
		// 로그인 처리
		System.out.println("자동 로그인을 진행합니다.");
		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(kakaoUser.getUsername(), cosKey));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		
		return "redirect:/";
	}
}
