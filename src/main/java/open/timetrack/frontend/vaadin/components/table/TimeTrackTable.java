package open.timetrack.frontend.vaadin.components.table;

import com.helger.commons.annotation.VisibleForTesting;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.grid.editor.EditorEvent;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import open.timetrack.frontend.vaadin.data.entity.TimeTrack;
import open.timetrack.frontend.vaadin.data.service.TimeTrackService;
import org.springframework.data.domain.PageRequest;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;

public class TimeTrackTable extends VerticalLayout {
    private static final int MINUTE_STEPS = 15;
    public static final String HOURS_WORKED_TEXT = "Hours worked this day: %.2f";
    private final Span hoursWorkedLabel = new Span();
    private final Grid<TimeTrack> grid;

    public TimeTrackTable(TimeTrackService service, LocalDate shownDate) {

        add(createHeadline(service, shownDate));

        grid = createGrid(service, shownDate);
        Editor<TimeTrack> editor = createInGridEditor(grid);
        createGridEvents(grid);
        createInGridEditorEvents(editor, service);
        add(grid);

        refreshHoursWorkedLabelText();

    }

    private HorizontalLayout createHeadline(TimeTrackService service, LocalDate shownDate) {
        HorizontalLayout headline = new HorizontalLayout(
                new H2(shownDate.format(DateTimeFormatter.ISO_LOCAL_DATE)),
                createCreationButton(service, shownDate),
                hoursWorkedLabel);
        headline.setAlignItems(Alignment.CENTER);
        headline.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headline.setWidthFull();
        return headline;
    }

    private Grid<TimeTrack> createGrid(TimeTrackService service, LocalDate shownDate) {
        Grid<TimeTrack> grid = new Grid<>(TimeTrack.class, false);

        grid.addColumn("startTime").setWidth("130px").setFlexGrow(0);
        grid.addColumn("endTime").setWidth("130px").setFlexGrow(0);
        grid.addColumn(timeTrack -> Math.round(timeTrack.getHoursTaken() * 10) / 10f).setWidth("60px").setFlexGrow(0).setHeader("#");
        grid.addColumn("task").setWidth("400px");
        grid.addColumn("note").setWidth("400px");
        grid.setSortableColumns();

        grid.setItems(query -> service.list(shownDate, PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query))).stream());

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        grid.setPartNameGenerator(timeTrack -> timeTrack.getEndTime() == null ? "work-in-progress" : null);

        grid.setAllRowsVisible(true);
        return grid;
    }

    private void createGridEvents(Grid<TimeTrack> grid) {
        grid.addItemDoubleClickListener(e -> {
            grid.getEditor().editItem(e.getItem());
            Component editorComponent = e.getColumn().getEditorComponent();
            if (editorComponent instanceof Focusable) {
                ((Focusable<?>) editorComponent).focus();
            }
        });

        grid.asSingleSelect().addValueChangeListener(event -> grid.getEditor().save());
        grid.addCellFocusListener(event -> {
            if (event.getItem().isEmpty() && grid.getEditor().isOpen()) {
                grid.getEditor().save();
            }
        });

        grid.getDataProvider().addDataProviderListener(event -> refreshHoursWorkedLabelText());
    }

    private Editor<TimeTrack> createInGridEditor(Grid<TimeTrack> grid) {
        final Editor<TimeTrack> editor = grid.getEditor();
        Binder<TimeTrack> binder = new Binder<>(TimeTrack.class);
        editor.setBinder(binder);
        editor.setBuffered(true);

        TimePicker startTime = new TimePicker();
        startTime.setStep(Duration.ofMinutes(MINUTE_STEPS));
        startTime.setLocale(VaadinService.getCurrentRequest().getLocale());
        startTime.setWidthFull();
        binder.forField(startTime).bind("startTime");
        grid.getColumnByKey("startTime").setEditorComponent(startTime);

        TimePicker endTime = new TimePicker();
        endTime.setStep(Duration.ofMinutes(MINUTE_STEPS));
        endTime.setLocale(VaadinService.getCurrentRequest().getLocale());
        endTime.setWidthFull();
        binder.forField(endTime).bind("endTime");
        grid.getColumnByKey("endTime").setEditorComponent(endTime);

        TextField task = new TextField();
        task.setWidthFull();
        binder.forField(task).bind("task");
        grid.getColumnByKey("task").setEditorComponent(task);

        TextArea note = new TextArea();
        note.setWidthFull();
        binder.forField(note).bind("note");
        grid.getColumnByKey("note").setEditorComponent(note);
        return editor;
    }

    private void createInGridEditorEvents(Editor<TimeTrack> editor, TimeTrackService service) {
        editor.addSaveListener(event -> Optional.ofNullable(event).map(EditorEvent::getItem).ifPresent(service::update));
        editor.addSaveListener(event -> refreshHoursWorkedLabelText());
    }

    private void refreshHoursWorkedLabelText() {
        hoursWorkedLabel.setText(HOURS_WORKED_TEXT.formatted(TimeTrackService.getSumHoursWorked(grid.getDataProvider().fetch(new Query<>()))));
    }

    private Button createCreationButton(TimeTrackService service, LocalDate shownDate) {
        Button creationButton = new Button(new Icon(VaadinIcon.PLUS));
        creationButton.addClickListener(event -> {
            TimeTrack emptyTimeTrackEntryOfThatDay = new TimeTrack();
            emptyTimeTrackEntryOfThatDay.setDate(shownDate);
            service.update(emptyTimeTrackEntryOfThatDay);
            refreshGrid();
        });
        return creationButton;
    }

    public void addGridSelectionListener(SelectionListener<Grid<TimeTrack>, TimeTrack> selectionListener) {
        grid.addSelectionListener(selectionListener);
    }

    public void deselectAllInGrid() {
        grid.deselectAll();
        grid.getEditor().save();
    }

    public Set<TimeTrack> getGridSelection() {
        return grid.getSelectedItems();
    }

    public void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    @VisibleForTesting
    public Grid<TimeTrack> getGrid() {
        return grid;
    }
}
