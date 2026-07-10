# 生物医药数字信息系统

面向高校科研、中药材数据管理和教学培训场景的前后端分离项目。实现中药材分布地图、生长数据采集、图谱比对与数据分析、溯源管理、课程与课题管理、培训素材与过程跟踪、评价体系与申报素材、业绩管理与分级认定、文件上传下载、用户层级权限、SOAP 数据交换和 AI 助手。


## 项目结构

```text
BioMedSystem/
├─ backend/                         Spring Boot 后端
│  ├─ pom.xml
│  └─ src/main/
│     ├─ java/com/cqutcm/biomed/
│     │  ├─ config/                 安全配置、数据初始化
│     │  ├─ controller/             REST / 文件 / SOAP 接口
│     │  ├─ entity/                 实体层
│     │  ├─ mapper/                 MyBatis Mapper
│     │  └─ service/                业务服务层
│     └─ resources/
│        ├─ db/                     schema.sql + data.sql
│        ├─ application.yml         配置文件
│        └─ application-example.yml 配置模板
├─ frontend/                        Vue 3 前端
│  ├─ src/
│  │  ├─ components/                通用组件
│  │  ├─ views/                     页面组件
│  │  ├─ services/                  API 调用
│  │  └─ config.js                 模块定义与权限矩阵
│  ├─ package.json
│  └─ vite.config.js
├─ docker/                          Docker 构建
├─ docs/                            接口示例
├─ start.bat                        Windows 一键启动
└─ README.md
```

## 快速开始

确保已安装 Java 21、Node.js 18+ 和 Maven 3.8+。

### 1. 准备数据库

在 MySQL 中创建数据库：

```sql
CREATE DATABASE biomed DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

### 2. 配置连接信息

```powershell
cp backend/src/main/resources/application-example.yml backend/src/main/resources/application.yml
```

编辑 `application.yml`，修改 `username` 和 `password` 为你的 MySQL 凭据。

### 3. 一键启动

```powershell
.\start.bat
```

脚本依次：安装前端依赖 → 构建 Vue → 打包 Spring Boot → 启动服务（启动时自动建表、插入种子数据、初始化 BCrypt 密码）。

访问 `http://localhost:8088`，使用以下任一账号登录：

| 账号 | 密码 | 角色 |
|------|------|------|
| admin | 123456 | 管理员 |
| teacher | 123456 | 教师 |
| researcher | 123456 | 科研人员 |
| student | 123456 | 学生 |

### 4. 分别开发

```powershell
# 后端
cd backend
mvn spring-boot:run

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

## 安全

- 密码使用 **BCrypt** 加密存储，`application.yml` 已在 `.gitignore` 中排除
- 用户角色四级权限矩阵（admin / teacher / researcher / student）
- 数据备份支持手动和定时自动执行
- 新成员部署：复制 `application-example.yml` → `application.yml` 并填写凭据；启动时自动初始化数据库和密码

## SOAP 接口

```text
POST http://localhost:8088/api/soap/school
Content-Type: text/xml
```

请求示例见 `docs/soap-example.xml`。

## Docker

```powershell
docker build -f docker/Dockerfile -t biomed-system .
docker run --rm -p 8088:8088 biomed-system
```
