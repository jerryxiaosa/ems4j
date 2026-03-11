package info.zhihui.ems.web.system.biz;

import cn.dev33.satoken.annotation.SaCheckPermission;
import info.zhihui.ems.web.system.vo.ApiOptionVo;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 系统接口选项业务编排
 */
@Service
@RequiredArgsConstructor
public class ApiOptionBiz {

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    /**
     * 查询系统接口选项列表
     */
    public List<ApiOptionVo> findApiOptionList() {
        Map<RequestMappingInfo, HandlerMethod> handlerMethodMap = requestMappingHandlerMapping.getHandlerMethods();
        if (CollectionUtils.isEmpty(handlerMethodMap)) {
            return Collections.emptyList();
        }

        List<ApiOptionVo> apiOptions = new ArrayList<>();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethodMap.entrySet()) {
            apiOptions.addAll(buildApiOptions(entry.getKey(), entry.getValue()));
        }
        apiOptions.sort(Comparator.comparing(ApiOptionVo::getKey)
                .thenComparing(ApiOptionVo::getPermissionCode));
        return apiOptions;
    }

    private List<ApiOptionVo> buildApiOptions(RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod) {
        List<String> permissionCodeList = resolvePermissionCodeList(handlerMethod);
        if (CollectionUtils.isEmpty(permissionCodeList)) {
            return Collections.emptyList();
        }

        Set<String> pathSet = requestMappingInfo.getPatternValues();
        Set<RequestMethod> methodSet = requestMappingInfo.getMethodsCondition().getMethods();
        if (CollectionUtils.isEmpty(pathSet) || CollectionUtils.isEmpty(methodSet)) {
            return Collections.emptyList();
        }

        List<ApiOptionVo> apiOptions = new ArrayList<>();
        for (RequestMethod requestMethod : methodSet) {
            for (String path : pathSet) {
                for (String permissionCode : permissionCodeList) {
                    apiOptions.add(new ApiOptionVo()
                            .setKey(requestMethod.name() + ":" + path)
                            .setPermissionCode(permissionCode));
                }
            }
        }
        return apiOptions;
    }

    private List<String> resolvePermissionCodeList(HandlerMethod handlerMethod) {
        SaCheckPermission methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), SaCheckPermission.class);
        if (methodAnnotation == null) {
            return Collections.emptyList();
        }

        List<String> permissionCodeList = new ArrayList<>();
        for (String permissionCode : methodAnnotation.value()) {
            if (StringUtils.hasText(permissionCode)) {
                permissionCodeList.add(permissionCode);
            }
        }
        return permissionCodeList;
    }
}
