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

}
