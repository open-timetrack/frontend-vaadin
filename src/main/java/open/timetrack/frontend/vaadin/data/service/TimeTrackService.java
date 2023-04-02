package open.timetrack.frontend.vaadin.data.service;

import open.timetrack.frontend.vaadin.data.entity.TimeTrack;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class TimeTrackService {

    private final TimeTrackRepository repository;

    public TimeTrackService(TimeTrackRepository repository) {
        this.repository = repository;
    }

    public Optional<TimeTrack> get(String id) {
        return repository.findById(id);
    }

    public TimeTrack update(TimeTrack entity) {
        return repository.save(entity);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    public Page<TimeTrack> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<TimeTrack> list(Pageable pageable, Specification<TimeTrack> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

    public Page<TimeTrack> list(LocalDate shownDate, Pageable pageable) {
        return repository.findAllAtDate(shownDate.atTime(0, 0, 0),
                shownDate.plusDays(1).atTime(0, 0, 0),
                pageable);
    }
}
