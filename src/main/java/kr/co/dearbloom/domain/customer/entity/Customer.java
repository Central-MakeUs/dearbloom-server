package kr.co.dearbloom.domain.customer.entity;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.member.entity.Member;
import kr.co.dearbloom.domain.university.entity.University;
import kr.co.dearbloom.global.entity.BaseTime;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
public class Customer extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id")
    private University university;

    private String name;

    // 실명 수정. 이름은 중복 허용이라 유니크 검증 없음.
    public void updateName(String name) {
        this.name = name;
    }

    // 회원 탈퇴 시 고객 프로필 PII 익명화.
    public void anonymize() {
        this.name = "탈퇴한 사용자";
        this.university = null;
    }

    // 역할 해지로 익명화됐던 프로필을 재온보딩 때 되살린다(같은 사람 복귀 — 기존 행·문의 이력 유지).
    public void reactivate(String name, University university) {
        this.name = name;
        this.university = university;
    }
}
