package httphandlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public abstract class BaseHttpHandler implements HttpHandler {
    protected static Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
            .serializeNulls()
            .create();

    public static Gson getGson() {
        return gson;
    }

    protected void sendResponse(HttpExchange exchange, Object object, int statusCode) throws IOException {
        String response = gson.toJson(object);
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    protected void sendNotFound(HttpExchange exchange, String error) throws IOException {
        sendResponse(exchange, error, 404);
        exchange.close();
    }

    protected void sendBadRequest(HttpExchange exchange, String message) throws IOException {
        sendResponse(exchange, message, 400);
        exchange.close();
    }

    protected void sendHasInteractions(HttpExchange exchange, String message) throws IOException {
        sendResponse(exchange, message, 406);
        exchange.close();
    }

    protected void sendError(HttpExchange exchange, String message) throws IOException {
        sendResponse(exchange, message, 500);
        exchange.close();
    }


}
