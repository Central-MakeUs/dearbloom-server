package kr.co.dearbloom.global.dev.controller;

import kr.co.dearbloom.global.dev.service.DevMemberService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/dev/member")
@Tag(name = "Dev - Member", description = "개발 전용 계정 관련 API")
public class DevMemberController {
    private final DevMemberService devMemberService;
}
