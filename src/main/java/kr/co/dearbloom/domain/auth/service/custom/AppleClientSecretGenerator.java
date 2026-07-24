package kr.co.dearbloom.domain.auth.service.custom;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.List;

/**
 * Apple token/revoke 엔드포인트에 쓰는 client_secret(ES256 JWT)을 .p8 개인키로 서명해 생성한다.
 * iss=TeamID, sub=clientId(앱 Bundle ID 또는 Services ID), aud=appleid.apple.com, alg=ES256(kid=KeyID).
 */
@Slf4j
@Component
public class AppleClientSecretGenerator {
    private static final String AUDIENCE = "https://appleid.apple.com";

    @Value("${apple.team-id}")
    private String teamId;

    @Value("${apple.key-id}")
    private String keyId;

    @Value("${apple.private-key}")
    private String privateKeyPem;

    /** clientId(revoke/교환 대상)에 맞춘 client_secret 을 발급한다. 유효기간 5분. */
    public String generate(String clientId) {
        try {
            Instant now = Instant.now();
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .issuer(teamId)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plus(5, ChronoUnit.MINUTES)))
                    .audience(List.of(AUDIENCE))
                    .subject(clientId)
                    .build();
            SignedJWT jwt = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(keyId).build(),
                    claims);
            jwt.sign(new ECDSASigner(parsePrivateKey()));
            return jwt.serialize();
        } catch (Exception e) {
            log.error("[AppleClientSecret] 생성 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private ECPrivateKey parsePrivateKey() throws Exception {
        String content = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] der = Base64.getDecoder().decode(content);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return (ECPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(der));
    }
}
