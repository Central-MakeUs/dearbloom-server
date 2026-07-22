package kr.co.dearbloom.domain.artist.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.dearbloom.domain.artist.dto.schedule.request.DateBlockRequest;
import kr.co.dearbloom.domain.artist.dto.schedule.request.RecurringBlockRequest;
import kr.co.dearbloom.domain.artist.dto.schedule.request.WeeklyAvailabilityUpdateRequest;
import kr.co.dearbloom.domain.artist.dto.schedule.response.DayAvailabilityResponse;
import kr.co.dearbloom.domain.artist.dto.schedule.response.ScheduleRuleResponse;
import kr.co.dearbloom.domain.artist.dto.schedule.response.ScheduleSummaryResponse;
import kr.co.dearbloom.domain.artist.entity.artist.Artist;
import kr.co.dearbloom.domain.artist.facade.ScheduleFacade;
import kr.co.dearbloom.domain.artist.util.BookingWindow;
import kr.co.dearbloom.global.auth.resolver.CurrentArtist;
import kr.co.dearbloom.global.dto.response.ApiResponse;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import kr.co.dearbloom.global.swagger.ApiErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/artists/me/schedule")
@RequiredArgsConstructor
@Tag(name = "Artist Schedule", description = "작가 일정 관리 API")
public class ScheduleController {
    private final ScheduleFacade scheduleFacade;

    @GetMapping
    @Operation(summary = "내 캘린더 조회 (합성 가용 슬롯)",
            description = """
                    from~to 기간의 날짜별 <b>예약 가능 30분 셀</b>을 조회합니다.
                    기본 촬영 가능에서 반복 예약 불가·개인 예약 불가·예약 확정을 뺀 결과입니다.<br>
                    시간축은 09:00~21:00 고정, 30분 단위입니다.<br>
                    <b>from/to 는 생략 가능</b>합니다 — 생략 시 예약 오픈 창 전체(오늘 ~ 오늘+3개월)를 조회합니다.<br>
                    예약 날짜는 <b>오늘부터 3개월</b>까지만 열립니다 — 창 밖·과거 날짜는 가용 셀이 빈 배열로 내려갑니다. (조회 span 최대 약 3개월)
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN,
            ErrorCode.ROLE_ACCESS_DENIED, ErrorCode.ARTIST_NOT_FOUND, ErrorCode.PARAMETER_BAD_REQUEST})
    public ResponseEntity<ApiResponse<List<DayAvailabilityResponse>>> getMyCalendar(
            @CurrentArtist Artist artist,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        LocalDate resolvedFrom = from != null ? from : BookingWindow.firstOpenDate();
        LocalDate resolvedTo = to != null ? to : BookingWindow.lastOpenDate();
        return ResponseEntity.ok(ApiResponse.success(
                scheduleFacade.getMyCalendar(artist, resolvedFrom, resolvedTo)
        ));
    }

    @GetMapping("/summary")
    @Operation(summary = "일정 규칙 요약 조회",
            description = """
                    기본 촬영 가능(/weekly) · 반복 예약 불가(/recurring-blocks) · 개인 예약 불가(/date-blocks) 리스트를
                    한 번에 묶어서 조회합니다. 세 섹션을 한 화면에서 같이 보여주는 설정 화면용입니다.<br>
                    개인 예약 불가만 조회 기간의 영향을 받습니다. <b>from/to 는 생략 가능</b>합니다 —
                    생략 시 예약 오픈 창 전체(오늘 ~ 오늘+3개월)로 조회합니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN,
            ErrorCode.ROLE_ACCESS_DENIED, ErrorCode.ARTIST_NOT_FOUND})
    public ResponseEntity<ApiResponse<ScheduleSummaryResponse>> getScheduleSummary(
            @CurrentArtist Artist artist,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        LocalDate resolvedFrom = from != null ? from : BookingWindow.firstOpenDate();
        LocalDate resolvedTo = to != null ? to : BookingWindow.lastOpenDate();
        return ResponseEntity.ok(ApiResponse.success(
                scheduleFacade.getScheduleSummary(artist, resolvedFrom, resolvedTo)
        ));
    }

    @GetMapping("/weekly")
    @Operation(summary = "기본 촬영 가능 일정 조회",
            description = "현재 등록된 기본 촬영 가능 일정 리스트를 요일·시작시각 순으로 조회합니다. PUT /weekly 요청 폼에 기존 값을 채울 때 사용합니다.")
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN,
            ErrorCode.ROLE_ACCESS_DENIED, ErrorCode.ARTIST_NOT_FOUND})
    public ResponseEntity<ApiResponse<List<ScheduleRuleResponse>>> getWeeklyAvailability(
            @CurrentArtist Artist artist
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                scheduleFacade.getWeeklyAvailability(artist)
        ));
    }

    @PutMapping("/weekly")
    @Operation(summary = "기본 촬영 가능 일정 업데이트",
            description = """
                    <b>이 API(/weekly)가 관리하는 기본 촬영 가능 일정만</b> 보낸 리스트로 업데이트합니다.
                    반복 예약 불가(/recurring-blocks)·개인 예약 불가(/date-blocks)는 건드리지 않습니다 — 각각 별도 API로 관리합니다.<br>
                    한 요일에 여러 구간을 넣을 수 있고, 빈 리스트를 보내면 기본 가능 시간을 모두 비웁니다.<br>
                    각 시간은 09:00~21:00, 30분 단위여야 합니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.ARTIST_NOT_FOUND, ErrorCode.INVALID_SCHEDULE_TIME})
    public ResponseEntity<ApiResponse<Void>> replaceWeekly(
            @CurrentArtist Artist artist,
            @RequestBody @Valid WeeklyAvailabilityUpdateRequest request
    ) {
        scheduleFacade.replaceWeeklyAvailability(artist, request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/recurring-blocks")
    @Operation(summary = "반복 예약 불가 리스트 조회",
            description = "현재 등록된 반복 예약 불가(WEEKLY_BLOCK) 리스트를 요일·시작시각 순으로 조회합니다. 각 항목의 scheduleRuleId 로 삭제할 수 있습니다.")
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN,
            ErrorCode.ROLE_ACCESS_DENIED, ErrorCode.ARTIST_NOT_FOUND})
    public ResponseEntity<ApiResponse<List<ScheduleRuleResponse>>> getRecurringBlocks(
            @CurrentArtist Artist artist
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                scheduleFacade.getRecurringBlocks(artist)
        ));
    }

    @PostMapping("/recurring-blocks")
    @Operation(summary = "반복 예약 불가 추가",
            description = "요일 기준 반복 차단(예: 매주 일요일, 매일 점심)을 1건 추가합니다(WEEKLY_BLOCK).")
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.ARTIST_NOT_FOUND, ErrorCode.INVALID_SCHEDULE_TIME})
    public ResponseEntity<ApiResponse<ScheduleRuleResponse>> addRecurringBlock(
            @CurrentArtist Artist artist,
            @RequestBody @Valid RecurringBlockRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                scheduleFacade.addRecurringBlock(artist, request)
        ));
    }

    @DeleteMapping("/recurring-blocks/{scheduleRuleId}")
    @Operation(summary = "반복 예약 불가 삭제",
            description = """
                    추가 시 응답으로 받은 scheduleRuleId 로 반복 예약 불가(WEEKLY_BLOCK) 1건을 삭제합니다.<br>
                    본인 소유가 아니거나 해당 id 가 반복 예약 불가가 아니면 404 를 반환합니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.ARTIST_NOT_FOUND, ErrorCode.SCHEDULE_RULE_NOT_FOUND})
    public ResponseEntity<ApiResponse<Void>> deleteRecurringBlock(
            @CurrentArtist Artist artist,
            @PathVariable Long scheduleRuleId
    ) {
        scheduleFacade.deleteRecurringBlock(artist, scheduleRuleId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/date-blocks")
    @Operation(summary = "개인 예약 불가 리스트 조회",
            description = """
                    현재 등록된 개인 예약 불가(DATE_BLOCK) 리스트를 날짜·시작시각 순으로 조회합니다.
                    각 항목의 scheduleRuleId 로 삭제할 수 있습니다.<br>
                    <b>from/to 는 생략 가능</b>합니다 — 생략 시 예약 오픈 창 전체(오늘 ~ 오늘+3개월)를 조회합니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN,
            ErrorCode.ROLE_ACCESS_DENIED, ErrorCode.ARTIST_NOT_FOUND})
    public ResponseEntity<ApiResponse<List<ScheduleRuleResponse>>> getDateBlocks(
            @CurrentArtist Artist artist,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        LocalDate resolvedFrom = from != null ? from : BookingWindow.firstOpenDate();
        LocalDate resolvedTo = to != null ? to : BookingWindow.lastOpenDate();
        return ResponseEntity.ok(ApiResponse.success(
                scheduleFacade.getDateBlocks(artist, resolvedFrom, resolvedTo)
        ));
    }

    @PostMapping("/date-blocks")
    @Operation(summary = "개인 예약 불가 추가",
            description = "특정 날짜 차단을 1건 추가합니다(DATE_BLOCK). 하루 전체를 막으려면 09:00~21:00 으로 보냅니다.")
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.ARTIST_NOT_FOUND, ErrorCode.INVALID_SCHEDULE_TIME})
    public ResponseEntity<ApiResponse<ScheduleRuleResponse>> addDateBlock(
            @CurrentArtist Artist artist,
            @RequestBody @Valid DateBlockRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                scheduleFacade.addDateBlock(artist, request)
        ));
    }

    @DeleteMapping("/date-blocks/{scheduleRuleId}")
    @Operation(summary = "개인 예약 불가 삭제",
            description = """
                    추가 시 응답으로 받은 scheduleRuleId 로 개인 예약 불가(DATE_BLOCK) 1건을 삭제합니다.<br>
                    본인 소유가 아니거나 해당 id 가 개인 예약 불가가 아니면 404 를 반환합니다.
                    """)
    @ApiErrorCodes({ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN, ErrorCode.ROLE_ACCESS_DENIED,
            ErrorCode.ARTIST_NOT_FOUND, ErrorCode.SCHEDULE_RULE_NOT_FOUND})
    public ResponseEntity<ApiResponse<Void>> deleteDateBlock(
            @CurrentArtist Artist artist,
            @PathVariable Long scheduleRuleId
    ) {
        scheduleFacade.deleteDateBlock(artist, scheduleRuleId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
