package httphandlerstest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import httphandlers.HttpTaskServer;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskobject.EpicTask;
import taskobject.Status;
import taskobject.SubTask;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerEpicTasksTest {

    // создаём экземпляр InMemoryTaskManager
    TaskManager manager = new InMemoryTaskManager();
    // передаём его в качестве аргумента в конструктор HttpHandlers.HttpTaskServer
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getHandlerGson();

    public HttpTaskServerEpicTasksTest() throws IOException {
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
    public void addEpicTaskTest() throws IOException, InterruptedException {
        EpicTask epic = new EpicTask("epic1", "disE1");
        SubTask sub1 = new SubTask(epic.getId(), "sub11", "disS11", Status.NEW);
        SubTask sub2 = new SubTask(epic.getId(), "sub22", "disS22", "13.00 12.10.25", "200", Status.IN_PROGRESS);

        String epicTaskJson = gson.toJson(epic);


        HttpClient client = HttpClient.newHttpClient();
        URI urlEpic = URI.create("http://localhost:8080/epictasks");
        HttpRequest requestEpic = HttpRequest.newBuilder().uri(urlEpic).POST(HttpRequest.BodyPublishers.ofString(epicTaskJson)).build();


        HttpResponse<String> responseEpic = client.send(requestEpic, HttpResponse.BodyHandlers.ofString());


        assertEquals(201, responseEpic.statusCode(), "Код ответа для эпика не верный");


        List<EpicTask> tasksFromManager = manager.getAllEpicTask();
        assertNotNull(tasksFromManager, "Задачи не возвращаются");

    }

    @Test
    public void getEpicTaskTest() throws IOException, InterruptedException {
        // создаём задачу
        EpicTask epic = new EpicTask("epic1", "disE1");
        // конвертируем её в JSON
        String epicJson = gson.toJson(epic);

        // создаём HTTP-клиент и POST запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epictasks");
        HttpRequest requestPost = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();
        client.send(requestPost, HttpResponse.BodyHandlers.ofString());

        HttpRequest requestGet = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(requestGet, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что создалась одна задача с корректным именем
        Type taskListType = new TypeToken<List<EpicTask>>() {
        }.getType();
        List<EpicTask> tasksFromResponse = gson.fromJson(response.body(), taskListType);

        assertNotNull(tasksFromResponse.getFirst(), "Задачи не возвращаются");
        assertEquals(epic.getTitle(), tasksFromResponse.getFirst().getTitle(), "Название задач различается");
        assertEquals(epic.getDescription(), tasksFromResponse.getFirst().getDescription(), "Описание задач отличается");
    }

    @Test
    public void DeleteEpicTaskTest() throws IOException, InterruptedException {
        // создаём задачу
        EpicTask epic = new EpicTask("epic1", "disE1");
        // конвертируем её в JSON
        String epicJson = gson.toJson(epic);

        // создаём HTTP-клиент и POST запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epictasks");
        HttpRequest requestPost = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();
        client.send(requestPost, HttpResponse.BodyHandlers.ofString());

        //удаляем задачу
        URI url1 = URI.create("http://localhost:8080/epictasks/1");
        HttpRequest requestDelete = HttpRequest.newBuilder().uri(url1).DELETE().build();
        HttpResponse<String> responseDelete = client.send(requestDelete, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, responseDelete.statusCode(), responseDelete.body());
        assertEquals("\"Задача удалена\"", responseDelete.body(), "Неверное сообщение");

        //запрашиваем задачу
        HttpRequest requestGet = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());

        //проверяем хранилище на пустоту
        Type taskListType = new TypeToken<List<EpicTask>>() {
        }.getType();
        List<EpicTask> tasksFromResponse = gson.fromJson(responseGet.body(), taskListType);

        assertTrue(tasksFromResponse.isEmpty(), "Хранилище не пустое");

    }

}
