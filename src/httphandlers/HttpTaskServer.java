package httphandlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import manager.Managers;
import manager.TaskManager;
import taskobject.EpicTask;
import taskobject.Status;
import taskobject.SubTask;
import taskobject.Task;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private TaskManager manager;
    private HttpServer server;
    private static final int PORT = 8080;

    public HttpTaskServer(TaskManager manager) throws IOException {
        this.manager = manager;
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks", new TasksHandler(manager));
        server.createContext("/subtasks", new SubTasksHandler(manager));
        server.createContext("/epictasks", new EpicTasksHandler(manager));
        server.createContext("/history", new HistoryHandler(manager));
        server.createContext("/prioritized", new PrioritizedHandler(manager));
    }

    public static Gson getHandlerGson() {
        return BaseHttpHandler.getGson();
    }

    public void start() {
        server.start();
        System.out.println("Сервер запустился на порту 8080");
    }

    public void stop() {
        server.stop(1);
        System.out.println("Сервер остановлен");
    }

    public static void main(String[] args) throws IOException {
        TaskManager manager = Managers.getDefault();


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        Task task1 = new Task("tit1", "dis1", Status.NEW); //задачи
        Task task2 = new Task("tit2", "dis2", "13.00 10.10.25", "200", Status.NEW);
        manager.createTask(task1);
        manager.createTask(task2);
        EpicTask epic1 = new EpicTask("epic1", "disE1"); // эпики
        EpicTask epic2 = new EpicTask("epic2", "disE2");
        manager.createTask(epic1);
        manager.createTask(epic2);

        SubTask sub11 = new SubTask(epic1.getId(), "sub21", "disS21", Status.NEW);
        SubTask sub12 = new SubTask(epic1.getId(), "sub22", "disS22", "13.00 12.10.25", "200", Status.IN_PROGRESS);
        SubTask sub13 = new SubTask(epic1.getId(), "sub23", "disS23", "10.00 11.10.25", "300", Status.DONE);
        manager.createTask(sub11);
        manager.createTask(sub12);
        manager.createTask(sub13);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        HttpTaskServer server = new HttpTaskServer(manager);
        server.start();


    }


}


        /*
        //TaskManager manager = Managers.getFileBacked("testFile");

        TaskManager manager = Managers.getDefault();


        //ПОЛЬЗОВАТЕЛЬСКИЙ СЦЕНАРИЙ (Дополнительное задание)

        Task task1 = new Task("tit1", "dis1", Status.NEW); //задачи
        Task task2 = new Task("tit2", "dis2", "13.00 10.10.25", "200", Status.NEW);
        manager.createTask(task1);
        manager.createTask(task2);
        EpicTask epic1 = new EpicTask("epic1", "disE1"); // эпики
        EpicTask epic2 = new EpicTask("epic2", "disE2");
        manager.createTask(epic1);
        manager.createTask(epic2);

        SubTask sub11 = new SubTask(epic1.getId(), "sub21", "disS21", Status.NEW);
        SubTask sub12 = new SubTask(epic1.getId(), "sub22", "disS22", "13.00 12.10.25", "200", Status.IN_PROGRESS);
        SubTask sub13 = new SubTask(epic1.getId(), "sub23", "disS23", "10.00 11.10.25", "300", Status.DONE);




        System.out.println(manager.getAllTaskAllType());


        System.out.println(manager.getAllTaskAllType() + "\nit is all\n");

        System.out.println(manager.getPrioritizedTasks() + "\nit is all\n");

        //TaskManager newManager = Managers.getFileBacked("testFile");

        //System.out.println(newManager.getAllTaskAllType() + "\nit is all\n");

        // ПРОВЕРКА ИСТОРИИ

        manager.getTask(1);
        manager.getTask(2);
        manager.getEpicTask(3);
        manager.getEpicTask(4);
        manager.getSubTask(5);
        manager.getSubTask(6);
        manager.getSubTask(7);

        System.out.println(manager.getHistory() + "\nit is all\n");

        manager.getSubTask(7);
        manager.getSubTask(5);
        manager.getEpicTask(3);
        manager.getTask(1);
        manager.getTask(2);
        manager.getEpicTask(4);
        manager.getSubTask(6);
        manager.getTask(2);
        manager.getTask(2);
        manager.getTask(2);

        System.out.println(manager.getHistory() + "\nit is all\n");

        manager.removeTask(2);
        manager.removeEpic(4);
        manager.removeSub(6);

        System.out.println(manager.getHistory() + "\nthat is all\n");

        manager.removeEpic(3);

        System.out.println(manager.getHistory() + "\nthat is all\n");

        System.out.println(manager.getAllTaskAllType() + "\nthat is all\n");*/
