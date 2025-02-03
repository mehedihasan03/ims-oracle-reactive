package net.celloscope.mraims.loanportfolio.core.util;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import net.celloscope.mraims.loanportfolio.core.util.enums.DateTimeFormatterPattern;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BaseToString {

    @Override
    public String toString() {
        DateTimeFormatter formater = DateTimeFormatter.ofPattern(DateTimeFormatterPattern.DATE_TIME.getValue());
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> LocalDateTime.parse(json.getAsString(), formater))
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonSerializer<LocalDateTime>) (localDateTime, type, jsonSerializationContext) ->
                                new JsonPrimitive(localDateTime.format(formater)))
                .registerTypeAdapter(LocalDate.class,
                        (JsonDeserializer<LocalDate>) (json, typeOfT, context) -> LocalDate.parse(json.getAsString(),
                                DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .registerTypeAdapter(LocalDate.class,
                        (JsonSerializer<LocalDate>) (localDateTime, type, jsonSerializationContext) ->
                                new JsonPrimitive(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
                .setPrettyPrinting().create().toJson(this);
    }
}
