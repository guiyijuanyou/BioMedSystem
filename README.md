# 生物医药数字信息系统

本项目从 0 搭建了一套中药材数字信息管理系统演示版，覆盖中药材分布地图、生长数据采集、图谱/资料上传下载、课程存储、课题研究、培训跟踪、评价体系、溯源管理、业绩分级分类、用户层级管理、SOAP 数据交换和自动备份。

## 当前可运行版本

项目已经升级为标准 SpringBoot Maven 工程，包含 `pom.xml`、Controller、Service、Repository、MySQL/H2 配置和静态前端资源。

本地演示启动方式：

```powershell
.\start.bat
```

打开浏览器访问：

```text
http://localhost:8088
```

如果已经把 Maven 配到环境变量，也可以执行：

```powershell
mvn.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

`local` 模式使用 H2 本地数据库文件，适合没有启动 MySQL 时演示。

## MySQL 配置

默认配置文件在 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/biomed?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:}
```

正式使用 MySQL 前，先创建数据库：

```sql
CREATE DATABASE biomed DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

然后运行：

```powershell
mvn.cmd spring-boot:run
```

## 数据库脚本

项目里现在有两类 SQL：

- `src/main/resources/db/schema.sql` 和 `src/main/resources/db/data.sql`：SpringBoot 启动时自动执行，适合当前系统快速运行。
- `database/schema.sql`：完整业务数据库设计脚本，包含用户权限、药材地图、生长采集、溯源、图谱比对、课程、课题、培训、评价、业绩审核、文件、备份日志、SOAP 交换日志等表，适合连接 MySQL 建正式库，也适合放到实训报告里。

## 已实现功能

- 重庆市中药材分布网络地图查询
- 中药材基础数据维护
- 手机端/传感器生长数据采集入库
- 试验课程、课题研究、培训素材存储
- 中药材评价体系与申报素材记录
- 工作业绩录入、审核状态、分类分级管理
- 分级分类标准动态更新
- 用户角色与权限层级管理
- 文件上传、下载
- 本地数据自动备份
- SOAP 协议数据交换示例接口

## 目录结构

```text
pom.xml                                      Maven 配置
src/main/java/com/cqutcm/biomed             SpringBoot 后端代码
src/main/resources/static                   前端页面、样式、交互
src/main/resources/application.yml          MySQL 配置
src/main/resources/application-local.yml    本地 H2 演示配置
src/main/resources/db                       初始化建表和种子数据
data/                                       运行后生成的数据库文件、备份与上传文件
database/schema.sql                         详细业务表设计参考
docs/soap-example.xml                       SOAP 请求示例
docker/                                     Docker 和 nginx 示例
```

## SOAP 示例

接口地址：

```text
POST http://localhost:8088/api/soap/school
```

请求体示例见 [docs/soap-example.xml](docs/soap-example.xml)。

## 后端分层

- `Controller`：REST 接口、文件接口、SOAP 接口
- `Service`：采集、评价、审核、备份、上传下载业务
- `Repository`：JDBC 数据访问
- `resources/static`：前端页面
- `resources/db`：初始化数据库脚本
