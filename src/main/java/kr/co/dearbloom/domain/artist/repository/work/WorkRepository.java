package kr.co.dearbloom.domain.artist.repository.work;

import kr.co.dearbloom.domain.artist.entity.work.Work;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkRepository extends JpaRepository<Work, Long> {
}
