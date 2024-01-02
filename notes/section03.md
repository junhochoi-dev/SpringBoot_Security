# 01. 실전 프로젝트 구성
1. 프로젝트 명 : core-spring-security
2. 기본 의존관계 설정 -  pom.xml
3. 기본 패키지 및 폴더 구성
4. 기본 View Template 생성
5. 기본 정적 자원 설정

`pom.xml`
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity5</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-configuration-processor</artifactId>
    <optional>true</optional>
</dependency>
```

`application.properties`
```java
spring.datasource.url=jdbc:postgresql://localhost:5432/springboot
spring.datasource.username=postgres
spring.datasource.password=pass

spring.jpa.hibernate.ddl-auto=create
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true

spring.thymeleaf.cache=false

spring.devtools.livereload.enabled=true
spring.devtools.restart.enabled=true
```

# 02. 메뉴 권한 및 WebIgnore 설정

- js / css / image 파일 등 보안 필터를 적용할 필요가 없는 리소스를 설정

```java
@Override
public void configure(WebSecurity web) throws Exception {
    web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
}
```

# 04. Form 인증 – User 등록 / PasswordEncoder

- Spring Security 5.0 이전에는 기본 PasswordEncoder 가 평문을 지원하는 NoOpPasswordEncoder

- 암호화 포맷 : {id}encodedPassword 
  - bcrypt, noop, pbkdf2, scrypt, sha256 (기본 포맷은 Bcrypt : {bcrypt}$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG)

- 생성 
  - PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

- 인터페이스 
  - encode(password)
    - 패스워드 암호화
  - matches(rawPassword, encodedPassword)
    - 패스워드 비교


# 05. Form 인증 – CustomUserDetailsService

```java
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Account account = userRepository.findByUsername(username);
        
        if (account == null) {
	throw new UsernameNotFoundException("No user found with username: " + username);            
        }

        // 권한
         ArrayList<GrantedAuthority> roles = new ArrayList<GrantedAuthority>();
        roles.add(new SimpleGrantedAuthority(account.getRole()));

        return new AccountContext(account, roles);
    }
```

# 06. Form 인증 – CustomAuthenticationProvider

```java
public Authentication authenticate(Authentication auth) throws AuthenticationException {

        String loginId = auth.getName();
        String passwd = (String) auth.getCredentials();
        
        UserDetails userDetails = uerDetailsService.loadUserByUsername(loginId);

        if (userDetails == null || !passwordEncoder.matches(passwd, userDetails.getPassword())) {
             throw new BadCredentialsException("Invalid password");
        }

        return new UsernamePasswordAuthenticationToken(userDetails.getUser(), null, userDetails.getAuthorities());
}
```

# 07. Form 인증 – Custom Login Form Page

```java
@Override
public void configure(HttpSecurity http) throws Exception {
    http.formLogin().loginPage("/customLogin")
}
```

# 08. Form 인증 - 로그아웃 및 화면 보안 처리

- 로그아웃 방법
  - <form> 태그를 사용해서 POST로 요청
  - <a> 태크를 사용해서 GET 으로 요청 – SecurityContextLogoutHandler 활용

- 인증 여부에 따라 로그인/로그아웃 표현
  - `<li sec:authorize="isAnonymous()"><a th:href="@{/login}">로그인</a></li>`
  - `<li sec:authorize="isAuthenticated()"><a th:href="@{/logout}">로그아웃</a></li>`

# 08. Form 인증 – WebAuthenticationDetails, AuthenticationDetailsSource

- WebAuthenticationDetails 
  - 인증 과정 중 전달된 데이터를 저장 
  - Authentication의 details 속성에 저장

- AuthenticationDetailsSource
  - WebAuthenticationDetails 객체를 생성

# 09. Form 인증 – CustomAuthenticationSuccessHandler

# 09. Form 인증 –CustomAuthenticationFailureHandler
