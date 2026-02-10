# ems-foundation-integration 模块文档

## 1. 模块概述

`ems-foundation-integration` 提供设备集成能力，包括设备命令下发、能源数据服务、设备型号/类型管理等功能。是业务层与 IoT 层的桥梁。
目录按照职责划分为 `core/`、`biz/`、`concrete/` 三层：

### 1.1 core 层
- **定位**：领域核心层
- 定义了不同设备模块的统一接口
- 提供了不同设备模块统一获取配置信息的方案
- 提供了设备类型的统一维护接口

### 1.2 concrete 层
- **定位**：具体厂商/协议的落地实现层，是连接外部系统的最后一环。
- 按设备和厂商划分子目录（如 `concrete/video/`），实现特定适配器：包含 API 客户端、协议解析、数据转换等。
- 每类设备下不同厂商的实现遵循同样的接口，从而使得上层应用可以统一化的调用。不需要在业务代码里针对厂商做硬编码。
 
### 1.3 biz 层
- **定位**：业务组装层，面向具体业务流程（如设备指令下发、状态同步）提供封装的服务，可以附加统一的功能。
比如，直接通过concrete层接口调用下发命令也是可以的，那就需要应用层自己维护结果。而实现了biz层的DeviceCommandExecutor
就可以获得命令执行的结果可视化、可重试、取消等功能。

### 1.4 不同平台对接实现原理

#### 1.4.1 设备模块上下文（DeviceModuleContext）
- 通过 `DeviceModuleConfigService` 维护不同区域各种设备的配置信息。配置信息里说明了在某个区域下的某种类型的设备，用的哪个实现类，配置信息是什么。
- `DeviceModuleContext` 通过 `DeviceModuleConfigService` 获得某个区域下的某种设备的实现类，从而返回给上层进行调用。
- 业务层只需要指定是什么类型的服务以及区域id，就可以获得这个区域下对应的服务接口，而不用知道具体对接的是什么品牌、型号的设备。
- 本质上是通过“DEVICE_CONFIG”这个配置信息来维护

#### 1.4.2 具体实现流程
1. **配置管理**：`DeviceModuleConfigService` 存储不同区域、设备类型的实现类配置
2. **上下文获取**：`DeviceModuleContext` 根据区域ID和设备类型获取对应服务实现
3. **服务调用**：业务层通过统一接口调用具体厂商/协议实现

#### 1.4.3 多厂商支持
假设A园区和B园区分别要接入两批不同厂商的电表X和Y。X和Y的协议和接口可能并不相同。但在系统里会被抽象成 `core/service/CommonDeviceModule` 的实现，即 `concrete/service/EnergyService`。业务系统只需要调用 `DeviceModuleContext.getService` 获取到对应的服务，然后调用服务的接口就可以和具体的设备通信，比如获取设备在线状态、开关闸等等。

#### 1.4.4 约定
- 为了便于项目维护和扩展，约定 integration 只负责与第三方 http 协议进行对接。如果是其他协议，需要增加 ProtocolAdapter 进行转换。例如本项目中的 ems-iot 模块
- 每个类型的接口，在 impl 目录下可以有多个实现。一般对应不同的厂商。
- 假设每个约定的区域范围内，设备的类型是兼容的（比如一个厂商的同一个类型或不同类型但协议接口一致）

#### 1.4.5 同一区域下不同厂商/类型的设备处理
- 如果在区域内已有A厂商类型的设备，后续又新增了B厂商类型的设备。两家厂商的设备协议不同（大概率是不同的），那么即使原先已经分别实现A、B协议，也还需要实现C做为A、B的路由

## 2. Service 类说明

### 2.1 DeviceCommandService

设备命令服务，负责命令的下发和状态管理。

**主要方法：**

| 方法 | 说明 |
|------|------|
| `saveDeviceCommand(DeviceCommandAddDto dto)` | 保存设备命令（仅落库，不自动下发） |
| `execDeviceCommand(Integer commandId, CommandSourceEnum commandSource)` | 按命令 ID 执行下发 |
| `findDeviceCommandPage(DeviceCommandQueryDto query, PageParam pageParam)` | 分页查询命令记录 |
| `getDeviceCommandDetail(Integer commandId)` | 查询单条命令详情 |
| `findCommandExecuteRecordList(Integer commandId)` | 查询命令执行/重试记录 |
| `cancelDeviceCommand(DeviceCommandCancelDto dto)` | 取消（废弃）可重试命令 |

### 2.2 EnergyService

能源数据服务接口，定义与设备交互的标准接口。

**主要方法：**

| 方法 | 说明 |
|------|------|
| `addDevice(ElectricDeviceAddDto addDto)` | 新增电表设备 |
| `editDevice(ElectricDeviceUpdateDto updateDto)` | 修改电表设备 |
| `delDevice(BaseElectricDeviceDto deleteDto)` | 删除电表设备 |
| `cutOff(BaseElectricDeviceDto cutOffDto)` | 分闸 |
| `recover(BaseElectricDeviceDto recoverDto)` | 合闸 |
| `setDuration(DailyEnergyPlanUpdateDto durationUpdateDto)` | 设置尖峰平谷深谷时段方案 |
| `getDuration(DailyEnergyPlanQueryDto durationQueryDto)` | 读取尖峰平谷深谷时段方案 |
| `setDateDuration(DateEnergyPlanUpdateDto dateDurationUpdateDto)` | 下发指定日期电价方案 |
| `getDateDuration(BaseElectricDeviceDto deviceDto)` | 读取指定日期电价方案 |
| `getMeterEnergy(ElectricDeviceDegreeDto degreeDto)` | 读取电表总用电量 |
| `isOnline(BaseElectricDeviceDto deviceDto)` | 查询设备在线状态 |
| `setElectricCt(ElectricDeviceCTDto ctDto)` | 设置电表 CT 变比 |
| `getElectricCt(BaseElectricDeviceDto deviceDto)` | 读取电表 CT 变比 |

### 2.3 DeviceModelService

设备型号管理服务。

**主要方法：**

| 方法 | 说明 |
|------|------|
| `findPage(DeviceModelQueryDto query, PageParam pageParam)` | 分页查询设备型号 |
| `findList(DeviceModelQueryDto query)` | 查询设备型号列表 |
| `getDetail(Integer id)` | 查询设备型号详情 |

### 2.4 DeviceTypeService

设备类型管理服务。

**主要方法：**

| 方法 | 说明 |
|------|------|
| `findList(DeviceTypeQueryDto query)` | 查询设备类型列表 |
| `getDetail(Integer id)` | 查询设备类型详情 |
| `getByKey(String typeKey)` | 按类型标识查询设备类型 |
| `add(DeviceTypeSaveDto updateBo)` | 新增设备类型 |
| `update(DeviceTypeSaveDto updateBo)` | 更新设备类型 |
| `delete(Integer id)` | 删除设备类型 |

### 2.5 DeviceModuleConfigService

设备模块配置服务。

**主要方法：**

| 方法 | 说明 |
|------|------|
| `getDeviceConfigByModule(Class<T> interfaceType, Integer areaId)` | 获取区域内指定模块的配置 |
| `getDeviceConfigValue(Class<T> interfaceType, Class<E> returnObject, Integer areaId)` | 获取区域内指定模块配置值并转换为目标类型 |
| `setDeviceConfigByArea(List<DeviceModuleConfigBo> deviceModuleConfigBoList, Integer areaId)` | 按区域批量设置模块配置 |

## 3. 模块依赖

```
+-------------------------------+
|  ems-foundation-integration   |
+-------------------------------+
            |
            v
+---------------------------+
|   ems-foundation-system   |
|    (系统配置服务)          |
+---------------------------+
            |
            v
+---------------------------+     +---------------------------+
|   ems-components-lock     |     |       ems-common          |
+---------------------------+     +---------------------------+
```

## 4. Service 内部依赖

```
+-------------------------------------+
|     DeviceCommandServiceImpl        |
+-------------------------------------+
                |
    +-----------+-----------+
    |           |           |
    v           v           v
+-------+  +--------+  +---------+
|Command|  |Executor|  |Lock     |
|Record |  |Context |  |Template |
|Repo   |  |        |  |         |
+-------+  +--------+  +---------+
```

## 5. 数据实体

| 实体 | 说明 |
|------|------|
| `DeviceCommandRecordEntity` | 设备命令记录表 |
| `DeviceCommandExecuteRecordEntity` | 命令执行记录表 |
| `DeviceModelEntity` | 设备型号表 |
| `DeviceTypeEntity` | 设备类型表 |
| `DeviceModuleConfigEntity` | 设备模块配置表 |

## 6. EnergyService 实现

```
+---------------------+
|    EnergyService    |  <-- 接口
+---------------------+
          ^
          |
    +-----+-----+
    |           |
+-------+  +-----------+
|Default|  |   Mock    |
|Energy |  |  Energy   |
|Service|  |  Service  |
+-------+  +-----------+
 (真实)     (模拟)

通过配置 useRealDevice 切换实现
```
