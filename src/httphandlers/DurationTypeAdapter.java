package httphandlers;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;

public class DurationTypeAdapter extends TypeAdapter<Duration> {

    @Override
    public void write(JsonWriter out, Duration duration) throws IOException {
        if (duration == null) {
            out.nullValue();
        } else {                                                                                        // Сериализуем
            out.value(duration.toMinutes());
        }
    }

    @Override
    public Duration read(JsonReader in) throws IOException {
        try {                                                                               // Десериализуем
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            long minutes = in.nextLong();
            return Duration.ofMinutes(minutes);
        } catch (NumberFormatException e) {
            throw new IOException("Неверный формат Duration. Ожидается число (минуты)");
        }
    }
}