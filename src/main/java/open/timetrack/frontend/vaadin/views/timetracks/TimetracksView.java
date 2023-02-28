package open.timetrack.frontend.vaadin.views.timetracks;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import java.time.Duration;
import java.util.Optional;
import open.timetrack.frontend.vaadin.data.entity.TimeTrack;
import open.timetrack.frontend.vaadin.data.service.TimeTrackService;
import open.timetrack.frontend.vaadin.views.MainLayout;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@PageTitle("Timetracks")
@Route(value = "timetracks/:timeTrackID?/:action?(edit)", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class TimetracksView extends Div implements BeforeEnterObserver {

    private final String TIMETRACK_ID = "timeTrackID";
    private final String TIMETRACK_EDIT_ROUTE_TEMPLATE = "timetracks/%s/edit";

    private final Grid<TimeTrack> grid = new Grid<>(TimeTrack.class, false);

    private DateTimePicker start;
    private DateTimePicker end;
    private TextField task;
    private TextField note;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private final BeanValidationBinder<TimeTrack> binder;

    private TimeTrack timeTrack;

    private final TimeTrackService timeTrackService;

    public TimetracksView(TimeTrackService timeTrackService) {
        this.timeTrackService = timeTrackService;
        addClassNames("timetracks-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("start").setAutoWidth(true);
        grid.addColumn("ende").setAutoWidth(true);
        grid.addColumn("task").setAutoWidth(true);
        grid.addColumn("note").setAutoWidth(true);
        grid.setItems(query -> timeTrackService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(TIMETRACK_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(TimetracksView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(TimeTrack.class);

        // Bind fields. This is where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.timeTrack == null) {
                    this.timeTrack = new TimeTrack();
                }
                binder.writeBean(this.timeTrack);
                timeTrackService.update(this.timeTrack);
                clearForm();
                refreshGrid();
                Notification.show("Data updated");
                UI.getCurrent().navigate(TimetracksView.class);
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show(
                        "Error updating the data. Somebody else has updated the record while you were making changes.");
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Failed to update the data. Check again that all values are valid");
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> timeTrackId = event.getRouteParameters().get(TIMETRACK_ID).map(Long::parseLong);
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
                event.forwardTo(TimetracksView.class);
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
        start = new DateTimePicker("Start");
        start.setStep(Duration.ofSeconds(1));
        end = new DateTimePicker("End");
        end.setStep(Duration.ofSeconds(1));
        task = new TextField("Task");
        note = new TextField("Note");
        formLayout.add(start, end, task, note);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
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
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(TimeTrack value) {
        this.timeTrack = value;
        binder.readBean(this.timeTrack);

    }
}
