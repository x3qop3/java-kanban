package taskobject;

public class SubTask extends Task {

    private int epicId;

    public SubTask(int epicId, String title, String description, Status status) {
        super(title, description, status);
        this.epicId = epicId;
    }

    public SubTask(int epicId, String title, String description, String startTime, String duration, Status status) {
        super(title, description, startTime, duration, status);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "\nSubTask{" +
                "id=" + getId() +
                ", epicId='" + epicId + '\'' +
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
        if (!super.equals(o)) return false;

        SubTask subTask = (SubTask) o;
        return epicId == subTask.epicId;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + epicId;
        return result;
    }
}
