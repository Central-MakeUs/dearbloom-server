package kr.co.dearbloom.domain.artist.repository;

import kr.co.dearbloom.domain.artist.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
}
