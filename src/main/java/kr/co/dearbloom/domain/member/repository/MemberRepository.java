package kr.co.dearbloom.domain.member.repository;

import kr.co.dearbloom.domain.member.entity.OAuthAccount;
import kr.co.dearbloom.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    @Query("SELECT m FROM Member m JOIN m.oauthAccounts oa WHERE oa = :oauthAccount")
    Optional<Member> findByOauthAccount(@Param("oauthAccount") OAuthAccount oauthAccount);

    Optional<Member> findByName(String name);

    @Query("SELECT m FROM Member m JOIN m.oauthAccounts oa WHERE oa.name = :name")
    Member findByOauthAccountName(@Param("name") String name);

    boolean existsByName(String name);

    boolean existsByEmail(String email);

}
