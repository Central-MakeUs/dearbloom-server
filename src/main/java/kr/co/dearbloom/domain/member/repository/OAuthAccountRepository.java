package kr.co.dearbloom.domain.member.repository;

import kr.co.dearbloom.domain.member.entity.OAuthAccount;
import kr.co.dearbloom.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {
    Optional<OAuthAccount> findByOauthId(String oauthId);

    Optional<OAuthAccount> findByMember(Member member);

    boolean existsByName(String name);
}
