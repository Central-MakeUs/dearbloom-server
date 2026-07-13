package kr.co.dearbloom.domain.product.repository;

import kr.co.dearbloom.domain.product.entity.PortfolioFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PortfolioFileRepository extends JpaRepository<PortfolioFile, Long> {
}
