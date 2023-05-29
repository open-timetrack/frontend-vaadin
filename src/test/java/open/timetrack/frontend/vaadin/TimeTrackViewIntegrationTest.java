package open.timetrack.frontend.vaadin;

import io.cucumber.spring.CucumberContextConfiguration;
import open.timetrack.frontend.vaadin.views.timetracks.TimeTrackViewTestAdapter;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest
public class TimeTrackViewIntegrationTest {

    protected static TimeTrackViewTestAdapter calledTimeTrackView = null;
}
