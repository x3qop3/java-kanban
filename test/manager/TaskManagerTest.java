package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskobject.EpicTask;
import taskobject.Status;
import taskobject.SubTask;
import taskobject.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {
    protected T manager;

    @BeforeEach
    void setup() {
        manager = createTaskManager();
    }

    protected abstract T createTaskManager();


    //СОЗДАНИЕ РАЗНЫХ ТИПОВ ЗАДАЧ

    @Test
    void createTask() {
        Task task1 = new Task("tit1", "dis1", Status.NEW);
        Task task2 = new Task("tit2", "dis2", Status.NEW);
        manager.createTask(task1);
        manager.createTask(task2);

        assertNull(manager.getTask(99), "Получена задача по несуществующему id");

        Task savedTask1 = manager.getTask(task1.getId());
        Task savedTask2 = manager.getTask(task2.getId());

        assertEquals(task1, savedTask1, "Задачи не совпадают");
        assertEquals(task2, savedTask2, "Задачи не совпадают");

        List<Task> tasks = manager.getAllTask();

        assertNotNull(tasks, "Список задач возвращается пустым");
        assertEquals(2, tasks.size(), "Неверный размер списка");
        assertEquals(task1, tasks.getFirst(), "Задачи 1 не совпадают");
        assertEquals(task2, tasks.getLast(), "Задачи 2 не совпадают");
    }

    @Test
    void createEpicTask() {
        EpicTask epic1 = new EpicTask("epic1", "disE1");
        EpicTask epic2 = new EpicTask("epic1", "disE1");
        manager.createTask(epic1);
        manager.createTask(epic2);

        assertNull(manager.getEpicTask(99), "Получена Эпик Задача по несуществующему id");

        Task savedTask1 = manager.getEpicTask(epic1.getId());
        Task savedTask2 = manager.getEpicTask(epic2.getId());

        assertEquals(epic1, savedTask1, "Задачи не совпадают");
        assertEquals(epic2, savedTask2, "Задачи не совпадают");

        List<EpicTask> tasks = manager.getAllEpicTask();

        assertNotNull(tasks, "Список задач возвращается пустым");
        assertEquals(2, tasks.size(), "Неверный размер списка");
        assertEquals(epic1, tasks.getFirst(), "Задачи не совпадают");
    }

    @Test
    void createSubTask() {
        EpicTask epic1 = new EpicTask("epic1", "disE1");
        manager.createTask(epic1);
        SubTask sub11 = new SubTask(1, "sub11", "disS11", Status.DONE);
        SubTask sub12 = new SubTask(1, "sub12", "disS12", Status.DONE);
        manager.createTask(sub11);
        manager.createTask(sub12);

        assertNull(manager.getSubTask(99), "Получена ПодЗадача по несуществующему id");

        SubTask savedTask1 = manager.getSubTask(sub11.getId());
        SubTask savedTask2 = manager.getSubTask(sub12.getId());

        assertEquals(sub11, savedTask1, "Задачи не совпадают");
        assertEquals(sub12, savedTask2, "Задачи не совпадают");

        List<SubTask> tasks = manager.getAllSubTasksByEpicId(1);

        assertNotNull(tasks, "Список задач возвращается пустым");
        assertEquals(2, tasks.size(), "Неверный размер списка");
        assertEquals(sub11, tasks.getFirst(), "Начальная задача и задача из списка не совпадают");

    }

    @Test
    void setAndChangeEpicStatus() {
        EpicTask epic1 = new EpicTask("epic1", "disE1");
        manager.createTask(epic1);

        assertEquals(Status.NEW, epic1.getStatus(), "Статус нового эпика не NEW");

        SubTask sub11 = new SubTask(1, "sub11", "disS11", Status.NEW);
        manager.createTask(sub11);
        SubTask sub12 = new SubTask(1, "sub11", "disS11", Status.IN_PROGRESS);
        manager.createTask(sub12);
        SubTask sub13 = new SubTask(1, "sub11", "disS11", Status.DONE);
        manager.createTask(sub13);

        assertEquals(Status.IN_PROGRESS, epic1.getStatus(), "Статус эпика с разными задачами не IN_PROGRESS");

        sub11.setStatus(Status.NEW);
        sub12.setStatus(Status.NEW);
        sub13.setStatus(Status.NEW);
        manager.updateSubTask(sub11);
        manager.updateSubTask(sub12);
        manager.updateSubTask(sub13);


        assertEquals(Status.NEW, epic1.getStatus(), "Статус эпика только с новыми задачами не NEW");

        sub11.setStatus(Status.IN_PROGRESS);
        sub12.setStatus(Status.IN_PROGRESS);
        sub13.setStatus(Status.IN_PROGRESS);
        manager.updateSubTask(sub11);
        manager.updateSubTask(sub12);
        manager.updateSubTask(sub13);

        assertEquals(Status.IN_PROGRESS, epic1.getStatus(), "Статус эпика с текущими задачами не IN_PROGRESS");

        sub11.setStatus(Status.DONE);
        sub12.setStatus(Status.DONE);
        sub13.setStatus(Status.DONE);
        manager.updateSubTask(sub11);
        manager.updateSubTask(sub12);
        manager.updateSubTask(sub13);

        assertEquals(Status.DONE, epic1.getStatus(), "Статус эпика с выполненными задачами не DONE");

    }

    //ПОЛУЧЕНИЕ РАЗНЫХ ТИПОВ ЗАДАЧ

    @Test
    void createAndGetTaskFromId() {
        Task task = new Task("tit1", "dis1", Status.NEW);
        manager.createTask(task);


        EpicTask epic1 = new EpicTask("epic1", "disE1");
        manager.createTask(epic1);

        SubTask sub11 = new SubTask(2, "sub11", "disS11", Status.DONE);
        manager.createTask(sub11);

        Task savedTask = manager.getTask(1);
        EpicTask savedEpic = (EpicTask) manager.getEpicTask(2);
        SubTask savedSub = manager.getSubTask(3);

        assertEquals(task, savedTask, "Обычные задачи не совпадают");
        assertEquals(epic1, savedEpic, "ЕпикЗадачи не совпадают");
        assertEquals(sub11, savedSub, "Подзадачи не совпадают");
    }

    @Test
    void getAllSubTaskTest() {
        EpicTask epic1 = new EpicTask("epic1", "disE1");
        manager.createTask(epic1);
        SubTask sub11 = new SubTask(1, "sub11", "disS11", Status.DONE);
        SubTask sub12 = new SubTask(1, "sub12", "disS12", Status.DONE);
        manager.createTask(sub11);
        manager.createTask(sub12);

        List<SubTask> subs = manager.getAllSubTask();

        assertArrayEquals(subs.toArray(new SubTask[2]), manager.getSubMap().values().toArray(new SubTask[2]));

    }

    //УДАЛЕНИЕ РАЗНЫХ ТИПОВ ЗАДАЧ


    @Test
    void removeAllSubTest() {
        EpicTask epic1 = new EpicTask("epic1", "disE1");
        manager.createTask(epic1);
        SubTask sub11 = new SubTask(1, "sub11", "disS11", Status.DONE);
        SubTask sub12 = new SubTask(1, "sub12", "disS12", Status.DONE);
        manager.createTask(sub11);
        manager.createTask(sub12);

        assertEquals(2, manager.getSubMap().size(), "Список подзадач не наполнен");

        manager.removeAllSub();

        assertEquals(0, manager.getSubMap().size(), "Список подзадач не пуст");

    }

    @Test
    void removeAllTask() {
        Task task1 = new Task("tit1", "dis1", Status.NEW);
        Task task2 = new Task("tit2", "dis2", Status.NEW);
        manager.createTask(task1);
        manager.createTask(task2);

        manager.removeAllTask();

        assertEquals(0, manager.getTaskMap().size(), "Спискок задач не пуст");
    }

    @Test
    void removeAllEpic() {
        EpicTask epic1 = new EpicTask("epic1", "disE1");
        manager.createTask(epic1);
        SubTask sub11 = new SubTask(1, "sub11", "disS11", Status.DONE);
        SubTask sub12 = new SubTask(1, "sub12", "disS12", Status.DONE);
        manager.createTask(sub11);
        manager.createTask(sub12);
        EpicTask epic2 = new EpicTask("epic2", "disE2");
        manager.createTask(epic1);

        manager.removeAllEpic();

        assertEquals(0, manager.getEpicMap().size(), "Список эпиков не пуст");
        assertEquals(0, manager.getSubMap().size(), "Список подзадач из удаленных эпиков не пуст");

    }

    @Test
    void removeTaskFromId() {
        Task task = new Task("tit1", "dis1", Status.NEW);
        manager.createTask(task);
        EpicTask epic1 = new EpicTask("epic1", "disE1");
        manager.createTask(epic1);
        SubTask sub11 = new SubTask(2, "sub11", "disS11", Status.DONE);
        SubTask sub12 = new SubTask(2, "sub12", "disS12", Status.DONE);
        manager.createTask(sub11);
        manager.createTask(sub12);
        EpicTask epic2 = new EpicTask("epic2", "disE2");
        manager.createTask(epic2);


        manager.removeTask(1);
        assertEquals(0, manager.getTaskMap().size(), "Задача не удалена");

        manager.removeEpic(5);
        assertEquals(1, manager.getEpicMap().size(), "Пустой эпик не удален");

        manager.removeSub(3);
        assertEquals(1, manager.getSubMap().size(), "Подзадача отдельно не удалена");

        manager.removeEpic(2);
        assertEquals(0, manager.getEpicMap().size(), "Эпик с подзадачей не удален");
        assertEquals(0, manager.getSubMap().size(), "Подзадача не удалена вместе с эпиком ");

    }

    @Test
    void stayRemovedSubTaskIdInEpic() {
        EpicTask epic1 = new EpicTask("epic1", "disE1");
        manager.createTask(epic1);
        SubTask sub11 = new SubTask(1, "sub11", "disS11", Status.DONE);
        SubTask sub12 = new SubTask(1, "sub12", "disS12", Status.DONE);
        SubTask sub13 = new SubTask(1, "sub13", "disS13", Status.DONE);
        manager.createTask(sub11);
        manager.createTask(sub12);
        manager.createTask(sub13);

        assertEquals(3, epic1.getSubTasksId().size(), "Эпик хранит неверное число подзадач");

        manager.removeSub(2);

        for (int id : epic1.getSubTasksId()) {
            assertNotEquals(2, id, "В эпике остался id удаленной задачи");
        }
    }

    @Test
    void checkCrossTime() {
        Task task1 = new Task("tit1", "dis1", Status.NEW); //задачи
        Task task2 = new Task("tit2", "dis2", "13.00 10.10.25", "200", Status.NEW);

        EpicTask epic1 = new EpicTask("epic1", "disE1"); // эпики
        EpicTask epic2 = new EpicTask("epic2", "disE2");

        SubTask sub11 = new SubTask(epic1.getId(), "sub21", "disS21", Status.NEW);
        SubTask sub12 = new SubTask(epic1.getId(), "sub22", "disS22", "13.00 12.10.25", "200", Status.IN_PROGRESS);
        SubTask sub13 = new SubTask(epic1.getId(), "sub23", "disS23", "10.00 11.10.25", "300", Status.DONE);

        assertFalse(manager.isTaskCross(task1, task2));
        assertFalse(manager.isTaskCross(epic1, epic2));
        assertFalse(manager.isTaskCross(sub11, sub12));
        assertFalse(manager.isTaskCross(sub12, sub13));
    }

    @Test
    void testTimeException() {

        assertThrows(IllegalArgumentException.class, () -> {
            Task task1 = new Task("tit1", "dis1", "14.00 10.10.25", "200", Status.NEW); //задачи
            Task task2 = new Task("tit2", "dis2", "13.00 10.10.25", "200", Status.NEW);

            manager.createTask(task1);
            manager.createTask(task2);
        }, "Пересечение задач не приводит к исключению");

    }
}
