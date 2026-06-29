package kr.co.dearbloom.domain.university.controller;

import kr.co.dearbloom.domain.university.dto.UniversitySearchResponse;
import kr.co.dearbloom.domain.university.service.UniversitySearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import kr.co.dearbloom.global.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/universities")
@RequiredArgsConstructor
@Tag(name = "University", description = "대학교 API")
public class UniversityController {
    private final UniversitySearchService universitySearchService;

    @Operation(summary = "대학교 검색 (prefix 자동완성)", description = "대학교 이름으로 검색하여 자동완성 결과를 반환합니다. <br> "
            + "한국어(한글) 대학교명만 검색할 수 있습니다. <br> "
            + "검색어 keyword는 입력한 글자로 시작하는 대학교를 prefix 매칭합니다. (1글자부터 검색, 공백만 입력 시 빈 결과) <br> "
            + "초성 검색을 지원합니다. 자음만 입력해도 됩니다. (예: 'ㄱ' → 초성이 ㄱ인 대학, '강ㅇ' → '강원대학교') <br> "
            + "자음을 연달아 입력하면 각 글자의 초성으로 매칭됩니다. (예: 'ㄱㄹ' → '고려대학교', 'ㅅㅇㄷ' → '서울대학교') <br> "
            + "limit는 반환 개수로 기본값 10이며 1 이상이어야 합니다. (별도 상한 없음)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "검색 성공 (조건에 맞는 대학이 없으면 빈 배열)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "잘못된 요청 (keyword 누락, limit가 1 미만이거나 정수가 아님)")
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UniversitySearchResponse>>> search(
            @Parameter(description = "검색어 (한글 대학교명, 초성 가능). 필수", example = "서울")
            @RequestParam String keyword,
            @Parameter(description = "반환 개수 (1 이상, 기본 10)", example = "10")
            @RequestParam(defaultValue = "10") @Positive(message = "limit은 1 이상이어야 합니다.") int limit) {
        return ResponseEntity.ok(ApiResponse.success(
                universitySearchService.search(keyword, limit)
        ));
    }
}
