package practicum.manager;

import practicum.statandtype.Status;
import practicum.model.Epic;
import practicum.model.Subtask;
import practicum.model.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskManager {


    List<Task> getAllTasks();
    void deleteAllTasks();
    Task getTask(int id);
    Task createTask(Task task);
    void updateTask(Task task);
    void deleteTask(int id);
    List<Task> getHistory();
    boolean containsTask(int id);
    void updateTaskTime(int taskId, LocalDateTime startTime, Duration duration);

    List<Epic> getAllEpics();
    void deleteAllEpics();
    Epic getEpic(int id);
    Epic createEpic(Epic epic);
    void updateEpic(Epic epic);
    void deleteEpic(int id);
    Status getEpicStatus(int epicId);
    void updateEpicStatus(int epicId);
    boolean containsEpic(int id);

    List<Subtask> getAllSubtasks();
    void deleteAllSubtasks();
    Subtask getSubtask(int id);
    Subtask createSubtask(Subtask subtask);
    void updateSubtask(Subtask subtask);
    void deleteSubtask(int id);
    boolean containsSubtask(int id);
    void updateSubtaskTime(int subtaskId, LocalDateTime startTime, Duration duration);

    List<Subtask> getSubtasksByEpic(int epicId);

    boolean isTimeSlotAvailable(LocalDateTime startTime, Duration duration);
    List<Task> getPrioritizedTasks();
}