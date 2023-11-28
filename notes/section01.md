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

# 02. 인증 API – 프로젝트 구성 및 의존성 추가

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

기본계정 설정 (`application.properties`)
```properties
spring.security.user.name=admin
spring.security.user.password=admin
```

# 03. 인증 API – HTTP Basic 인증, BasicAuthenticationFilter

- HTTP는 자체적인 인증 관련 기능을 제공하며 HTTP 표준에 정의된 가장 단순한 인증 기법이다
- 간단한 설정과 Stateless가 장점 - Session Cookie(JSESSIONID) 사용하지 않음
- 보호자원 접근시 서버가 클라이언트에게  401 Unauthorized 응답과 함께 WWW-Authenticate header를 기술해서 인증요구를 보냄
- Client는 ID:Password 값을 Base64로 Encoding한 문자열을 Authorization Header에 추가한 뒤 Server에게 Resource를 요청
  - Authorization: Basic cmVzdDpyZXN0
- ID, Password가 Base64로 Encoding되어 있어 ID, Password가 외부에 쉽게 노출되는 구조이기 때문에 SSL이나 TLS는 필수이다

```java
protected void configure(HttpSecurity http) throws Exception {
	http.httpBasic();
}
```

# 04. 인증 API – Form 인증

`http.formLogin() // Form 로그인 인증 기능이 작동함`
```java
protected void configure(HttpSecurity http) throws Exception {
	 http.formLogin()
            .loginPage("/login.html")   				// 사용자 정의 로그인 페이지
            .defaultSuccessUrl("/home")				// 로그인 성공 후 이동 페이지
            .failureUrl("/login.html?error=true")		// 로그인 실패 후 이동 페이지
            .usernameParameter("username")			// 아이디 파라미터명 설정
            .passwordParameter("password")			// 패스워드 파라미터명 설정
            .loginProcessingUrl("/login")			// 로그인 Form Action Url
            .successHandler(loginSuccessHandler())		// 로그인 성공 후 핸들러
            .failureHandler(loginFailureHandler())		// 로그인 실패 후 핸들러
}
```
