package kr.co.dearbloom.global.auth.oauth.custom;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@Tag(name = "Apple OAuth Web Login", description = "애플 웹 로그인")
@Hidden
public class AppleWebLoginController {
    private final AppleWebLoginService appleWebLoginService;

    /** 애플 인증 페이지로 리다이렉트. */
    @GetMapping("/oauth2/apple/authorize")
    public void authorize(HttpServletResponse response) throws IOException {
        response.sendRedirect(appleWebLoginService.createAuthorizeUrl(response));
    }

    /** 애플이 form_post 로 보내는 콜백. 검증·회원처리 후 프론트로 리다이렉트. */
    @PostMapping("/oauth2/apple/callback")
    public void callback(
            @RequestParam(name = "id_token", required = false) String idToken,
            @RequestParam(name = "state", required = false) String state,
            @RequestParam(name = "error", required = false) String error,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        response.sendRedirect(appleWebLoginService.handleCallback(idToken, state, error, request, response));
    }
}
