package kr.co.dearbloom.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@ConfigurationProperties("jwt")
public record JwtProperties(
        String issuer,
        String secretKey,

        @DurationUnit(ChronoUnit.MILLIS)
        Duration refreshTokenExpiry,

        @DurationUnit(ChronoUnit.MILLIS)
        Duration accessTokenExpiry
) {
}