package com.cos.blog.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
//ORM -> Object를 테이블로 매핑해주는 기술
@Entity //User Class가 자동으로 MySQL에 테이블로 생성됨
// @DynamicInsert // insert시에 null인 필드를 제외시켜줌
public class User {
	
	@Id // Primary Key
	@GeneratedValue(strategy = GenerationType.IDENTITY) //프로젝트에서 연결된 DB의 넘버링 전략을 따라감
	private int id; // 시퀀스(오라클), auto-increment(MySQL)
	
	@Column(nullable = false, length = 30, unique = true)
	private String username; // ID
	
	@Column(nullable = false, length = 100)
	private String password;
	
	@Column(nullable = false, length = 50)
	private String Email;
	
	//	@ColumnDefault("'user'") <- @DynamicInsert를 안쓰면 null값이 들어가기 때문에 안좋음
	// DB는 RoleType이라는게 없음
	@Enumerated(EnumType.STRING)
	private RoleType role; // Enum을 쓰는게 좋음 > admin or user or manager 등 해당 컬럼에 도메인을 줄 수 있음
	
	@CreationTimestamp //시간 자동 입력
	private Timestamp createDate;
	
	
	

}
