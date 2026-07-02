package kr.co.dearbloom.global.auth.jwt;

import kr.co.dearbloom.domain.member.repository.OAuthAccountRepository;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.member.repository.MemberRepository;
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
import java.util.Collections;
import java.util.Date;
import java.util.Set;

@RequiredArgsConstructor
@Service
// 토큰을 생성하고 올바른 토큰인지 유효성 검사를 하고, 토큰에서 필요한 정보를 가져오는 클래스
public class TokenProvider {
    private final JwtProperties jwtProperties;
    private final OAuthAccountRepository oauthAccountRepository;
    private final MemberRepository memberRepository;

    public String generateToken(Member member, Duration expiredAt){
        Date now = new Date();
        return makeToken(new Date(now.getTime() + expiredAt.toMillis()), member);
    }

    /**
     * {
     *   "sub": "123",
     *   "activeRole": "PHOTOGRAPHER",
     *   "activeProfileId": "45", (Customer.id 또는 Photographer.id)
     *   "availableRoles": ["CUSTOMER", "PHOTOGRAPHER"],
     *   "iat": 1718000000,
     *   "exp": 1718000900
     * }
     */
    // JWT 토큰 생성 메서드
    private String makeToken(Date expiry, Member member) {
        Date now = new Date();

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE) // 헤더 typ: JWT
                .setIssuer(jwtProperties.issuer()) // 내용 iss: asdf@mail.com(properties에서 설정한 값)
                .setIssuedAt(now)       // 내용 iat: 현재 시간
                .setExpiration(expiry)  // 내용 exp: expiry 멤버 변수값
                .setSubject(member.getEmail()) // 내용 sub: member의 이메일
                .claim("memberId", member.getMemberId()) // 클레임 id: memberId
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
        Set<SimpleGrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));

        Member member = memberRepository.findById(claims.get("memberId", Long.class)).orElseThrow();
        return new UsernamePasswordAuthenticationToken(member, token, authorities);
//        return new UsernamePasswordAuthenticationToken(new org.springframework.security.core.userdetails.User(claims.getSubject
//                (), "", authorities), token, authorities);
    }

    // 토큰 기반으로 회원 ID를 가져오는 메서드
    public Long getMemberId(String token){
        Claims claims = getClaims(token);
        return claims.get("memberId", Long.class);
    }

    private Claims getClaims(String token) {
        return Jwts.parser() // 클레임 조회
                .setSigningKey(jwtProperties.secretKey())
                .parseClaimsJws(token)
                .getBody();
    }

}
