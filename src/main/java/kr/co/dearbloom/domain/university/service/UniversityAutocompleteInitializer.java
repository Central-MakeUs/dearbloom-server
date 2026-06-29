package kr.co.dearbloom.domain.university.service;

import kr.co.dearbloom.domain.university.repository.UniversityAutocompleteRepository;
import kr.co.dearbloom.domain.university.repository.UniversityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UniversityAutocompleteInitializer implements ApplicationRunner {
    private final UniversityRepository universityRepository;
    private final UniversityAutocompleteRepository autocompleteRepository;

    @Override
    public void run(ApplicationArguments args) {
        var universities = universityRepository.findAll();
        autocompleteRepository.buildIndex(universities);
        log.info("대학교 자동완성 인덱스 생성 완료: {}건", universities.size());
    }
}
