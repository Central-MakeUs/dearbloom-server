package kr.co.dearbloom.domain.customer.entity;

import jakarta.persistence.*;
import kr.co.dearbloom.domain.artist.entity.work.Work;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_saved_work_customer_work",
        columnNames = {"customer_id", "work_id"}))
public class SavedWork {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long savedWorkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_id", nullable = false)
    private Work work;
}
