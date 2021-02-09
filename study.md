# STUDY

## 1. 영속성 컨텍스트와 더티체킹

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



## 2. 스프링 기본파싱전략과 json통신

### 1. Get 요청

- 주소에 데이터를 담아 보냄
  - http://localhost:8000/blog/user?username=ssar
- key = value 형태의 데이터
- 웹브라우저의 주소창에 쳐서 보내는 요청은 전부 get 요청
- body로 데이터를 담아 보내지 않음
  - http요청인데 body가 없음

### 2. Post, Put, Delete 요청 (데이터를 변경)

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



## 3. Ajax를 사용하는 이유, 2가지

### 1. 요청에 대한 응답을 html이 아닌 Data(json)를 받기 위해서

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

### 2. 비동기 통신을 하기 위해서

- 웹 브라우저가 여러가지 일을 하는데 순차적으로 하게 되면 시간이 오래걸리는 작업에서 막혔다가 그 작업이 끝나야 뒤에 작업을 이어갈 수 있게 됨
  - 사용자 경험이 안좋아짐
- 그래서 모든 일을 순차적으로 진행하는 것이 아니라 따로 요청을 하고 나서, 하던 일을 계속하다가 요청한 것이 응답이 오면 다시 돌아가서 응답에 대한 작업을 처리하고(Callback) 다시 하던일을 하도록 함 => 비동기 처리(Ajax를 사용함)



## 4. Service 계층을 쓰는 이유

### 1. 트랜잭션 관리

- 트랜잭션: 일이 처리되기 위한 가장 작은 단위

### 2. 서비스 의미

- 서비스는 하나의 기능을 의미함
  - 송금이라는 서비스(기능)를 예로 들면 
  - 돈을 주는 사람의 계좌에서는 - 처리 후, 업데이트
  - 돈을 받는 사람의 계좌에서는 + 처리 후, 업데이트
  - 하나의 기능 안에 여러번의 DB 조작이 발생함
    - 송금은 2개의 DB조작으로 이루어진 하나의 트랜잭션 => 서비스임
  - 이런 모든 조작들이 오류없이 이루어지고나서 커밋을 해줘야 제대로 된 서비스를 구현한 것임 
  - 중간에 오류가 나면 다시 되돌려둬야함(rollback)



## 5. DB 격리수준

### 1. READ COMMIT 격리수준

- 오라클의 DB 격리 수준 전략은 READ COMMIT
  - 커밋된 것만 READ 할 수 있음
  - A가 Board 테이블에 업데이트를 진행하고 있는 동안에는 B가 Select를 해도 변화된 것을 볼 수 없음
    - 기본적으로 insert, update, delete 등의 데이터 조작이 일어나면 DB는 undo 영역에 변경 이전의 데이터들을 보관하고 있음
    - B는 undo 영역의 데이터에서 Select한 결과를 보는 것
  - A가 업데이트를 다하고 커밋을 날리면, undo 영역의 데이터가 변경되고 B는 이 변경사항을 확인할 수 있음
- READ COMMIT의 문제점
  - A가 데이터 조작을 하고 있을 때, B가 단순히 읽기만하는 것은 중간에 값이 변화된다고해서 큰 문제가 안됨
  - 그런데 B가 여러번 읽은 그 결과를 가지고 어떤 처리를해서 insert를 해주기 위해서 트랜잭션을 시작했다고 가정하면
  - A가 트랜잭션 시작 > B가 트랜잭션 시작 > B가 데이터 select > A가 변경 사항 커밋 >  B가 데이터 select > B가 변경 사항 커밋
  - B가 작업하고 있는 트랜잭션 안에서 똑같은 데이터를 select한 결과가 다르게 나타남 => 데이터 정합성이 깨지는 문제가 발생함
    - 하나의 트랜잭션에서 똑같은 select 쿼리를 실행했을 때, 항상 같은 결과를 가져와야하는 REPEATABLE READ의 정합성에 어긋남

### 2. REPEATABLE READ 격리 수준

- MySQL의 DB 격리 수준 전략은 REPEATABLE READ
- NON-REPEATABLE READ 문제 해결
  - 트랜잭션 중에 select를 해도 그 결과가 변하지 않음
  - 해당 트랜잭션이 발생하기 전에 커밋된 결과들이 저장되어있는 undo 테이블에서 조회해오기 때문임
  - 그래서 그 이후에 다른 트랜잭션이 시작되고 커밋이 되어도 조회한 데이터는 그 트랜잭션의 변경 사항이 반영되지 않은 상태임
- select 할 때, 트랜잭션 처리를 하지 않고 그냥 select하면 다른 트랜잭션의 변경에 따라 다른 결과가 나오게 됨
  - 그래서 select문을 사용할 때에도 트랜잭션 처리를 하고 사용하자 
- PHANTOM READ 문제는 해결 못함

### 3. 각 수준별 발생 현상

|    격리 수준    | Dirty Read | Non-Repeatable Read | Phantom Read |
| :-------------: | :--------: | :-----------------: | :----------: |
| Read Uncommited |     O      |          O          |      O       |
|  Read Commited  |     -      |          O          |      O       |
| Repeatable Read |     -      |          -          |      O       |
|  Serializable   |     -      |          -          |      -       |

- Dirty Read
  - 트랜잭션 작업이 완료되지 않았는데, 다른 트랜잭션에서 볼 수 있게 되는 현상
- Non-Repeatable Read
  - 한 트랜잭션에서 같은 쿼리를 두 번 수행할 때, 두 쿼리의 결과가 상이하게 나타나는 비일관적인 현상
  - 한 트랜잭션이 수행 중일 때 다른 트랜잭션이 값을 수정 또는 삭제함으로써 발생함
- Phantom Read
  - 한 트랜잭션에서 같은 쿼리를 두 번 수행할 때, 첫 번째 쿼리에서 없던 레코드가 두 번째 쿼리에서 나타나는 현상
  - 한 트랜잭션이 수행 중일 때 다른 트랜잭션이 새로운 레코드를 insert함으로써 발생함

### 4. 참고

- https://medium.com/@sunnkis/database-%ED%8A%B8%EB%9E%9C%EC%9E%AD%EC%85%98%EC%9D%98-%EA%B2%A9%EB%A6%AC-%EC%88%98%EC%A4%80%EC%9D%B4%EB%9E%80-10224b7b7c0e



## 6. 스프링의 전통적인 트랜잭션

### 1. 스프링 시작

1. 톰캣 시작
2. web.xml 읽음
3. DB 연결 테스트
4. 서버 세팅 완료

### 2. request 요청

- 송금 서비스 요청

### 3. web.xml

- DB 연결 세션 생성(JDBC 커넥션 시작)
- 트랜잭션 시작
- 영속성 컨텍스트 시작
- 위의 세개는 요청을 하는 사용자 마다 시작되는 것

### 4. 필터

### 5. 스프링 컨테이너

- Controller
  - 요청 분기해서 Service 호출
- Service
  - 송금 처리 함수 실행
  - 돈을 보내는 사람과 받는 사람 데이터를 select하도록 Repository에 요청
- Repository
  - Service에서 요청 받은 데이터들을 DB에서 조회해서 영속성 컨텍스트에 가져옴

### 6. DB

- select 결과를 영속성 컨텍스트에 전달

### 7. 영속성 컨텍스트

- Repositroy에 의해서 DB에서 전달받은 객체가 들어있음

### 8. 스프링 컨테이너

- Service
  - 영속성 컨테이너의 객체들을 Repository를 통해 가져옴
  - 송금을 위한 변경 처리
  - Controller에게 결과를 리턴해줌
- Controller
  - Service에게 받은 결과에 따라 Data(json) 또는 html 형태로 클라이언트에게 Response해줌
  - 이 시점에서 트랜잭션이 종료됨
    - commit을 날리게 되고, 영속성 컨텍스트가 변경을 감지하고 DB에 flush

### 9. 영속성 컨텍스트

- 변경사항을 감지하고 DB에 flush
- 종료

### 10. DB

- 테이블 갱신

### 11. 스프링 컨테이너

- DB 연결 세션 종료



## 7. 스프링 JPA의 OSIV 전략

- 스프링의 전통적인 트랜잭션 방식에는 문제점이 존재함
  - 컨트롤러 이전 시점에 JDBC 연결, 트랜잭션, 영속성 컨텍스트 등이 시작되기 때문에 부하가 생김
- 이를 해결하기 위해 JDBC 연결, 트랜잭션, 영속성 컨텍스트를 시작하는 시점을 Service 시점으로 변경함
- 이렇게 하면 LazyLoding을 할 때 문제가 발생하게 됨
  - LazyLoding은 객체를 가지고 오면 그 객체와 연관된 다른 객체의 정보들은 나중에 필요할 때 가지고 오는 방식인데
  - 이게 가능하려면 그 시점에 영속성 컨텍스트가 종료되지 않아야함
  - 그런데 이런 요청은 View와 관련이 있기 때문에 컨트롤러에서 처리해줘야하는데 이 시점에는 이미 영속성 컨텍스트가 종료되어있음
- 이런 문제를 해결하기 위해서 스프링 2.0부터는 Open Session In View라는 전략이 나옴
  - 스프링부트 2.0에서부터는 이 전략이 기본으로 세팅되어있음
    - Application.yml의 jpa설정 쪽에 보면 있음(open-in-view 부분)
  - 이 전략을 사용하면 영속성 컨텍스트 시작 및 종료 시점을 컨트롤러 시점으로 가지고 옴
    - LazyLoading이 가능해짐
    - LazyLoading 전략을 사용하면 객체를 가지고 올때 관계가 있는 객체를 같이 가져오는게 아니라 프록시(실제는 아니지만 실제인 척하는 객체)객체를 가지고 옴
    - 나중에 컨트롤러에서 관련 객체의 정보를 가지고 오려하면 영속성 컨텍스트에 있는 프록시 객체가 잠시 JDBC 연결을 시작해서 해당 객체의 데이터를 select해서 영속성 컨텍스트로 가지고 온 뒤, JDBC 연결은 종료시킴
    - 컨트롤러에서는 프록시를 통해 가지고 온 진짜 객체를 받아서 뷰에 전달해줌
    - 이 때 프록시를 통해서 update, delete, insert 등의 데이터 변경 로직은 불가능함, 오직 select만 가능
  - 이걸 false로 변경하면 트랜잭션이 종료되는 시점(서비스 시점)에 영속성 컨텍스트도 같이 종료됨



## 8. XSS와 CSRF

### XSS(Cross Site Scripting)

- 자바 스크립트 공격
- 게시판 글 내용에 자바 스크립트 코드를 넣어서 다른 사용자가 해당 페이지를 요청했을 때, 자바 스크립트가 실행되면서 공격하는 방법
- naver에서 제공하는 오픈 소스 lucy를 사용하면 간단하게 막을 수 있음

### CSRF(Cross Site request forgery)

- 사용자가 네이버나 페이스북 등 신뢰할 수 있는 사이트에 로그인되어있는 상태에서 해커가 만든 악의적인 사이트라던가 메일에 있는 링크 등을 클릭하면서 해당 유저의 권한을 이용해서 신뢰할 수 있는 사이트에 요청을 날리게끔 하는 공격
- 방어하는 방법
  - Referer 체크
    - HTTP 헤더에 있는 referer 정보를 체크하여 실제 로그인 된 도메인으로부터 온 요청인지 확인하는 방법
    - HTTP 헤더 정보를 조작할 수 있기 때문에 완전한 방어는 아님
  - CSRF 토큰 발급
    - 로그인한 유저에게 토큰을 발급해주고 서버에 요청을 보낼 때, 항상 해당 토큰을 함께 전송해야만 이에 대한 응답을 해주는 방법
    - 세션에 토큰을 저장해두고 요청이 오면 확인함
  - CAPTCHA
    - 간혹 웹사이트에서 무언가 요청을 할려고할 때, 로봇인지 아닌지 테스트하는 것들을 볼 수가 있는데, 이를 통해서 진짜 사용자의 요청인지 체크하는 방식







## 참고

- https://getinthere.tistory.com/
- 유튜브 채널:  데어 프로그래밍

















