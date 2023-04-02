package open.timetrack.frontend.vaadin.data.service;

import open.timetrack.frontend.vaadin.data.entity.TimeTrack;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface TimeTrackRepository extends JpaRepository<TimeTrack, String>, JpaSpecificationExecutor<TimeTrack> {
    /**
     * Returns a {@link Page} of entities meeting the paging restriction provided in the {@link Pageable} object.
     *
     * @param pageable the pageable to request a paged result, can be {@link Pageable#unpaged()}, must not be
     *                 {@literal null}.
     * @return a page of entities
     */
    @Query("from TimeTrack tt where tt.start >= :startRange and tt.start <= :endRange order by tt.start")
    Page<TimeTrack> findAllAtDate(@Param("startRange") LocalDateTime start, @Param("endRange") LocalDateTime end, Pageable pageable);
}
