package kr.co.dearbloom.domain.university.service;

import kr.co.dearbloom.domain.university.entity.University;
import kr.co.dearbloom.domain.university.repository.UniversityRepository;
import kr.co.dearbloom.global.dto.response.exception.CustomException;
import kr.co.dearbloom.global.dto.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UniversityQueryService {
    private final UniversityRepository universityRepository;

    public University findById(Long universityId) {
        return universityRepository.findById(universityId)
                .orElseThrow(() -> new CustomException(ErrorCode.UNIVERSITY_NOT_FOUND));
    }
}
