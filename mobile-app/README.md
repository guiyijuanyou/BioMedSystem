# 生物医药移动端 APP

这是基于 UniApp + Vue 3 的移动采集端工程，面向 Android APK 打包，也可以先用 H5 在浏览器调试。

## 已实现功能

- 移动工作台：首页汇总、设备同步状态、离线队列统计。
- 账号登录：使用网页版账号登录后查看业务数据。
- 设备激活：保存 APP 采集设备编号和设备令牌。
- 批次选择：通过 `X-Device-Token` 获取可采集的 active 药材批次。
- 生长采集：记录温度、湿度、土壤 pH、生长阶段、备注和 GPS。
- GPS 定位：调用 UniApp 定位能力，Android 打包时申请定位权限。
- 离线队列：APP 环境优先使用 SQLite，H5 调试环境回退到本地存储。
- 自动补传：按后端接口批量上传，支持 accepted、duplicate、rejected、processing 状态回写。
- 业务数据：手机端查看批次、生长记录、溯源事件、课程、教学资料和课题。

## 本地调试

```powershell
cd mobile-app
npm install
npm run dev:h5
```

H5 调试时接口地址可以留空，开发服务器会把 `/api` 转发到 `http://localhost:8088`。

手机真机访问电脑后端时，在“设备”页把接口地址填写为局域网地址，例如：

```text
http://172.20.10.2:8088
```

## Android 打包

推荐用 HBuilderX 打开 `mobile-app` 目录，然后选择“发行 -> 原生 App 云打包”。

也可以在安装 UniApp CLI 环境后运行：

```powershell
npm run build:app
```

## 后端接口

移动端采集接口：

- `GET /api/mobile/batches`
- `GET /api/mobile/sync/status`
- `POST /api/mobile/growth-records/batch`

管理端接口仍复用网页版：

- `POST /api/auth/login`
- `GET /api/summary`
- `GET /api/{resourceType}`
