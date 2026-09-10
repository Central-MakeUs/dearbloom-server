# DearBloom Backend
<img width="3072" height="1500" alt="그래픽 이미지" src="https://github.com/user-attachments/assets/64cee248-6d1a-4011-afde-daf275d76a1a" />

## 📃 개요

| 항목     | 상세                                                                                                                                                                                                                                            |
| ------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 소개     | 졸업스냅 작가 탐색부터 문의 · 채팅 · 예약까지 하나로 연결하는 매칭 플랫폼                                                                                                                                                                                                   |
| 기간     | 26년 06월 ~ 08월                                                                                                                                                                                                                                 |
| 인원     | 기획 1, 디자인 1명, 프론트엔드 2, 백엔드 1                                                                                                                                                                                                                  |
| 서비스 링크 | [iOS](https://apps.apple.com/kr/app/dearbloom-%EB%94%94%EC%96%B4%EB%B8%94%EB%A3%B8-%EC%A1%B8%EC%97%85%EC%8A%A4%EB%83%85/id6792470769) / [Android](https://play.google.com/store/apps/details?id=kr.co.dearbloom.mobile&pcampaignid=web_share) |


## 📜 주요 기능

| 기능 | 상세 |
|---|---|
| 멀티 역할 계정 | 토큰 activeRole 전환, 역할별 커스텀 ArgumentResolver 3종 |
| 작가 일정 관리 | 규칙만 저장(sparse), 24비트 마스크 합성, 연속 슬롯 시프트 검증 |
| 문의·예약 | 상태 단일화로 슬롯 잠금, append-only 이력, 시점 스냅샷 보존 |
| 대학교 자동완성 | Redis Sorted Set ZRANGEBYLEX, 한글 초성 매칭 |
| 작품 탐색 | QueryDSL 동적 필터, 커서 페이지네이션, 첫 화면 캐시 + 이벤트 무효화 |
| 조회수 어뷰징 방지 | Redis SETNX+TTL 중복 제거, 증분 누적 후 주기 flush |
| 소셜 로그인 | OAuth2 구글·애플 (네이티브/웹), JWT, HttpOnly 쿠키 |
| 실시간 채팅 | WebSocket·STOMP, 구독 시점 인가, 읽음 상태 브로드캐스트 |
| 푸시 알림 | FCM HTTP v1 직접 호출, iOS·Android 단일 요청 분기 |
| 이미지 업로드 | S3 Presigned URL 클라이언트 직접 업로드, CloudFront |
| 애플 토큰 revoke | .p8 client_secret 생성, 앱 심사 대응 |
| 안내 메일 | AWS SES SMTP, 온보딩 완료 이벤트 |
| 설계 기록 | ADR 25편, Swagger 공통 에러 자동 문서화 |

## 🧭 IA (Information Architecture)
<img width="2024" height="824" alt="image" src="https://github.com/user-attachments/assets/85913465-ec57-442d-80e7-6dd7757c856d" />

## ⚙️ 기술 스택
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
| Mail            | Spring Boot Starter Mail, <br>AWS SES (SMTP)     | 안내 메일 전송                                    |

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

## 🏛️ System Architecture
<img width="1307" height="939" alt="dearbloom-system-architecture" src="https://github.com/user-attachments/assets/284d55ae-337b-4d87-abc6-33174bba8ee8" />

## 📊 ERD
<img width="1720" height="1308" alt="dearbloom-erd-summary" src="https://github.com/user-attachments/assets/6d2bf592-3792-4683-8e6a-bf1f039259ad" />
<img width="1730" height="1232" alt="dearbloom-erd-cloud" src="https://github.com/user-attachments/assets/9c91ffe9-1546-49ec-968d-57f1e43b2689" />

## 📁 프로젝트 구조 (패키지 트리)
도메인 주도 패키지 구조 — ERD 색상의 핵심 도메인 8개를 기준으로 스프링 부트 패키지를 구성

```
kr.co.dearbloom
├── DearbloomServerApplication.java
│
├── domain/
│   ├── artist/
│   ├── artwork/
│   ├── auth/
│   ├── board/
│   ├── chat/
│   ├── customer/
│   ├── inquiry/
│   ├── member/
│   ├── notification/
│   ├── report/
│   ├── review/
│   └── university/
│
└── global/
    ├── auth/
    ├── config/
    ├── dev/
    ├── dto/
    ├── entity/
    ├── file/
    ├── health/
    ├── properties/
    ├── push/
    ├── swagger/
    ├── util/
    └── validation/
```

## 🔗 API 명세서

| 환경 | Swagger |
| --- | --- |
| 운영 | https://api.dearbloom.co.kr/swagger-ui/index.html |
| 개발 | https://dev-api.dearbloom.co.kr/swagger-ui/index.html |

