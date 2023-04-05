package open.timetrack.frontend.vaadin.data.service;

import open.timetrack.frontend.vaadin.data.entity.TimeTrack;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface TimeTrackRepository extends JpaRepository<TimeTrack, String>, JpaSpecificationExecutor<TimeTrack> {
    Page<TimeTrack> findAllByDateOrderByStartTime(LocalDate date, Pageable pageable);
}
