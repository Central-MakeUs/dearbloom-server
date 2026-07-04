package kr.co.dearbloom.global.dev.controller;

import kr.co.dearbloom.global.dev.dto.SampleMemberResponse;
import kr.co.dearbloom.global.dev.dto.SamplePageResponse;
import kr.co.dearbloom.global.dto.response.ApiResponse;
import kr.co.dearbloom.global.dto.response.exception.ErrorDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/dev/response")
@Tag(name = "Dev - Response", description = "API 공통 응답 형태 예시")
@Profile("!prod")
public class DevResponseController {

    @GetMapping("/success/data")
    @Operation(summary = "성공 응답 예시 (단건)")
    public ResponseEntity<ApiResponse<SampleMemberResponse>> successExample() {
        SampleMemberResponse data = new SampleMemberResponse(1L, "블루밍", "bloom@dearbloom.co.kr");
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/success/data/list")
    @Operation(summary = "성공 응답 예시 (단순 리스트)")
    public ResponseEntity<ApiResponse<List<SampleMemberResponse>>> successListExample() {
        List<SampleMemberResponse> data = List.of(
                new SampleMemberResponse(1L, "블루밍", "bloom@dearbloom.co.kr"),
                new SampleMemberResponse(2L, "디어", "dear@dearbloom.co.kr"),
                new SampleMemberResponse(3L, "로즈", "rose@dearbloom.co.kr")
        );
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/success/data/page")
    @Operation(summary = "성공 응답 예시 (페이지)")
    public ResponseEntity<ApiResponse<SamplePageResponse>> successPageExample() {
        SamplePageResponse data = new SamplePageResponse(
                List.of(
                        new SampleMemberResponse(1L, "블루밍", "bloom@dearbloom.co.kr"),
                        new SampleMemberResponse(2L, "디어", "dear@dearbloom.co.kr"),
                        new SampleMemberResponse(3L, "로즈", "rose@dearbloom.co.kr")
                ),
                12L, 4L, 1L
        );
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/success/no-data")
    @Operation(summary = "성공 응답 예시 (데이터 없음)")
    public ResponseEntity<ApiResponse<Void>> successNoDataExample() {
        return ResponseEntity.ok(ApiResponse.success());
    }

    // ────────────────────────────────────────────────────────────────────────────────────────────────

    @GetMapping("/error/not-found")
    @Operation(summary = "실패 응답 예시 (404)")
    public ResponseEntity<ApiResponse<Void>> errorNotFoundExample() {
        return ResponseEntity.status(404).body(ApiResponse.error(
                ErrorDetail.builder()
                        .code("MEMBER-404")
                        .message("Member를 찾을 수 없습니다.")
                        .build()
        ));
    }

    @GetMapping("/error/unauthorized")
    @Operation(summary = "실패 응답 예시 (401)")
    public ResponseEntity<ApiResponse<Void>> errorUnauthorizedExample() {
        return ResponseEntity.status(401).body(ApiResponse.error(
                ErrorDetail.builder()
                        .code("AUTH-401")
                        .message("유효하지 않은 토큰입니다.")
                        .build()
        ));
    }

    @GetMapping("/error/conflict")
    @Operation(summary = "실패 응답 예시 (409)")
    public ResponseEntity<ApiResponse<Void>> errorConflictExample() {
        return ResponseEntity.status(409).body(ApiResponse.error(
                ErrorDetail.builder()
                        .code("MEMBER-409")
                        .message("이미 존재하는 닉네임입니다.")
                        .build()
        ));
    }
}
