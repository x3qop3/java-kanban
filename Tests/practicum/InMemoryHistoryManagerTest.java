package practicum;

import org.junit.jupiter.api.Test;
import practicum.manager.InMemoryHistoryManager;
import practicum.model.Task;
import practicum.statandtype.Status;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;


public class InMemoryHistoryManagerTest {


    @Test
    void add() {
        Task task = new Task("Title1", "description1", Status.NEW);  // Создаем задачу с нужными параметрами

        InMemoryHistoryManager historyManager = new InMemoryHistoryManager();

        historyManager.add(task);

        final List<Task> history = historyManager.getHistory();

        assertNotNull(history, "После добавления задачи, история не должна быть пустой.");
        assertEquals(1, history.size(), "После добавления задачи, история не должна быть пустой.");

    }

}
