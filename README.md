# 스프링에서 JWT 사용해보기 with Kotlin

## Demo URL
- 서비스(FE) Demo URL: [jwt.abz.kr](https://jwt.abz.kr)
- API(BE) Demo URL: [api.jwt.abz.kr](https://api.jwt.abz.kr)

![image](https://github.com/skorea6/jwt-spring-security-project/assets/13993684/c15937fa-ff8a-461c-bf35-55f963b6d032)
- FrontEnd(React) Github URL: [https://github.com/skorea6/jwt-react](https://github.com/skorea6/jwt-react)

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

- Github actions를 이용하여 master 브런치에 푸시가 되면 자동으로 배포가 되는 시스템입니다. Github Actions탭에서 상황을 확인할 수 있습니다.
- Docker를 이용하여 Blue&Green 무중단 배포 시스템을 구축하였습니다. Blue는 8081, Green은 8082 포트로 Docker에서 실행됩니다. Nginx가 Loadbalancing을 진행하여 Blue와 Green중 Upstream 상태인 서버에 접속하도록합니다.
- Certificate Manager에서 SSL을 발급 후 Cloudfront와 연동하여 사용하고 있으며, http to https redirection 규칙을 사용하고 있어 API는 https연결만 허용합니다.
- 현재는 AWS 요금 이슈로 인하여 AWS Auto Scaling과 LoadBalancing은 사용하지 않고 있습니다. 1대의 EC2와 Cloudfront가 직접적으로 연결되어 있는 상태입니다.

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

- 구글의 reCAPTCHA v2 연동
  - 일반 회원가입, 아이디 찾기, 비밀번호 찾기, 이메일 변경 서비스에 적용
  - 관련 블로그 포스팅 작성(1): [스프링 reCAPTCHA v2 사용하기](https://skorea6.tistory.com/entry/%EC%8A%A4%ED%94%84%EB%A7%81-reCAPTCHA-v2-%EC%82%AC%EC%9A%A9%ED%95%98%EA%B8%B0-Kotlin)

- AWS의 SES(Simple Email Service) 연동
  - 일반 회원가입, 아이디 찾기, 비밀번호 찾기, 이메일 변경 서비스에 적용

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
  - 이메일 인증번호 발송 API
  - 이메일 인증번호 확인 API
- Oauth2 회원가입 API
  - Oauth2 회원가입 전 회원 정보 가져오기 API
- 일반 로그인 API
- Oauth2 로그인 API
- 아이디 찾기 API
- 비밀번호 찾기 API (비밀번호 변경)
  - 이메일 인증번호 발송 API
  - 이메일 인증번호 확인 API
- Refresh 토큰을 이용한 Access, Refresh 토큰 재발급 API
- <로그인시> 모든 Refresh 토큰의 브라우저(기기) 정보 확인 API
  - <로그인시> 특정 브라우저(기기) 제거 API (code를 이용한)
- <로그인시> 특정 Refresh 토큰 제거 API (refresh token을 이용한)
- <로그인시> 모든 Refresh 토큰 제거 API
- <로그인시> 내 정보 보기 API
- <로그인시> 내 정보 업데이트 API
- <로그인시> 내 비밀번호 업데이트 API
- <로그인시> 내 이메일 업데이트 API
  - <로그인시> 이메일 인증번호 발송 API
  - <로그인시> 이메일 인증번호 확인 API
- <로그인시> 회원 탈퇴 API
