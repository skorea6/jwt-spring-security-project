# 스프링에서 JWT 사용해보기 with Kotlin

## 구현 환경
- Kotlin
- Spring 3.1.0
- Spring Data JPA
- Spring Security
- MySQL
- Redis
- JSON (API)

## Github CI/CD + AWS Diagram
![diagram](https://github.com/skorea6/jwt-spring-security-project/assets/13993684/adcdd848-f3f4-4946-a843-a0acf09072e0)


## 목표
- Spring Security 구조 파악 및 구현
- JWT + OAuth2 깊게 파기
- Kotlin에 익숙해지기
- 인증 서비스의 다양한 보안 취약점 개선
- 추후 서비스를 운영한다고 가정하고 실무처럼 만드려고 노력


## 진행 상황
- JWT 검증 시스템 연동
  - Refresh 토큰 도입 : Access, Refresh 토큰 재발급 기능 추가
  - Refresh 토큰 발급시 IpAddress, User-Agent 등 유저 브라우저 정보 기록
  - Redis 연동로 보안 기능 향상
  - 관련 블로그 포스팅 작성(1): [JWT Access, Refresh 토큰 + Redis : 소개 및 보안](https://skorea6.tistory.com/entry/1-JWT-Access-Refresh-%ED%86%A0%ED%81%B0-Redis-%EC%86%8C%EA%B0%9C-%EB%B0%8F-%EB%B3%B4%EC%95%88)
  - 관련 블로그 포스팅 작성(2): [JWT Access, Refresh 토큰 + Redis : 스프링 코드 구현 ](https://skorea6.tistory.com/entry/2-JWT-Access-Refresh-%ED%86%A0%ED%81%B0-Redis-%EC%8A%A4%ED%94%84%EB%A7%81-%EC%BD%94%EB%93%9C-%EA%B5%AC%ED%98%84-Kotlin)
- OAuth2 연동
  - Kakao, Google, Naver 3사 연동
  - Handler, Service 및 각 소셜 로그인 데이터에 맞는 Info 구현
  - 프론트와 백엔드가 분리 되어있다고 가정하고 구현
 
- 일반 로그인
  - 이메일, 전화번호 인증 서비스 구현

- UserDetails, OAuth2User를 상속하는 CustomPrinciapl 객체 구현 (Spring Security Principal)

- Redis 캐시 도입 (API)
  - 관련 블로그 포스팅 작성: [스프링에서 Redis 캐시 사용하기](https://skorea6.tistory.com/entry/Kotlin-%EC%8A%A4%ED%94%84%EB%A7%81%EC%97%90%EC%84%9C-Redis-%EC%BA%90%EC%8B%9C-%EC%82%AC%EC%9A%A9%ED%95%98%EA%B8%B0)

- Auditing (생성자, 생성일, 수정자, 수정일) 추가 및 테스트
  - 관련 블로그 포스팅 작성: [스프링 Data JPA: Auditing 적용 방법](https://skorea6.tistory.com/entry/%EC%8A%A4%ED%94%84%EB%A7%81-Data-JPA-Auditing-%EC%A0%81%EC%9A%A9-%EB%B0%A9%EB%B2%95-Kotlin)

- API Exception Handler 구현
  - 관련 블로그 포스팅 작성: [스프링 API 예외처리 방법 : @RestControllerAdvice](https://skorea6.tistory.com/entry/%EC%8A%A4%ED%94%84%EB%A7%81-API-%EC%98%88%EC%99%B8%EC%B2%98%EB%A6%AC-%EB%B0%A9%EB%B2%95-RestControllerAdvice-Kotlin)
- 지속적인 리펙토링, 최적화


## 회원 API
- 일반 회원가입 API
- Oauth2 회원가입 API (소셜 로그인 후 추가적인 필드를 받기 위함)
- Oauth2 회원가입 전 회원 정보 가져오기 API
- 일반 로그인 API
- Oauth2 로그인 API
- Refresh 토큰을 이용한 Access, Refresh 토큰 재발급 API
- <로그인시> 모든 Refresh 토큰의 브라우저(기기) 정보 확인 API
- <로그인시> 특정 Refresh 토큰 제거 API
- <로그인시> 모든 Refresh 토큰 제거 API
- <로그인시> 내 정보 보기 API
- <로그인시> 내 정보 수정 API
