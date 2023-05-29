package open.timetrack.frontend.vaadin.views.timetracks;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import open.timetrack.frontend.vaadin.components.table.TimeTrackTable;
import open.timetrack.frontend.vaadin.data.service.TimeTrackService;
import open.timetrack.frontend.vaadin.views.MainLayout;

import java.time.LocalDate;
import java.util.Collection;

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
                });
                        """);

        UI.getCurrent().addShortcutListener(this::deleteSelectedTimeTracks, Key.DELETE);
        UI.getCurrent().addShortcutListener(this::deleteSelectedTimeTracks, Key.BACKSPACE);
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
        TimeTrackTable timeTrackTable = new TimeTrackTable(timeTrackService, dateToShow);
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
