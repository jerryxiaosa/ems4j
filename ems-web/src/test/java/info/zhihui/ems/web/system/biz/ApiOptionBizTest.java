package info.zhihui.ems.web.system.biz;

import cn.dev33.satoken.annotation.SaCheckPermission;
import info.zhihui.ems.web.system.vo.ApiOptionVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@ExtendWith(MockitoExtension.class)
class ApiOptionBizTest {

    @InjectMocks
    private ApiOptionBiz apiOptionBiz;

    @Mock
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Test
    @DisplayName("查询接口选项_应只返回带权限注解且权限值非空的接口")
    void testFindApiOptionList_ShouldOnlyReturnProtectedMappings() throws NoSuchMethodException {
        SampleController controller = new SampleController();
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = new LinkedHashMap<>();
        handlerMethods.put(buildRequestMappingInfo("/v1/users/page", GET),
                new HandlerMethod(controller, SampleController.class.getMethod("page")));
        handlerMethods.put(buildRequestMappingInfo("/v1/users", GET),
                new HandlerMethod(controller, SampleController.class.getMethod("list")));
        handlerMethods.put(buildRequestMappingInfo("/v1/users/login", POST),
                new HandlerMethod(controller, SampleController.class.getMethod("login")));
        handlerMethods.put(buildRequestMappingInfo("/v1/users/blank", GET),
                new HandlerMethod(controller, SampleController.class.getMethod("blankPermission")));
        when(requestMappingHandlerMapping.getHandlerMethods()).thenReturn(handlerMethods);

        List<ApiOptionVo> result = apiOptionBiz.findApiOptionList();

        assertThat(result).containsExactly(
                new ApiOptionVo()
                        .setKey("GET:/v1/users")
                        .setPermissionCode("users:users:list"),
                new ApiOptionVo()
                        .setKey("GET:/v1/users/page")
                        .setPermissionCode("users:users:page")
        );
    }

    @Test
    @DisplayName("查询接口选项_类级权限注解在当前实现下应被过滤")
    void testFindApiOptionList_WithClassLevelPermission_ShouldBeFilteredByCurrentImplementation() throws NoSuchMethodException {
        ClassLevelPermissionController controller = new ClassLevelPermissionController();
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = new LinkedHashMap<>();
        handlerMethods.put(buildRequestMappingInfo("/v1/class-level", GET),
                new HandlerMethod(controller, ClassLevelPermissionController.class.getMethod("detail")));
        when(requestMappingHandlerMapping.getHandlerMethods()).thenReturn(handlerMethods);

        List<ApiOptionVo> result = apiOptionBiz.findApiOptionList();

        assertThat(result).isEmpty();
    }

    private RequestMappingInfo buildRequestMappingInfo(String path, org.springframework.web.bind.annotation.RequestMethod method) {
        return RequestMappingInfo.paths(path)
                .methods(method)
                .build();
    }

    @SuppressWarnings("unused")
    private static class SampleController {

        @GetMapping("/page")
        @SaCheckPermission("users:users:page")
        public void page() {
        }

        @GetMapping
        @SaCheckPermission("users:users:list")
        public void list() {
        }

        @PostMapping("/login")
        public void login() {
        }

        @GetMapping("/blank")
        @SaCheckPermission("")
        public void blankPermission() {
        }
    }

    @SuppressWarnings("unused")
    @SaCheckPermission("users:users:detail")
    private static class ClassLevelPermissionController {

        @GetMapping("/detail")
        public void detail() {
        }
    }
}
