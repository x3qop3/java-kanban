package httphandlers;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import taskobject.EpicTask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EpicTasksHandler extends BaseHttpHandler {
    TaskManager manager;

    public EpicTasksHandler(TaskManager manager) {
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
            List<EpicTask> epicTasks = manager.getAllEpicTask();
            sendResponse(exchange, epicTasks, 200);
        } else if (pathParts.length == 3) {
            try {
                int id = Integer.parseInt(pathParts[2]);
                EpicTask epicTask = manager.getEpicTask(id);
                if (epicTask != null) {
                    sendResponse(exchange, epicTask, 200);
                } else {
                    sendNotFound(exchange, "Задача не найдена");
                }
            } catch (Exception e) {
                sendBadRequest(exchange, "Неверный формат у запрошенной задачи");
            }
        } else {
            sendBadRequest(exchange, "Неверный запрос");
        }


    }

    private void handlePost(HttpExchange exchange) throws IOException {

        try {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            EpicTask epicTask = gson.fromJson(body, EpicTask.class);

            if (epicTask.getTitle() == null || epicTask.getDescription() == null) {
                sendBadRequest(exchange, "У задачи нет описания");
                return;
            }
            if (epicTask.getId() == 0) {
                manager.createTask(epicTask);
                sendResponse(exchange, epicTask, 201);
            } else {
                if (manager.getEpicTask(epicTask.getId()) != null) {
                    manager.updateTask(epicTask);
                    sendResponse(exchange, epicTask, 200);
                } else {
                    sendNotFound(exchange, "Задача для обновления не найдена");
                }
            }
        } catch (Exception e) {
            sendBadRequest(exchange, "Неверный формат у создаваемой задачи: " + e.getMessage());
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
            EpicTask epicTask = manager.getEpicTask(id);
            if (epicTask != null) {
                manager.removeEpic(id);
                sendResponse(exchange, "Задача удалена", 200);
            } else {
                sendNotFound(exchange, "Задача не найдена");
            }
        } catch (Exception e) {
            sendBadRequest(exchange, "Неверный формат у удаляемой задачи");
        }
    }
}
