# 地图工具条与统一下拉组件设计

## 目标

在不修改后端、路由和业务数据逻辑的前提下，修复工作台地图工具条的视觉问题，并将项目内 30 个原生 `select` 统一替换为与当前沉浸式首页和认证页视觉系统一致的 Vue 下拉组件。

## 地图工具条

- 移除 `.map-tools` 当前明显的外层深色边框，保留低对比浅灰背景和紧凑间距。
- 将“资源点 / 药材批次”实现为等宽分段控件，两个按钮共享一个绝对定位的紫色活动滑块。
- 使用 GSAP 仅动画滑块的 `transform`，切换时长 220ms，缓动为 `power2.out`，快速连续点击时覆盖上一段动画。
- 检测 `prefers-reduced-motion`；启用减少动态效果时立即定位滑块，不执行位移动画。
- 新增按钮固定宽度，显示 Lucide `Plus` 图标和“新增”，字号、字重、字体族与分段按钮一致。
- 地图缩放、复位按钮继续使用既有点击逻辑和 Lucide 图标，保持固定尺寸。

## 统一下拉组件

新增一个通用 `AppSelect` Vue 组件，替换当前 30 个原生 `select`。组件负责视觉和交互，调用方继续负责选项数据与业务处理。

### 组件接口

- `modelValue`：支持字符串、数字、空值。
- `options`：统一为 `{ value, label, disabled? }` 数组。
- `placeholder`：无选择时的提示文字。
- `disabled`、`required`、`name`、`aria-label`：保留原生表单语义。
- 发出 `update:modelValue` 和 `change`，以兼容现有 `v-model` 及切换回调。

### 交互

- 点击触发器打开或关闭浮动选项层。
- 点击组件外部、按 `Escape` 或完成选择后关闭。
- 支持 `ArrowUp`、`ArrowDown`、`Home`、`End` 和 `Enter` 键盘操作。
- 打开时焦点定位当前选项；禁用选项不可选择。
- 长标签单行省略，悬停时通过 `title` 查看完整内容。
- 选中项显示 Lucide `Check`，触发器使用 Lucide `ChevronDown`。

### 视觉

- 触发器使用现有 `--ink`、`--muted`、`--line`、`--brand` 和 `--brand-soft` 变量。
- 统一 12px 圆角、白色背景和低对比边框；聚焦时显示紫色焦点环。
- 下拉层使用白色背景、柔和阴影和 8px 内边距，不使用系统蓝色高亮。
- 选项为 8px 圆角；悬停使用淡紫背景，选中项使用品牌色文字和勾选图标。
- 展开动画仅使用 `transform` 与 `opacity`，进入 180ms、退出 120ms，并支持减少动态效果。

## 替换范围

- `DashboardView.vue`：5 处。
- `ResourceView.vue`：13 处，包括溯源码流转链路。
- `ImprovementWorkflowView.vue`：6 处。
- `QualityMetricView.vue`：2 处。
- `FilesView.vue`、`MobileDeviceView.vue`、`MultiEvaluationView.vue`、`SpectrumCompareView.vue`：各 1 处。

## 逻辑保持

- 不改变任何 API、路由目标、权限判断或提交方法。
- 原有 `@change` 处理器继续在值变化后执行。
- 原有字符串和数字值类型保持不变，分页页数等数字选项不得变成字符串。
- 原有 `required` 与 `disabled` 状态在组件触发器和隐藏表单字段中保持一致。

## 测试

- 组件测试覆盖字符串值、数字值、禁用状态、键盘导航、点击外部关闭和 `change` 事件。
- Dashboard 测试覆盖资源点/药材批次切换、滑块 GSAP 调用以及固定“新增”标签。
- 关键页面测试覆盖批次选择后原有回调仍被调用。
- 运行全部 Vitest、前端生产构建，并检查没有修改后端文件。

## 不在范围内

- 不修改移动端专用布局。
- 不调整业务字段、选项内容或权限。
- 不修改后端数据库和接口。
