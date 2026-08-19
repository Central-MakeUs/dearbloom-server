package kr.co.dearbloom.global.dev.controller;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import kr.co.dearbloom.global.dev.dto.DevLoginResponse;
import kr.co.dearbloom.global.dev.service.DevSampleService;
import kr.co.dearbloom.global.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dev/sample")
@Hidden
@Profile("prod")
public class DevSampleController {
    private final DevSampleService devSampleService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<DevLoginResponse>> login(
            @RequestParam String password,
            @RequestParam Long memberId,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(devSampleService.login(password, memberId, request)));
    }
}
