package httphandlers;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import taskobject.SubTask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SubTasksHandler extends BaseHttpHandler {
    TaskManager manager;

    public SubTasksHandler(TaskManager manager) {
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
            List<SubTask> subTasks = manager.getAllSubTask();
            sendResponse(exchange, subTasks, 200);
        } else if (pathParts.length == 3) {
            try {
                int id = Integer.parseInt(pathParts[2]);
                SubTask subTask = manager.getSubTask(id);
                if (subTask != null) {
                    sendResponse(exchange, subTask, 200);
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
            SubTask subTask = gson.fromJson(body, SubTask.class);
            if (subTask.getTitle() == null || subTask.getDescription() == null) {
                sendBadRequest(exchange, "Неверный формат задачи");
                return;
            }
            if (subTask.getId() == 0) {
                manager.createTask(subTask);
                sendResponse(exchange, subTask, 201);
            } else {
                if (manager.getSubTask(subTask.getId()) != null) {
                    manager.updateTask(subTask);
                    sendResponse(exchange, subTask, 200);
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
            SubTask subTask = manager.getSubTask(id);
            if (subTask != null) {
                manager.removeSub(id);
                sendResponse(exchange, "Задача удалена", 200);
            } else {
                sendNotFound(exchange, "Задача не найдена");
            }
        } catch (Exception e) {
            sendBadRequest(exchange, "Неверный формат");
        }

    }
}
