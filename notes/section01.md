# 01. 강의에서 다루는 내용, 개발환경, 선수지식

1. 스프링 시큐리티의 보안 설정 API 와 이와 연계된 각 Filter 들에 대해 학습한다
- 각 API 의 개념과 기본적인 사용법, API 처리 과정, API 동작방식 등 학습
- API 설정 시 생성 및 초기화 되어 사용자의 요청을 처리하는 Filter 학습

2. 스프링 시큐리티 내부 아키텍처와 각 객체의 역할 및 처리과정을 학습한다
- 초기화 과정, 인증 과정, 인가과정 등을 아키텍처적인 관점에서 학습

3. 실전 프로젝트
- 인증 기능 구현 – Form 방식, Ajax 인증 처리
- 인가 기능 구현 – DB 와 연동해서 권한 제어 시스템 구현

개발 환경
- JDK 1.8 이상   
- DB - Postgres
- IDE – Intellij or STS

선수 지식
- Spring Boot
- Spring MVC
- Spring Data JPA
- Thymeleaf
- Postgres
- Lombok

# 01. 인증 API – 프로젝트 구성 및 의존성 추가

`pom.xml`
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

- 스프링 시큐리티의 의존성 추가 시 일어나는 일들
  - 서버가 기동되면 스프링 시큐리티의 초기화 작업 및 보안 설정이 이루어진다
  - 별도의 설정이나 구현을 하지 않아도 기본적인 웹 보안 기능이 현재 시스템에 연동되어 작동함 
    1. 모든 요청은 인증이 되어야 자원에 접근이 가능하다
    2. 인증 방식은 폼 로그인 방식과 httpBasic 로그인 방식을 제공한다
    3. 기본 로그인 페이지 제공한다
    4. 기본 계정 한 개 제공한다 – username : user / password : 랜덤 문자열

- 문제점
  - 계정 추가, 권한 추가, DB 연동 등
  - 기본적인 보안 기능 외에 시스템에서 필요로 하는 더 세부적이고 추가적인 보안기능이 필요

# 02. 인증 API – 사용자 정의 보안 기능 구현
- SecurityConfiguration : 사용자 정의 보안 설정 클래스
- WebSecurityConfigurerAdapter : 스프링 시큐리티의 웹 보안 기능 초기화 및 설정
- HttpSecurity : 세부적인 보안 기능을 설정할 수 있는 API 제공

SecurityConfiguration → WebSecurityConfigurerAdapter → HttpSecurity → (인증 API, 인가 API)

```java
@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

@Override
protected void configure(HttpSecurity http) throws Exception { 
	http
		.authorizeRequests()			
		.anyRequest().authenticated()		
	.and()
		.formLogin(); 			
}
```

