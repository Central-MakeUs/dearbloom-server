package kr.co.dearbloom.domain.member.event;

import kr.co.dearbloom.domain.member.entity.MemberRole;

/**
 * 신규 회원이 역할 프로필까지 만들었다 — 가입 안내 메일의 트리거.
 *
 * <p><b>소셜 로그인 직후({@code createMember})가 아니라 온보딩 완료 시점</b>에 발행한다.
 * 그 전에는 역할이 정해지지 않아 고객/작가별 내용을 고를 수 없고, 온보딩 도중 이탈한 사람에게도 메일이 나간다.
 *
 * <p>엔티티가 아니라 값만 싣는다. 수신 측이 비동기라 detached 엔티티의 LAZY 필드를 건드리면 터진다.
 * 메일 주소·로그인 수단은 수신 측이 자기 트랜잭션에서 조회한다 — 온보딩 응답을 그만큼 늦출 이유가 없다.
 *
 * @param profileName 고객이면 이름, 작가면 닉네임
 */
public record MemberSignedUpEvent(Long memberId, MemberRole role, String profileName) {
}
