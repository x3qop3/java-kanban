package manager;

import exceptions.ManagerSaveException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskobject.EpicTask;
import taskobject.Status;
import taskobject.SubTask;
import taskobject.Task;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    File temp;

    @Override
    protected FileBackedTaskManager createTaskManager() {
        return new FileBackedTaskManager("testFile");
    }

    @BeforeEach
    public void createFile() {
        try {
            temp = File.createTempFile("testFile", ".txt");
            manager = new FileBackedTaskManager(temp.getPath());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @AfterEach
    public void deleteFile() {
        temp.delete();
    }

    @Test
    void saveAndLoadTaskAllType() throws IOException {
        Task task1 = new Task("tit1", "dis1", Status.NEW);
        manager.createTask(task1);
        EpicTask epic1 = new EpicTask("epic1", "disE1");
        manager.createTask(epic1);
        SubTask sub11 = new SubTask(epic1.getId(), "sub21", "disS21", Status.NEW);
        manager.createTask(sub11);

        manager = FileBackedTaskManager.loadFromFile(temp);

        assertEquals(task1, manager.getTask(1), "Записанная и прочитанная задачи не совпадают");
        assertEquals(epic1, manager.getEpicTask(2), "Записанный и прочитанный Эпик не совпадают");
        assertEquals(sub11, manager.getSubTask(3), "Записанная и прочитанная Подзадачи не совпадают");
    }

    @Test
    void saveAndLoadEmptyFile() {
        Task task1 = new Task("tit1", "dis1", Status.NEW);
        manager.createTask(task1);
        manager.removeTask(1);

        manager = FileBackedTaskManager.loadFromFile(temp);
        assertTrue(manager.getAllTaskAllType().isEmpty());

    }

    @Test
    void testLoadException() {
        File file = new File("");
        assertThrows(ManagerSaveException.class, () -> {
            manager = FileBackedTaskManager.loadFromFile(file);
        }, "Ошибка чтения из файла не привела к исключению");
    }

    @Test
    void testSaveException() {
        FileBackedTaskManager badManager = new FileBackedTaskManager("");
        assertThrows(ManagerSaveException.class, () -> {
            badManager.save();
        }, "Запись в несуществующий файл не привела к исключению");
    }

    @Test
    void testConstructorException() {
        assertThrows(RuntimeException.class, () -> {
            TaskManager badManager = new FileBackedTaskManager("\0");
        }, "Запись в несуществующий файл не привела к исключению");
    }
}