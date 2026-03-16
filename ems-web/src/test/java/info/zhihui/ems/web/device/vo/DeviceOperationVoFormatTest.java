package info.zhihui.ems.web.device.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceOperationVoFormatTest {

    @Test
    @DisplayName("设备操作列表时间字段应声明统一序列化格式")
    void testDeviceOperationVo_ShouldDeclareJsonFormat() throws NoSuchFieldException {
        assertJsonFormat(DeviceOperationVo.class.getDeclaredField("createTime"));
    }

    @Test
    @DisplayName("设备操作详情时间字段应声明统一序列化格式")
    void testDeviceOperationDetailVo_ShouldDeclareJsonFormat() throws NoSuchFieldException {
        assertJsonFormat(DeviceOperationDetailVo.class.getDeclaredField("successTime"));
        assertJsonFormat(DeviceOperationDetailVo.class.getDeclaredField("lastExecuteTime"));
        assertJsonFormat(DeviceOperationDetailVo.class.getDeclaredField("createTime"));
    }

    @Test
    @DisplayName("设备操作执行记录时间字段应声明统一序列化格式")
    void testDeviceOperationExecuteRecordVo_ShouldDeclareJsonFormat() throws NoSuchFieldException {
        assertJsonFormat(DeviceOperationExecuteRecordVo.class.getDeclaredField("runTime"));
    }

    private void assertJsonFormat(Field field) {
        JsonFormat jsonFormat = field.getAnnotation(JsonFormat.class);
        assertThat(jsonFormat)
                .as("%s should declare @JsonFormat", field.getName())
                .isNotNull();
        assertThat(jsonFormat.pattern()).isEqualTo("yyyy-MM-dd HH:mm:ss");
    }
}
