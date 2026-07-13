# 生物医药数字信息系统

面向高校科研、中药材数据管理和教学培训场景的前后端分离项目。实现中药材分布地图、生长数据采集、图谱比对与数据分析、溯源管理、课程与课题管理、培训素材与过程跟踪、评价体系与申报素材、业绩管理与分级认定、文件上传下载、用户层级权限、SOAP 数据交换和 AI 助手。


## 环境要求

| 组件 | 版本 | 说明 |
|------|------|------|
| Java | 21 (Temurin) | 运行后端 |
| Maven | 3.8+ | 或用项目自带的 `mvnw` |
| Node.js | 18+ | 构建前端 |
| MySQL | 8.0+ | 使用 utf8mb4 |
| Redis | 7.x | 可选，无 Redis 自动降级为内存缓存 |
| Docker | 24+ | 可选，用于容器化部署 |

## 快速开始

### 1. 准备数据库

```sql
CREATE DATABASE biomed DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

### 2. 配置环境变量（可选）

项目所有配置已使用 `${VAR:default}` 环境变量占位符，默认值可直接运行。如需修改：

```bash
# 从模板创建环境变量文件（仅供参考，非必需）
cp .env.example .env
```

关键变量：

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `DB_USERNAME` | `root` | 数据库用户名 |
| `DB_PASSWORD` | `root` | 数据库密码 |
| `REDIS_HOST` | `localhost` | Redis 地址 |
| `APP_CORS_ORIGINS` | `http://localhost:8088,http://localhost:5173` | 跨域来源 |
| `APP_SESSION_TTL_MINUTES` | `480` | 会话过期时间（分钟） |
| `APP_PASSWORD_MIN_LENGTH` | `8` | 密码最小长度 |
| `SQL_INIT_MODE` | `always` | 开发用 `always`，生产用 `never` |

### 3. 一键启动（Windows）

```powershell
.\start.bat
```

脚本依次：安装前端依赖 → 构建 Vue → 打包 Spring Boot → 启动服务。

访问 `http://localhost:8088`，使用以下任一账号登录：

| 账号 | 密码 | 角色 |
|------|------|------|
| admin | 123456 | 管理员 |
| teacher | 123456 | 教师 |
| researcher | 123456 | 科研人员 |
| student | 123456 | 学生 |

### 4. 分别开发

```powershell
# 后端（使用 Maven Wrapper，无需全局安装 Maven）
cd backend
./mvnw.cmd spring-boot:run

# 前端
cd frontend
npm install
npm run dev          # http://localhost:5173，自动代理 /api → :8088
```

## 功能模块

| 模块 | 接口路径 | 说明 |
|------|---------|------|
| 工作台 | `/api/summary` | 各模块统计概览 |
| 中药材分布 | `/api/herbs` | 品种、区县、经纬度、种植规模、生态环境、溯源码 |
| 生长数据采集 | `/api/growth-records` | 温湿度、土壤 PH、生长阶段、采集来源（APP/传感器/电脑） |
| 溯源管理 | `/api/trace-events` | 按溯源码串联种植-采集-检测-加工全链路 |
| 图谱比对 | `/api/spectrum-comparisons` | HPLC、薄层、红外等图谱相似度判定 |
| 数据分析 | `/api/growth-analysis` | 多批次生长指标趋势对比 |
| 课程管理 | `/api/courses` | 课程名称、教师、学时、资料类型、发布状态 |
| 教学资源 | `/api/teaching-resources` | 视频/课件/图谱上传与审核发布 |
| 课题研究 | `/api/projects` | 课题创建、学生申请、负责人审批、成果转化 |
| 培训素材 | `/api/trainings` | 培训主题、对象、过程记录归档 |
| 评价体系 | `/api/evaluations` | 药材评价指标、评分、申报素材（非遗/品牌） |
| 业绩管理 | `/api/achievements` | 录入、审核、分级分类认定 |
| 认定标准 | `/api/standards` | 分级规则动态更新 |
| 用户管理 | `/api/users` | 管理员-教师-科研人员-学生四级授权 |
| 资料文件 | `/api/files` | multipart / base64 上传、下载、预览 |
| 数据备份 | `/api/backup` | 手动触发 + 每日凌晨 3:00 自动备份 |
| SOAP 交换 | `/api/soap/school` | 校内系统 XML 数据交换 |
| AI 助手 | `/api/assistant/chat` | 系统问答 |

## 项目结构

```
BioMedSystem/
├── backend/                          Spring Boot 后端
│   ├── mvnw / mvnw.cmd               Maven Wrapper（无需全局 Maven）
│   ├── pom.xml                       dev/prod 双 profile
│   └── src/main/
│       ├── java/com/cqutcm/biomed/
│       │   ├── config/               安全配置、限流、数据初始化
│       │   ├── controller/           REST / 文件 / SOAP 接口
│       │   ├── entity/               实体层
│       │   ├── mapper/               MyBatis Mapper
│       │   └── service/              业务服务层
│       └── resources/
│           ├── application.yml       主配置（全部环境变量化）
│           ├── application-prod.yml  生产 profile（Flyway、Actuator）
│           ├── application-example.yml   配置模板
│           ├── logback-spring.xml    日志滚动（按天、30 天保留）
│           └── db/migration/         Flyway 迁移脚本 (V2-V13)
├── frontend/                         Vue 3 + Vite 前端
│   ├── src/
│   │   ├── components/               通用组件
│   │   ├── views/                    页面组件
│   │   ├── services/                 API 调用
│   │   └── config.js                 模块定义与权限矩阵
│   └── package.json
├── docker/
│   ├── Dockerfile                    多阶段构建（4 阶段：前端→依赖→后端→JRE）
│   └── nginx.conf                    HTTPS + 安全头 + 限流 + gzip
├── docker-compose.yml                容器编排（MySQL + Redis + Backend + Nginx）
├── docker-compose.prod.yml           生产覆盖（SSL、资源限制）
├── scripts/                          部署辅助脚本（备份、迁移等）
├── .env.example                      环境变量模板
├── deploy.sh / deploy.ps1            部署脚本
├── start.bat                         Windows 一键启动
└── README.md
```

## 部署方案

### 方案 A：Windows 局域网

1. 确保本机 IP 固定（如 `192.168.x.x`）
2. 放行防火墙端口：
   ```powershell
   netsh advfirewall firewall add rule name="BioMedSystem" dir=in protocol=tcp localport=8088 action=allow
   ```
3. 构建生产包并启动：
   ```bash
   cd backend
   .\mvnw.cmd -Pprod -DskipTests package
   java -jar target/biomed-digital-system-1.0.0.jar --spring.profiles.active=prod
   ```
4. 局域网设备访问 `http://192.168.x.x:8088`

### 方案 B：Docker（WSL2 / Linux）

```bash
# 首次部署
cp .env.example .env
# 修改 .env 中的 DB_PASSWORD 等生产配置

# 构建并启动所有服务
docker compose up -d

# 查看状态
docker compose ps

# 查看日志
docker compose logs -f
```

首次部署会自动创建数据库表结构并初始化种子数据。

### 方案 C：云服务器

```bash
# 生产覆盖配置（SSL、资源限制、Certbot）
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

## 安全

- 密码使用 **BCrypt** 加密存储，`application.yml` 已在 `.gitignore` 中排除
- 用户角色四级权限矩阵（admin / teacher / researcher / student）
- 后端限流：登录 5 次/分钟，API 100 次/秒（`RateLimitFilter`）
- Nginx 安全头：X-Frame-Options、X-Content-Type-Options、HSTS 等
- 数据备份支持手动和定时自动执行
- SOAP 请求 HMAC-SHA256 签名 + 5 分钟有效期 + Nonce 防重放

## SOAP 接口

```text
POST http://localhost:8088/api/soap/school
Content-Type: text/xml
```

请求示例见 `docs/soap-example.xml`。

## Docker

```bash
# 单容器（不含外部 MySQL/Redis，仅用于测试）
docker build -f docker/Dockerfile -t biomed-system .
docker run --rm -p 8088:8088 biomed-system

# 完整环境（推荐）
docker compose up -d
```

## CI/CD

位于 `.github/workflows/ci.yml`，推送到 GitHub 时自动并行执行后端编译、前端构建和 Docker 镜像构建。
