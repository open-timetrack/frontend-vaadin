package open.timetrack.frontend.vaadin.data.service;

import jakarta.transaction.Transactional;
import open.timetrack.frontend.vaadin.data.entity.TimeTrack;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class TimeTrackService {

    private final TimeTrackRepository repository;

    public TimeTrackService(TimeTrackRepository repository) {
        this.repository = repository;
    }

    public Optional<TimeTrack> get(String id) {
        return repository.findById(id);
    }

    @Transactional
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
        return repository.findAllByDateOrderByStartTime(shownDate, pageable);
    }
    public Float getSumHoursWorked(LocalDate shownDate){
        return getSumHoursWorked(repository.findAllByDateOrderByStartTime(shownDate, Pageable.unpaged()).stream());
    }
    public static Float getSumHoursWorked(Stream<TimeTrack> items){
        double sum = items.map(TimeTrack::getHoursTaken)
                .filter(Objects::nonNull)
                .mapToDouble(value -> value)
                .sum();
        return (float) Math.round(sum * 10f) / 10;
    }
}
