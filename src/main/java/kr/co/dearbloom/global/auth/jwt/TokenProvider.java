package kr.co.dearbloom.global.auth.jwt;

import kr.co.dearbloom.domain.artist.repository.ArtistRepository;
import kr.co.dearbloom.domain.customer.repository.CustomerRepository;
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
    private final MemberQueryService memberQueryService;
    private final CustomerRepository customerRepository;
    private final ArtistRepository artistRepository;

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
     *   "activeProfileId": 45,
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
        // activeRole 은 요청이 지정한 role(override)로 정한다. 로그인·전환·온보딩·리프레시 모두 role 을 명시하므로
        // 정상 흐름에선 override 가 항상 있다. (recentRole 은 최근 접속 role 확인용 컬럼일 뿐 여기서 쓰지 않는다.)
        // override 가 없을 때만(엣지 케이스) 보유 role 중 첫 번째로, 그마저 없으면 null(프로필 미생성).
        String activeRoleName = overrideActiveRole != null
                ? overrideActiveRole.name()
                : availableRoleNames.isEmpty() ? null : availableRoleNames.getFirst();
        Long activeProfileId = resolveActiveProfileId(member, activeRoleName);

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE) // 헤더 typ: JWT
                .setIssuer(jwtProperties.issuer()) // 내용 iss: asdf@mail.com(properties에서 설정한 값)
                .setIssuedAt(now)       // 내용 iat: 현재 시간
                .setExpiration(expiry)  // 내용 exp: expiry 멤버 변수값
                .setSubject(member.getEmail()) // 내용 sub: member의 이메일
                .claim("memberId", member.getMemberId()) // 클레임 id: memberId
                .claim("activeRole", activeRoleName) // 클레임: 최근 접속 Role
                .claim("activeProfileId", activeProfileId) // 클레임: activeRole 에 대응하는 Customer/Artist PK
                .claim("availableRoles", availableRoleNames) // 클레임: 생성되어 있는 Role 목록
                .signWith(SignatureAlgorithm.HS256, jwtProperties.secretKey())
                //서명: secretKey와 함께 해시값을 HS256 방식으로 암호화
                .compact();
    }

    // activeRole 에 대응하는 Customer.id 또는 Artist.id 조회. activeRole 이 없거나 해당 프로필이 없으면 null.
    private Long resolveActiveProfileId(Member member, String activeRoleName) {
        if (activeRoleName == null) {
            return null;
        }
        return switch (MemberRole.valueOf(activeRoleName)) {
            case CUSTOMER -> customerRepository.findByMember(member).map(c -> c.getCustomerId()).orElse(null);
            case ARTIST -> artistRepository.findByMember(member).map(a -> a.getArtistId()).orElse(null);
        };
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

    // 토큰 기반으로 activeRole 에 대응하는 Customer/Artist PK 를 가져오는 메서드
    public Long getActiveProfileId(String token) {
        Claims claims = getClaims(token);
        return claims.get("activeProfileId", Long.class);
    }

    private Claims getClaims(String token) {
        return Jwts.parser() // 클레임 조회
                .setSigningKey(jwtProperties.secretKey())
                .parseClaimsJws(token)
                .getBody();
    }

}
