# IoT 设备标识与映射规范

本文固化业务侧 `iotId` 与协议侧 `deviceNo` 的对齐规则，避免上报、下发与在线状态出现错绑或找不到设备的问题。

## 1. 标识定义

- `iotId`：IoT 平台设备主键，业务/集成层对外使用。
- `deviceNo`：设备物理编号/协议标识，协议层用于通道路由与会话绑定。
- `parentId`：子设备关联的网关 `iotId`。
- `portNo`：网关串口号（两位，来自网关报文 `meter id` 的前两位）。
- `meterAddress`：电表通讯地址（三位，来自网关报文 `meter id` 的后三位）。

## 2. 统一映射规则（必须遵守）

| 场景 | 业务侧关键字段 | IoT 侧 deviceNo | IoT 侧 parentId | IoT 侧 portNo + meterAddress | 关键约束 |
|---|---|---|---|---|---|
| 4G 直连电表 | `meterNo` + `iotId` | `serialNumber` | 为空 | 为空 | `meterNo == serialNumber` |
| 网关设备 | `gatewayNo` + `iotId` | `gateway_id` | 为空 | 为空 | `gatewayNo == gateway_id` |
| 网关子电表 | `meterNo` + `portNo` + `meterAddress` + `iotId` | `meterNo` | `gateway.iotId` | `portNo` + `meterAddress` | `meterId == portNo(2)+meterAddress(3)` |

> 说明：若 `deviceNo` 无法全局唯一，需要以 `vendor + deviceNo` 或 `areaId + deviceNo` 作为唯一口径，统一在运维/数据治理中执行。

## 3. 注册/同步流程（不改代码）

- 设备创建时：先在 IoT 平台建立 `(deviceNo, parentId, portNo, meterAddress)` 映射，返回 `iotId` 后写回业务设备。
- 业务侧只依赖 `iotId` 调用能耗平台，协议侧只依赖 `deviceNo` 路由通道。

## 4. 上报与下发路由规范

- 上报入站：协议层解析 `meterId`，拆分出 `portNo` + `meterAddress` 查找设备并发布事件。
- 下发命令：
  - 直连设备：按 `deviceNo` 下发。
  - 网关子电表：按 `gateway deviceNo + portNo + meterAddress` 下发。

## 5. 数据治理与验收清单

- `deviceNo` 唯一性校验（或按 `vendor/areaId` 分区唯一）。
- 业务设备必须有 `iotId`，且 IoT 侧能查到对应 `deviceNo`。
- 网关子电表必须具备 `parentId`、`portNo` 与 `meterAddress`。
- 4G 直连设备必须保证 `meterNo == serialNumber`。

## 6. 变更规则

- `deviceNo` 变更：视为新设备，重新注册并更新 `iotId`，旧映射作废。
- `meterAddress` 或 `portNo` 变更（网关子表）：同步更新映射，否则上报与下发失配。
- 网关替换：更新子设备 `parentId` 指向新网关 `iotId`。

## 7. 风险清单

- `deviceNo` 重复：通道路由错绑、指令下发到错误设备。
- `iotId` 未对齐：在线状态/能耗入库失败。
- `portNo/meterAddress` 不一致：网关子设备无法定位。
