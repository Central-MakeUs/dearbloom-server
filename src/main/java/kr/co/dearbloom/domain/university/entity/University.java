package kr.co.dearbloom.domain.university.entity;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class University {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long universityId;

    @Column(unique = true)
    private String schoolCode;

    @Column(nullable = false)
    private String name;

    private String nameEn;

    private String campusType;

    private String region;

    private String address;

    private String postalCode;

    private String establishmentType;

    private String website;

    private String phone;
}
