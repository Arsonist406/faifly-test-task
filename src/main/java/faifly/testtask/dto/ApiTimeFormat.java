package faifly.testtask.dto;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class ApiTimeFormat {

    public static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(PATTERN);

    private ApiTimeFormat() {
    }

    public static String parse(Instant instant, ZoneId zoneId) {
        return FORMATTER.format(instant.atZone(zoneId));
    }
}
