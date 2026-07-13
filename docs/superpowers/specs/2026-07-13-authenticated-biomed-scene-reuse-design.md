# 登录后复用沉浸式首页 DNA 背景设计

## 目标

登录后的应用壳层直接复用沉浸式首页现有的 `BiomedScene.vue`，呈现相同的 DNA 双螺旋、粒子、灯光和 `#f6f9fc` 场景背景。认证页不再维护独立的药用叶片几何、运行策略或静态叶片降级层。

## 视觉与运行状态

- 认证背景使用首页首屏场景状态，向 `BiomedScene` 固定传入 `progress=0`。
- 桌面端、移动端、粗指针设备和 `prefers-reduced-motion` 环境都始终渲染同一个 WebGL 场景。
- 认证背景与首页共享同一组件、同一场景样式、同一动画和同一响应式配色，唯一差异是认证背景不响应指针事件。
- 认证背景固定覆盖视口，位于导航、工作区、弹层、助手和 Toast 之后。
- 背景设置 `aria-hidden="true"` 和 `pointer-events: none`，不进入可访问性树，也不拦截业务交互。
- 认证页不复制这些组件，也不修改 `BiomedScene.vue`、`DnaHelix.vue`、`useLoginScene.js` 的渲染与交互逻辑；DNA 的结构、动画和配色由首页组件统一维护。

## 组件架构

### `App.vue`

保留当前认证分支中的异步背景入口。登录前的 `<router-view v-if="!isAuthenticated" />` 不变；登录后的 `.shell` 仍只挂载一个 `AuthenticatedLeafBackground`。

### `AuthenticatedLeafBackground.vue`

保留该组件作为认证壳层与登录场景之间的装饰性边界：

- 直接静态导入 `@/components/login/BiomedScene.vue`。
- 根节点继续使用 `.authenticated-leaf-background` 和 `aria-hidden="true"`。
- 内部只渲染 `<BiomedScene :progress="0" />`。
- 不再执行 WebGL 能力检测、媒体查询、页面可见性策略或异步叶片场景加载。
- 通过认证包装器的高优先级限定规则将 `.biomed-scene` 设置为 `pointer-events: none`；首页中的 `BiomedScene` 继续保持可交互。

`App.vue` 已经异步加载整个认证背景入口，因此 `BiomedScene` 及 TresJS 代码仍不会由未认证分支主动挂载。

## 共享场景样式

新增 `frontend/src/styles/biomed-scene.css`，保存 `BiomedScene` 当前在首页最终生效的全部视觉规则：绝对定位、Canvas 透明度、桌面渐变叠层、移动端渐变叠层和现有 fallback 显示状态。`BiomedScene.vue` 直接导入该样式，因此无论它由登录页还是认证壳层挂载，都使用同一份视觉定义。

从 `login-experience.css` 中移除已经迁入共享文件的 `.biomed-scene` 规则，避免重复声明和级联顺序差异。登录页的布局规则，例如 `.login-scene-layer`，仍保留在登录页样式中。

认证样式只定义包装器边界和唯一差异：

- `.authenticated-leaf-background` 使用 `position: fixed; inset: 0; background: #f6f9fc; pointer-events: none`。
- 使用 `.authenticated-leaf-background .biomed-scene` 限定选择器强制 `pointer-events: none`。
- Canvas 的尺寸、透明度、渲染色、灯光和响应式渐变全部来自共享场景组件及其共享样式。
- 隐藏当前药用叶片 SVG、网格、渐变和移动端 Canvas 降级规则。
- `.workspace` 继续使用正 `z-index`，确保业务内容位于场景上方。

认证包装器不覆盖其他场景视觉属性，保证不可交互是登录后背景与首页背景之间的唯一差异。

## 删除范围

删除以下仅服务于药用叶片方案的文件：

- `frontend/src/composables/useAuthenticatedBackground.js`
- `frontend/src/components/background/medicinalLeafGeometry.js`
- `frontend/src/components/background/MedicinalLeafModel.vue`
- `frontend/src/components/background/MedicinalLeafScene.vue`
- `frontend/src/components/__tests__/medicinalLeafGeometry.test.js`

保留并重写 `AuthenticatedLeafBackground.vue`，同时替换 `styles.css` 中的认证药用叶片样式段。

`BiomedScene.vue` 只增加共享样式导入，不修改场景模板、指针计算、相机、灯光或 DNA 数据。

## 错误与降级

认证背景遵循首页 `BiomedScene` 的现有运行行为，不增加第二套错误处理或设备降级策略。背景仍是装饰层；即使 Canvas 未成功绘制，认证内容因独立层叠关系仍保持可见和可交互。

## 测试与验证

### 自动化测试

- 认证背景源码直接导入并渲染 `BiomedScene`。
- `progress` 固定为 `0`。
- `BiomedScene` 自己加载共享场景样式，首页与认证页没有两份视觉实现。
- 认证包装器明确覆盖 `.biomed-scene` 为不可交互，而首页仍保留指针交互。
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
- 首页指针移动仍能更新 DNA 场景，认证页上的相同指针输入不会传递给 `BiomedScene`。

主观视觉验收继续由用户完成。

## 明确不包含

- 不修改沉浸式登录首页及其滚动叙事。
- 不修改 DNA 几何、粒子数量、灯光、材质或动画速度。
- 不为认证页新增鼠标跟踪、滚动绑定或业务路由联动。
- 不引入新依赖、第二套 3D 引擎或复制版 DNA 组件。
