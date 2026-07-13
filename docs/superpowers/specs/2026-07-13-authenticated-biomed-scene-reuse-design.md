# 登录后复用沉浸式首页 DNA 背景设计

## 目标

登录后的应用壳层直接复用沉浸式首页现有的 `BiomedScene.vue`，呈现相同的 DNA 双螺旋、粒子、灯光和 `#f6f9fc` 场景背景。认证页不再维护独立的药用叶片几何、运行策略或静态叶片降级层。

## 视觉与运行状态

- 认证背景使用首页首屏场景状态，向 `BiomedScene` 固定传入 `progress=0`。
- 桌面端、移动端、粗指针设备和 `prefers-reduced-motion` 环境都始终渲染同一个 WebGL 场景。
- 认证背景固定覆盖视口，位于导航、工作区、弹层、助手和 Toast 之后。
- 背景设置 `aria-hidden="true"` 和 `pointer-events: none`，不进入可访问性树，也不拦截业务交互。
- 认证页不复制或修改 `BiomedScene.vue`、`DnaHelix.vue`、`useLoginScene.js`，DNA 的结构、动画和配色由首页组件统一维护。

## 组件架构

### `App.vue`

保留当前认证分支中的异步背景入口。登录前的 `<router-view v-if="!isAuthenticated" />` 不变；登录后的 `.shell` 仍只挂载一个 `AuthenticatedLeafBackground`。

### `AuthenticatedLeafBackground.vue`

保留该组件作为认证壳层与登录场景之间的装饰性边界：

- 直接静态导入 `@/components/login/BiomedScene.vue`。
- 根节点继续使用 `.authenticated-leaf-background` 和 `aria-hidden="true"`。
- 内部只渲染 `<BiomedScene :progress="0" />`。
- 不再执行 WebGL 能力检测、媒体查询、页面可见性策略或异步叶片场景加载。

`App.vue` 已经异步加载整个认证背景入口，因此 `BiomedScene` 及 TresJS 代码仍不会由未认证分支主动挂载。

## 样式策略

不在认证页导入完整的 `login-experience.css`。该文件包含大量首页专用全局选择器，直接导入会增加后台样式覆盖风险。

认证样式只定义复用场景所需的边界：

- `.authenticated-leaf-background` 使用 `position: fixed; inset: 0; background: #f6f9fc; pointer-events: none`。
- 使用 `.authenticated-leaf-background .biomed-scene` 限定选择器，将复用场景铺满认证背景。
- Canvas 填满容器并保持首页组件自身的渲染色、灯光和透明度。
- 隐藏当前药用叶片 SVG、网格、渐变和移动端 Canvas 降级规则。
- `.workspace` 继续使用正 `z-index`，确保业务内容位于场景上方。

这些限定样式只作用于认证包装器，不改变首页中 `.biomed-scene` 的现有效果。

## 删除范围

删除以下仅服务于药用叶片方案的文件：

- `frontend/src/composables/useAuthenticatedBackground.js`
- `frontend/src/components/background/medicinalLeafGeometry.js`
- `frontend/src/components/background/MedicinalLeafModel.vue`
- `frontend/src/components/background/MedicinalLeafScene.vue`
- `frontend/src/components/__tests__/medicinalLeafGeometry.test.js`

保留并重写 `AuthenticatedLeafBackground.vue`，同时替换 `styles.css` 中的认证药用叶片样式段。

## 错误与降级

认证背景遵循首页 `BiomedScene` 的现有运行行为，不增加第二套错误处理或设备降级策略。背景仍是装饰层；即使 Canvas 未成功绘制，认证内容因独立层叠关系仍保持可见和可交互。

## 测试与验证

### 自动化测试

- 认证背景源码直接导入并渲染 `BiomedScene`。
- `progress` 固定为 `0`。
- 认证背景不再引用 `MedicinalLeafScene` 或 `useAuthenticatedBackground`。
- 药用叶片实现文件已删除。
- `App.vue` 仍仅在认证 `.shell` 内异步挂载一个背景入口。
- 登录页仍引用原始 `BiomedScene` 和 `DnaHelix`。
- 完整 Vitest 与 Vite 生产构建通过。

### 技术运行验证

- 登录进入 `/dashboard` 后只有一个认证背景和一个 WebGL Canvas。
- Canvas 固定覆盖视口，背景色为 `#f6f9fc`，页面无框架错误层或控制台错误。
- 刷新数据等业务控件可以点击，证明背景没有拦截事件。
- 390px 移动视口仍保留 WebGL Canvas，且页面无横向溢出。

主观视觉验收继续由用户完成。

## 明确不包含

- 不修改沉浸式登录首页及其滚动叙事。
- 不修改 DNA 几何、粒子数量、灯光、材质或动画速度。
- 不为认证页新增鼠标跟踪、滚动绑定或业务路由联动。
- 不引入新依赖、第二套 3D 引擎或复制版 DNA 组件。
