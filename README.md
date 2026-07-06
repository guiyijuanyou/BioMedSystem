# 生物医药数字信息系统

面向高校科研、中药材数据管理和教学培训场景的前后端分离项目。系统包含中药材分布地图、生长数据采集、课程与课题管理、培训素材、评价体系、业绩管理、文件资料、用户层级、SOAP 数据交换和 AI 助手等功能。

## 项目结构

```text
BioMedSystem/
├─ backend/                         Spring Boot 后端工程
│  ├─ pom.xml                       Maven 配置
│  └─ src/main/
│     ├─ java/com/cqutcm/biomed/
│     │  ├─ controller/             REST、文件和 SOAP 接口
│     │  ├─ model/                  数据模型
│     │  ├─ repository/             JDBC 数据访问
│     │  └─ service/                业务逻辑
│     └─ resources/
│        ├─ db/                     初始化 SQL
│        ├─ application.yml         MySQL 配置
│        └─ application-local.yml   H2 本地配置
├─ frontend/                        Vue 3 前端工程
│  ├─ src/
│  │  ├─ components/                通用组件
│  │  ├─ views/                     页面组件
│  │  └─ services/                  接口调用
│  ├─ package.json
│  └─ vite.config.js
├─ database/                        完整业务数据库设计
├─ docker/                          Docker 与 nginx 配置
├─ docs/                            接口示例和项目资料
└─ start.bat                        Windows 一键启动
```

## 一键启动

确保电脑已安装 Java 21、Node.js 和 Maven，然后在项目根目录运行：

```powershell
.\start.bat
```

脚本会依次完成：

1. 安装前端依赖（首次运行）。
2. 构建 Vue 前端。
3. 打包 Spring Boot 后端并嵌入前端资源。
4. 使用本地 H2 配置启动系统。

访问地址：

```text
http://localhost:8088
```

## 分别开发

启动后端：

```powershell
cd backend
mvn.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

启动前端：

```powershell
cd frontend
npm install
npm run dev
```

前端开发地址为 `http://localhost:5173`，Vite 会将 `/api` 请求转发到 `http://localhost:8088`。

## MySQL 配置

正式环境配置位于 `backend/src/main/resources/application.yml`。建议通过环境变量提供连接信息：

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/biomed?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的数据库密码"
```

创建数据库：

```sql
CREATE DATABASE biomed DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

`backend/src/main/resources/db/` 是应用启动脚本，`database/schema.sql` 是完整业务数据库设计参考。

## Docker

在项目根目录执行：

```powershell
docker build -f docker/Dockerfile -t biomed-system .
docker run --rm -p 8088:8088 biomed-system
```

## SOAP 示例

接口地址：

```text
POST http://localhost:8088/api/soap/school
```

请求示例见 `docs/soap-example.xml`。
