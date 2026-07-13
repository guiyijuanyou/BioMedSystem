# 校内 SOAP 集成与签名协议

## 1. 契约

```text
WSDL: http://localhost:8088/api/soap/school-integration.wsdl
XSD:  http://localhost:8088/api/soap/school-integration.xsd
Endpoint: http://localhost:8088/api/soap/school
SOAPAction: http://cqutcm.com/biomed/school/v1/queryGrowthData
```

服务采用 SOAP 1.1、document/literal 和 UTF-8。请求样例参见 [soap-request.xml](examples/soap-request.xml)。

## 2. 登记校内客户端

管理员调用：

```http
POST /api/integration-clients
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "clientCode": "CQUTCM-SCHOOL",
  "clientName": "校内数据中心",
  "allowedIps": ["127.0.0.1", "10.10.20.15"]
}
```

返回的 `secret` 仅显示一次。数据库使用 AES-GCM 加密保存该密钥。生产环境必须配置 `INTEGRATION_MASTER_KEY`。

## 3. 请求头

| 请求头 | 说明 |
| --- | --- |
| `SOAPAction` | 必须等于契约中声明的操作地址，可带双引号 |
| `X-Integration-Client` | 管理员登记的 `clientCode` |
| `X-Timestamp` | 当前 Unix 秒级时间戳，允许误差 5 分钟 |
| `X-Nonce` | 16 至 128 位随机字符串，仅允许字母、数字、点、下划线、波浪线和短横线 |
| `X-Signature` | HMAC-SHA256 结果的小写十六进制字符串 |
| `X-Request-Id` | 可选，8 至 64 位安全字符；建议调用方生成 |

## 4. 签名计算

首先计算 SOAP 请求体原始 UTF-8 字节的 SHA-256 小写十六进制摘要，然后按下列格式拼接规范字符串：

```text
HTTP_METHOD\n
REQUEST_PATH\n
TIMESTAMP\n
NONCE\n
SHA256_HEX_OF_RAW_BODY
```

本接口的示例为：

```text
POST
/api/soap/school
<X-Timestamp>
<X-Nonce>
<SOAP XML 原始字节的 SHA-256>
```

使用客户端 `secret` 对完整规范字符串执行 `HMAC-SHA256`，输出小写十六进制值作为 `X-Signature`。

重要事项：

- 签名后不得改变 XML 的空格、换行、声明或编码。
- 不要对完整 URL 签名，只签名路径 `/api/soap/school`。
- 每次请求必须生成新 Nonce；重复 Nonce 会被判定为重放攻击。
- 客户端和服务器应使用 NTP 保持时间同步。

可直接运行 [soap_client.py](examples/soap_client.py) 验证实现。

## 5. SOAP Fault

| Fault Code | HTTP 状态 | 含义 |
| --- | --- | --- |
| `soap:Client.Authentication` | 401 | 缺少签名头、签名错误、过期或 Nonce 重复 |
| `soap:Client.Authorization` | 403 | 客户端被禁用或来源 IP 不允许 |
| `soap:Client` | 400 | SOAPAction、命名空间或请求 XML 错误 |
| `soap:Server` | 500 | 服务端内部错误 |

Fault 样例参见 [soap-fault.xml](examples/soap-fault.xml)。
