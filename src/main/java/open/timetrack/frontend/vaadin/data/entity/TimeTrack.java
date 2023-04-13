package open.timetrack.frontend.vaadin.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Entity
public class TimeTrack extends AbstractEntity {

    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String task;
    private String note;

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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    @Transient
    public Float getHoursTaken() {
        return getStartTime() != null ? Math.round(getStartTime().until(getEndTime() != null ? getEndTime() : LocalTime.now(), ChronoUnit.MINUTES) / 60f * 10) / 10f : null;
    }
}
