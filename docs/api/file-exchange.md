# 文件交换接口

文件接口使用系统用户登录令牌或 `BIOMED_SESSION` Cookie。允许上传的角色为管理员、教师和科研人员。

## 接口

| 方法 | 地址 | 说明 |
| --- | --- | --- |
| `GET` | `/api/files` | 查询文件列表 |
| `POST` | `/api/files/upload` | multipart 上传 |
| `POST` | `/api/files/upload-json` | Base64 JSON 上传 |
| `GET` | `/api/files/{id}/download` | 下载文件 |
| `GET` | `/api/files/{id}/preview` | 安全预览 |
| `DELETE` | `/api/files/{id}` | 删除文件 |

## multipart 上传

```powershell
$headers = @{ Authorization = "Bearer $env:BIOMED_TOKEN" }
$form = @{
  file = Get-Item "C:\data\sample.csv"
}
Invoke-RestMethod -Method Post -Uri "http://localhost:8088/api/files/upload" `
  -Headers $headers -Form $form
```

完整脚本参见 [file_exchange.ps1](examples/file_exchange.ps1)。

## 安全限制

- 默认单文件最大 256 MB。
- 同时校验扩展名、Content-Type 风险和常见文件魔数。
- 禁止 HTML、SVG、JavaScript 等可执行网页内容。
- 预览响应使用 CSP sandbox 和 `nosniff`。
- 文件存储名由服务端生成，客户端文件名不能决定实际路径。
- 所有文件操作均写入外部调用审计，并返回 `X-Request-Id`。

如需提交 `category`，必须使用系统配置的业务分类；省略时使用服务端默认分类。
