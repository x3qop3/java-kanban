package Server;

import Server.Handler.*;
import com.sun.net.httpserver.HttpServer;
import practicum.manager.TaskManager;
import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;
    private final TaskManager taskManager;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);

        configureContexts();
    }

    private void configureContexts() {
        server.createContext("/tasks/task", new TasksHandler(taskManager));
        server.createContext("/tasks/subtask", new SubtasksHandler(taskManager));
        server.createContext("/tasks/epic", new EpicsHandler(taskManager));
        server.createContext("/tasks/subtask/epic", new EpicSubtasksHandler(taskManager));
        server.createContext("/tasks/history", new HistoryHandler(taskManager));
        server.createContext("/tasks", new PrioritizedHandler(taskManager));
    }

    public void start() {
        System.out.println("Сервер запущен на порту " + PORT);
        System.out.println("http://localhost:" + PORT + "/tasks");
        server.start();
    }

    public void stop() {
        server.stop(0);
        System.out.println("Сервер остановлен на порту " + PORT);
    }
}