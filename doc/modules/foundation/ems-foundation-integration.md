# ems-foundation-integration 模块文档

## 1. 模块概述

`ems-foundation-integration` 提供设备集成能力，包括设备命令下发、能源数据服务、设备型号/类型管理等功能。是业务层与 IoT 层的桥梁。
目录按照职责划分为 `core/`、`biz/`、`concrete/` 三层：

### 1.1 core 层
- **定位**：领域核心层，抽象对接场景中可复用的基础模型、仓储接口、领域服务以及校验逻辑。

### 1.2 biz 层
- **定位**：业务组装层，面向具体业务流程（如设备指令下发、状态同步）提供应用服务。

### 1.3 concrete 层
- **定位**：具体厂商/协议的落地实现层，是连接外部系统的最后一环。
- **内容**：
  - 按设备和厂商划分子目录（如 `concrete/video/`），实现特定适配器：包含 API 客户端、协议解析、数据转换等。
  - 将处理结果回调到 biz/core 层，实现与平台统一模型的对接。
- **特点**：高度定制，易变，遵循"入口统一、实现分散"的策略；只在此层处理差异化细节，保持上层稳定。

### 1.4 不同平台对接实现原理

#### 1.4.1 设备模块上下文（DeviceModuleContext）
- 通过 `DeviceModuleConfigService` 维护不同区域各种设备的配置信息。配置信息里说明了在某个区域下的某种类型的设备，用的哪个实现类，配置信息是什么。
- `DeviceModuleContext` 通过 `DeviceModuleConfigService` 获得某个区域下的某种设备的实现类，从而返回给上层进行调用。
- 业务层只需要指定是什么类型的服务以及区域id，就可以获得这个区域下对应的服务接口，而不用知道具体对接的是什么品牌、型号的设备。

#### 1.4.2 具体实现流程
1. **配置管理**：`DeviceModuleConfigService` 存储不同区域、设备类型的实现类配置
2. **上下文获取**：`DeviceModuleContext` 根据区域ID和设备类型获取对应服务实现
3. **服务调用**：业务层通过统一接口调用具体厂商/协议实现

#### 1.4.3 多厂商支持
假设A园区和B园区分别要接入两批不同厂商的电表X和Y。X和Y的协议和接口可能并不相同。但在系统里会被抽象成 `core/service/CommonDeviceModule` 的实现，即 `concrete/service/EnergyService`。业务系统只需要调用 `DeviceModuleContext.getService` 获取到对应的服务，然后调用服务的接口就可以和具体的设备通信，比如获取设备在线状态、开关闸等等。

#### 1.4.4 业务封装
biz 层通过封装 concrete 层服务的接口，提供各类附加的业务逻辑。例如：通过调用 `biz/command` 下的接口进行命令处理，可以看到命令执行记录；每条命令执行的状态；自动进行失败重试等等。而如果直接调用 concrete 层的服务接口，就是简单的直接执行指令。

#### 1.4.5 约定
- 为了便于项目维护和扩展，约定 integration 只负责与第三方 http 协议进行对接。如果是其他协议，需要增加 ProtocolAdapter 进行转换。例如本项目中的 ems-iot 模块
- 假设每个约定的区域范围内，设备的类型是兼容的（比如一个厂商的同一个类型或不同类型但协议接口一致），这个区域一般约定为一个园区
- 每个类型的接口，在 impl 目录下可以有多个实现。一般对应不同的厂商。

#### 1.4.6 同一区域下不同厂商/类型的设备处理
- **推荐** 在 concrete 对应的服务下新增一个实现。这个实现需要通过设备信息代理到原有实现。

## 2. Service 类说明

### 2.1 DeviceCommandService

设备命令服务，负责命令的下发和状态管理。

**主要方法：**

| 方法 | 说明 |
|------|------|
| `saveDeviceCommand()` | 保存设备命令 |
| `executeCommand()` | 执行命令 |
| `cancelCommand()` | 取消命令 |
| `findCommandRecords()` | 查询命令记录 |
| `getCommandDetail()` | 获取命令详情 |

### 2.2 EnergyService

能源数据服务接口，定义与设备交互的标准接口。

**主要方法：**

| 方法 | 说明 |
|------|------|
| `readMeterDegree()` | 读取电表度数 |
| `switchOn()` | 合闸 |
| `switchOff()` | 分闸 |
| `setPowerLimit()` | 设置功率限制 |
| `getDeviceStatus()` | 获取设备状态 |

### 2.3 DeviceModelService

设备型号管理服务。

**主要方法：**

| 方法 | 说明 |
|------|------|
| `findList()` | 查询型号列表 |
| `getDetail()` | 获取型号详情 |

### 2.4 DeviceTypeService

设备类型管理服务。

**主要方法：**

| 方法 | 说明 |
|------|------|
| `findList()` | 查询类型列表 |
| `getDetail()` | 获取类型详情 |

### 2.5 DeviceModuleConfigService

设备模块配置服务。

**主要方法：**

| 方法 | 说明 |
|------|------|
| `getConfig()` | 获取模块配置 |
| `updateConfig()` | 更新模块配置 |

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
