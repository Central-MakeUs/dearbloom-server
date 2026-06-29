package kr.co.dearbloom.domain.university.dto;

public record UniversitySearchResponse(
        Long universityId,
        String name,
        String campusType,
        String region
) {
    private static final String DELIMITER = "\\|";

    public static UniversitySearchResponse fromRedisMember(String member) {
        String[] parts = member.split(DELIMITER);
        return new UniversitySearchResponse(
                Long.parseLong(parts[3]),
                parts[0],
                parts[1],
                parts[2]
        );
    }
}
