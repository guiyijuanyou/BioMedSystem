# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

项目简要需求
生物医药数字信息系包括中药材生长数据实时采集、存储，图谱比对，数据对比分析，溯源管理，线上教学、数据自动备份等基本功能，并能实现上传、下载数据及文件，线上视频教学等拓展功能；系统还能与手机APP、电脑终端共享；而且用户还能实现层级管理。主要功能要求如下：1.建立重庆市中药材分布网络地图，以方便科研人员、学生、科研机构在线查询品种分布地点。2.具备与校内系统（采用SOAP协议）实现数据交换，以便校内师生获取中药材生长的相关数据。同时，应具备手机端的采集功能，可通过手机APP获取生长数据。3.具备中药材试验课程存储，以方便教师、学生了解试验内容。4.具备中药材试验课题研究存储，以方便教师、学生了解课题内容，参与课题研究及相关成果转化。5.具备中药材培训素材的制作及存储，以方便教师、培训人员了解培训内容，对培训过程进行跟踪记录。6.具备中药材的评价体系，对评价过程进行和结果进行跟踪记录，并且记录相关的数据为相关申报提供素材（如非遗申请等）。7.实现工作业绩录入、审核、编辑、分级分类等功能，按照学校现有业绩分类认定办法进行分类分级统计与认定，并实现相关分级分类标准动态更新。

## Commands

### Backend (Spring Boot)
```bash
cd backend
mvn spring-boot:run           # Start dev server on :8088
mvn -DskipTests package        # Build JAR
mvn test                       # Run all tests
mvn test -Dtest=TestClass      # Run single test class
```

### Frontend (Vue 3 + Vite)
```bash
cd frontend
npm install                    # Install dependencies
npm run dev                    # Dev server on :5173 (proxies /api -> :8088)
npm run build                  # Production build
npm test                       # Run vitest tests
npm run test:watch             # Watch mode
```

### One-click start (production-like)
```powershell
.\start.bat                    # Install frontend deps -> build Vue -> package Spring Boot -> start
```

### Docker
```bash
docker build -f docker/Dockerfile -t biomed-system .
docker run --rm -p 8088:8088 biomed-system
```

## Prerequisites
- Java 21, Maven 3.8+, Node.js 18+
- MySQL 8 (database `biomed`, charset utf8mb4)
- Redis (optional — auto-fallback to in-memory cache)

## Project Architecture

### Backend (`backend/`)

**Stack**: Spring Boot 3.3.6 + MyBatis-Plus + Spring Security + Redis

**Package layout** (`com.cqutcm.biomed`):
- `config/` — SecurityConfig (token auth filter chain), MyBatisPlusConfig (optimistic lock, camel-case maps), TokenAuthenticationFilter, ExternalAuditFilter, SpaWebConfig (SPA fallback), DataInitializer
- `controller/` — REST endpoints. Single `ResourceController` handles CRUD for most business modules via dynamic routing. Dedicated controllers for auth, files, SOAP, map-tiles, mobile collection, integration clients, quality metrics, evaluations, improvement workflows, user profiles
- `service/` — Business logic. Key services: `AuthService` (token-based login), `PermissionService` (role-based access), `StructuredRecordService` (generic CRUD for herbs/trainings/evaluations/achievements/standards/users), `CacheService` (Redis + in-memory fallback), `BackupService` (manual + scheduled 3am), `FileAssetService`, domain services for growth records, trace events, courses, projects, etc.
- `entity/` — JPA/MyBatis entities with `version` field for optimistic locking
- `mapper/` — MyBatis-Plus mappers (base: `BiomedBaseMapper<T>`)
- `dto/` — Request/response DTOs grouped by module (one file per module)
- `persistence/` — `BiomedBaseMapper<T>`, `CamelCaseMapWrapperFactory`

**Key patterns**:
- **Generic CRUD**: `ResourceController` routes `GET/POST/PUT/DELETE /api/{resourceType}` to the appropriate service. Structured resources (herbs, trainings, evaluations, achievements, standards, users) use `StructuredRecordService`. Specialized resources (batches, samples, growth-records, trace-events, projects, courses, etc.) use dedicated services.
- **Auth**: Token via `Authorization: Bearer <token>` header or `BIOMED_SESSION` cookie. `AuthService` stores sessions in Redis with in-memory fallback. 8-hour TTL, configurable login failure lockout (5 failures / 15 min).
- **Permissions**: Four roles — `admin` (full access), `teacher`, `researcher`, `student` (restricted). `PermissionService` controls CRUD + ownership checks. Admin-only: review/submit operations, user management, standards.
- **Status workflow**: `StatusMachine` implements: `草稿(DRAFT) → 待审核(PENDING_REVIEW) → 已通过(APPROVED)/已驳回(REJECTED) → 已发布(PUBLISHED) → 已归档(ARCHIVED)`. Students only see PUBLISHED items.
- **Caching**: `CacheService` wraps Redis with local `ConcurrentHashMap` fallback. Automatic Redis health-check reconnection. Cache eviction on writes.
- **Audit**: `ExternalAuditFilter` logs APP/SOAP/FILE requests to `integration_audit` table (no tokens or file bodies stored).
- **File upload**: Multipart and base64-encoded JSON upload. Preview with content-type detection, sandbox CSP, same-origin policy.

### Frontend (`frontend/`)

**Stack**: Vue 3 (Composition API) + Vite 6 + Vue Router 4 + Leaflet + Three.js

**Key files**:
- `src/App.vue` — Main shell: sidebar nav, topbar, user menu, mobile bottom nav, toast notifications, session management
- `src/router/index.js` — Route config: dashboard, module (generic CRUD view), batch-detail, files, spectrum-compare, profile, etc.
- `src/config.js` — Module definitions (field schemas), role-based menus, permission matrix
- `src/services/api.js` — Fetch wrapper with auth header injection, 401 auto-redirect
- `src/views/ResourceView.vue` — Generic CRUD page driven by module config (dynamic forms, tables, map picker)
- `src/components/SidebarNav.vue` — Collapsible sidebar navigation

**Key patterns**:
- Login credentials stored in `sessionStorage` as `biomed-session` (token, role, name, roleLabel)
- Generic `ResourceView` renders any module's table + form from field definitions in `config.js`
- Vite dev server proxies `/api` → `localhost:8088`
- SPA fallback in backend (`SpaWebConfig`) for Vue Router history mode

### Database
- MySQL 8 with `utf8mb4` charset
- Schema auto-initialized from `backend/src/main/resources/db/schema.sql`
- Seed data from `backend/src/main/resources/db/data.sql`
- Migrations in `backend/src/main/resources/db/migration/` (V2–V13)
- All entity IDs are UUID strings (VARCHAR(64) PRIMARY KEY)
- Optimistic locking via `version` INT column

### SOAP Integration
- Endpoint: `POST /api/soap/school` (text/xml)
- HMAC-SHA256 signed requests with timestamp + nonce (5-min window)
- WSDL/XSD at `backend/src/main/resources/wsdl/`
- Secure XML parsing (XXE protection)
- Standard SOAP Fault responses

## Default accounts
| Username   | Password | Role       |
|------------|----------|------------|
| admin      | 123456   | Admin      |
| teacher    | 123456   | Teacher    |
| researcher | 123456   | Researcher |
| student    | 123456   | Student    |
