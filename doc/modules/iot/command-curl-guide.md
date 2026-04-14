# ems-iot HTTP 命令 curl 调试手册

本文整理 `ems-iot` 当前 `CommandController` 暴露的全部 HTTP 命令，便于本地联调、联机排障和设备命令回归测试。

## 1. 适用范围

- 适用模块：`ems-iot`
- 适用控制器：`/api/commands/**`
- 适用场景：读取设备参数、下发设备命令、调试电价方案、调试分时电量

当前 `ems-iot` 开发环境默认 HTTP 端口为 `8880`。如果你的环境通过网关、反向代理或容器服务名访问，请把下文中的 `BASE_URL` 替换成实际地址。

## 2. 通用约定

### 2.1 基础变量

```bash
BASE_URL="http://127.0.0.1:8880"
DEVICE_ID=1
```

### 2.2 返回结构

接口统一返回 `RestResult`：

```json
{
  "success": true,
  "code": 100001,
  "message": "成功",
  "data": {}
}
```

常见返回码：

- `100001`：成功
- `-101001`：业务失败，例如设备返回失败、设备离线、命令执行失败
- `-102001`：参数错误，例如 `deviceId` 非法、`type` 越界、`dailyPlanId` 越界
- `-100001`：系统异常

### 2.3 参数约束

- `deviceId`：正整数
- `dailyPlanId`：范围 `1~2`
- `used-power.type`：范围 `0~5`
- `duration.period`：当前枚举编码沿用 `ElectricPricePeriodEnum`

### 2.4 时段编码

`period` 和 `used-power.type` 都使用下面这套编码：

| 编码 | 含义 |
|---|---|
| `0` | 总电量 |
| `1` | 尖 |
| `2` | 峰 |
| `3` | 平 |
| `4` | 谷 |
| `5` | 深谷 |

## 3. 命令总览

| 方法 | 路径 | 说明 |
|---|---|---|
| `POST` | `/api/commands/{deviceId}/cut-off` | 下发拉闸命令 |
| `POST` | `/api/commands/{deviceId}/recover` | 下发合闸命令 |
| `GET` | `/api/commands/{deviceId}/ct` | 读取 CT 倍率 |
| `POST` | `/api/commands/{deviceId}/ct` | 下发 CT 倍率 |
| `GET` | `/api/commands/{deviceId}/duration` | 读取日时段电价方案 |
| `POST` | `/api/commands/{deviceId}/duration` | 下发日时段电价方案 |
| `GET` | `/api/commands/{deviceId}/date-duration` | 读取指定日期电价方案 |
| `POST` | `/api/commands/{deviceId}/date-duration` | 下发指定日期电价方案 |
| `GET` | `/api/commands/{deviceId}/used-power` | 读取分时电量 |

## 4. 断闸与合闸

### 4.1 下发拉闸命令

```bash
curl -X POST "${BASE_URL}/api/commands/${DEVICE_ID}/cut-off"
```

### 4.2 下发合闸命令

```bash
curl -X POST "${BASE_URL}/api/commands/${DEVICE_ID}/recover"
```

说明：

- 这两个命令属于高风险操作，建议先确认设备在线状态和测试环境范围。
- 若设备未在线或厂商侧返回失败，通常会得到业务失败响应。

## 5. CT 倍率

### 5.1 读取 CT 倍率

```bash
curl -G "${BASE_URL}/api/commands/${DEVICE_ID}/ct"
```

### 5.2 下发 CT 倍率

示例把 CT 设置为 `200`：

```bash
curl -X POST "${BASE_URL}/api/commands/${DEVICE_ID}/ct?ct=200"
```

## 6. 日时段电价方案

### 6.1 读取日时段电价方案

读取 `1` 号日方案：

```bash
curl -G "${BASE_URL}/api/commands/${DEVICE_ID}/duration" \
  --data-urlencode "dailyPlanId=1"
```

读取 `2` 号日方案：

```bash
curl -G "${BASE_URL}/api/commands/${DEVICE_ID}/duration" \
  --data-urlencode "dailyPlanId=2"
```

### 6.2 下发日时段电价方案

示例：为 `1` 号日方案下发一组时段。

```bash
curl -X POST "${BASE_URL}/api/commands/${DEVICE_ID}/duration" \
  -H "Content-Type: application/json" \
  -d '{
    "dailyPlanId": 1,
    "electricDurations": [
      {
        "period": 1,
        "hour": "08",
        "min": "00"
      },
      {
        "period": 2,
        "hour": "12",
        "min": "00"
      },
      {
        "period": 3,
        "hour": "18",
        "min": "00"
      },
      {
        "period": 4,
        "hour": "22",
        "min": "00"
      }
    ]
  }'
```

字段说明：

- `dailyPlanId`：日方案编号，范围 `1~2`
- `period`：时段编码，当前使用 `0~5`
- `hour`：两位小时，范围 `00~23`
- `min`：两位分钟，范围 `00~59`

## 7. 指定日期电价方案

### 7.1 读取指定日期电价方案

```bash
curl -G "${BASE_URL}/api/commands/${DEVICE_ID}/date-duration"
```

### 7.2 下发指定日期电价方案

示例：把 `1 月 1 日` 和 `1 月 2 日` 绑定到不同的日方案。

```bash
curl -X POST "${BASE_URL}/api/commands/${DEVICE_ID}/date-duration" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "month": "1",
      "day": "1",
      "dailyPlanId": "1"
    },
    {
      "month": "1",
      "day": "2",
      "dailyPlanId": "2"
    }
  ]'
```

字段说明：

- `month`：范围 `1~12`
- `day`：范围 `1~31`
- `dailyPlanId`：范围 `1~2`

## 8. 分时电量

### 8.1 读取总电量

不传 `type` 时，默认读取总电量：

```bash
curl -G "${BASE_URL}/api/commands/${DEVICE_ID}/used-power"
```

也可以显式传 `type=0`：

```bash
curl -G "${BASE_URL}/api/commands/${DEVICE_ID}/used-power" \
  --data-urlencode "type=0"
```

### 8.2 读取各分时电量

```bash
# 尖电量
curl -G "${BASE_URL}/api/commands/${DEVICE_ID}/used-power" \
  --data-urlencode "type=1"

# 峰电量
curl -G "${BASE_URL}/api/commands/${DEVICE_ID}/used-power" \
  --data-urlencode "type=2"

# 平电量
curl -G "${BASE_URL}/api/commands/${DEVICE_ID}/used-power" \
  --data-urlencode "type=3"

# 谷电量
curl -G "${BASE_URL}/api/commands/${DEVICE_ID}/used-power" \
  --data-urlencode "type=4"

# 深谷电量
curl -G "${BASE_URL}/api/commands/${DEVICE_ID}/used-power" \
  --data-urlencode "type=5"
```

## 9. 常见调试方式

### 9.1 配合 `jq` 查看响应

```bash
curl -s -G "${BASE_URL}/api/commands/${DEVICE_ID}/used-power" \
  --data-urlencode "type=2" | jq
```

### 9.2 常见失败排查

如果命令执行失败，优先检查：

1. `ems-iot` 是否已启动，HTTP 端口是否正确
2. `deviceId` 是否存在，且已在 `iot_db` 中注册
3. 设备是否在线，网关路由是否可达
4. 参数是否越界，例如 `dailyPlanId=3`、`type=6`
5. 当前环境前面是否挂了网关或认证层；若有，需要补充对应请求头

### 9.3 典型成功响应

读取分时电量成功示例：

```json
{
  "success": true,
  "code": 100001,
  "message": "成功",
  "data": 12.34
}
```

读取日时段电价成功示例：

```json
{
  "success": true,
  "code": 100001,
  "message": "成功",
  "data": [
    {
      "period": 1,
      "hour": "08",
      "min": "30"
    }
  ]
}
```

## 10. 备注

- 本文示例基于当前代码中的 `CommandController`、`ElectricDurationUpdateVo`、`ElectricDurationVo`、`ElectricDateDurationVo` 和 `ElectricPricePeriodEnum` 整理。
- 如果后续 `ems-iot` 新增命令接口，应优先补充本文，再同步更新 [README.md](./README.md) 索引。
