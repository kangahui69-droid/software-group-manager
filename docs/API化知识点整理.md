---
title: "我第一次做 API 化：用一个真实项目，把「从返回网页到返回数据」讲明白"
aliases:
  - /posts/api-ization/
  - /posts/learn-rest-api/
  - /posts/jsp-to-json-rest/
description: "API 化的字面意思我早就听过，但一直没搞懂它到底在改什么。直到我把自己那个高校管理系统里「Servlet 直接把拼好的 HTML 甩给浏览器」的老套路，改成「Servlet 只吐 JSON、前端自己拿数据渲染」，才第一次看懂 API 化。这篇文章不是 REST 规范手册，而是我脑子里的那张 API 化地图——如果你也卡在「知道要写接口，但不知道接口到底改变了什么」，希望它有用。"
---

# 我第一次做 API 化：用一个真实项目，把「从返回网页到返回数据」讲明白

> **摘要**：在做那套高校管理系统的时候，我听到"API 化"这三个字很久了，但一直说不清它到底改了什么——我的功能明明都能用，为什么还要再写一套接口？直到我把系统里"Servlet 把拼好的 HTML 直接甩给浏览器"的老套路，改成"Servlet 只吐 JSON、谁都能来取数据"，才第一次明白：**API 化的本质，是把后端从"返回网页"改成"返回数据"**。这篇文章记的是我理清这件事的过程，用我自己的项目当标本。

---

## 一、引子：API 化之前，我的后端只会"吐网页"

API 化之前，我那套系统的每个功能都是**服务端渲染**：Servlet 取数据 → 塞进 request → 转发给 JSP → JSP 拼成 HTML → **整页 HTML 返回给浏览器**。

看我项目里当时的一个 Servlet（`MemberServlet.java`）：

```java
private void listMembers(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    String keyword = request.getParameter("keyword");
    List<User> users = userDAO.findByConditions(keyword, role, status);
    request.setAttribute("users", users);                          // 把数据塞进去
    request.getRequestDispatcher("/jsp/admin/member/list.jsp")    // 转发给 JSP
           .forward(request, response);                           // JSP 拼 HTML
}
```

前端那份 JSP 是这么消费的：

```jsp
<c:forEach var="user" items="${users}">   <%-- 直接吃 Servlet 塞进 request 的 users --%>
    <tr><td>${user.username}</td><td>${user.email}</td> ...</tr>
</c:forEach>
```

能跑，但**数据和页面焊死在一起**：

- Servlet 只会"吐 HTML"，不会"吐数据"。想拿这份用户列表去做别的事（小程序、导出、给别的系统调）——没门。
- 换个前端的活儿（改按钮、改布局）要动 JSP，后端改个 SQL 也可能碰前端。
- 一句 `request.setAttribute` + `forward`，把"数据"和"页面"绑成了一坨。

这三个问题让我下决心做一件事：**把后端从"吐网页"改成"吐数据"**。这就是 API 化。

---

## 二、API 化到底是什么？

我现在是这么理解的：

> **API 化 = 在已有的 Service 之上，给每个业务功能开一个"标准数据出入口"：用 URL 表示"要什么资源"，用 HTTP 方法表示"要对它做什么"，返回统一的 JSON。**

还拿餐馆打比方：

| | 服务端渲染（旧） | API 化（新） |
|---|---|---|
| 厨房（Servlet）交付什么 | **做好的整盘菜**（HTML） | **标准化原料包 + 说明书**（JSON） |
| 谁能用 | 只有这家餐厅能端上桌 | 网页能摆盘、小程序能摆盘、别的系统也能摆盘 |
| 换摆盘方式 | 得回厨房重做 | 前端自己改，厨房不动 |

**关键认知**：API 化不是"重写一套系统"，而是**给你的 Service 开一个标准出口**。我项目文档里有一句话是整个重构的行动纲领：

> **先有 Service 层，后有 API 层。Service 是业务逻辑的核心，API Servlet 应该是"薄"的。**

也就是说——**服务分层是"练内功"，API 化是"开外窗"**。Service 是那个可复用的业务核心，API 化就是把这个核心接出来给外面用。

---

## 三、对照：同一个"成员列表"，新旧两个 Servlet

这是理解 API 化最直观的方式。同一个功能，两套写法：

### 旧写法（`MemberServlet`）—— 服务端渲染

| 维度 | 旧 Servlet |
|---|---|
| URL | `/admin/member/?action=list`（动作藏在参数里） |
| 认证 | 每个方法开头手写 `session.getAttribute("user")` + 判角色 |
| 调谁 | **直接调 DAO**（`userDAO.findByConditions`） |
| 输出 | `forward` 到 JSP，返回 **HTML** |
| 前端怎么用 | 只能整页刷新 |

### 新写法（`MemberApiServlet`）—— REST API

| 维度 | 新 API Servlet |
|---|---|
| URL | `/api/members`（资源名，动作靠 HTTP 方法） |
| 认证 | 统一 `requireAuthenticatedUser()`（基类提供） |
| 调谁 | **调 Service**（`memberService.listMembers`） |
| 输出 | `writeJson(resp, Result)`，返回 **JSON** |
| 前端怎么用 | `fetch()` 拿数据，自己渲染 |

看新写法那段真实代码：

```java
private void handleListMembers(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
    int page = parseIntParam(req.getParameter("page"), 1);
    int pageSize = parseIntParam(req.getParameter("pageSize"), 20);
    String keyword = req.getParameter("keyword");
    // ... 组 filter
    writeJson(resp, memberService.listMembers(filter, page, pageSize));   // 一句话：调 Service，写 JSON
}
```

**它和"创建成员"是同一个道理**：API Servlet **不碰 SQL、不写业务**，只做三件事——**解析参数 → 调 Service → 写 JSON**。这就是"薄"。

---

## 四、REST 的五条规矩（用真实端点讲）

"API 化"产出的接口，通常遵循 **REST** 风格。我项目里那几十个端点，就是按这五条规矩排出来的：

### 规矩 1：URL 用**名词复数**表示"资源"

```
/api/members      ← 成员这个"资源"
/api/awards       ← 奖项这个"资源"
/api/activities   ← 活动这个"资源"
```

不是 `/api/getMemberList`、`/api/deleteMember`——**动作不进 URL，资源才进 URL。**

### 规矩 2：**HTTP 方法**表示"动作"

| 方法 | 含义 | 我项目里的例子 |
|---|---|---|
| `GET` | 查 | `GET /api/members` 成员列表 |
| `POST` | 增 / 触发动作 | `POST /api/members` 创建成员 |
| `PUT` | 改 | `PUT /api/members/{id}` 更新成员 |
| `DELETE` | 删 | `DELETE /api/members/{id}` 删除成员 |

### 规矩 3：**HTTP 状态码**表示结果

```java
private int resolveHttpStatus(Result result) {
    return result.isSuccess() ? 200 : result.getCode();   // 成功→200，失败→业务错误码
}
```

所以我项目里：未登录 → **401**，参数错 → **400**，找不到 → **404**，成功 → **200**。

### 规矩 4：**统一响应格式**（这是"标准化"的核心）

所有接口返回同一个壳：

```json
{ "code": 0, "message": "ok", "data": { ... } }
{ "code": 400, "message": "用户名已存在", "data": null }
```

**不管哪个模块、哪个接口，前端永远只认这三个字段。** 这是 API 化最省心的地方——前端写一套解析逻辑就够了。

### 规矩 5：**无状态 / 统一认证**

每个请求自带身份，不靠服务器记住"你上一步干了啥"。（我项目 WAR 里还是用 Session，微服务里换成了 JWT——后面讲。）

---

## 五、走一遍完整链路：`GET /api/members?page=1`

```
浏览器: fetch('/api/members?page=1')
   │
   ▼  nginx（按 /api/* 规则转发）
   │
   ▼  AuthFilter（先拦一刀：有没有登录？没登录直接回 401 JSON）
   │
   ▼  MemberApiServlet.doGet            ← ① 分发：是列表？还是详情？
   │      isListPath(pathInfo) → handleListMembers
   │
   ▼  handleListMembers                 ← ① 解析 page/keyword/role 参数
   │      memberService.listMembers(...)
   │
   ▼  MemberService.listMembers         ← ② 业务：组过滤条件、调 DAO、拼分页
   │      userDAO.findByConditions(...)
   │
   ▼  UserDAO.findByConditions          ← ③ SQL：WHERE 1=1 + 条件
   │
   ▼  MySQL
   ▲
   │  Result.ok(分页对象)
   ▲  BaseApiServlet.writeJson          ← ① 序列化成 JSON、设 HTTP 200
{ "code":0, "message":"ok", "data":{"list":[...],"total":42,"page":1} }
   │
   ▼  前端拿到 JSON，用 JS 自己画 <tr>
```

**对比旧流程**：旧流程在 `handleListMembers` 那一步是 `forward` 到 JSP（拼 HTML）；新流程是 `writeJson`（吐 JSON）。**中间的三层调用链完全一样**——这正是"API 化建立在服务分层之上"的意思。

---

## 六、三个必须搞懂的关键机制

### 1. `BaseApiServlet` —— API 化的"公共机关"

所有 API Servlet 都 `extends BaseApiServlet`。它帮你把**每个接口都要做、但每个接口都不想重复写**的事收拢了：

| 基类提供 | 干什么 | 你不用再写 |
|---|---|---|
| `writeJson(resp, result)` | 统一设 JSON 头 + 状态码 + 序列化 | 手拼 JSON 字符串 |
| `handleException(resp, e)` | 统一异常 → JSON | try/catch 手写错误响应 |
| `getCurrentUser(req)` | 取当前登录用户 | 每个方法开头 `session.getAttribute` |
| `parseJsonRequest(req)` | 解析请求体 | 手读 InputStream |
| `sendUnauthorized / sendBadRequest` | 常用错误响应 | 手写 401/400 |

**这就是 API 化能"又快又整齐"的原因**：我项目有 14 个 API Servlet，但它们都长一个样——因为公共部分被基类吃掉了。这是一种叫**"模板方法模式"**的做法。

### 2. 路径解析 —— 从 URL 里读出"要操作谁"

REST 的 URL 长短不一，`/api/members`（列表）、`/api/members/5`（详情）、`/api/members/5/enable`（动作），得有个东西把它们拆开。`MemberApiServlet` 里那个 `parsePathInfo` + `MemberPathInfo` 内部类就是干这个的：

```
/                  → ROOT（列表）
/5                 → 成员 id=5（详情）
/5/enable          → 对 id=5 执行 enable 动作
/5/profile         → id=5 的 profile 子资源
```

**这套"解析路径 → 分发到 handler"的模式，14 个 API Servlet 里都有**，只是资源名不同。

### 3. 注册：为什么 API Servlet 走 web.xml

`MemberApiServlet` 头上**有** `@WebServlet(urlPatterns={"/api/members/*"})` 注解——但项目约定很明确：

> **API Servlet registration** —— @WebServlet 注解在 IDEA 里不可靠；一律用 web.xml 手动注册。

所以真正生效的是 `web.xml` 里的手写映射：

```xml
<servlet>
    <servlet-name>MemberApiServlet</servlet-name>
    <servlet-class>servlet.api.MemberApiServlet</servlet-class>
</servlet>
<servlet-mapping>
    <url-pattern>/api/members/*</url-pattern>
</servlet-mapping>
```

**这是我踩过的坑**：注解写了但**不生效**，注册一定要在 web.xml 里补一份。忘了这步，接口就是 404。

---

## 七、API 化之后，项目变成了什么样（一个诚实观察）

有个现象可能会让你困惑：**API 层全建好了，但大部分 JSP 还在走旧的服务端渲染。** 比如前面那份 `jsp/admin/member/list.jsp`——它消费的还是 `${users}`（旧 Servlet forward 进来的），**并没有改成 `fetch('/api/members')`**。

这不是"没做完"，而是 API 化的价值**远不只为了网页**：

| API 化的受益方 | 怎么受益 |
|---|---|
| **微服务拆分** | 8 个微服务就是靠这些 `/api/*` 边界切出来的（nginx 按 `/api/*` 转发给对应微服务） |
| **前端解耦** | 将来把 JSP 改成 `fetch` 调 API，后端一行不用动 |
| **多端复用** | 同一份 `/api/members`，网页、小程序、App 都能用 |
| **可测试** | 接口能脱离浏览器直接测（项目里的 `*ApiServletTest`） |
| **可文档化 / 版本化** | 一套 URL 就是一套契约，能生成文档、能升 `/api/v1` |

而且 API 化在微服务那边**彻底兑现**了——一个模块一个 `/api/*` 前缀，正好是一个微服务的边界。**API 化其实是"微服务拆分"的前置准备**：没有清晰的 API 边界，就切不出干净的服务。

> 补充：认证方式也随架构演进。WAR 里 API Servlet 靠 **Session**（`getCurrentUser(req)` 读 session），而 8 个微服务改成了 **JWT**（`JwtAuthFilter` 读 `Authorization` 头）。同一个 `/api/members` 语义，底层认证机制可以换——这也是 API 层"解耦"的好处。

---

## 八、动手：加一个新 API 的标准动作

| 步骤 | 做什么 | 例子 |
|---|---|---|
| 1 | **先确认 Service 方法存在**（没有就先写） | `memberService.listMembers` |
| 2 | **继承 `BaseApiServlet`**，写 handler | `extends BaseApiServlet` |
| 3 | handler 只做三件事 | 解析参数 → 调 Service → `writeJson` |
| 4 | 写 `doGet/doPost/doPut/doDelete` 分发 | 按 pathInfo 分流 |
| 5 | **在 web.xml 注册 `url-pattern`** | `<url-pattern>/api/xxx/*</url-pattern>` |
| 6 | 写 API 测试 | `XxxApiServletTest` |

**模板骨架**：

```java
public class XxxApiServlet extends BaseApiServlet {
    private XxxService xxxService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.xxxService = new XxxService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = requireAuthenticatedUser(req, resp);   // 统一认证
        if (user == null) return;
        String pathInfo = derivePathInfo(req);
        if (isListPath(pathInfo)) {
            writeJson(resp, xxxService.list(...));         // 调 Service + 写 JSON
        } else {
            writeJson(resp, xxxService.detail(...));
        }
    }
}
```

**千万别这样**（那就白 API 化了）：

```java
// ❌ API Servlet 里直接调 DAO + 手拼 JSON
protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
    List<User> users = userDAO.findByConditions(...);      // 越级调 DAO
    resp.getWriter().write("{\"code\":0,\"data\":[" + ...  // 手拼 JSON
}
```

---

## 九、我脑子里的那张 API 化地图

```
        服务端渲染（旧）                     API 化（新）
   ┌────────────────────┐          ┌────────────────────┐
   │ Servlet ─▶ JSP ─▶ HTML │    ⇒   │ Servlet ─▶ JSON     │
   │ （数据与页面焊死）    │          │ （只吐数据，谁都能用）│
   └────────────────────┘          └────────────────────┘

   API 化的位置：在 Service 之上，开一个标准出口

        Service（业务核心）
           │
           ▼   ← API 化在这里加一层"薄出口"
        BaseApiServlet（公共机关：统一 JSON / 认证 / 异常）
           │
           ▼
        /api/xxx  ← REST：名词 URL + HTTP 动词 + 状态码 + Result 统一体
           │
     ┌─────┴─────┬──────────┐
   网页        小程序      微服务（8 个服务按 /api 边界切）

   统一响应体：{ "code":0, "message":"ok", "data":... }
```

**我现在的理解浓缩成三句话**：

1. **API 化 = 把后端从"吐 HTML"改成"吐 JSON"**，让数据和页面解耦，谁都能来取。
2. **它是服务分层的"下游"**——Service 是业务核心，API Servlet 只是它一个"薄"的标准出口；永远是"先有 Service，后有 API"。
3. **REST 五规矩：名词 URL + HTTP 动词 + 状态码 + 统一 `Result` + 无状态认证**；所有接口长得一样，才叫"标准化"。

拿到任何需求，我现在会问三个问题：

- 这个功能**对应哪个资源**？（定 URL 名词）
- 是"查 / 增 / 改 / 删"里的哪个动作？（定 HTTP 方法）
- **Service 里有没有现成方法？**（有就薄薄包一层，没有就先写 Service）

---

*如果你也是第一次做 API 化：别急着背 REST 规范，先挑一个自己写的"返回 JSP 页面"的 Servlet，试着让它改吐 JSON，再写个前端 `fetch` 把数据画出来。你会立刻感受到那层"数据与页面焊死"的胶水被融开——API 化这件事，讲一百遍不如亲手拆一次。*
