# 外部系统集成接口

本文档面向手机 APP、校内数据中心和文件交换客户端的开发与联调人员。

## 接口入口

| 场景 | 地址 | 认证方式 |
| --- | --- | --- |
| APP 设备管理 | `/api/mobile/devices` | 管理员登录令牌 |
| APP 生长数据上传 | `/api/mobile/growth-records/batch` | `X-Device-Token` |
| APP 同步状态 | `/api/mobile/sync/status` | `X-Device-Token` |
| 校内 SOAP 服务 | `/api/soap/school` | HMAC-SHA256 签名头 |
| SOAP WSDL | `/api/soap/school-integration.wsdl` | 无 |
| SOAP XSD | `/api/soap/school-integration.xsd` | 无 |
| 文件交换 | `/api/files/**` | 用户登录令牌或会话 Cookie |
| 外部调用审计 | `/api/integration-audits` | 管理员登录令牌 |

默认本地服务地址为 `http://localhost:8088`。

## 联调顺序

1. 管理员登记 APP 设备或校内集成客户端，并立即安全保存仅显示一次的密钥。
2. APP 使用设备令牌上传记录；校内客户端按 SOAP 文档计算签名。
3. 保存响应头 `X-Request-Id`，出现问题时交给管理员检索审计记录。
4. 禁止在日志、截图、源代码和即时通信中发送设备令牌或集成密钥。

## 文档目录

- [手机 APP 采集接口](mobile-collection.md)
- [SOAP 与签名协议](soap-integration.md)
- [文件交换接口](file-exchange.md)
- [错误处理说明](error-handling.md)
- `examples/`：可直接运行的调用脚本和请求样例
