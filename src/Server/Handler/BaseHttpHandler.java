package Server.Handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class BaseHttpHandler {
    protected static final Gson GSON = new Gson();
    protected static final int OK = 200;
    protected static final int CREATED = 201;
    protected static final int BAD_REQUEST = 400;
    protected static final int NOT_FOUND = 404;
    protected static final int CONFLICT = 406;
    protected static final int INTERNAL_ERROR = 500;

    protected String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream input = exchange.getRequestBody()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    protected void sendResponse(HttpExchange exchange, Object response, int statusCode) throws IOException {
        byte[] bytes = GSON.toJson(response).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    protected void sendError(HttpExchange exchange, String errorMessage, int statusCode) throws IOException {
        byte[] bytes = errorMessage.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    protected void handleCommonExceptions(HttpExchange exchange, Exception e) throws IOException {
        if (e instanceof JsonSyntaxException) {
            sendError(exchange, "Неверный формат JSON", BAD_REQUEST);
        } else if (e instanceof TaskValidationException) {
            sendError(exchange, e.getMessage(), CONFLICT);
        } else if (e instanceof ManagerSaveException) {
            sendError(exchange, "Ошибка сохранения", INTERNAL_ERROR);
        } else {
            sendError(exchange, "Внутренняя ошибка сервера", INTERNAL_ERROR);
        }
    }
}