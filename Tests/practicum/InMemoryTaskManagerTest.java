package practicum;

import org.junit.jupiter.api.Test;
import practicum.manager.InMemoryTaskManager;
import practicum.model.Task;
import practicum.statandtype.Status;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {
    @Test
    void TaskWithIDShouldBeEqual() {
        Task task1 = new Task("Title1", "Description1", Status.NEW);
        task1.setId(1);
        Task task2 = new Task("Title2", "Description2", Status.IN_PROGRESS);
        task2.setId(1);
        assertEquals(task1, task2);
    }


    @Test
    void addNewTask() {
        InMemoryTaskManager manager = new InMemoryTaskManager();
        Task task = new Task("Title1", "description1", Status.NEW);

        Task createdTask = manager.createTask(task);
        final int taskId = createdTask.getId();

        final Task savedTask = manager.getTask(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");
        final List<Task> tasks = manager.getAllTasks();
        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(0), "Задачи не совпадают.");
    }

}