package open.timetrack.frontend.vaadin.data.entity;

import jakarta.persistence.Entity;
import java.time.LocalDateTime;

@Entity
public class TimeTrack extends AbstractEntity {

    private LocalDateTime start;
    private LocalDateTime ende;
    private String task;
    private String note;

    public LocalDateTime getStart() {
        return start;
    }
    public void setStart(LocalDateTime start) {
        this.start = start;
    }
    public LocalDateTime getEnde() {
        return ende;
    }
    public void setEnde(LocalDateTime end) {
        this.ende = end;
    }
    public String getTask() {
        return task;
    }
    public void setTask(String task) {
        this.task = task;
    }
    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }

}
