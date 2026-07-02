package kr.co.dearbloom.domain.artist.repository.work;

import kr.co.dearbloom.domain.artist.entity.work.WorkFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkFileRepository extends JpaRepository<WorkFile, Long> {
}
