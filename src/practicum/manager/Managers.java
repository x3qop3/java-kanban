package practicum.manager;

import com.google.gson.*;
import ru.yandex.practicum.http.HttpTaskServer;
import ru.yandex.practicum.util.LocalDateTimeAdapter;
import java.time.LocalDateTime;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HttpTaskServer getDefaultHttpServer() throws IOException {
        return new HttpTaskServer(getDefault());
    }

    public static Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }
}