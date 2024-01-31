package open.timetrack.frontend.vaadin;

import io.cucumber.java8.En;

import static org.assertj.core.api.Assertions.assertThat;

public class ViewAssertions extends TimeTrackViewIntegrationTest implements En {
    public ViewAssertions() {
        Then("^(\\d+) timeTrack shown$", (Integer count) -> {
            assertThat(calledTimeTrackView.countShownTimeTracks()).isEqualTo(count);
        });
    }
}
