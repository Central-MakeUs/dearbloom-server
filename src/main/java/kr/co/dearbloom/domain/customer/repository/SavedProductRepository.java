package kr.co.dearbloom.domain.customer.repository;

import kr.co.dearbloom.domain.customer.entity.SavedProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavedProductRepository extends JpaRepository<SavedProduct, Long> {
}
