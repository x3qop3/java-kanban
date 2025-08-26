package httphandlers;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import taskobject.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
            switch (method) {
                case ("GET"):
                    handleGet(exchange);
                    break;
                case ("POST"):
                    handlePost(exchange);
                    break;
                case ("DELETE"):
                    handleDelete(exchange);
                    break;
                default:
                    sendBadRequest(exchange, "Неизвестный метод");
            }
        } catch (Exception e) {
            sendError(exchange, "Внутренняя ошибка сервера: " + e.getMessage());
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");

        if (pathParts.length == 2) {

            List<Task> tasks = manager.getAllTask();
            sendResponse(exchange, tasks, 200);
        } else if (pathParts.length == 3) {
            try {
                int id = Integer.parseInt(pathParts[2]);
                Task task = manager.getTask(id);
                if (task != null) {
                    sendResponse(exchange, task, 200);
                } else {
                    sendNotFound(exchange, "Задача не найдена");
                }
            } catch (Exception e) {
                sendBadRequest(exchange, "Неверный формат");
            }
        } else {
            sendBadRequest(exchange, "Неверный запрос");
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Task task = gson.fromJson(body, Task.class);

            if (task.getTitle() == null || task.getDescription() == null) {
                sendBadRequest(exchange, "Неверный формат задачи");
                return;
            }
            if (task.getId() == 0) {
                manager.createTask(task);
                sendResponse(exchange, task, 201);
            } else {
                if (manager.getTask(task.getId()) != null) {
                    manager.updateTask(task);
                    sendResponse(exchange, task, 201);
                } else {

                    sendNotFound(exchange, "Задача для обновления не найдена");
                }
            }
        } catch (Exception e) {
            sendBadRequest(exchange, "Неверный формат");
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");

        if (pathParts.length != 3) {
            sendBadRequest(exchange, "Неверная команда");
            return;
        }

        try {
            int id = Integer.parseInt(pathParts[2]);
            Task task = manager.getTask(id);
            if (task != null) {
                manager.removeTask(id);
                sendResponse(exchange, "Задача удалена", 200);
            } else {
                sendNotFound(exchange, "Задача не найдена");
            }
        } catch (Exception e) {
            sendBadRequest(exchange, "Неверный формат");
        }

    }
}
