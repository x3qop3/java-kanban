package taskobject;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Task {
    public static final DateTimeFormatter form = DateTimeFormatter.ofPattern("HH.mm dd.MM.yy");
    protected String title;
    protected String description;
    protected Status status;
    protected Duration duration;
    protected LocalDateTime startTime;
    private int id;

    public Task(String title, String description, Status status) {
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public Task(String title, String description, String startTime, String duration, Status status) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.duration = Duration.ofMinutes(Long.parseLong(duration));
        this.startTime = LocalDateTime.parse(startTime, form);

    }

    @Override
    public String toString() {
        return "\nTask{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", duration=" + (duration != null ? duration.toMinutes() : "не указано") +
                ", startTime=" + (startTime != null ? startTime.format(form) : "не указано") +
                ", endTime=" + (getEndTime() != null ? getEndTime().format(form) : "не указано") +
                ", status=" + status +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Task task = (Task) o;
        return id == task.id || title.equals(task.title) && description.equals(task.description) &&
                status.equals(task.status) && startTime.equals(task.startTime);
    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + id;
        return result;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        if (startTime != null) return startTime.plus(duration);
        else return null;
    }

    public long durationInMinute(Duration duration) {
        return duration.toMinutes();
    }

}
