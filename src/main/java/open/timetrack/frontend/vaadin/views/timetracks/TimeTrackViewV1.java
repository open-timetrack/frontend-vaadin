package open.timetrack.frontend.vaadin.views.timetracks;

import com.helger.commons.annotation.VisibleForTesting;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import open.timetrack.frontend.vaadin.data.entity.TimeTrack;
import open.timetrack.frontend.vaadin.data.service.TimeTrackService;
import open.timetrack.frontend.vaadin.views.MainLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;

@PageTitle("TimeTrack")
@Route(value = "timeTrack/v1/:timeTrackID?/:action?(edit)", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class TimeTrackViewV1 extends Div implements BeforeEnterObserver, TimeTrackView {

    public static final int MINUTE_STEPS = 15;
    private final String TIMETRACK_ID = "timeTrackID";
    private final String TIMETRACK_EDIT_ROUTE_TEMPLATE = "timeTrack/%s/edit";

    private final Grid<TimeTrack> grid = new Grid<>(TimeTrack.class, false);
    private final Span hoursWorkedText;
    private LocalDate shownDate;

    private TimePicker startTime;
    private TimePicker endTime;
    private TextField task;
    private TextArea note;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");
    private final Button delete = new Button("Delete");
    private final Button endNow = new Button("End now");

    private final BeanValidationBinder<TimeTrack> binder;

    private TimeTrack timeTrack;

    private final TimeTrackService timeTrackService;

    private static final Logger LOG = LoggerFactory.getLogger(TimeTrackViewV1.class);

    public TimeTrackViewV1(TimeTrackService timeTrackService) {
        this.timeTrackService = timeTrackService;
        shownDate = LocalDate.now();
        addClassNames("timetracks-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        DatePicker datePicker = new DatePicker(shownDate);
        datePicker.setLocale(VaadinService.getCurrentRequest().getLocale());
        datePicker.addValueChangeListener(event -> {
            shownDate = event.getValue();
            refreshGrid();
        });

        Button backOneDay = new Button(new Icon(VaadinIcon.ARROW_LEFT));
        backOneDay.addClickListener(buttonClickEvent -> {
            shownDate = shownDate.minusDays(1);
            datePicker.setValue(shownDate);
            clearForm();
        });
        Button forwardOneDay = new Button(new Icon(VaadinIcon.ARROW_RIGHT));
        forwardOneDay.addClickListener(buttonClickEvent -> {
            shownDate = shownDate.plusDays(1);
            datePicker.setValue(shownDate);
            clearForm();
        });
        HorizontalLayout horizontalLayout = new HorizontalLayout(backOneDay, datePicker, forwardOneDay);
        horizontalLayout.setPadding(true);
        horizontalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        add(horizontalLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("startTime").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn("endTime").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn("hoursTaken").setWidth("60px").setFlexGrow(0).setHeader("#");
        grid.addColumn("task").setAutoWidth(true);
        grid.addColumn("note").setAutoWidth(true);
        grid.setSortableColumns();
        grid.setItems(query -> this.timeTrackService.list(shownDate, PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query))).stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        grid.setPartNameGenerator(timeTrack -> {
            if (timeTrack.getEndTime() == null) return "work-in-progress";
            return null;
        });

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(TIMETRACK_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(TimeTrackViewV1.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(TimeTrack.class);

        // Bind fields. This is where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            UI.getCurrent().navigate(TimeTrackViewV1.class);
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.timeTrack == null) {
                    this.timeTrack = new TimeTrack();
                }
                timeTrack.setDate(shownDate);
                binder.writeBean(this.timeTrack);
                this.timeTrackService.update(this.timeTrack);
                clearForm();
                refreshGrid();
                Notification.show("Data updated");
                UI.getCurrent().navigate(TimeTrackViewV1.class);
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show("Error updating the data. Somebody else has updated the record while you were making changes.");
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Failed to update the data. Check again that all values are valid");
            }
        });

        endNow.addClickListener(event -> {
            try {
                if (this.timeTrack == null) {
                    Notification n = Notification.show("cannot end new tasks");
                    n.setPosition(Position.MIDDLE);
                    n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                binder.writeBean(this.timeTrack);
                timeTrack.setEndTime(LocalTime.now().withSecond(0).withNano(0));
                this.timeTrackService.update(this.timeTrack);
                clearForm();
                refreshGrid();
                Notification.show("Data updated");
                UI.getCurrent().navigate(TimeTrackViewV1.class);
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show("Error updating the data. Somebody else has updated the record while you were making changes.");
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Failed to update the data. Check again that all values are valid");
            }
        });

        delete.addClickListener(event -> {
            try {
                if (this.timeTrack == null) {
                    Notification n = Notification.show("cannot delete new tasks");
                    n.setPosition(Position.MIDDLE);
                    n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                binder.writeBean(this.timeTrack);
                this.timeTrackService.delete(this.timeTrack.getId());
                clearForm();
                refreshGrid();
                Notification.show("Data deleted");
                UI.getCurrent().navigate(TimeTrackViewV1.class);
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show("Error updating the data. Somebody else has updated the record while you were making changes.");
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Failed to update the data. Check again that all values are valid");
            }
        });


        hoursWorkedText = new Span();
        add(new HorizontalLayout(new Span("Hours worked this day: "), hoursWorkedText));

        hoursWorkedText.setText(String.valueOf(this.timeTrackService.getSumHoursWorked(shownDate)));
        datePicker.addValueChangeListener(event -> hoursWorkedText.setText(String.valueOf(this.timeTrackService.getSumHoursWorked(shownDate))));

        datePicker.setValue(LocalDate.now());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<String> timeTrackId = event.getRouteParameters().get(TIMETRACK_ID);
        if (timeTrackId.isPresent()) {
            Optional<TimeTrack> timeTrackFromBackend = timeTrackService.get(timeTrackId.get());
            if (timeTrackFromBackend.isPresent()) {
                populateForm(timeTrackFromBackend.get());
            } else {
                Notification.show(String.format("The requested timeTrack was not found, ID = %s", timeTrackId.get()),
                        3000, Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(TimeTrackViewV1.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        startTime = new TimePicker("Start of that task");
        startTime.setStep(Duration.ofMinutes(MINUTE_STEPS));
        startTime.setMin(LocalTime.of(8, 0));
        startTime.setMax(LocalTime.of(20, 0));
        startTime.setLocale(VaadinService.getCurrentRequest().getLocale());
        UI.getCurrent().getPage().retrieveExtendedClientDetails(extendedClientDetails -> {
            final LocalTime now = LocalTime.now(ZoneId.of(extendedClientDetails.getTimeZoneId()));
            startTime.setValue(now.withNano(0)
                    .withSecond(0)
                    .minusMinutes(now.getMinute() % MINUTE_STEPS));
        });
        endTime = new TimePicker("End of that task");
        endTime.setStep(Duration.ofMinutes(MINUTE_STEPS));
        endTime.setMin(LocalTime.of(8, 0));
        endTime.setMax(LocalTime.of(20, 0));
        endTime.setLocale(VaadinService.getCurrentRequest().getLocale());
        task = new TextField("Task");
        note = new TextArea("Note");

        formLayout.add(startTime, endTime, task, note);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        endNow.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        buttonLayout.add(save, cancel,endNow,delete);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
        hoursWorkedText.setText(String.valueOf(timeTrackService.getSumHoursWorked(shownDate)));
    }

    private void clearForm() {
        populateForm(null);
        UI.getCurrent().getPage().retrieveExtendedClientDetails(extendedClientDetails -> {
            final LocalTime now = LocalTime.now(ZoneId.of(extendedClientDetails.getTimeZoneId()));
            startTime.setValue(now.withNano(0)
                    .withSecond(0)
                    .minusMinutes(now.getMinute() % MINUTE_STEPS));
        });
    }

    private void populateForm(TimeTrack value) {
        this.timeTrack = value;
        binder.readBean(this.timeTrack);
    }


    @VisibleForTesting
    Grid<TimeTrack> getGrid() {
        return grid;
    }
}
