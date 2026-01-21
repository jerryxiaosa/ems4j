package info.zhihui.ems.common.utils.testdata;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalTime;

@Data
@Accessors(chain = true)
public class TestTimeClass {
    private Integer type;

    private LocalTime start;
}
