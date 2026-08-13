package kr.co.dearbloom.domain.report.service;

import kr.co.dearbloom.domain.artwork.entity.Artwork;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.report.entity.Report;
import kr.co.dearbloom.domain.report.repository.ReportRepository;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportCommandService {
    private final ReportRepository reportRepository;

    // 작품 신고 1건. 같은 대상을 이미 신고했으면 409(nullable FK 라 unique 만으론 부족해 여기서 막는다).
    public void reportArtwork(Member reporter, Artwork artwork, String content) {
        if (reportRepository.existsByReporterAndArtwork(reporter, artwork)) {
            throw new CustomException(ErrorCode.ALREADY_REPORTED);
        }
        reportRepository.save(Report.ofArtwork(reporter, artwork, content));
    }

    // 작품 삭제 시 그 작품에 달린 신고 정리(작품 삭제 경로에서 호출).
    public void deleteByArtwork(Artwork artwork) {
        reportRepository.deleteByArtwork(artwork);
    }

    // 회원 탈퇴 시 이 회원이 넣은 신고 정리. 신고자 FK 가 Member 라 남겨두면 탈퇴자와 계속 이어진다.
    public void deleteByReporter(Member reporter) {
        reportRepository.deleteByReporter(reporter);
    }
}
