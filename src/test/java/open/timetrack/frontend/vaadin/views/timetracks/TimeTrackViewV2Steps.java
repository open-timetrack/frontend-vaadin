package open.timetrack.frontend.vaadin.views.timetracks;

import com.github.mvysny.kaributesting.v10.GridKt;
import com.vaadin.flow.component.UI;
import io.cucumber.java8.En;
import open.timetrack.frontend.vaadin.TimeTrackViewIntegrationTest;
import open.timetrack.frontend.vaadin.components.table.TimeTrackTable;

import static com.github.mvysny.kaributesting.v10.LocatorJ._find;

public class TimeTrackViewV2Steps extends TimeTrackViewIntegrationTest implements En, TimeTrackViewTestAdapter {
    public TimeTrackViewV2Steps() {
        When("^timeTrackViewV2 is called$", () -> {
            UI.getCurrent().navigate(TimeTrackViewV2.class);
            calledTimeTrackView = this;
        });
    }

    @Override
    public int countShownTimeTracks() {
        return _find(TimeTrackTable.class).stream()
                .map(TimeTrackTable::getGrid)
                .mapToInt(GridKt::_size)
                .sum();
    }

    @Override
    public void showLastDay() {
        //initially there are 7 Days shown and more are lazy loaded after scrolling. Scrolling cannot be tested with karibu testing because this is frontend js.
    }

    @Override
    public void showNextDay() {
        //initially there are 7 Days shown and more are lazy loaded after scrolling. Scrolling cannot be tested with karibu testing because this is frontend js.
    }
}
