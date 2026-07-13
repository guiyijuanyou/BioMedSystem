# 错误处理与问题定位

## REST/APP 错误

普通 REST 接口使用 JSON 错误结构：

```json
{
  "code": "BAD_REQUEST",
  "status": 400,
  "message": "records are required",
  "timestamp": "2026-07-13T09:00:00"
}
```

SOAP 接口始终使用 XML SOAP Fault，具体分类见 SOAP 文档。

## 常见状态码

| 状态码 | 客户端处理 |
| --- | --- |
| 400 | 修正字段、XML、SOAPAction 或签名格式后重试 |
| 401 | 重新检查令牌、客户端编号、时间戳和签名 |
| 403 | 联系管理员检查设备/客户端状态和 IP 白名单 |
| 409 | 当前业务状态冲突，刷新数据后再操作 |
| 500 | 不立即生成新业务编号；保留原请求并使用相同幂等编号重试 |

## 使用请求编号排查

客户端应记录每次响应头中的 `X-Request-Id`。管理员可查询：

```http
GET /api/integration-audits?protocol=APP&success=false&limit=100
Authorization: Bearer <admin-token>
```

审计日志不保存凭证明文和业务正文。如需深入排查，应结合请求编号、客户端本地日志和服务端应用日志，而不是要求用户发送令牌或密钥。
