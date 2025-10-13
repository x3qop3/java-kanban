package Server.Handler;

import com.sun.net.httpserver.HttpExchange;
import practicum.manager.TaskManager;
import practicum.model.Task;


import java.io.IOException;
import java.util.List;

public class TasksHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public TasksHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String query = exchange.getRequestURI().getQuery();

            switch (method) {
                case "GET":
                    if (query == null) {
                        List<Task> tasks = manager.getAllTasks();
                        sendResponse(exchange, tasks, OK);
                    } else {
                        int id = Integer.parseInt(query.split("=")[1]);
                        Task task = manager.getTask(id);
                        sendResponse(exchange, task, OK);
                    }
                    break;
                case "POST":
                    String body = readRequest(exchange);
                    Task task = GSON.fromJson(body, Task.class);
                    if (task.getId() == 0) {
                        manager.createTask(task);
                        sendResponse(exchange, task, CREATED);
                    } else {
                        manager.updateTask(task);
                        sendResponse(exchange, task, OK);
                    }
                    break;
                case "DELETE":
                    if (query == null) {
                        manager.deleteAllTasks();
                        sendText(exchange, "Все задачи удалены", OK);
                    } else {
                        int id = Integer.parseInt(query.split("=")[1]);
                        manager.deleteTask(id);
                        sendText(exchange, "Задача " + id + " удалена", OK);
                    }
                    break;
                default:
                    sendText(exchange, "Метод не поддерживается", BAD_REQUEST);
            }
        } catch (Exception e) {
            handleException(exchange, e);
        }
    }
}