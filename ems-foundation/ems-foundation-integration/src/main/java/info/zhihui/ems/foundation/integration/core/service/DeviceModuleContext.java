package info.zhihui.ems.foundation.integration.core.service;

import com.google.common.collect.ImmutableMap;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.foundation.integration.core.bo.DeviceModuleConfigBo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模块服务上下文，用于获取对应模块的服务实现
 *
 * @author jerryxiaosa
 */

@Service
@Slf4j
public class DeviceModuleContext {

    // 根据模块分组
    private final Map<Class<?>, List<CommonDeviceModule>> deviceServiceGroupByModuleType = new HashMap<>();
    // 每个模块只有一个mock
    private final Map<Class<?>, CommonDeviceModule> mockDeviceService = new HashMap<>();

    private final DeviceModuleConfigService deviceModuleConfigService;

    @Value("${useRealDevice:true}")
    public Boolean useRealDevice;

    private static final String MOCK_PREFIX = "mock";


    /**
     *  这里会获取module目录下不同模块服务的实现，比如CarService，CameraService
     *  <br/>
     *  他们都extends CommonDeviceModuleService
     *  所以这里的实现类，实际上都实现了CommonDeviceModuleService的子类
     *  <pre>
     *  deviceServiceGroupByModuleType 行如：
     *  {
     *       CarService    => [CarImpl1, CarImpl2 ...],
     *       CameraService => [CameraImpl1, CameraImpl2 ...]
     *  }
     *  </pre>
     *
     * @param commonDeviceModuleServiceMap 模块通用信息处理接口
     * @param deviceModuleConfigService 模块配置信息接口
     */
    @Autowired
    public DeviceModuleContext(Map<String, CommonDeviceModule> commonDeviceModuleServiceMap, DeviceModuleConfigService deviceModuleConfigService) {
        commonDeviceModuleServiceMap.forEach((s, commonDeviceModuleService) -> {
            Class<?> deviceModule = getOnlyInterface(ClassUtils.getUserClass(commonDeviceModuleService.getClass()));

            if (isMockDeviceService(s)) {
                mockDeviceService.put(deviceModule, commonDeviceModuleService);
            }

            if (deviceServiceGroupByModuleType.containsKey(deviceModule)) {
                List<CommonDeviceModule> exist = deviceServiceGroupByModuleType.get(deviceModule);
                exist.add(commonDeviceModuleService);
            } else {
                List<CommonDeviceModule> notExist = new ArrayList<>();
                notExist.add(commonDeviceModuleService);
                deviceServiceGroupByModuleType.put(deviceModule, notExist);
            }
        });

        this.deviceModuleConfigService = deviceModuleConfigService;
    }

    /**
     * 获取某个区域下，某个模块的服务实现
     * <br/>
     * 例如：A和B两个园区，分别对接了海康和大华的视频服务，这里可以方便的获取对应园区的视频服务实现类
     *
     * @param moduleType 模块类型
     * @param areaId 区域id
     * @return T
     * @param <T> 模块具体实现
     */
    public <T extends CommonDeviceModule> T getService(Class<T> moduleType, Integer areaId) {
        T service;
        if (useRealDevice) {
            service = getRealService(moduleType, areaId);
        } else {
            service = getMockService(moduleType);
        }

        return service;
    }

    /**
     * 获取当前系统支持的全部模块名称，以及每个模块支持的服务名称
     * <br/>
     * 可以在配置模块时，提供选择
     *
     * <pre>
     *     {
     *      模块名1:[实现名1，实现名2]
     *      模块名2:[实现名21，实现名22]
     *     }
     * </pre>
     *
     * @return 模块名称为key，实现名称集合为value
     */
    public Map<String, List<String>> getModuleAndServiceName() {
        Map<String, List<String>> res = new HashMap<>();

        deviceServiceGroupByModuleType.forEach((deviceInterface, serviceList) -> {
            res.put(deviceInterface.getSimpleName(), serviceList.stream().map(s -> s.getClass().getSimpleName()).map(StringUtils::toRootLowerCase).collect(Collectors.toList()));
        });

        return ImmutableMap.copyOf(res);
    }

    // MockXXXService以Mock开头
    private Boolean isMockDeviceService(String serviceName) {
        return serviceName.toLowerCase().indexOf(MOCK_PREFIX) == 0;
    }

    // 实现类需要实现module目录下的接口
    private Class<?> getOnlyInterface(Class<?> service) {
        Class<?>[] interfaces = service.getInterfaces();

        // 先看本类的接口，否则再看是否继承了父类
        if (interfaces.length == 1) {
            return interfaces[0];
        } else if (interfaces.length == 0 && service.getSuperclass() != null) {
            interfaces = service.getSuperclass().getInterfaces();
            if (interfaces.length == 1) {
                return interfaces[0];
            }
        }

        throw new RuntimeException("未按照约定编码: " + service.getSimpleName());
    }

    private <T extends CommonDeviceModule> T getRealService(Class<T> moduleType, Integer areaId) {
        List<CommonDeviceModule> deviceServiceImplList = deviceServiceGroupByModuleType.get(moduleType);
        if (deviceServiceImplList == null) {
            throw new NotFoundException("没有找到对应的模块");
        }

        DeviceModuleConfigBo config = deviceModuleConfigService.getDeviceConfigByModule(moduleType, areaId);
        String implName = StringUtils.toRootLowerCase(config.getImplName());

        for (CommonDeviceModule service : deviceServiceImplList) {
            if (StringUtils.toRootLowerCase(service.getClass().getSimpleName()).equals(implName)) {
                return moduleType.cast(service);
            }
        }
        throw new NotFoundException("没有找到指定的服务实现");
    }

    private <T extends CommonDeviceModule> T getMockService(Class<T> moduleType) {
        CommonDeviceModule service = mockDeviceService.get(moduleType);
        if (service == null) {
            throw new NotFoundException("没有找到mock的服务实现");
        }

        return moduleType.cast(service);
    }

}
