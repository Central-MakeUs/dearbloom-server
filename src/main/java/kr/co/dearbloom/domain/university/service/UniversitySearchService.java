package kr.co.dearbloom.domain.university.service;

import kr.co.dearbloom.domain.university.dto.UniversitySearchResponse;
import kr.co.dearbloom.domain.university.repository.UniversityAutocompleteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UniversitySearchService {
    private final UniversityAutocompleteRepository autocompleteRepository;

    public List<UniversitySearchResponse> search(String query, int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return autocompleteRepository.search(query.strip(), limit).stream()
                .map(UniversitySearchResponse::fromRedisMember)
                .toList();
    }
}
