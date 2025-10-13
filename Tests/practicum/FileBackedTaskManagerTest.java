package practicum;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import practicum.manager.FileBackedTaskManager;
import practicum.model.Epic;
import practicum.model.Subtask;
import practicum.model.Task;
import practicum.statandtype.Status;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;



class FileBackedTaskManagerTest {
    private FileBackedTaskManager taskManager;
    private File tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("tasks", ".csv");
        taskManager = new FileBackedTaskManager(tempFile);
    }

    @AfterEach
    void tearDown() {
        tempFile.delete();
    }

    private String readFileContent(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    @Test
    void shouldSaveAndLoadEmptyManager() throws IOException {
        taskManager.save();
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loadedManager.getAllTasks().isEmpty());
        assertTrue(loadedManager.getAllEpics().isEmpty());
        assertTrue(loadedManager.getAllSubtasks().isEmpty());
    }

    @Test
    void shouldSaveTaskToFile() throws IOException {
        File testFile = File.createTempFile("tasks", ".csv");
        try {
            FileBackedTaskManager manager = new FileBackedTaskManager(testFile);
            Task task = new Task("Test title", "Test description", Status.NEW);
            manager.createTask(task);
            String fileContent = readFileContent(testFile);
            assertTrue(fileContent.contains("Test title"));
            assertTrue(fileContent.contains("NEW"));
        } finally {
            testFile.delete();
        }
    }

    @Test
    void shouldSaveEpicToFile() throws IOException {
        File testFile = File.createTempFile("tasks", ".csv");
        try {
            FileBackedTaskManager manager = new FileBackedTaskManager(testFile);
            Epic epic = new Epic("Test Epic", "Test description EPIC");
            manager.createEpic(epic);
            String fileContent = readFileContent(testFile);
            assertTrue(fileContent.contains("Test Epic"));
            assertTrue(fileContent.contains("EPIC"));
        } finally {
            testFile.delete();
        }
    }
}