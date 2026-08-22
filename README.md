<img width="3072" height="1500" alt="그래픽 이미지" src="https://github.com/user-attachments/assets/64cee248-6d1a-4011-afde-daf275d76a1a" />

# 기술 스택
### Backend
| 구분              | 기술                                               | 비고                                          |
| --------------- | ------------------------------------------------ | ------------------------------------------- |
| Language        | Java 21 (LTS)                                    |                                             |
| Framework       | Spring Boot 4.1, Spring Web MVC                  |                                             |
| Build           | Gradle                                           |                                             |
| ORM             | Spring Data JPA (Hibernate), QueryDSL 5.0        | 동적 필터 + 커서 기반 무한스크롤                         |
| Database        | MySQL 8.4                                        |                                             |
| Cache / Store   | Redis 7.2 (Spring Data Redis)                    | 리프레시 토큰 세션 · 작품 탐색 페이지 캐시 · 대학교 검색 자동완성 인덱스 |
| Security        | Spring Security, OAuth 2.0 (Google · Apple), JWT | 뷰어/고객/작가 역할 기반 인가                           |
| Realtime        | WebSocket + STOMP                                | 실시간 채팅, 메시지·읽음 실시간 브로드캐스트                   |
| Push            | Firebase Cloud Messaging (HTTP v1)               | iOS · Android, Admin SDK 없이 직접 호출           |
| API Docs        | springdoc-openapi (Swagger UI)                   | 공통 에러 응답 자동 문서화                             |
| External Client | AWS SDK v2, Spring RestClient                    | S3·SES 연동 / FCM HTTP v1 직접 호출               |
| Mail            | Spring Boot Starter Mail, <br>AWS SES (SMTP)     | 안내 메일 전송                                    |
| Observability   | OpenTelemetry                                    | 분산 추적 (예정)                                  |


### Cloud & External Services
| 서비스                      | 용도                                      |
| ------------------------ | --------------------------------------- |
| AWS EC2                  | 애플리케이션 서버 (t4g, ARM64 Graviton)         |
| AWS S3                   | 이미지·파일 저장 (Presigned URL로 클라이언트 직접 업로드) |
| AWS CloudFront           | 정적 파일 CDN 배포                            |
| AWS Route 53             | 도메인 · DNS                               |
| AWS SES                  | 메일 발송                                   |
| Firebase Cloud Messaging | 푸시 알림 (iOS · Android, HTTP v1 API)      |

### Infra & DevOps
| 구분         | 기술                                  | 비고            |
| ---------- | ----------------------------------- | ------------- |
| Container  | Docker, Docker Compose              |               |
| CI/CD      | GitHub Actions (self-hosted runner) | 운영 / 개발 환경 분리 |
| Web Server | Nginx                               | 리버스 프록시       |
| SSL        | Certbot (Let's Encrypt)             | 인증서 자동 갱신     |
### 개발 도구

| 구분         | 기술            | 비고                                    |
| ---------- | ------------- | ------------------------------------- |
| IDE        | IntelliJ IDEA | 백엔드 개발 환경                             |
| DB GUI     | DataGrip      | MySQL 스키마·쿼리 확인용                      |
| Cache GUI  | RedisInsight  | Redis 키·TTL·자료구조 확인용 (세션·캐시·자동완성 인덱스) |
| API Client | Postman       | API 호출·테스트 (Swagger로 어려운 경우들에서)       |

### 아키텍처 특징
- **멀티 역할 계정** — 한 계정이 고객·작가를 겸하며, 토큰의 `activeRole` 로 모드를 전환하고
  커스텀 ArgumentResolver 가 역할별 엔티티를 주입
- **작품 탐색 페이지 Redis 캐시** — 트래픽이 집중되는 첫 진입만 캐시하고, 작품·작가 정보 변경 시
  커밋 후 이벤트로 무효화 (TTL 은 무효화 누락 대비 안전망)
- **커서 기반 무한스크롤** — 동점 tie-break 로 페이지 간 항목 누락 방지
- **STOMP 구독 인가** — 구독 시점에 방 참여자 여부를 검증해 타인 대화 도청 차단
- **Presigned URL 업로드** — 이미지가 서버를 거치지 않고 클라이언트에서 S3 직접 업로드
- **가입 메일 발송 대상 판정** — Apple 로그인이 이메일을 안 줄 때 생기는 placeholder 주소를
  진짜 relay 주소와 구분해 걸러낸다. 도메인이 아니라 로컬파트가 sub 와 같은지로 판정
- **푸시 알림 (iOS · Android)** — 한 요청에 apns·android 블록을 함께 실어
  FCM 이 대상 토큰의 플랫폼에 맞는 쪽만 고르도록 구성

# System Architecture
<img width="1700" height="1190" alt="dearbloom-sa" src="https://github.com/user-attachments/assets/bc395bee-63bb-4b09-a9a1-9b4d5d728f6a" />

# ERD
<img width="1720" height="1308" alt="dearbloom-erd-summary" src="https://github.com/user-attachments/assets/6d2bf592-3792-4683-8e6a-bf1f039259ad" />
<img width="1730" height="1232" alt="DearBloom ERD" src="https://github.com/user-attachments/assets/3e4a2b9b-39ce-487d-b0e4-5f56a12dde4b" />

도메인 주도 패키지 구조 — ERD 색상으로 표시한 핵심 도메인 7개를 기준으로 스프링 부트 패키지를 구성

# API 명세서

| 환경 | Swagger |
| --- | --- |
| 운영 | https://api.dearbloom.co.kr/swagger-ui/index.html |
| 개발 | https://dev-api.dearbloom.co.kr/swagger-ui/index.html |

