package info.zhihui.ems.common.utils.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;

/**
 * MonthDay 序列化为 MM-dd 格式。
 */
public class MonthDayDashFormatSerializer extends JsonSerializer<MonthDay> {

    private static final DateTimeFormatter MONTH_DAY_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    @Override
    public void serialize(MonthDay value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        gen.writeString(value.format(MONTH_DAY_FORMATTER));
    }
}
