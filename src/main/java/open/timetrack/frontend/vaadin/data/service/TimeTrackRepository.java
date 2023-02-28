package open.timetrack.frontend.vaadin.data.service;

import open.timetrack.frontend.vaadin.data.entity.TimeTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TimeTrackRepository extends JpaRepository<TimeTrack, Long>, JpaSpecificationExecutor<TimeTrack> {

}
