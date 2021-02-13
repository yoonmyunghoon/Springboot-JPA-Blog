package com.cos.blog.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cos.blog.model.RoleType;
import com.cos.blog.model.User;
import com.cos.blog.repository.UserRepository;

@Service
public class UserService {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder encoder;
	
	@Transactional
	public void 회원가입(User user) {
		String rawPassword = user.getPassword(); // 비밀번호 원문 
		String encPassword = encoder.encode(rawPassword); // 해쉬화
		user.setPassword(encPassword);
		user.setRole(RoleType.USER);
		userRepository.save(user);
	}

	//	@Transactional(readOnly = true) // select할때 트랜잭션이 시작, 서비스 종료 시에 트랜잭션 종료 (정합성 보장)
	//	public User 로그인(User user) {
	//		return userRepository.findByUsernameAndPassword(user.getUsername(), user.getPassword());
	//	}
	
	@Transactional
	public void 회원수정(User user) {
		// 수정 시에는 영속성 컨텍스트 User 오브젝트를 영속화를 시키고, 영속화된 User 오브젝트를 수정
		// select를 해서 User 오브젝트를 DB로부터 가져오는 이유는 영속화를 하기 위해서임
		// 영속화된 오브젝트를 변경하면 자동으로 DB에 update문을 날려줌
		User persistance = userRepository.findById(user.getId()).orElseThrow(()->{
			return new IllegalArgumentException("회원 찾기 실패");
		});
		
		String rawPassword = user.getPassword();
		String encPassword = encoder.encode(rawPassword);
		persistance.setPassword(encPassword);
		persistance.setEmail(user.getEmail());		
		// 회원수정 함수 종료시 = 서비스 종료 = 트랜잭션 종료 = commit 이 자동으로 됨
		// 영속화된 persistance 객체의 변화가 감지되면 더티체킹이 되어 update문을 날려줌
	}

}
