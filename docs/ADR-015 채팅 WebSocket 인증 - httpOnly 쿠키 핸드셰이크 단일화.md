> **ADR-015 · Auth · Accepted · 2026-08-09**
> 웹에서 STOMP CONNECT 헤더에 토큰을 실을 수 없어 채팅 연결이 아예 안 되던 문제를 어떻게 풀지 결정. → **`/ws` 핸드셰이크(HTTP)에서 httpOnly `accessToken` 쿠키를 읽어 세션 속성에 넣고, CONNECT 는 그 값만 검증한다. Authorization 헤더 경로는 제거한다. 쿠키 인증이므로 Origin 허용목록이 필수가 된다.**

## 맥락
- [[ADR-012 채팅 - 1대1 방·메시지 + 문의 자동연동 + WebSocket 수신]] 최초 구현은 STOMP `CONNECT` 프레임의 `Authorization: Bearer` 헤더로만 인증했다.
- 그런데 로그인 토큰은 **httpOnly 쿠키**다([[환경별 쿠키 설정]]). JS 가 `document.cookie` 로 읽을 수 없으니 웹 프론트는 **CONNECT 헤더에 넣을 문자열을 얻을 방법이 없다.** 웹에서는 연결이 성립조차 못 하는 상태였다.
- 브라우저 `WebSocket`/SockJS API 는 **커스텀 헤더를 붙일 수 없다.** 핸드셰이크 헤더로도 못 넣는다.
- 반면 `/ws` 핸드셰이크는 일반 HTTP 요청이라 **브라우저가 쿠키를 자동으로 붙인다.**
- 모바일 앱은 Expo + WebView 껍데기라 결국 같은 브라우저 컨텍스트다 — "앱은 헤더를 쓸 수 있다"는 전제가 성립하지 않는다.

## 결정
- **핸드셰이크에서 쿠키를 읽어 세션 속성에 보관** — `CookieTokenHandshakeInterceptor` 가 `accessToken` 쿠키를 꺼내 `attributes` 에 넣는다.
- **핸드셰이크는 토큰이 없어도 통과시킨다.** 인증 실패 판정은 CONNECT 단계에서 한다 — 핸드셰이크에서 거부하면 브라우저에 에러 사유가 남지 않는다.
- **Authorization 헤더 경로를 제거**(1차엔 "헤더 우선, 없으면 쿠키" fallback 이었으나 걷어냄). 웹뷰 포함 모든 클라이언트가 쿠키를 쓰므로 경로가 둘일 이유가 없고, 두 경로는 테스트되지 않는 쪽이 썩는다.
- **CONNECT 에서 Principal 확정** — `StompAuthChannelInterceptor` 가 토큰을 검증하고 `ChatPrincipal(memberId, role, profileId)` 을 `accessor.setUser()` 로 심는다. 실패 시 연결 거부.
- **SUBSCRIBE 에서 방 참여자 검증** — `/topic/rooms/{roomId}` 구독 시 그 방의 고객/작가 본인인지 확인(타인 방 도청 차단). 인터셉터는 트랜잭션 밖이라 LAZY 없이 참여자 PK 만 조회한다.
- **Origin 허용목록 필수** — `setAllowedOrigins(corsOrigins)`, REST CORS 와 같은 값. 와일드카드 금지.
- 메시지 **전송은 REST 유지**, WebSocket 은 **수신 전용**([[ADR-012 채팅 - 1대1 방·메시지 + 문의 자동연동 + WebSocket 수신]] 그대로).

## 검토한 대안
| 대안 | 기각 이유 |
|---|---|
| CONNECT 헤더 방식 유지 + 프론트가 토큰을 헤더에 첨부 | httpOnly 라 JS 가 토큰을 못 읽는다. 읽게 하려면 httpOnly 를 포기해야 하고 그러면 XSS 로 토큰이 털린다 |
| 쿼리 파라미터로 토큰 전달(`/ws?token=...`) | URL 은 서버 액세스 로그·프록시·Referer 에 남는다. 토큰을 로그에 뿌리는 셈 |
| 헤더 우선 + 쿠키 fallback (1차 구현) | 실제 클라이언트가 전부 웹/웹뷰라 헤더 경로를 아무도 안 탄다. 안 쓰는 인증 경로가 남으면 검증 안 된 채로 썩는다 |
| 핸드셰이크에서 인증 실패 시 거부 | 브라우저가 핸드셰이크 실패 사유를 노출하지 않아 프론트 디버깅이 불가능 |
| Origin 와일드카드 유지 | **쿠키 인증에서는 치명적.** 브라우저가 쿠키를 자동으로 붙이므로 임의 사이트가 사용자 쿠키를 실어 붙을 수 있다(CSWSH). WS 핸드셰이크는 CORS preflight 를 타지 않아 서버가 직접 Origin 을 봐야 한다 |

## 결과 (트레이드오프)
- ✅ 웹에서 채팅이 동작한다. 프론트는 연결 시 토큰을 다룰 필요가 없다.
- ✅ 토큰이 JS 에 노출되지 않아 httpOnly 의 이점을 유지한다.
- ✅ 인증 경로가 하나라 검증 지점이 명확하다.
- ⚠️ **Origin 허용목록이 유일한 CSWSH 방어선이다.** `setAllowedOrigins("*")` 는 Spring 이 막아주지 않는다(REST 는 `allowCredentials(true)` + `*` 조합이면 부팅 시 터지지만 WS 는 조용히 통과). 배포 환경변수에 와일드카드가 없는지 확인이 필요하다.
- ⚠️ 프론트 배포 도메인이 목록에 없으면 **핸드셰이크 단계에서 막힌다.** 이전에는 `*` 라 목록이 틀려도 드러나지 않았다 — 연결 실패 시 여기부터 의심할 것.
- ⚠️ **핸드셰이크 때 잡은 토큰이 세션 내내 유지된다.** 소켓이 살아 있는 동안은 토큰이 만료돼도 연결이 끊기지 않는다. 채팅방을 오래 열어두는 화면이면 주기적 재연결·만료 체크가 필요하다.
- ⚠️ 네이티브 SDK 로 붙는 클라이언트가 생기면 헤더 경로를 되살려야 한다 — 그때 다시 두 경로가 된다.
- ⚠️ **nginx 에 WebSocket 업그레이드 프록시 설정이 필요하다.** `Upgrade`/`Connection` 은 hop-by-hop 헤더라 기본적으로 전달되지 않고, upstream 연결이 HTTP/1.0 이면 핸드셰이크가 성립하지 않는다. `proxy_read_timeout` 기본 60초도 올려야 유휴 소켓이 끊기지 않는다.

## 관련
- `domain/chat/ws/{CookieTokenHandshakeInterceptor, StompAuthChannelInterceptor, ChatPrincipal}`
- `global/config/WebSocketConfig` (`setAllowedOrigins`, `url.cors-origins`)
- 인프라: `nginx.conf` 의 `/ws` location (`proxy_http_version 1.1`, `Upgrade`/`Connection`, `proxy_read_timeout`)

## 관련 노트
- [[ADR-012 채팅 - 1대1 방·메시지 + 문의 자동연동 + WebSocket 수신]]
- [[ADR-013 채팅 API 역할 분리 - 고객·작가 컨트롤러·DTO 분기]]
- [[채팅-WebSocket-쿠키인증-변경사항]] — 프론트 전달용 안내(헤더 fallback 이 있던 시점 기준이라 지금과 다름)
- [[환경별 쿠키 설정]]
