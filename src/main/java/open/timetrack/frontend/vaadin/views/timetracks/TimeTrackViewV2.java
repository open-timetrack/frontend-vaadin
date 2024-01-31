package open.timetrack.frontend.vaadin.views.timetracks;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import open.timetrack.frontend.vaadin.components.table.TimeTrackTable;
import open.timetrack.frontend.vaadin.data.entity.TimeTrack;
import open.timetrack.frontend.vaadin.data.service.TimeTrackService;
import open.timetrack.frontend.vaadin.views.MainLayout;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Optional;

@PageTitle("TimeTrackV2")
@Route(value = "timeTrack/v2/:timeTrackID?/:action?(edit)", layout = MainLayout.class)
@RouteAlias(value = "v2", layout = MainLayout.class)
public class TimeTrackViewV2 extends Scroller {
    private final TimeTrackService timeTrackService;
    private final VerticalLayout content;

    public TimeTrackViewV2(TimeTrackService timeTrackService) {
        this.timeTrackService = timeTrackService;
        this.content = new VerticalLayout();
        content.setHeightFull();
        setContent(this.content);
        addClassNames("timetracks-view");

        setHeightFull();

        for (int i = 0; i < 7; i++)
            addTimeTrackTableForNextDate();

        getElement().executeJs("""
                var el = this;
                el.addEventListener("scroll", function(e) {
                    if(el.scrollTop + el.clientHeight == el.scrollHeight) {
                        el.$server.addTimeTrackTableForNextDate();
                    }
                });""");

        UI.getCurrent().addShortcutListener(this::deleteSelectedTimeTracks, Key.KEY_D, KeyModifier.ALT);
        UI.getCurrent().addShortcutListener(this::addTimeTrackOnToday, Key.KEY_N, KeyModifier.ALT);
        UI.getCurrent().addShortcutListener(this::addTimeTrackOnDate, Key.KEY_N, KeyModifier.ALT, KeyModifier.SHIFT);
    }

    private void addTimeTrackOnToday() {
        UI.getCurrent().getPage().retrieveExtendedClientDetails(extendedClientDetails -> {
            TimeTrack timeTrack = new TimeTrack();
            final LocalDateTime now = LocalDateTime.now(ZoneId.of(extendedClientDetails.getTimeZoneId()));
            timeTrack.setDate(now.toLocalDate());

            Optional<TimeTrack> lastTimeTrackOfToday = timeTrackService.list(now.toLocalDate(), Pageable.unpaged()).stream().reduce((timeTrack1, timeTrack2) -> timeTrack2);
            LocalTime startTime;
            if (lastTimeTrackOfToday.isPresent())
                startTime = lastTimeTrackOfToday.get().getEndTime();
            else
                startTime = now.toLocalTime().withSecond(0).withNano(0).withMinute(now.getMinute() / 15 * 15);

            timeTrack.setStartTime(startTime);

            timeTrackService.update(timeTrack);

            content.getChildren()
                    .map(TimeTrackTable.class::cast)
                    .findFirst().ifPresent(TimeTrackTable::refreshGrid);
        });
    }

    private void addTimeTrackOnDate() {
        // todo: show date picker to pick a date. Add a new timeTrack to that
    }

    private void deleteSelectedTimeTracks() {
        content.getChildren()
                .map(TimeTrackTable.class::cast)
                .map(TimeTrackTable::getGridSelection)
                .flatMap(Collection::stream)
                .forEach(timeTrack -> timeTrackService.delete(timeTrack.getId()));

        content.getChildren()
                .map(TimeTrackTable.class::cast)
                .filter(table -> !table.getGridSelection().isEmpty())
                .forEach(TimeTrackTable::refreshGrid);
    }

    @ClientCallable
    public void addTimeTrackTableForNextDate() {
        LocalDate dateToShow = LocalDate.now().minusDays(content.getComponentCount());
        TimeTrackTable timeTrackTable = new TimeTrackTable(timeTrackService, dateToShow, false);
        timeTrackTable.addGridSelectionListener(event -> {
            if (event.getFirstSelectedItem().isPresent())
                content.getChildren()
                        .map(TimeTrackTable.class::cast)
                        .filter(table -> table != timeTrackTable)
                        .forEach(TimeTrackTable::deselectAllInGrid);
        });
        content.add(timeTrackTable);
    }
}
