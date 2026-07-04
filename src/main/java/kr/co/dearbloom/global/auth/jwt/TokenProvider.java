package kr.co.dearbloom.global.auth.jwt;

import kr.co.dearbloom.domain.member.entity.MemberRole;
import kr.co.dearbloom.domain.auth.repository.OAuthAccountRepository;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.member.service.MemberQueryService;
import kr.co.dearbloom.global.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
// 토큰을 생성하고 올바른 토큰인지 유효성 검사를 하고, 토큰에서 필요한 정보를 가져오는 클래스
public class TokenProvider {
    private final JwtProperties jwtProperties;
    private final OAuthAccountRepository oauthAccountRepository;
    private final MemberQueryService memberQueryService;

    public String generateToken(Member member, Duration expiredAt){
        return generateToken(member, expiredAt, null);
    }

    // overrideActiveRole 이 있으면 최근 접속 Role 대신 강제 사용 (예: Dev 로그인에서 role 지정 테스트)
    public String generateToken(Member member, Duration expiredAt, MemberRole overrideActiveRole){
        Date now = new Date();
        return makeToken(new Date(now.getTime() + expiredAt.toMillis()), member, overrideActiveRole);
    }

    /**
     * {
     *   "sub": "123",
     *   "activeRole": "ARTIST",
     *   "availableRoles": ["CUSTOMER", "ARTIST"],
     *   "iat": 1718000000,
     *   "exp": 1718000900
     * }
     */
    // JWT 토큰 생성 메서드
    private String makeToken(Date expiry, Member member, MemberRole overrideActiveRole) {
        Date now = new Date();

        List<MemberRole> availableRoles = memberQueryService.getAvailableRoles(member);
        List<String> availableRoleNames = availableRoles.stream().map(Enum::name).toList();
        // 최근 접속 Role 이 없으면(둘 다 미생성 등) 생성된 Role 중 첫 번째로 대체
        String activeRoleName = overrideActiveRole != null
                ? overrideActiveRole.name()
                : member.getRecentRole() != null
                        ? member.getRecentRole().name()
                        : availableRoleNames.isEmpty() ? null : availableRoleNames.getFirst();

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE) // 헤더 typ: JWT
                .setIssuer(jwtProperties.issuer()) // 내용 iss: asdf@mail.com(properties에서 설정한 값)
                .setIssuedAt(now)       // 내용 iat: 현재 시간
                .setExpiration(expiry)  // 내용 exp: expiry 멤버 변수값
                .setSubject(member.getEmail()) // 내용 sub: member의 이메일
                .claim("memberId", member.getMemberId()) // 클레임 id: memberId
                .claim("activeRole", activeRoleName) // 클레임: 최근 접속 Role
                .claim("availableRoles", availableRoleNames) // 클레임: 생성되어 있는 Role 목록
                .signWith(SignatureAlgorithm.HS256, jwtProperties.secretKey())
                //서명: secretKey와 함께 해시값을 HS256 방식으로 암호화
                .compact();
    }

    // JWT 토큰 유효성 검증 메서드
    public boolean validToken(String token){
        try{
            Jwts.parser()
                    .setSigningKey(jwtProperties.secretKey()) // secretKey로 복호화
                    .parseClaimsJws(token);
            return true;
        }catch(Exception e){ // 복호화 과정에서 에러나면 유효하지 않은 토큰
            return false;
        }
    }

    // 토큰 기반으로 인증 정보를 가져오는 메서드
    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);

        Set<SimpleGrantedAuthority> authorities = Stream.concat(
                Stream.of(new SimpleGrantedAuthority("ROLE_USER")),
                getAvailableRoles(token).stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
        ).collect(Collectors.toSet());

        Member member = memberQueryService.getByMemberIdOrThrow(claims.get("memberId", Long.class));
        return new UsernamePasswordAuthenticationToken(member, token, authorities);
    }

    // 토큰 기반으로 회원 ID를 가져오는 메서드
    public Long getMemberId(String token){
        Claims claims = getClaims(token);
        return claims.get("memberId", Long.class);
    }

    // 토큰 기반으로 생성되어 있는 Role 목록을 가져오는 메서드
    @SuppressWarnings("unchecked")
    public List<MemberRole> getAvailableRoles(String token) {
        Claims claims = getClaims(token);
        List<String> roleNames = claims.get("availableRoles", List.class);
        if (roleNames == null) {
            return List.of();
        }
        return roleNames.stream().map(MemberRole::valueOf).toList();
    }

    // 토큰 기반으로 최근 접속 Role 을 가져오는 메서드
    public MemberRole getActiveRole(String token) {
        Claims claims = getClaims(token);
        String activeRoleName = claims.get("activeRole", String.class);
        return activeRoleName != null ? MemberRole.valueOf(activeRoleName) : null;
    }

    private Claims getClaims(String token) {
        return Jwts.parser() // 클레임 조회
                .setSigningKey(jwtProperties.secretKey())
                .parseClaimsJws(token)
                .getBody();
    }

}
