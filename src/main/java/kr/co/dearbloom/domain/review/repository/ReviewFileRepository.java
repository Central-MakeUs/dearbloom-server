package kr.co.dearbloom.domain.review.repository;

import kr.co.dearbloom.domain.review.entity.ReviewFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewFileRepository extends JpaRepository<ReviewFile, Long> {
}
