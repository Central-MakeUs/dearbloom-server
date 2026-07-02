package kr.co.dearbloom.domain.member.repository;

import kr.co.dearbloom.domain.member.entity.RedisRefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RedisRefreshTokenRepository extends CrudRepository<RedisRefreshToken, Long> {
}
