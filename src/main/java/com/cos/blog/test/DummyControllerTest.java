package com.cos.blog.test;

import java.util.List;
import java.util.function.Supplier;

import javax.transaction.Transactional;
import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.cos.blog.model.Board;
import com.cos.blog.model.RoleType;
import com.cos.blog.model.User;
import com.cos.blog.repository.BoardRepository;
import com.cos.blog.repository.UserRepository;



// html이 아니라 data를 return해주는 controller
@RestController
public class DummyControllerTest {
	
	@Autowired // 의존성 주입
	private UserRepository userRepository;
	
	@Autowired
	private BoardRepository boardRepository;
	
	@DeleteMapping("/dummy/user/{id}")
	public String delete(@PathVariable int id) {
		try {
			userRepository.deleteById(id);
		} catch (EmptyResultDataAccessException e) {
			return "삭제에 실패하였습니다. 해당 id는 DB에 없습니다. id : " + id;
		}
		return "삭제되었습니다. id : " + id;
	}
	
	// email, password 받기
	@Transactional // 함수 종료시에 자동 commit이 됨
	@PutMapping("/dummy/user/{id}")
	public User updateUser(@PathVariable int id, @RequestBody User requestUser) { //json 데이터 => Java Object(MessageConverter의 Jackson라이브러리가 변환해서 받아줌)
		System.out.println("id : "+id);
		System.out.println("password : "+requestUser.getPassword());
		System.out.println("email : "+requestUser.getEmail());
		
		// 여기서 영속화가 이루어짐(DB에서 해당 id로 user데이터를 select해서 1차 캐시로 가지고 오고, 그 객체 여기로 가지고 온 것)
		User user = userRepository.findById(id).orElseThrow(()->{
			return new IllegalArgumentException("수정에 실패하였습니다.");
		});
		
		// 해당 객체의 데이터를 변경
		user.setPassword(requestUser.getPassword());
		user.setEmail(requestUser.getEmail());
		// save는 insert할 때 쓰는데, 만약 이미 해당 id가 있으면 update도 해줌
		// 이때 변경사항을 제외하고 전달받지 않은 값들은 null로 들어가기 때문에 이에 대한 처리가 필요함
		// 보통 update할 때는 save를 사용하지 않음
		// save함수는 id를 전달하지 않으면 insert를 해주고,
		// save함수는 id를 전달하면 해당 id에 대한 데이터가 있으면 update를 해주고,
		// save함수는 id를 전달하면 해당 id에 대한 데이터가 없으면 insert를 해줌
		// userRepository.save(user);
		
		return user;
		// 함수가 종료될 때, 영속성 컨텍스트가 변경을 감지함
		// 1차 캐시에 있는 user객체와 비교해서 달라진 부분을 update하기 위해 자동으로 DB에 쿼리를 날려주고 commit을 함
		// => 더티 체킹
	}
	
	
	// user 전체 리스트
	@GetMapping("/dummy/users")
	public List<User> list() {
		return userRepository.findAll();
	}
	
	// 한 페이지 당 2건의 데이터를 리턴 받아보자
	@GetMapping("/dummy/user")
	public Page<User> pageList(@PageableDefault(size = 2, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
		// 필요한 로직이 있으면 Page를 사용해서 처리하고 최종적으로는 content만 리턴
		Page<User> pagingUser = userRepository.findAll(pageable);
		List<User> users = pagingUser.getContent();
		return pagingUser;
	}
	
	@GetMapping("/dummy/board")
	public Page<Board> boardPageList(@PageableDefault(size = 3, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
		// 필요한 로직이 있으면 Page를 사용해서 처리하고 최종적으로는 content만 리턴
		Page<Board> pagingBoard = boardRepository.findAll(pageable);
		return pagingBoard;
	}
	
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
		return "회원가입에 성공하였습니다.";
	}
}
