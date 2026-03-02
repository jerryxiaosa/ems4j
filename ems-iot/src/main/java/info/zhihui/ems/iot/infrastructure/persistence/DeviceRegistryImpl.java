package info.zhihui.ems.iot.infrastructure.persistence;

import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.Product;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.enums.ProductEnum;
import info.zhihui.ems.iot.infrastructure.persistence.entity.DeviceEntity;
import info.zhihui.ems.iot.infrastructure.persistence.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceRegistryImpl implements DeviceRegistry {

    private final DeviceRepository repository;

    @Override
    public Integer save(Device device) {
        DeviceEntity entity = toEntity(device);
        repository.insert(entity);

        return entity.getId();
    }

    @Override
    public void update(Device device) {
        int affected = repository.updateById(toEntity(device));
        if (affected == 0) {
            throw new NotFoundException("设备记录不存在，id=" + device.getId());
        }
    }

    @Override
    public void deleteById(Integer id) {
        int affected = repository.deleteById(id);
        if (affected == 0) {
            log.warn("删除的设备不存在，id: {}", id);
        }
    }

    @Override
    public Device getById(Integer id) {
        DeviceEntity entity = requireDeviceEntity(repository.selectById(id));
        ProductEnum productEnum = requireProductEnum(entity.getProductCode());
        return toDomain(entity, productEnum);
    }

    @Override
    public Device getByDeviceNo(String deviceNo) {
        DeviceEntity entity = requireDeviceEntity(repository.getByDeviceNo(deviceNo));
        ProductEnum productEnum = requireProductEnum(entity.getProductCode());
        return toDomain(entity, productEnum);
    }

    @Override
    public Device getByParentIdAndPortNoAndMeterAddress(Integer parentId, Integer portNo, Integer meterAddress) {
        DeviceEntity entity = requireDeviceEntity(repository.getByParentIdAndPortNoAndMeterAddress(parentId, portNo, meterAddress));
        ProductEnum productEnum = requireProductEnum(entity.getProductCode());
        return toDomain(entity, productEnum);
    }

    private DeviceEntity toEntity(Device device) {
        if (device == null) {
            throw new IllegalArgumentException("设备不能为空");
        }
        Product product = device.getProduct();
        if (product == null || !StringUtils.hasText(product.getCode())) {
            throw new IllegalArgumentException("产品编码不能为空");
        }
        String productCode = product.getCode().trim();
        requireProductEnum(productCode);

        return new DeviceEntity()
                .setId(device.getId())
                .setDeviceNo(device.getDeviceNo())
                .setPortNo(device.getPortNo())
                .setMeterAddress(device.getMeterAddress())
                .setDeviceSecret(device.getDeviceSecret())
                .setSlaveAddress(device.getSlaveAddress())
                .setProductCode(productCode)
                .setParentId(device.getParentId())
                .setLastOnlineAt(device.getLastOnlineAt());
    }

    private Device toDomain(DeviceEntity entity, ProductEnum productEnum) {
        if (entity == null) {
            throw new IllegalArgumentException("设备不能为空");
        }

        if (productEnum == null) {
            throw new IllegalArgumentException("产品枚举不能为空");
        }

        return new Device()
                .setId(entity.getId())
                .setDeviceNo(entity.getDeviceNo())
                .setPortNo(entity.getPortNo())
                .setMeterAddress(entity.getMeterAddress())
                .setDeviceSecret(entity.getDeviceSecret())
                .setSlaveAddress(entity.getSlaveAddress() == null ? 0 : entity.getSlaveAddress())
                .setProduct(toDomain(productEnum))
                .setParentId(entity.getParentId())
                .setLastOnlineAt(entity.getLastOnlineAt());
    }

    private Product toDomain(ProductEnum productEnum) {
        return new Product()
                .setCode(productEnum.getCode())
                .setVendor(productEnum.getVendor())
                .setDeviceType(productEnum.getDeviceType())
                .setIsNb(productEnum.isNb())
                .setHasParent(productEnum.isHasParent())
                .setProtocol(productEnum.getProtocol())
                .setAccessMode(productEnum.getAccessMode());
    }

    private DeviceEntity requireDeviceEntity(DeviceEntity entity) {
        if (entity == null) {
            throw new NotFoundException("设备记录不存在");
        }
        return entity;
    }

    private ProductEnum requireProductEnum(String productCode) {
        if (!StringUtils.hasText(productCode)) {
            throw new IllegalArgumentException("产品编码不能为空");
        }
        ProductEnum productEnum = CodeEnum.fromCode(productCode, ProductEnum.class);
        if (productEnum == null) {
            throw new NotFoundException("产品记录不存在");
        }
        return productEnum;
    }
}
