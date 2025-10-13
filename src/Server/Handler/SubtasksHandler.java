package Server.Handler;


import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;

public class SubtasksHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public SubtasksHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String query = exchange.getRequestURI().getQuery();

            switch (method) {
                case "GET":
                    if (query == null) {
                        List<Subtask> subtasks = manager.getAllSubtasks();
                        sendResponse(exchange, subtasks, OK);
                    } else {
                        int id = Integer.parseInt(query.split("=")[1]);
                        Subtask subtask = manager.getSubtask(id);
                        sendResponse(exchange, subtask, OK);
                    }
                    break;
                case "POST":
                    String body = readRequest(exchange);
                    Subtask subtask = GSON.fromJson(body, Subtask.class);
                    if (subtask.getId() == 0) {
                        manager.createSubtask(subtask);
                        sendResponse(exchange, subtask, CREATED);
                    } else {
                        manager.updateSubtask(subtask);
                        sendResponse(exchange, subtask, OK);
                    }
                    break;
                case "DELETE":
                    if (query == null) {
                        manager.deleteAllSubtasks();
                        sendText(exchange, "Все подзадачи удалены", OK);
                    } else {
                        int id = Integer.parseInt(query.split("=")[1]);
                        manager.deleteSubtask(id);
                        sendText(exchange, "Подзадача " + id + " удалена", OK);
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