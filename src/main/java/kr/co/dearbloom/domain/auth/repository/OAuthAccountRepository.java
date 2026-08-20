package kr.co.dearbloom.domain.auth.repository;

import kr.co.dearbloom.domain.auth.entity.OAuthAccount;
import kr.co.dearbloom.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {
    Optional<OAuthAccount> findByOauthId(String oauthId);

    Optional<OAuthAccount> findByMember(Member member);

    // 가입 안내 메일처럼 Member 엔티티 없이 memberId 만 들고 오는 비동기 경로에서 쓴다.
    Optional<OAuthAccount> findByMember_MemberId(Long memberId);

    boolean existsByName(String name);
}
