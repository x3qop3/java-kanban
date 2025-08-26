package httphandlerstest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import httphandlers.HttpTaskServer;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskobject.Status;
import taskobject.Task;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerTasksTest {

    // создаём экземпляр InMemoryTaskManager
    TaskManager manager = new InMemoryTaskManager();
    // передаём его в качестве аргумента в конструктор HttpHandlers.HttpTaskServer
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getHandlerGson();

    public HttpTaskServerTasksTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        manager.removeAllTasksAllType();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void addTaskTest() throws IOException, InterruptedException {
        // создаём задачу
        Task task = new Task("Test 2", "Testing task 2", LocalDateTime.now().format(Task.form), "200",
                Status.NEW);
        // конвертируем её в JSON
        String taskJson = gson.toJson(task);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создалась одна задача с корректным именем
        List<Task> tasksFromManager = manager.getAllTask();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", tasksFromManager.getFirst().getTitle(), "Некорректное имя задачи");
    }

    @Test
    public void getTaskTest() throws IOException, InterruptedException {
        // создаём задачу
        Task task = new Task("Test 2", "Testing task 2", LocalDateTime.now().format(Task.form), "200",
                Status.NEW);
        // конвертируем её в JSON
        String taskJson = gson.toJson(task);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest requestPost = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(requestPost, HttpResponse.BodyHandlers.ofString());

        HttpRequest requestGet = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(requestGet, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что создалась одна задача с корректным именем
        Type taskListType = new TypeToken<List<Task>>() {
        }.getType();
        List<Task> tasksFromResponse = gson.fromJson(response.body(), taskListType);

        assertNotNull(tasksFromResponse.getFirst(), "Задачи не возвращаются");
        assertEquals(task, tasksFromResponse.getFirst(), "Задачи отличаются");
    }

    @Test
    public void deleteTaskTest() throws IOException, InterruptedException {
        Task task = new Task("Test 2", "Testing task 2", LocalDateTime.now().format(Task.form), "200",
                Status.NEW);
        // конвертируем её в JSON
        String taskJson = gson.toJson(task);

        // публикуем задачу
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest requestPost = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(requestPost, HttpResponse.BodyHandlers.ofString());

        //удаляем задачу
        URI url1 = URI.create("http://localhost:8080/tasks/1");
        HttpRequest requestDelete = HttpRequest.newBuilder().uri(url1).DELETE().build();
        HttpResponse<String> responseDelete = client.send(requestDelete, HttpResponse.BodyHandlers.ofString());

        //запрашиваем задачу
        HttpRequest requestGet = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа удаления
        assertEquals(200, responseDelete.statusCode());
        assertEquals("\"Задача удалена\"", responseDelete.body(), "Неверное сообщение");

        //проверяем хранилище на пустоту
        Type taskListType = new TypeToken<List<Task>>() {
        }.getType();
        List<Task> tasksFromResponse = gson.fromJson(responseGet.body(), taskListType);

        assertTrue(tasksFromResponse.isEmpty(), "Хранилище не пустое");

    }


}
