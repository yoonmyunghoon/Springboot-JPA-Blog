package com.cos.blog.model;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Board {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(nullable = false, length = 100)
	private String title;
	
	@Lob // 대용량 데이터
	private String content; // 섬머노트 라이브러리를 사용할 예정, html 태그가 섞여서 디자인이 되기 때문에 용량이 클 것으로 예상됨 
	
	private int count; // 조회수
	
	// ManyToOne은 fetch = FetchType.EAGER가 Default
	@ManyToOne(fetch = FetchType.EAGER) // Many = Board, One = User
	@JoinColumn(name="userId")
	private User user; // DB는 오브젝트를 저장할 수 없음(FK를 사용해야함) - 자바는 오브젝트를 저장할 수 있음 -> 충돌이 발생하게 됨 -> JPA가 해결
	
	// OneToMany은 fetch = FetchType.LAZY가 Default, 여기서는 SELECT했을 때 바로 받아올 수 있도록 EAGER 사용
	@OneToMany(mappedBy = "board", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE) // mappedBy가 있으면 연관관계의 주인이 아님을 의미(FK가 아니므로 DB에 컬럼을 만들지 말라는 의미)
	@JsonIgnoreProperties({"board"})
	@OrderBy("id desc")
	private List<Reply> replys;
	
	@CreationTimestamp
	private Timestamp createDate;
	

}
