package com.cos.blog.test;

import java.util.function.Supplier;

import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cos.blog.model.RoleType;
import com.cos.blog.model.User;
import com.cos.blog.repository.UserRepository;

// html이 아니라 data를 return해주는 controller
@RestController
public class DummyControllerTest {
	
	@Autowired // 의존성 주입
	private UserRepository userRepository;
	
	// {}를 사용하면 주소를 통해 변수값을 전달 받을 수 있음
	@GetMapping("/dummy/user/{id}")
	public User detail(@PathVariable int id) {
		
		// userRepository.findById(id)의 반환 타입은 Optional임 > User 타입이 아니기 때문에 빨간줄이 뜸 
		// 해당 User 객체가 없을 경우, 문제가 발생할 수 있기 때문에 이에 대한 조치를 해주어야함
		// 1) .get(): 그냥 받겠다는 의미
		// 2) .orElseGet(): 해당 객체가 없을 경우엔 직접 만들어서 넣어줌
		//		User user = userRepository.findById(id).orElseGet(new Supplier<User>() {
		//			@Override
		//			public User get() {
		//				return new User();
		//			}
		//		});
		// 3) .orElseThrow(): IllegalArgumentException 처리해주기
		User user = userRepository.findById(id).orElseThrow(new Supplier<IllegalArgumentException>() {
			@Override
			public IllegalArgumentException get() {
				return new IllegalArgumentException("해당 User가 없습니다. id : " + id);
			}
		});
		
		// User 객체는 자바 오브젝트
		// 웹 브라우저가 이해할 수 있는 데이터로 변환해줘야함 -> JSON(Gson 라이브러리같은 거 사용했음)
		// 스프링 부트는 MessageConverter가 응답 시에 자동으로 작동함
		// 자바 오브젝트를 리턴하면 MessageConverter가 Jackson라이브러리를 호출해서 user 오브젝트를 json으로 변환해서 브라우저에게 응답해줌
		return user;
		
		// 람다식으로 쓰는 방법도 있지만 여기서는 사용하지 말자
		//		User user = userRepository.findById(id).orElseThrow(()->{
		//			return new IllegalArgumentException("id가 " + id + "인 User가 없습니다.");
		//			});
		//		return user;
	}
	
	// http://localhost:8000/blog/dummy/join 으로 요청
	// http의 body에 username, password, email 데이터를 가지고 요청했을 경우,
	// 원래는 매개변수에 @RequestParam("username") String username 해줘야하는데 정확하게 이름을 적어주면 생략 가능
	// public String join(String username, String password, String email)
	// 이렇게 말고도 Object로 바로 받아줄 수 있음
	@PostMapping("/dummy/join")
	public String join(User user) {
		System.out.println("id : " + user.getId());
		System.out.println("username : " + user.getUsername());
		System.out.println("password : " + user.getPassword());
		System.out.println("email : " + user.getEmail());
		System.out.println("role : " + user.getRole());
		System.out.println("createDate : " + user.getCreateDate());
		
		user.setRole(RoleType.USER);
		userRepository.save(user);
		return "success !!";
	}
}
