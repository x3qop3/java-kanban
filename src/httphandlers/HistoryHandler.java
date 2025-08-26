package httphandlers;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import taskobject.Task;

import java.io.IOException;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public HistoryHandler(TaskManager manager) {
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
        try {
            List<Task> history = manager.getHistory();
            if (!history.isEmpty()) {
                sendResponse(exchange, history, 200);
            } else {
                sendNotFound(exchange, "История пуста");
            }
        } catch (Exception e) {
            sendBadRequest(exchange, "Ошибка запроса: " + e.getMessage());
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");

        if (pathParts.length == 2) {
            manager.clearHistory();
            sendResponse(exchange, "История очищена", 200);
        } else if (pathParts.length == 3) {
            try {
                int id = Integer.parseInt(pathParts[2]);
                manager.removeHistoryItem(id);
                sendResponse(exchange, "Просмотр задачи удален", 200);
            } catch (Exception e) {
                sendBadRequest(exchange, "Неверный формат");
            }
        }

    }
}



