package com.cos.blog.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.cos.blog.model.User;

// DAO 역할
// 자동으로 bean으로 등록이 됨 -> @Repository 를 써줄 필요가 없음
public interface UserRepository extends JpaRepository<User, Integer>{
	
	// select * from user where username=?1;
	Optional<User> findByUsername(String username);
	
	// JPA Naming 전략
	// findBy를 사용하면 
	// SELECT * FROM user WHERE username = ?1 AND password = ?2; 이런 쿼리를 날려주는 함수를 자동으로 만들어줌 
	//	User findByUsernameAndPassword(String username, String password);
	
	// 이렇게 쿼리를 직접 써서 만들어줄 수도 있음 
	//	@Query(value = "SELECT * FROM user WHERE username = ?1 AND password = ?2", nativeQuery = true)
	//	User login(String username, String password);

}
