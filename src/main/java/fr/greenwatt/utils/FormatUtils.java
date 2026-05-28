package fr.greenwatt.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class FormatUtils {

    public static final DateTimeFormatter FR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private FormatUtils() {}

    public static String formaterDate(LocalDateTime d) {
        return d == null ? "" : d.format(FR_DATE);
    }

    public static String formaterKwh(double v) {
        return String.format("%,.0f kWh", v);
    }

    public static String formaterEuros(double v) {
        return String.format("%,.2f €", v);
    }
}
