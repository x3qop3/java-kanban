package Server.Handler;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;

public class EpicsHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public EpicsHandler(TaskManager manager) {
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
                        List<Epic> epics = manager.getAllEpics();
                        sendResponse(exchange, epics, OK);
                    } else {
                        int id = Integer.parseInt(query.split("=")[1]);
                        Epic epic = manager.getEpic(id);
                        sendResponse(exchange, epic, OK);
                    }
                    break;
                case "POST":
                    String body = readRequest(exchange);
                    Epic epic = GSON.fromJson(body, Epic.class);
                    if (epic.getId() == 0) {
                        manager.createEpic(epic);
                        sendResponse(exchange, epic, CREATED);
                    } else {
                        manager.updateEpic(epic);
                        sendResponse(exchange, epic, OK);
                    }
                    break;
                case "DELETE":
                    if (query == null) {
                        manager.deleteAllEpics();
                        sendText(exchange, "Все эпики удалены", OK);
                    } else {
                        int id = Integer.parseInt(query.split("=")[1]);
                        manager.deleteEpic(id);
                        sendText(exchange, "Эпик " + id + " удален", OK);
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