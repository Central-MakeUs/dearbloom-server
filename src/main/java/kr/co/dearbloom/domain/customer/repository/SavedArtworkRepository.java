package kr.co.dearbloom.domain.customer.repository;

import kr.co.dearbloom.domain.customer.entity.SavedArtwork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavedArtworkRepository extends JpaRepository<SavedArtwork, Long> {
}
