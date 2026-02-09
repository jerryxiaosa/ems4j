package info.zhihui.ems.common.utils.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * MonthDay 反序列化，默认读取 MM-dd，并兼容历史 --MM-dd。
 */
public class MonthDayDashFormatDeserializer extends JsonDeserializer<MonthDay> {

    private static final DateTimeFormatter MONTH_DAY_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    @Override
    public MonthDay deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        if (value == null) {
            return null;
        }
        String normalizedValue = normalize(value);
        if (normalizedValue.isEmpty()) {
            return null;
        }
        try {
            return MonthDay.parse(normalizedValue, MONTH_DAY_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new IOException("MonthDay格式错误，应为MM-dd", ex);
        }
    }

    private String normalize(String value) {
        String trimmedValue = value.trim();
        if (trimmedValue.startsWith("--")) {
            return trimmedValue.substring(2);
        }
        return trimmedValue;
    }
}
