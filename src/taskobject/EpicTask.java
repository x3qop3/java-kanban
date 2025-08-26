package taskobject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EpicTask extends Task {
    private List<Integer> subTasksId;
    private LocalDateTime endTime;


    public EpicTask(String title, String description) {
        super(title, description, Status.NEW);

        subTasksId = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "\nEpicTask{" +
                "id=" + getId() +
                ", subTasksId=" + subTasksId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", startTime=" + (startTime != null ? startTime.format(form) : "не указано") +
                ", duration=" + (duration != null ? duration.toMinutes() : "не указано") +
                ", endTime=" + (endTime != null ? endTime.format(form) : "не указано") +
                ", status=" + status +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        EpicTask epicTask = (EpicTask) o;
        return subTasksId.equals(epicTask.subTasksId);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + subTasksId.hashCode();
        return result;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public List<Integer> getSubTasksId() {
        return subTasksId;
    }

    public void setSubTasksId(Integer id) { //метод для добавления подзадачи в список эпика
        subTasksId.add(id);
    }

    public void removeSubId(Integer id) {
        subTasksId.remove(id);
    }

    public void removeAllSubTasks() {
        subTasksId.clear();
    }


}
