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















