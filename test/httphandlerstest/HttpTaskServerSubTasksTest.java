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

public class HttpTaskServerSubTasksTest {

    // создаём экземпляр InMemoryTaskManager
    TaskManager manager = new InMemoryTaskManager();
    // передаём его в качестве аргумента в конструктор HttpHandlers.HttpTaskServer
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getHandlerGson();

    public HttpTaskServerSubTasksTest() throws IOException {
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
    public void addSubTaskTest() throws IOException, InterruptedException {
        EpicTask epic = new EpicTask("epic1", "disE1");
        SubTask sub1 = new SubTask(epic.getId(), "sub11", "disS11", Status.NEW);
        SubTask sub2 = new SubTask(epic.getId(), "sub22", "disS22", "13.00 12.10.25", "200", Status.IN_PROGRESS);


        String epicJson = gson.toJson(epic);
        String sub1TaskJson = gson.toJson(sub1);
        String sub2TaskJson = gson.toJson(sub2);

        HttpClient client = HttpClient.newHttpClient();

        URI urlSub = URI.create("http://localhost:8080/subtasks");
        URI urlEpic = URI.create("http://localhost:8080/epictasks");
        HttpRequest requestEpicPost = HttpRequest.newBuilder().uri(urlEpic).POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();
        HttpRequest requestSub1Post = HttpRequest.newBuilder().uri(urlSub).POST(HttpRequest.BodyPublishers.ofString(sub1TaskJson)).build();
        HttpRequest requestSub2Post = HttpRequest.newBuilder().uri(urlSub).POST(HttpRequest.BodyPublishers.ofString(sub2TaskJson)).build();


        client.send(requestEpicPost, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> responseSub1 = client.send(requestSub1Post, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> responseSub2 = client.send(requestSub2Post, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, responseSub1.statusCode(), "Код ответа для Подзадачи 1 не верный");
        assertEquals(201, responseSub2.statusCode(), "Код ответа для Подзадачи 2 не верный");

        List<SubTask> tasksFromManager = manager.getAllSubTask();
        assertNotNull(tasksFromManager, "Задачи не возвращаются");

    }

    @Test
    public void getSubTaskTest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        EpicTask epic = new EpicTask("epic1", "disE1");
        String epicJson = gson.toJson(epic);
        URI urlEpic = URI.create("http://localhost:8080/epictasks");
        HttpRequest requestEpicPost = HttpRequest.newBuilder().uri(urlEpic).POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();
        client.send(requestEpicPost, HttpResponse.BodyHandlers.ofString());


        SubTask sub1 = new SubTask(1, "sub11", "disS11", Status.NEW);
        SubTask sub2 = new SubTask(1, "sub22", "disS22", "13.00 12.10.25", "200", Status.IN_PROGRESS);


        String sub1TaskJson = gson.toJson(sub1);
        String sub2TaskJson = gson.toJson(sub2);

        URI urlSub = URI.create("http://localhost:8080/subtasks");

        HttpRequest requestSub1Post = HttpRequest.newBuilder().uri(urlSub).POST(HttpRequest.BodyPublishers.ofString(sub1TaskJson)).build();
        HttpRequest requestSub2Post = HttpRequest.newBuilder().uri(urlSub).POST(HttpRequest.BodyPublishers.ofString(sub2TaskJson)).build();


        client.send(requestSub1Post, HttpResponse.BodyHandlers.ofString());
        client.send(requestSub2Post, HttpResponse.BodyHandlers.ofString());


        HttpRequest requestGet = HttpRequest.newBuilder().uri(urlSub).GET().build();
        HttpResponse<String> response = client.send(requestGet, HttpResponse.BodyHandlers.ofString());


        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что создалась задача с корректным именем
        Type taskListType = new TypeToken<List<SubTask>>() {
        }.getType();
        List<SubTask> tasksFromResponse = gson.fromJson(response.body(), taskListType);

        assertNotNull(tasksFromResponse.getFirst(), "Задачи не возвращаются");
        assertEquals(sub1.getTitle(), tasksFromResponse.getFirst().getTitle(), "Название первых задач различается");
        assertEquals(sub2.getTitle(), tasksFromResponse.getLast().getTitle(), "Название последних задач различается");
        assertEquals(sub1.getDescription(), tasksFromResponse.getFirst().getDescription(), "Описание первых задач отличается");
        assertEquals(sub2.getDescription(), tasksFromResponse.getLast().getDescription(), "Описание последних задач отличается");
    }

    @Test
    public void deleteSubTaskTest() throws IOException, InterruptedException {
        //создание эпика
        HttpClient client = HttpClient.newHttpClient();
        EpicTask epic = new EpicTask("epic1", "disE1");
        String epicJson = gson.toJson(epic);
        URI urlEpic = URI.create("http://localhost:8080/epictasks");
        HttpRequest requestEpicPost = HttpRequest.newBuilder().uri(urlEpic).POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();
        client.send(requestEpicPost, HttpResponse.BodyHandlers.ofString());


        //создание подзадач
        SubTask sub1 = new SubTask(1, "sub11", "disS11", Status.NEW);
        SubTask sub2 = new SubTask(1, "sub22", "disS22", "13.00 12.10.25", "200", Status.IN_PROGRESS);


        String sub1TaskJson = gson.toJson(sub1);
        String sub2TaskJson = gson.toJson(sub2);

        URI urlSub = URI.create("http://localhost:8080/subtasks");

        //публикуем
        HttpRequest requestSub1Post = HttpRequest.newBuilder().uri(urlSub).POST(HttpRequest.BodyPublishers.ofString(sub1TaskJson)).build();
        HttpRequest requestSub2Post = HttpRequest.newBuilder().uri(urlSub).POST(HttpRequest.BodyPublishers.ofString(sub2TaskJson)).build();

        client.send(requestSub1Post, HttpResponse.BodyHandlers.ofString());
        client.send(requestSub2Post, HttpResponse.BodyHandlers.ofString());

        //удаляем задачу
        URI urlSubDel1 = URI.create("http://localhost:8080/subtasks/2");
        HttpRequest requestDelete1 = HttpRequest.newBuilder().uri(urlSubDel1).DELETE().build();
        HttpResponse<String> responseDelete = client.send(requestDelete1, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа удаления
        assertEquals(200, responseDelete.statusCode(), "Неверный код ответа");
        assertEquals("\"Задача удалена\"", responseDelete.body(), "Неверное сообщение");

        //удаляем вторую задачу
        URI urlSubDel2 = URI.create("http://localhost:8080/subtasks/3");
        HttpRequest requestDelete2 = HttpRequest.newBuilder().uri(urlSubDel2).DELETE().build();
        HttpResponse<String> responseDelete2 = client.send(requestDelete2, HttpResponse.BodyHandlers.ofString());


        // проверяем код ответа удаления
        assertEquals(200, responseDelete2.statusCode(), "Неверный код ответа");
        assertEquals("\"Задача удалена\"", responseDelete2.body(), "Неверное сообщение");


        //запрашиваем подзадачи
        HttpRequest requestGet = HttpRequest.newBuilder().uri(urlSub).GET().build();
        HttpResponse<String> response = client.send(requestGet, HttpResponse.BodyHandlers.ofString());

        // проверяем, что создалась задача с корректным именем
        Type taskListType = new TypeToken<List<SubTask>>() {
        }.getType();
        List<SubTask> tasksFromResponse = gson.fromJson(response.body(), taskListType);

        assertTrue(tasksFromResponse.isEmpty(), "Хранилище подзадач не пусто");

    }


}
