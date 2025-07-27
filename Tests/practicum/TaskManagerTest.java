package practicum;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import practicum.manager.TaskManager;
import practicum.model.Epic;
import practicum.model.Subtask;
import practicum.model.Task;
import practicum.statandtype.Status;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    protected abstract T createTaskManager();

    @BeforeEach
    void setUp() {
        taskManager = createTaskManager();
    }
    @Test
    void shouldCreateAndGetTask() {
        Task task = new Task("Test1", "Description1", Status.NEW, null, null);
        Task created = taskManager.createTask(task);

        assertNotNull(created.getId());
        assertEquals(task.getTitle(), created.getTitle());
        assertEquals(task.getDescription(), created.getDescription());
        assertEquals(task.getStatus(), created.getStatus());
    }
    @Test
    void shouldUpdateTask() {
        Task task = taskManager.createTask(new Task("Test1", "Desc1", Status.NEW, null, null));
        Task updated = new Task(task.getId(), "Updated", "New Desc", Status.IN_PROGRESS, null, null);

        taskManager.updateTask(updated);
        Task result = taskManager.getTask(task.getId());

        assertEquals("Updated", result.getTitle());
        assertEquals(Status.IN_PROGRESS, result.getStatus());
    }
    @Test
    void shouldDeleteTask() {
        Task task = taskManager.createTask(new Task("Test", "Desc", Status.NEW, null, null));
        taskManager.deleteTask(task.getId());

        assertNull(taskManager.getTask(task.getId()));
    }
    @Test
    void shouldCreateEpic() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));
        assertNotNull(epic.getId());
        assertEquals(Status.NEW, epic.getStatus());
    }
    @Test
    void shouldCreateSubtaskWithEpic() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask = taskManager.createSubtask(
                new Subtask("Sub", "Desc", Status.NEW, epic.getId(), null, null));

        assertNotNull(subtask.getId());
        assertEquals(epic.getId(), subtask.getEpicId());
        assertTrue(epic.getSubtaskIds().contains(subtask.getId()));
    }
    @Test
    void shouldPrioritizeTasksCorrectly() {
        Task task1 = new Task("Task 1", "Desc", Status.NEW,
                Duration.ofHours(1), LocalDateTime.now());
        Task task2 = new Task("Task 2", "Desc", Status.NEW,
                Duration.ofHours(2), LocalDateTime.now().plusHours(3));

        taskManager.createTask(task1);
        taskManager.createTask(task2);

        List<Task> prioritized = taskManager.getPrioritizedTasks();
        assertEquals(2, prioritized.size());
        assertEquals(task1, prioritized.get(0));
        assertEquals(task2, prioritized.get(1));
    }
    @Test
    void epicStatusShouldBeNewWhenNoSubtasks() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc"));
        assertEquals(Status.NEW, epic.getStatus());
    }
    @Test
    void epicStatusShouldBeNewWhenAllSubtasksNew() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask = taskManager.createSubtask(
                new Subtask("Sub", "Desc", Status.NEW, epic.getId(), null, null));
        assertEquals(Status.NEW, epic.getStatus());
    }
    @Test
    void epicStatusShouldBeDoneWhenAllSubtasksDone() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask = taskManager.createSubtask(
                new Subtask("Sub", "Desc", Status.NEW, epic.getId(), null, null));

        subtask.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask);

        assertEquals(Status.DONE, epic.getStatus());
    }
    @Test
    void epicStatusShouldBeInProgressWhenMixedStatuses() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc"));
        Subtask sub1 = taskManager.createSubtask(
                new Subtask("Sub1", "Desc", Status.NEW, epic.getId(), null, null));
        Subtask sub2 = taskManager.createSubtask(
                new Subtask("Sub2", "Desc", Status.DONE, epic.getId(), null, null));

        assertEquals(Status.IN_PROGRESS, epic.getStatus());
    }
    @Test
    void shouldPreventTimeOverlaps() {
        Task task1 = new Task("Task 1", "Desc", Status.NEW,
                Duration.ofHours(1), LocalDateTime.now());
        taskManager.createTask(task1);

        Task task2 = new Task("Task 2", "Desc", Status.NEW,
                Duration.ofHours(1), LocalDateTime.now().plusMinutes(30));

        assertThrows(IllegalStateException.class, () -> taskManager.createTask(task2));
    }
}