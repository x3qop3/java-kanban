package Server.Handler;

import com.sun.net.httpserver.HttpExchange;
import practicum.manager.TaskManager;
import practicum.model.Subtask;

import java.io.IOException;
import java.util.List;

public class EpicSubtasksHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public EpicSubtasksHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if ("GET".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                if (query != null && query.startsWith("id=")) {
                    int epicId = Integer.parseInt(query.split("=")[1]);
                    List<Subtask> subtasks = manager.getEpicSubtasks(epicId);
                    sendResponse(exchange, subtasks, OK);
                } else {
                    sendText(exchange, "Не указан ID эпика", BAD_REQUEST);
                }
            } else {
                sendText(exchange, "Метод не поддерживается", BAD_REQUEST);
            }
        } catch (Exception e) {
            handleException(exchange, e);
        }
    }
}