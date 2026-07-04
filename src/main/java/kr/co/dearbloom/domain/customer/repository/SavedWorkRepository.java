package kr.co.dearbloom.domain.customer.repository;

import kr.co.dearbloom.domain.customer.entity.SavedWork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavedWorkRepository extends JpaRepository<SavedWork, Long> {
}
