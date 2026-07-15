# 项目宪法 —— RULES.md

> 本文件是项目的最高规范，所有代码必须遵守。违反宪法的代码不允许合入。
> 修订需经项目负责人确认，最后更新：2026-07-13。

---

## 总则

1. 本宪法适用于项目所有代码（前端 / 后端 / 测试 / 脚本）。
2. 规则与 CLAUDE.md、docs/development.md 冲突时，以本文件为准。
3. Code Review 以本宪法为依据，违反者打回修改，不做"下次注意"放行。

---

## 第一章 样式与 UI

### 规则 1：禁止硬编码视觉原子属性，必须使用语义化 CSS 变量

**禁止**在任何 CSS / SCSS / style-jsx / 内联 style 中出现硬编码的：
- 颜色（color / background / border-color 等）
- 间距（margin / padding / gap / top / left 等）
- 字号（font-size）
- 圆角（border-radius）
- 阴影（box-shadow / text-shadow）

**必须**通过 `:root` 或 design-system 中定义的语义化 CSS 变量引用。

```css
/* ❌ 错误 */
.card { background: #ffffff; padding: 16px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,.1); }

/* ✅ 正确 */
.card {
  background: var(--color-bg-primary);
  padding: var(--spacing-md);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-elevated);
}
```

现有的 `css/design-system.css` 是唯一的视觉原子 token 来源，新增 token 必须语义化命名（`--color-*` / `--spacing-*` / `--text-*` / `--radius-*` / `--shadow-*`），不允许出现 `--color-blue-500` 这类在业务侧还需要重新理解语义的名字。

---

## 第二章 异步与数据

### 规则 2：所有异步操作必须完整处理 loading / empty / error / success 四种状态

"异步操作"包括但不限于：fetch / axios 请求、表单提交、文件上传、AI 流式响应、定时器驱动的数据刷新。

四种状态缺一不可：
| 状态 | UI 表现（最低要求） |
|------|--------------------|
| loading | 展示骨架屏或 Spinner，禁用重复提交按钮 |
| empty   | 数据为空数组/空对象时展示空状态插图+文案，不展示空白卡片 |
| error   | 展示错误提示（含重试按钮，可重试场景），禁止静默失败 |
| success | 展示目标内容/Toast 反馈，操作类必须有明确反馈 |

```jsx
// ❌ 错误：只处理 success
const { data } = useSWR('/api/activities', fetcher);
return <ActivityList data={data} />;

// ✅ 正确：四态齐备
const { data, error, isLoading } = useSWR('/api/activities', fetcher);
if (isLoading) return <Skeleton rows={3} />;
if (error) return <ErrorState onRetry={mutate} />;
if (!data?.length) return <EmptyState description="暂无活动" />;
return <ActivityList data={data} />;
```

后端对应约束：Servlet / Service 返回的 JSON 必须有明确的 `code` / `message` / `data` 结构（Result 统一响应），不允许"无响应体 200 OK"表示成功、"抛异常 500"表示失败这种隐式约定。

### 规则 3：所有数组/集合操作前必须做空值（null / undefined）判断

"数组操作"包括但不限于：`.map()` / `.filter()` / `.forEach()` / `.reduce()` / `.length` / 下标访问 / `for...of` / Java 的 `stream()` / `get(i)`。

禁止"接口一定会返回数组"的假设，网络/后端异常/未初始化都可能让值为 `null` 或 `undefined`。

```jsx
// ❌ 错误
{activities.map(a => <ActivityCard key={a.id} activity={a} />)}

// ✅ 正确
{(activities ?? []).map(a => <ActivityCard key={a.id} activity={a} />)}
```

```java
// ❌ 错误
List<Activity> list = activityDAO.findByStatus(1);
list.stream().filter(...).collect(Collectors.toList());

// ✅ 正确
List<Activity> list = activityDAO.findByStatus(1);
if (list != null && !list.isEmpty()) {
    list.stream().filter(...).collect(Collectors.toList());
}
```

解构时同样要保护：`const { items = [] } = resp ?? {};`。

---

## 第三章 组件与工程组织

### 规则 4：使用函数组件和 Hooks，禁止 Class 组件

适用于所有 React / React Native / 小程序 Taro 等类 React 框架。

```jsx
// ❌ 错误
class MemberList extends React.Component {
  componentDidMount() { ... }
  render() { return <div />; }
}

// ✅ 正确
function MemberList() {
  useEffect(() => { ... }, []);
  return <div />;
}
```

现有的 JSP + Tabler 服务端渲染页面不受此条约束（非 React 代码），但新增前端页面必须使用函数组件。

### 规则 5：每个组件 / 每个 Service 方法 / 每个 DAO 写操作必须有对应的单元测试文件

测试文件与源文件同目录，命名后缀 `.test.tsx` / `.test.ts` / `Test.java`。

最低覆盖要求：
- **前端组件**：至少覆盖"正常渲染"+"空状态"+"错误/加载状态"三类场景。
- **Service 方法**：覆盖成功路径 + 至少一种失败路径（非法参数 / DAO 抛异常 / 权限不足）。
- **DAO 写操作**：H2 集成测试，验证写入后可读出。

没有测试的代码不允许合入。bug fix 必须先写能复现 bug 的失败测试，再修代码。

### 规则 6：命名规范

| 元素 | 规范 | 示例 |
|------|------|------|
| React 组件 / 类 / 接口 / 类型别名 / 枚举 | **PascalCase** | `ActivityCard.tsx`, `IUserService`, `ActivityStatus` |
| 变量 / 函数 / Hook / 方法 / Java 方法 / 包 | **camelCase** | `useActivities`, `fetchList`, `userDAO` |
| 常量（真正常量，非 const 修饰的普通变量） | **UPPER_SNAKE_CASE** | `MAX_FILE_SIZE`, `DEFAULT_PAGE_SIZE` |
| CSS 类名 | **kebab-case**（对应 BEM 变体用 `__` 和 `--`） | `.activity-card`, `.activity-card__title--active` |
| CSS 变量 | `--类别-语义` kebab-case | `--color-bg-primary`, `--spacing-md` |
| 事件处理函数 | `handle{事件}` 或 `on{事件}` | `handleSubmit`, `onRowClick` |
| Boolean 变量/Props | `is/has/should/can` 前缀 | `isLoading`, `hasPermission`, `canEdit` |
| 测试文件 | `{被测对象}.test.tsx` / `{被测对象}Test.java` | `ActivityCard.test.tsx`, `UserDAOTest.java` |
| 测试方法名 | `should_{期望}_when_{条件}` | `should_return_member_when_id_exists` |
| 文件名（非组件） | **camelCase** | `dateUtils.ts`, `TransactionTemplate.java` |
| 数据库表 / 列 | **snake_case**（沿用现有约定） | `activity_participant`, `created_at` |
| 路由 URL | kebab-case 或现有 `/{module}/{action}` 约定 | `/api/activities`, `/member/profile` |

禁止：
- 拼音命名（`huodong`、`chengyuan`）
- 无意义缩写（`act` → `activity`，`usr` → `user`；常见通用缩写 `id`/`url`/`sql`/`dao`/`dto` 除外）
- 单字母变量（循环索引 `i`/`j` 除外）
- 下划线开头"私有"标记（JS/TS 没有私有语义，用模块作用域；Java 用 `private`）

---

## 第四章 类型系统

### 规则 7：禁止使用 `any` 类型（TS/JS 侧）

适用于所有 TypeScript 代码。

- 不知道类型时用 `unknown` 替代 `any`，收窄后再使用。
- 第三方库确实无类型定义时，在 `src/types/` 下写最小 `declare module` 或 `.d.ts`，禁止大面积 `as any`。
- 临时类型断言用 `as unknown as T` 过渡，但必须留 `// TODO: 补全类型` 注释并在同一 PR 内解决。
- API 响应必须定义对应 interface，禁止直接用 `any` 接 `res.data`。
- ESLint 规则 `@typescript-eslint/no-explicit-any` 设为 error，CI 阻断。

Java 侧对应约束：
- 禁止用 `Map<String, Object>` 传递业务数据（方法参数 / 返回值），必须定义 DTO / Model 类。
- 禁止 `@SuppressWarnings("unchecked")`，确需使用必须在注释中说明原因并限定范围。
- 原始类型 `List` / `Map`（不带泛型）禁止出现，必须 `List<Activity>` / `Map<String, Activity>`。

### 规则 8：所有 Props 必须显式定义 TypeScript 接口

```tsx
// ❌ 错误：不写类型或内联写
function ActivityCard(props: any) { ... }
function ActivityCard(props: { id: number; title: string }) { ... }

// ✅ 正确：独立 interface，组件同文件或同目录 types.ts
export interface ActivityCardProps {
  activity: Activity;
  onRegister?: (activityId: number) => void;
  variant?: 'default' | 'compact';
}

export function ActivityCard({ activity, onRegister, variant = 'default' }: ActivityCardProps) {
  ...
}
```

约束细节：
- 可选 Props 必须用 `?` 标注并在组件内处理 `undefined`（或给默认值）。
- Props 接口名统一为 `{组件名}Props`，导出供父组件/测试使用。
- 回调 Props 必须明确参数/返回值类型，禁止 `() => any` / `Function`。
- 禁止用 `React.FC`（隐式带 `children` 但不校验类型）；函数声明+显式 Props 接口即可。
- Java 侧对应约束：所有 Servlet `request.getParameter()` 的结果必须校验非空/转换类型后再传入 Service，Service 方法参数类型与业务语义对齐。

---

## 第五章 后端补充规则（同源精神）

上述前端规则在 Java 后端的等价要求：

1. **禁止硬编码魔法值**：状态码、配置 key、SQL 片段、文件路径必须定义为常量或走 Config / 枚举，业务代码中不允许裸字符串/数字。
2. **Service 方法统一返回 `Result<T>`**：不允许抛 RuntimeException 表示业务失败；业务错误用明确的错误码 + message 经 Result 返回。
3. **数据库写操作必须在事务内**：通过 TransactionTemplate 执行，禁止手动 `conn.setAutoCommit(false)` / `commit()` / `rollback()` 散落在业务代码。
4. **Servlet 只做三件事**：解析参数、调用 Service、写响应（JSON 或 forward）。业务逻辑禁止出现在 Servlet。
5. **DAO 不互相调用**：跨表业务逻辑在 Service 层组合。
6. **配置必须走 Config 单例**：禁止 `new Properties()` / `Class.getResourceAsStream()` 私自加载配置。

---

## 第六章 生效与执行

- 本宪法自合入之日起生效。
- 历史代码不要求一次性整改，但新代码 / 修改代码必须遵守"修改即合规"（Boy Scout Rule：每次触碰一个文件就把它拉到合规线）。
- CI 流水线在合入前必须执行：
  - 前端：ESLint（含 `no-explicit-any: error`）+ 单元测试 + 类型检查
  - 后端：`mvn verify`（含单元测试 + 集成测试）
- 对宪法有异议时，提 PR 修改本文件，不允许在代码里"绕过"。

---

## 附录：规则速查表

| # | 规则 | 一句话要点 |
|---|------|-----------|
| 1 | 语义化 CSS 变量 | 不写死颜色/间距/字号/圆角/阴影，用 `--xxx-yyy` 变量 |
| 2 | 异步四态 | loading / empty / error / success 缺一不可 |
| 3 | 数组空值判断 | 操作前 `?? []` / `if (list != null)` 保护 |
| 4 | 函数组件 + Hooks | 禁 Class 组件 |
| 5 | 测试必写 | 组件/Service/DAO 写操作必须有测试 |
| 6 | 命名规范 | PascalCase 组件类 / camelCase 函数变量 / UPPER_SNAKE 常量 / kebab-case CSS类 |
| 7 | 禁止 any | 用 unknown + 收窄，补类型定义 |
| 8 | Props 接口 | 独立 `XxxProps` interface，显式导出 |
