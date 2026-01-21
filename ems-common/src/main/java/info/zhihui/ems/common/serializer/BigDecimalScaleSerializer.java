package info.zhihui.ems.common.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Jackson 序列化器，将 {@link BigDecimal} 输出时统一保留指定位数的小数。
 * 默认保留 2 位，可通过 {@link BigDecimalScale} 注解按字段自定义。
 */
public class BigDecimalScaleSerializer extends JsonSerializer<BigDecimal> implements ContextualSerializer {

    private static final int DEFAULT_SCALE = 2;

    private final int scale;

    public BigDecimalScaleSerializer() {
        this(DEFAULT_SCALE);
    }

    private BigDecimalScaleSerializer(int scale) {
        this.scale = scale;
    }

    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        BigDecimal scaled = value.setScale(scale, RoundingMode.DOWN);
        gen.writeNumber(scaled);
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        if (property != null) {
            BigDecimalScale annotation = property.getAnnotation(BigDecimalScale.class);
            if (annotation == null) {
                annotation = property.getContextAnnotation(BigDecimalScale.class);
            }
            if (annotation != null) {
                return new BigDecimalScaleSerializer(annotation.scale());
            }
        }
        return this;
    }
}
