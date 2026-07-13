# 手机 APP 采集接口

## 1. 登记设备

管理员调用：

```http
POST /api/mobile/devices
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "deviceCode": "APP-001",
  "deviceName": "石柱采集终端 1",
  "owner": "张三",
  "platform": "Android"
}
```

响应中的 `token` 仅显示一次，数据库只保存其 SHA-256 摘要。

管理员还可以调用：

```text
GET  /api/mobile/devices
POST /api/mobile/devices/{id}/rotate-token
PUT  /api/mobile/devices/{id}/status  body: {"status":"active|disabled"}
```

## 2. 批量上传生长记录

```http
POST /api/mobile/growth-records/batch
X-Device-Token: <device-token>
Content-Type: application/json
X-Request-Id: app-request-20260713-0001
```

请求体参见 [mobile-batch-request.json](examples/mobile-batch-request.json)。每批最多 200 条。

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| `clientRecordId` | 是 | APP 本地生成且在同一设备内永久唯一；重传必须保持不变 |
| `batchId` | 是 | 系统中状态为 `active` 的药材批次 ID |
| `recordedAt` | 是 | ISO 本地时间，例如 `2026-07-13T08:30:00` |
| `temperature` | 否 | 温度，数字 |
| `humidity` | 否 | 湿度，数字 |
| `soilPh` | 否 | 土壤 pH，数字 |
| `growthStage` | 否 | 生长阶段 |
| `longitude` | 否 | 经度，范围 -180 至 180；必须与纬度同时提供 |
| `latitude` | 否 | 纬度，范围 -90 至 90；必须与经度同时提供 |
| `locationAccuracy` | 否 | 定位精度，单位米，不得为负数 |

`recordedAt` 不得超过服务器时间 10 分钟。APP 离线存储时应保留原始采集时间。

## 3. 幂等与重传

- 服务端以 `deviceId + clientRecordId` 作为幂等键。
- 已成功记录再次上传时返回 `duplicate`，不会重复写入。
- 失败记录修正后可以使用相同 `clientRecordId` 重传。
- 正在处理的并发请求返回 `processing`。
- 处理状态超过 15 分钟后允许重新认领，防止服务中断造成永久卡住。
- 一条记录失败不会回滚同批次的其他记录。

响应示例：

```json
{
  "accepted": 1,
  "duplicates": 1,
  "rejected": 0,
  "processing": 0,
  "items": [
    {"clientRecordId": "android-001-00001", "status": "accepted", "detail": "growth-record-id"},
    {"clientRecordId": "android-001-00002", "status": "duplicate", "detail": "existing-growth-id"}
  ]
}
```

## 4. 同步状态

```http
GET /api/mobile/sync/status
X-Device-Token: <device-token>
```

响应包含 `accepted`、`rejected`、`processing` 和 `lastSeenAt`。APP 应以逐条上传结果作为本地状态更新依据，汇总接口用于诊断，不用于替代本地同步队列。

## 5. APP 建议

- 本地先写 SQLite，再执行后台上传队列。
- `clientRecordId` 建议采用 `设备编号-UUID`，不要使用数组下标或时间秒数。
- 网络超时后使用相同请求重试，不生成新的记录编号。
- 收到 `401` 时提示设备令牌失效；收到 `403` 时停止同步并提示设备被禁用。
- 保存响应头 `X-Request-Id` 以便服务端排查。
