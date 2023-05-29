package open.timetrack.frontend.vaadin.views.timetracks;

import io.cucumber.java8.En;
import open.timetrack.frontend.vaadin.TimeTrackViewIntegrationTest;
import open.timetrack.frontend.vaadin.data.entity.TimeTrack;
import open.timetrack.frontend.vaadin.data.service.TimeTrackRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalTime;

public class TimeTrackViewSteps extends TimeTrackViewIntegrationTest implements En {

    @Autowired
    TimeTrackRepository repository;

    public TimeTrackViewSteps() {
        Given("^(\\d+) timeTrack for today$", (Integer arg0) -> {
            TimeTrack timeTrack = new TimeTrack();
            timeTrack.setDate(LocalDate.now());
            timeTrack.setStartTime(LocalTime.NOON);
            timeTrack.setEndTime(LocalTime.NOON.plusHours(2));
            timeTrack.setTask("Test Task");
            timeTrack.setNote("Test note\nwhich is multilined");
            repository.save(timeTrack);
        });

        And("^view the last day$", () -> {
            calledTimeTrackView.showLastDay();
        });
        And("^view the next day$", () -> {
            calledTimeTrackView.showNextDay();
        });
    }
}
