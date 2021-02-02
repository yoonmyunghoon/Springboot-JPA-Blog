package com.cos.blog.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cos.blog.model.User;

// DAO 역할
// 자동으로 bean으로 등록이 됨 -> @Repository 를 써줄 필요가 없음
public interface UserRepository extends JpaRepository<User, Integer>{

}
