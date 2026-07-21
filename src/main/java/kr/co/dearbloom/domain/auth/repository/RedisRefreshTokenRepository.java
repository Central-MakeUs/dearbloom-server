package kr.co.dearbloom.domain.auth.repository;

import kr.co.dearbloom.domain.auth.entity.RedisRefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RedisRefreshTokenRepository extends CrudRepository<RedisRefreshToken, Long> {
}
