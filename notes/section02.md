# 01. DelegatingFilterProxy, FilterChainProxy

DelegatingFilterProxy → Spring Bean → Servlet Filter

1. 서블릿 필터는 스프링에서 정의된 빈을 주입해서 사용할 수 없음
2. 특정한 이름을 가진 스프링 빈을 찾아 그 빈에게 요청을 위임 
   - springSecurityFilterChain 이름으로 생성된 빈을 ApplicationContext 에서 찾아 요청을 위임
   - 실제 보안처리를 하지 않음

> FilterChainProxy

1. springSecurityFilterChain 의 이름으로 생성되는 필터 빈
2. DelegatingFilterProxy 으로 부터 요청을 위임 받고 실제 보안 처리
3. 스프링 시큐리티 초기화 시 생성되는 필터들을 관리하고 제어
   - 스프링 시큐리티가 기본적으로 생성하는 필터
   - 설정 클래스에서 API 추가 시 생성되는 필터
4. 사용자의 요청을 필터 순서대로 호출하여 전달
5. 사용자정의 필터를 생성해서 기존의 필터 전.후로 추가 가능
   - 필터의 순서를 잘 정의
6. 마지막 필터까지 인증 및 인가 예외가 발생하지 않으면 보안 통과

> DelegatingFilterProxy + FilterChainProxy

User → DelegatingFilterProxy(Servlet Container) → FilterChainProxy(Spring Container) → DispatcherServlet

# 02. 필터 초기화와 다중 보안 설정

- 설정클래스 별로 보안 기능이 각각 작동
- 설정클래스 별로 RequestMatcher 설정
  - http.antMatcher("/user/**")
  - http.antMatcher("/admin/**")
- 설정클래스 별로 필터가 생성
- FilterChainProxy가 각 필터들 가지고 있음
- 요청에 따라 RequestMatcher와 매칭되는 필터가 작동하도록 함

> 필터 초기화와 다중 설정 클래스

FilterChainProxy가 아래의 Filter들을 갖고 Request를 Match 해준다.
- SecurityConfig01 → HttpSecurity → SecurityFilterChain("/user/**") → FilterChainProxy
- SecurityConfig02 → HttpSecurity → SecurityFilterChain("/admin/**") → FilterChainProxy

```java
@Configuration
@EnableWebSecurity
@Order(0)
class SecurityConfig01 extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .antMatcher("/admin/**")
                .authorizeRequests()
                .anyRequest().authenticated()


                .and()


                .httpBasic()
        ;
    }
}

@Configuration
@EnableWebSecurity
@Order(1)
class SecurityConfig02 extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .anyRequest().permitAll()


                .and()


                .formLogin()
        ;
    }
}
```

`@Order(N)`를 사용할 때는 우선순위가 높다면 좁은 범위를 우선적으로 해야한다.

# 03. Authentication

당신이 누구인지 증명하는 것

- 사용자의 인증 정보를 저장하는 토큰 개념
- 인증 시 id 와 password 를 담고 인증 검증을 위해 전달되어사용된다
- 인증 후 최종 인증 결과 (user 객체, 권한정보) 를 담고 SecurityContext 에 저장되어 전역적으로 참조가 가능하다
  - `Authentication authentication = SecurityContextHolder.getContext().getAuthentication()`
- 구조
  1. principal : 사용자 아이디 혹은 User 객체를 저장
  2. credentials : 사용자 비밀번호
  3. authorities : 인증된 사용자의 권한 목록
  4. details : 인증 부가 정보
  5. Authenticated : 인증 여부


# 04. SecurityContextHolder, SecurityContext

(SecurityContext → Authentication → User 객체)
- SecurityContext
  - Authentication 객체가 저장되는 보관소로 필요 시 언제든지 Authentication 객체를 꺼내어 쓸 수 있도록 제공되는 클래스
  - ThreadLocal 에 저장되어 아무 곳에서나 참조가 가능하도록 설계함
  - 인증이 완료되면 HttpSession 에 저장되어 어플리케이션 전반에 걸쳐 전역적인 참조가 가능하다

- SecurityContextHolder 
  - SecurityContext 객체 저장 방식
    - MODE_THREADLOCAL : 스레드당 SecurityContext 객체를 할당, 기본값
    - MODE_INHERITABLETHREADLOCAL : 메인 스레드와 자식 스레드에 관하여 동일한 SecurityContext 를 유지
    - MODE_GLOBAL :  응용 프로그램에서 단 하나의 SecurityContext를 저장한다
  - SecurityContextHolder.clearContext() : SecurityContext 기존 정보 초기화
```java
SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_THREADLOCAL);
SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_GLOBAL);
```

- Authentication authentication = SecurityContextHolder.getContext().getAuthentication()

`SPRING_SECURITY_CONTEXT` 형태로 HttpSession에 저장된다.

- `Authentication authentication = SecurityContextHolder.getContext().getAuthentication();`
- `SecurityContext context = (SecurityContext) session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);`
- `Authentication authentication1 = context.getAuthentication();`

```
UsernamePasswordAuthenticationToken
[Principal=org.springframework.security.core.userdetails.User 
    [Username=user, Password=[PROTECTED], Enabled=true, AccountNonExpired=true, credentialsNonExpired=true, AccountNonLocked=true, Granted Authorities=[ROLE_USER]], 
Credentials=[PROTECTED], Authenticated=true, Details=WebAuthenticationDetails
    [RemoteIpAddress=0:0:0:0:0:0:0:1, SessionId=F8CE457DA61EE827C6D44EF3ADB81242], Granted Authorities=[ROLE_USER]]
```

# 05. SecurityContextPersistenceFilter

- SecurityContext 객체의 생성, 저장, 조회

- 익명 사용자 
  - 새로운 SecurityContext 객체를 생성하여 SecurityContextHolder 에 저장 
  - AnonymousAuthenticationFilter 에서 AnonymousAuthenticationToken 객체를 SecurityContext 에 저장

- 인증 시
  - 새로운 SecurityContext 객체를 생성하여 SecurityContextHolder 에 저장
  - UsernamePasswordAuthenticationFilter 에서 인증 성공 후 SecurityContext 에 UsernamePasswordAuthenticationToken 객체를 SecurityContext 에 저장
  - 인증이 최종 완료되면 Session 에 SecurityContext 를 저장

- 인증 후
  - Session 에서 SecurityContext 꺼내어 SecurityContextHolder 에서 저장
  - SecurityContext 안에 Authentication 객체가 존재하면 계속 인증을 유지한다

- 최종 응답 시 공통 
  - SecurityContextHolder.clearContext()

SecurityHolder(ThreadLocal(SecurityContext(null))) →
SecurityContextPersistenceFilter →
SecurityHolder(ThreadLocal(SecurityContext(Authentication(사용자 정보))))

# 10. Authentication Flow

Client →
UsernamePasswordAuthenticationFilter →
AuthenticationManager →
AuthenticationProvider →
UserDetailsService →
Repository

# 06. AuthenticationManager

아이디와 패스워드를 Authentication 인증 객체에 담아서 받는다. 인증처리 요건에 맞는 AuthenticationProvider를 찾아서 AuthenticationManager가 위임한다. 

- AuthenticationProvider 목록 중에서 인증 처리 요건에 맞는 AuthenticationProvider를 찾아 인증처리를 위임한다
- 부모 ProviderManager 를 설정하여  AuthenticationProvider 를 계속 탐색 할 수 있다

# 07. AuthenticationProvider

# 08. Authorization, FilterSecurityInterceptor 

> Authorization
> 
  - 당신에게 무엇이 허가 되었는지 증명하는 것 
  - 스프링 시큐리티가 지원하는 권한 계층
- 웹 계층 
  - URL 요청에 따른 메뉴 혹은 화면단위의 레벨 보안 
- 서비스 계층 
  - 화면 단위가 아닌 메소드 같은 기능 단위의 레벨 보안 
- 도메인 계층(Access Control List, 접근제어목록)
  - 객체 단위의 레벨 보안

> FilterSecurityInterceptor

- 마지막에 위치한 필터로써 인증된 사용자에 대하여 특정 요청의 승인/거부 여부를 최종적으로 결정 
- 인증 객체 없이 보호자원에 접근을 시도할 경우 AuthenticationException 을 발생 
- 인증 후 자원에 접근 가능한 권한이 존재하지 않을 경우 AccessDeniedException 을 발생 
- 권한 제어 방식 중 HTTP 자원의 보안을 처리하는 필터 
- 권한 처리를 AccessDecisionManager에게 맡김

# 09. AccessDecisionManager, AccessDecisionVoter

> AccessDecisionManager

- 인증 정보, 요청정보, 권한정보를 이용해서 사용자의 자원접근을 허용할 것인지 거부할 것인지를 최종 결정하는 주체 
- 여러 개의 Voter 들을 가질 수있으며 Voter 들로부터 접근허용, 거부, 보류에 해당하는 각각의 값을 리턴받고 판단 및 결정 
- 최종 접근 거부  시 예외 발생

- 접근결정의 세가지 유형 
  - AffirmativeBased :
    - 여러개의 Voter 클래스 중 하나라도 접근 허가로 결론을 내면 접근 허가로 판단한다
    - 거부 승인 거부 = 최종 승인
    - 거부 거부 거부 = 최종 거부

  - ConsensusBased :
    - 다수표(승인 및 거부)에 의해 최종 결정을 판단한다
    - 동수일경우 기본은 접근허가이나 allowIfEqualGrantedDeniedDecisions 을 false 로
    설정할 경우 접근거부로 결정된다
    - 승인 승인 거부 = 최종 승인
    - 승인 거부 거부 = 최종 거부

  - UnanimousBased :
    - 모든 보터가 만장일치로 접근을 승인해야 하며 그렇지 않은 경우 접근을 거부한다
    - 승인 승인 승인 = 최종 승인
    - 승인 승인 거부 = 최종 거부
      판단을 심사하는 것(위원)

> AccessDecisionManager

- Voter 가 권한 부여 과정에서 판단하는 자료
  - Authentication - 인증 정보(user)
  - FilterInvocation – 요청 정보 (antMatcher("/user"))
  - ConfigAttributes - 권한 정보 (hasRole("USER"))

- 결정 방식 
  - ACCESS_GRANTED : 접근허용(1)
  - ACCESS_DENIED : 접근 거부(0)
  - ACCESS_ABSTAIN : 접근 보류(-1)
    - Voter 가 해당 타입의 요청에 대해 결정을 내릴 수 없는 경우

# 10. AccessDecisionManager, AccessDecisionVoter 응용편

# 11. 스프링 시큐리티 필터 및 아키텍처 정리

ppt97
