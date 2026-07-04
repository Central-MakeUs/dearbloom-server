package kr.co.dearbloom.domain.artist.repository;

import kr.co.dearbloom.domain.artist.entity.PortfolioFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PortfolioFileRepository extends JpaRepository<PortfolioFile, Long> {
}
