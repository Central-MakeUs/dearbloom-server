package kr.co.dearbloom.domain.artist.repository.timeslot;

import kr.co.dearbloom.domain.artist.entity.timeslot.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
}
