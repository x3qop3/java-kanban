package Server.Handler;

import com.sun.net.httpserver.HttpExchange;

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
            if ("GET".equals(exchange.getRequestMethod())) {
                List<Task> history = manager.getHistory();
                sendResponse(exchange, history, OK);
            } else {
                sendText(exchange, "Метод не поддерживается", BAD_REQUEST);
            }
        } catch (Exception e) {
            handleException(exchange, e);
        }
    }
}