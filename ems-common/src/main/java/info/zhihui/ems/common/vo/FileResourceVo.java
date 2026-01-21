package info.zhihui.ems.common.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 文件资源VO
 */
@Data
@Accessors(chain = true)
public class FileResourceVo {

    private String base64Content;

    private String contentType;
}