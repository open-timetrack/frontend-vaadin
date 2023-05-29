package open.timetrack.frontend.vaadin.views.timetracks;


import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.cucumber.java8.En;
import open.timetrack.frontend.vaadin.TimeTrackViewIntegrationTest;

import static com.github.mvysny.kaributesting.v10.GridKt._size;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;

public class TimeTrackViewV1Steps extends TimeTrackViewIntegrationTest implements En, TimeTrackViewTestAdapter {

    public TimeTrackViewV1Steps() {
        When("^timeTrackViewV1 is called$", () -> {
            UI.getCurrent().navigate(TimeTrackViewV1.class);
            calledTimeTrackView = this;
        });
    }

    @Override
    public int countShownTimeTracks() {
        return _size(_get(Grid.class));
    }

    @Override
    public void showLastDay() {
        _get(Button.class,buttonSearchSpecJ -> buttonSearchSpecJ.withIcon(VaadinIcon.ARROW_LEFT)).click();
    }
    @Override
    public void showNextDay() {
        _get(Button.class,buttonSearchSpecJ -> buttonSearchSpecJ.withIcon(VaadinIcon.ARROW_RIGHT)).click();
    }
}
