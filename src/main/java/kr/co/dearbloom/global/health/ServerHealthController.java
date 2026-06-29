package kr.co.dearbloom.global.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;

import static kr.co.dearbloom.global.health.HealthStatus.*;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Health - Spring", description = "스프링 부트 서버 상태 | 정보 | 시간 확인")
public class ServerHealthController {

    @Operation(summary = "스프링 부트 서버 헬스체크")
    @GetMapping
    public String healthCheck() {
        return "DearBloom Server " + CONNECTED;
    }

    @Operation(summary = "서버 정보 확인")
    @GetMapping("/info")
    public String info(HttpServletRequest request) {
        String requestUrl = request.getRequestURL().toString();
        return String.format("""
                        [서버 정보 확인]
                        - 시간       : %s
                        - IP         : %s
                        - User-Agent : %s
                        - 요청 URL   : %s
                        """,
                ZonedDateTime.now(),
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                requestUrl
        );
    }

    @Operation(summary = "서버 시간 확인")
    @GetMapping("/time")
    public ZonedDateTime time() {
        return ZonedDateTime.now();
    }


}
