package httphandlers;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import taskobject.Task;

import java.io.IOException;
import java.util.Set;

public class PrioritizedHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public PrioritizedHandler(TaskManager manager) {
        this.manager = manager;
    }


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            if (method.equals("GET")) {
                handleGet(exchange);
            } else {
                sendBadRequest(exchange, "Неизвестный метод");
            }
        } catch (Exception e) {
            sendError(exchange, "Внутренняя ошибка сервера: " + e.getMessage());
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        try {
            Set<Task> priorityList = manager.getPrioritizedTasks();
            if (!priorityList.isEmpty()) {
                sendResponse(exchange, priorityList, 200);
            } else {
                sendNotFound(exchange, "Список по приоритету пуст");
            }
        } catch (Exception e) {
            sendBadRequest(exchange, "Ошибка запроса: " + e.getMessage());
        }
    }

}
