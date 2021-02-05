# Blog 만들기 연습

## 1. 개발 환경

- 개발 언어: Java 1.8
- 라이브러리 관리: Maven
- 패키징: Jar
- 프레임워크: 스프링부트 2.4.2
- 템플릿 엔진: jsp
- 데이터베이스: MySQL(mariaDB)
- 개발 tool: STS
- 의존성 설정:
  - Spring Boot DevTools
    - Automatic restart 기능: 변경 사항이 있을 때, 자동으로 재시작해줌
  - Lombok
    - getter, setter, 생성자 등을 어노테이션을 통해서 자동으로 만들어줌
  - Spring Data JPA
    - ORM 기능
  - MySQL Driver
    - MySQL 사용하기 위함
  - Spring Security
    - 보안적인 기능
  - Spring Web
    - 어노테이션을 사용하기 위해서 추가해줘야함
    - 내장형 컨테이너로 톰캣을 기본 탑재하고 있음
      - 예전에 기본 스프링으로 프로젝트를 할 때는 톰캣을 따로 설치해서 서버 연동을 했었음
- 추가적인 의존성 설정:
  - 시큐리티 태그 라이브러리
  - JSP 템플릿 엔진
  - JSTL



## 2. 기술 정리

### 영속성 컨텍스트와 더티체킹

- Controller
  - 요청 받고, 응답을 해줌
  - insert, update, delete, select 등을 처리
  - 예시1) 
    - user 객체를 추가하는 요청이 들어옴(회원가입)
    - save함수를 사용
  - 예시2)
    - user 객체를 변경하는 update 요청을 해보자
    - save함수 사용 안함
    - @Transactional 사용
- JPA
  - 영속성 컨텍스트라는 것을 가지고 있음
  - 영속성 컨텍스트 안에 1차 캐시가 존재함
  - 예시1)
    - Controller에서 user객체를 insert(save함수)하면 1차 캐시에 해당 객체가 들어감
      - 영속화 되었다고 표현함
    - 1차 캐시에 있는 user 객체를 DB에 넣음(insert 쿼리)
      - flush라고 표현함(Transaction commit이 일어나기  바로 직전에 발생함)
        - 영속성 컨텍스트의 변경 사항들과 DB의 상태를 맞추는 작업
      - 보통 flush라는 표현은 buffer에 있는 데이터들을 전달하고 buffer를 비우는 과정을 의미하는데 여기서는 1차 캐시를 비우지는 않음
      - 비우지 않고 남아있는 user객체는 나중에 select문이 요청될 때, DB에 접근하지 않고 바로 1차 캐시에서 Controller로 전달됨
        - DB 부하가 덜 함
  - 예시2)
    - Controller에서 select가 먼저 발생함(findById함수)
    - DB에 있는 해당 id의 user 객체가 1차 캐시로 들어옴
      - 영속화됨
    - 1차 캐시에 있는 user객체가 Controller에 전달되고, Controller에서 해당 객체의 데이터를 변경함
      - 변경을 다하고 만약 save함수를 실행하면 변경된 객체가 1차 캐시에 들어감
        - 이미 1차 캐시에는 변경되기 이전의 user객체가 있음
        - id가 같기 때문에 변경된 값들만 기존의 객체에 반영됨(update)
        - 이후에 변경된 user객체가 flush되서 DB에 반영됨
          - 실제 user 함수 구현 소스코드를 보면 @Transactional으로 둘러쌓여있음
          - save함수가 사용되면 트랜잭션이 일어난다는 것
          - 트랜잭션이 일어나기 직전에 해당 변화에 대해 DB에 반영하는 쿼리가 날라감(flush)
      - save함수를 사용하지 않으면,
        - @Transactional이 있기 때문에 해당 함수가 종료되는 시점에 commit이 일어남
          - 이때 자동으로 1차 캐시에 있는 영속화된 user객체와 Controller에서 변경된 user객체를 비교하여 달라진 부분을 감지하게 됨
          - 이후에 변화된 부분을 DB에 update쿼리를 날림(flush)
          - flush 이후 바로 commit이 이뤄짐
        - 변경을 감지하고 DB에 수정을 날려주는 것 == 더티체킹

- DummyControllerTest.java

```java
@Transactional // 함수 종료시에 자동 commit이 됨
@PutMapping("/dummy/user/{id}")
public User updateUser(@PathVariable int id, @RequestBody User requestUser) {
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
  
  return null;
  // 함수가 종료될 때, 영속성 컨텍스트가 변경을 감지함
  // 1차 캐시에 있는 user객체와 비교해서 달라진 부분을 update하기 위해 자동으로 DB에 쿼리를 날려주고 commit을 함
}
```



### 스프링 기본파싱전략과 json통신

#### 1. Get 요청

- 주소에 데이터를 담아 보냄
  - http://localhost:8000/blog/user?username=ssar
- key = value 형태의 데이터
- 웹브라우저의 주소창에 쳐서 보내는 요청은 전부 get 요청
- body로 데이터를 담아 보내지 않음
  - http요청인데 body가 없음

#### 2. Post, Put, Delete 요청 (데이터를 변경)

- 보내야할 데이터의 양이 많거나 중요함
  - 담아서 보냄
- Post 요청
  - 회원가입을 예로 들면
  - username, password, email, address, ... 등 많은 데이터를 보내야함
  - form 태그를 사용해서 보낼 수도 있음
    - method='POST' 로해서 보냄
    - 다만, 데이터의 형태가 key = value 형태인 경우에만 form 태그 사용
    - form 태그는 Get요청과 Post요청만 가능
- Put, Delete 요청
  - form 태그로 요청 못함
  - 자바스트립트로 요청을 해야함
- form 태그의 한계를 고려해서 자바스크립트를 사용하는 방법으로 통일하는 것이 좋음
  - Post, Put, Delete 통일
  - 자바스크립트로 ajax요청 + 데이터는 json
- 스프링에는 form:form 태그라는 것도 있음
  - Get, Post, Put, Delete 요청도 가능함
  - 사용은 안할거

### 3. 스프링 컨트롤러의 파싱 전략 1

- 스프링 컨트롤러는 key=value 형태의 데이터를 받으면 자동으로 파싱해서 변수에 담아줌(매개변수)
- get 요청(key=value 형태)
- post 요청 중에서 x-www-form-urlencoded(form태그, key=value 형태)
- 일 경우에 다음과 같이 파라미터로 받을 수 있음

```java
PostMapping("/home")
public String home(String username, String email) {
	return "home";
}
```

### 4. 스프링 컨트롤러의 파싱 전략 2

- 스프링은 key=value 형태의 데이터를 오브젝트로 파싱해서 받아주는 역할도 함
- 주의할점은 setter가 없으면 스프링이 파싱해서 넣어주지 못함
  - 해당 데이터를 파싱해서 setter를 사용해서 오브젝트를 만들어줌

### 5. key=value 형태가 아닌 데이터는 어떻게 파싱할까?

- 이런 데이터는 자동적으로 파싱이 안되기 때문에 @RequestBody 어노테이션을 붙여서 받아줘야함
- @RequestBody를 붙이면 MessageConverter 클래스를 구현한 Jackson 라이브러리가 발동하면서 json 데이터를 자바 오브젝트로 파싱하여 받아줌

### 6. form 태그로 json데이터 요청방법

- jsp나 html에서 form태그를 작성하고 button으로 javascript 호출(ajax)



### Ajax를 사용하는 이유, 2가지

#### 1. 요청에 대한 응답을 html이 아닌 Data(json)를 받기 위해서

- 기존의 웹 브라우저(클라이언트)에서 요청을 하면 서버는 html로 응답해줬었음
  - 클라이언트(웹 브라우저)가 회원가입페이지 요청
  - 서버가 회원가입페이지(html) 응답해줌
  - 클라이언트가 회원가입 수행 요청
  - 서버가 회원가입 수행한 후, 메이지페이지(html) 응답 
- 그런데 앱(클라이언트)이 발달하기 시작함
- 앱은 이미 페이지들을 가지고 있는 상태에서 필요한 데이터만 요청해서 응답받는 형태이기 때문에 서버쪽에서는 데이터만 응답해주면 됨
  - 앱으로 회원가입 페이지에 들어감
  - 앱에서 회원가입 수행 요청
  - 서버에서 회원가입 수행 후, 성공했다는 데이터를 응답
  - 앱에서 응답을 받고 메인페이지로 이동
- 웹과 앱의 요청을 처리하려면 서버는 두 가지 형태를 제공해야했음
  - html을 전송하는 방법, 데이터만 전송하는 방법
- 웹도 데이터를 넘겨주는 방식을 사용하고 html문서가 필요한 경우에만 추가적으로 주자
  - 웹 브라우저에서 회원가입 수행 요청
  - 서버에서 수행한 후, 데이터 응답
  - 웹 브라우저에서 데이터 확인 후, 다시 메인페이지 요청
  - 서버에서 메인페이지(html) 응답
- 결국, 서버를 두가지 방식으로 만들지 않고, 둘다 처리할 수 있는 방식으로 만들기 위해서 ajax를 사용함
  - Ajax를 사용하면 웹은 서버로부터 데이터(json)를 리턴받을 수 있으며 그렇게 되면 서버의 분리없이 하나의 서버로 웹과 앱의 요청에 대해 응답해줄 수 있음
  - 웹에서는 추가적인 요청으로 html파일을 받을 수 있음

#### 2. 비동기 통신을 하기 위해서

- 웹 브라우저가 여러가지 일을 하는데 순차적으로 하게 되면 시간이 오래걸리는 작업에서 막혔다가 그 작업이 끝나야 뒤에 작업을 이어갈 수 있게 됨
  - 사용자 경험이 안좋아짐
- 그래서 모든 일을 순차적으로 진행하는 것이 아니라 따로 요청을 하고 나서, 하던 일을 계속하다가 요청한 것이 응답이 오면 다시 돌아가서 응답에 대한 작업을 처리하고(Callback) 다시 하던일을 하도록 함 => 비동기 처리(Ajax를 사용함)





## 참고

- https://getinthere.tistory.com/
- 유튜브 채널:  데어 프로그래밍

















