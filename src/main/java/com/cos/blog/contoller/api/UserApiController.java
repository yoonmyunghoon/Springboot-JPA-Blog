package com.cos.blog.contoller.api;

//import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
//import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.cos.blog.dto.ResponseDto;
import com.cos.blog.model.User;
import com.cos.blog.service.UserService;

@RestController
public class UserApiController {

	@Autowired
	private UserService userService;

	// @Autowired
	// private HttpSession session;

//	@Autowired
//	private AuthenticationManager authenticationManager;

	@PostMapping("/auth/joinProc")
	public ResponseDto<Integer> save(@RequestBody User user) {
		System.out.println("UserApiController : save 호출됨");
		userService.회원가입(user);
		return new ResponseDto<Integer>(HttpStatus.OK.value(), 1);
	}

	// @PostMapping("/api/user/login")
	// public ResponseDto<Integer> login(@RequestBody User user) { // HttpSession
	// session을 매개변수로 받을 수도 있음
	// System.out.println("UserApiController : login 호출됨");
	// User principal = userService.로그인(user);
	// if (principal != null) {
	// session.setAttribute("principal", principal);
	// }
	//
	// return new ResponseDto<Integer>(HttpStatus.OK.value(), 1);
	// }

	@PutMapping("/user")
	public ResponseDto<Integer> update(@RequestBody User user) {
		userService.회원수정(user);
		// 여기서는 트랜잭션이 종료되기 때문에 DB에 값은 변경이 됐지만, 세션값은 변경되지 않은 상태임
		// 직접 세션값을 변경해줘야함
		 Authentication authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());
		 SecurityContext securityContext = SecurityContextHolder.getContext();
		 securityContext.setAuthentication(authentication);
//		 session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
		// 직접 변경해줄려고 했지만, 실패함(실패한줄알았는데 user객체를 사용하니까 정상작동함),, 로그인 할 때처럼 전체과정을 다시해주는 식으로 처리해줘야할듯
		// 세션 등록(직접 로그인 처리를 해주는 것)
//		Authentication authentication = authenticationManager
//				.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
//		SecurityContextHolder.getContext().setAuthentication(authentication);
		 // 이렇게 해줘도 되는데 처음 방법에서 principal 대신 변경된 정보를 가지고 있는 user 객체를 사용하니까 정상 작동함..


		return new ResponseDto<Integer>(HttpStatus.OK.value(), 1);
	}

}
