package kr.co.dearbloom.global.dev.controller;

import kr.co.dearbloom.global.dev.service.DevAccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/dev/account")
@Tag(name = "Dev - Account", description = "개발 전용 계정 관련 API")
public class DevAccountController {
    private final DevAccountService devAccountService;
}
