package manager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import taskobject.EpicTask;
import taskobject.Status;
import taskobject.SubTask;
import taskobject.Task;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryHistoryManagerTest {
    InMemoryTaskManager taskManager = new InMemoryTaskManager();
    HistoryManager historyManager = new InMemoryHistoryManager();


    @AfterEach
    void clearAll() {
        taskManager.removeAllTasksAllType();
    }

    @Test
    void add() {
        Task task = new Task("tit1", "dis1", Status.NEW);
        taskManager.createTask(task);

        historyManager.add(task);

        assertEquals(task, historyManager.getHistList().getFirst(), "Объекты не совпадают");
    }


    @Test
    void saveOldVersionAfterAddInHistory() {
        Task oldTask = new Task("tit1", "dis1", Status.NEW);
        taskManager.createTask(oldTask);
        taskManager.getTask(1);

        Task updTask = new Task("tit11", "dis11", Status.IN_PROGRESS);
        updTask.setId(1);

        taskManager.updateTask(updTask);

        Task taskFromHist = taskManager.getHistory().getFirst();

        assertEquals(taskFromHist, oldTask, "В историю сохранена другая версия");


        assertEquals(taskManager.getTask(1), updTask, "В массиве лежит не новая задача ");
    }

    @Test
    void containHistoryAndRemoveViewFromHistory() {
        Task task = new Task("tit1", "dis1", Status.NEW);
        taskManager.createTask(task);


        EpicTask epic1 = new EpicTask("epic1", "disE1");
        taskManager.createTask(epic1);

        SubTask sub11 = new SubTask(2, "sub11", "disS11", Status.DONE);
        taskManager.createTask(sub11);

        taskManager.getTask(1);
        taskManager.getEpicTask(2);
        taskManager.getSubTask(3);

        taskManager.removeHistoryItem(5);

        assertEquals(3, taskManager.getHistory().size(), "Из истории удалена несуществующая задача");

        assertEquals(3, taskManager.getHistory().size(), "История просмотра пуста");

        taskManager.removeHistoryItem(1);

        assertEquals(2, taskManager.getHistory().size(), "Обычная задача не удалена из истории");

        taskManager.removeHistoryItem(2);

        assertEquals(1, taskManager.getHistory().size(), "ЭпикЗадача не удалена из истории");

        taskManager.removeHistoryItem(3);

        assertEquals(0, taskManager.getHistory().size(), "ПодЗадача не удалена из истории");

    }

    @Test
    void handLinkedListAddAndRemoveTest() {
        Task task1 = new Task("tit1", "dis1", Status.NEW);
        Task task2 = new Task("tit2", "dis1", Status.NEW);
        Task task3 = new Task("tit3", "dis1", Status.NEW);
        Task task4 = new Task("tit4", "dis1", Status.NEW);
        Task task5 = new Task("tit5", "dis1", Status.NEW);
        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.createTask(task3);
        taskManager.createTask(task4);
        taskManager.createTask(task5);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.add(task4);
        historyManager.add(task5);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.add(task4);
        historyManager.add(task5);
        historyManager.add(task5);
        historyManager.add(task5);
        historyManager.add(task5);
        historyManager.add(task5);


        assertEquals(5, historyManager.getHistList().size(), "Не верное кол-во задач в истории");
        assertEquals(task1, historyManager.getHistList().getFirst(), "Первые элементы отличаются");

        historyManager.removeView(1);

        assertEquals(4, historyManager.getHistList().size(), "Первый элемент не удален");
        assertEquals(task2, historyManager.getHistList().getFirst(), "Вторые элементы отличаются");

        historyManager.removeView(5);

        assertEquals(3, historyManager.getHistList().size(), "Последний элемент не удален");
        assertEquals(task4, historyManager.getHistList().getLast(), "Последние элементы отличаются");

        historyManager.removeView(3);

        assertEquals(2, historyManager.getHistList().size(), "Серединный элемент не удален");

        assertEquals(historyManager.getHistList().getFirst(), task2, "Первая задача в списке не совпадает");
        assertEquals(historyManager.getHistList().getLast(), task4, "Последняя задача в списке не совпадает");

    }


}