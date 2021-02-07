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



## 참고

- https://getinthere.tistory.com/
- 유튜브 채널:  데어 프로그래밍













