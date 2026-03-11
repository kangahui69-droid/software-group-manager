# AI_README.md - AI智能体开发指南

> **重要提示**：本文档专为AI智能体设计，用于指导AI在该项目中进行开发。任何人類开发者也应阅读此文档以确保代码一致性。

## 项目概述

**项目名称**：高校软件小组管理系统
**技术栈**：Java 1.8 + Maven + Servlet + JDBC + MySQL
**架构模式**：MVC（Model-View-Controller）
**Web容器**：Apache Tomcat 9.0+

## 核心架构

```
┌─────────────────────────────────────────────────────────────┐
│  表现层 (View) - JSP + Bootstrap 5 + jQuery                  │
│  - WebContent/ 和 src/main/webapp/                          │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  控制层 (Controller) - Servlet                              │
│  - servlet/ 包下的所有 *Servlet.java 类                      │
│  - 处理HTTP请求，调用DAO，返回响应                            │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  业务层/数据访问层 (DAO)                                     │
│  - dao/ 包下的所有 *DAO.java 类                              │
│  - 执行SQL操作，返回Model对象或集合                           │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  模型层 (Model) - POJO                                      │
│  - model/ 包下的所有 *.java 类                               │
│  - 与数据库表结构对应的实体类                                 │
└─────────────────────────────────────────────────────────────┘
```

## 代码规范与约定

### 1. 包结构约定

```
src/main/java/
├── config/          # 配置类（Config, ConfigManager）
├── dao/             # 数据访问对象（所有 *DAO.java）
├── disposable/      # 一次性工具（不要在此开发新功能）
├── filter/          # Servlet过滤器（AuthFilter, EncodingFilter）
├── model/           # 实体类（与数据库表对应）
├── servlet/         # Servlet控制器（所有 *Servlet.java）
├── util/            # 工具类（DBUtil, DESUtil, FileUtil等）
└── test/            # 测试类（删除，已移至disposable）
```

### 2. 命名规范

| 类型 | 命名规则 | 示例 |
|------|----------|------|
| 类名 | PascalCase，语义清晰 | `UserDAO`, `LoginServlet` |
| 方法名 | camelCase，动词开头 | `findById`, `doPost` |
| 变量名 | camelCase，有意义 | `userList`, `connection` |
| 常量名 | UPPER_SNAKE_CASE | `MAX_SIZE`, `DEFAULT_TIMEOUT` |
| 包名 | 全小写 | `dao`, `util` |

### 3. Servlet编写规范

```java
package servlet;

import dao.XxxDAO;
import model.Xxx;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Xxx功能控制器
 * 映射路径: /xxx
 */
public class XxxServlet extends HttpServlet {

    private XxxDAO xxxDAO = new XxxDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // GET请求处理（查询、显示页面）
        String action = req.getParameter("action");

        if ("list".equals(action)) {
            listXxx(req, resp);
        } else if ("detail".equals(action)) {
            showDetail(req, resp);
        } else {
            // 默认操作
            req.getRequestDispatcher("/xxx/xxx.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // POST请求处理（创建、更新、删除）
        req.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");

        if ("create".equals(action)) {
            createXxx(req, resp);
        } else if ("update".equals(action)) {
            updateXxx(req, resp);
        } else if ("delete".equals(action)) {
            deleteXxx(req, resp);
        }
    }

    // ========== 私有方法 ==========

    private void listXxx(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Xxx> list = xxxDAO.findAll();
        req.setAttribute("xxxList", list);
        req.getRequestDispatcher("/admin/xxx_list.jsp").forward(req, resp);
    }

    private void createXxx(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // 参数获取和验证
        String name = req.getParameter("name");

        if (name == null || name.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/xxx?action=create&error=empty");
            return;
        }

        // 创建实体
        Xxx xxx = new Xxx();
        xxx.setName(name.trim());

        // 保存
        if (xxxDAO.save(xxx)) {
            resp.sendRedirect(req.getContextPath() + "/xxx?action=list&success=created");
        } else {
            resp.sendRedirect(req.getContextPath() + "/xxx?action=create&error=failed");
        }
    }

    // ... 其他方法
}
```

### 4. DAO编写规范

```java
package dao;

import model.Xxx;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Xxx数据访问对象
 */
public class XxxDAO {

    /**
     * 保存实体
     * @param xxx 实体对象
     * @return 是否成功
     */
    public boolean save(Xxx xxx) {
        String sql = "INSERT INTO xxx (name, description, created_at) VALUES (?, ?, NOW())";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, xxx.getName());
            pstmt.setString(2, xxx.getDescription());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    xxx.setId(rs.getInt(1));
                }
                rs.close();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[XxxDAO.save] 保存失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 根据ID查询
     * @param id 主键
     * @return 实体对象，未找到返回null
     */
    public Xxx findById(Integer id) {
        String sql = "SELECT * FROM xxx WHERE id = ? AND deleted = 0";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToEntity(rs);
            }
        } catch (SQLException e) {
            System.err.println("[XxxDAO.findById] 查询失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 查询所有（排除已删除）
     * @return 实体列表
     */
    public List<Xxx> findAll() {
        String sql = "SELECT * FROM xxx WHERE deleted = 0 ORDER BY created_at DESC";
        List<Xxx> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("[XxxDAO.findAll] 查询失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 软删除（推荐）
     * @param id 主键
     * @return 是否成功
     */
    public boolean softDelete(Integer id) {
        String sql = "UPDATE xxx SET deleted = 1, updated_at = NOW() WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[XxxDAO.softDelete] 删除失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, null);
        }
        return false;
    }

    // ========== 私有方法 ==========

    /**
     * ResultSet映射为实体
     */
    private Xxx mapResultSetToEntity(ResultSet rs) throws SQLException {
        Xxx xxx = new Xxx();
        xxx.setId(rs.getInt("id"));
        xxx.setName(rs.getString("name"));
        xxx.setDescription(rs.getString("description"));
        xxx.setCreatedAt(rs.getTimestamp("created_at"));
        xxx.setUpdatedAt(rs.getTimestamp("updated_at"));
        xxx.setDeleted(rs.getInt("deleted"));
        return xxx;
    }

    /**
     * 关闭资源（保留用于兼容性）
     */
    private void closeResources(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        DBUtil.closeResources(conn, pstmt, rs);
    }
}
```

### 5. Model编写规范

```java
package model;

import java.util.Date;

/**
 * Xxx实体类
 * 对应数据库表: xxx
 */
public class Xxx {

    // ========== 基本字段 ==========

    private Integer id;                    // 主键ID
    private String name;                   // 名称
    private String description;            // 描述
    private Date createdAt;                // 创建时间
    private Date updatedAt;                // 更新时间
    private Integer deleted;               // 软删除标志(0-正常, 1-已删除)

    // ========== 关联字段（可选）==========

    private User creator;                  // 创建人对象（关联查询时使用）

    // ========== 构造方法 ==========

    public Xxx() {
    }

    public Xxx(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // ========== Getter/Setter ==========

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    // ========== 便捷方法 ==========

    /**
     * 检查是否已删除
     */
    public boolean isDeleted() {
        return deleted != null && deleted == 1;
    }

    /**
     * 标记为已删除
     */
    public void markAsDeleted() {
        this.deleted = 1;
    }

    // ========== toString ==========

    @Override
    public String toString() {
        return "Xxx{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", deleted=" + deleted +
                '}';
    }
}
```

### 6. 数据库操作规范

#### 6.1 SQL编写规范

```java
// ✅ 正确：使用PreparedStatement防止SQL注入
String sql = "SELECT * FROM user WHERE username = ? AND status = ?";
PreparedStatement pstmt = conn.prepareStatement(sql);
pstmt.setString(1, username);
pstmt.setInt(2, 1);

// ❌ 错误：字符串拼接，存在SQL注入风险
String sql = "SELECT * FROM user WHERE username = '" + username + "'";
```

#### 6.2 事务处理规范

```java
/**
 * 批量操作示例 - 使用事务
 */
public boolean batchOperation(List<Xxx> list) {
    String sql = "INSERT INTO xxx (name, value) VALUES (?, ?)";
    Connection conn = null;
    PreparedStatement pstmt = null;

    try {
        conn = DBUtil.getConnection();
        conn.setAutoCommit(false);  // 关闭自动提交

        pstmt = conn.prepareStatement(sql);

        for (Xxx item : list) {
            pstmt.setString(1, item.getName());
            pstmt.setString(2, item.getValue());
            pstmt.addBatch();
        }

        pstmt.executeBatch();
        conn.commit();  // 提交事务

        return true;
    } catch (SQLException e) {
        try {
            if (conn != null) conn.rollback();  // 回滚事务
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        System.err.println("[XxxDAO.batchOperation] 批量操作失败: " + e.getMessage());
        e.printStackTrace();
        return false;
    } finally {
        try {
            if (conn != null) conn.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        DBUtil.closeResources(conn, pstmt, null);
    }
}
```

### 7. 安全规范

#### 7.1 SQL注入防护

```java
// ✅ 必须使用的防护方式
// 1. 使用PreparedStatement
String sql = "SELECT * FROM user WHERE username = ? AND password = ?";
PreparedStatement pstmt = conn.prepareStatement(sql);
pstmt.setString(1, username);
pstmt.setString(2, encryptedPassword);

// 2. 输入验证
if (username == null || !username.matches("^[a-zA-Z0-9_]{3,20}$")) {
    throw new IllegalArgumentException("无效的用户名格式");
}
```

#### 7.2 XSS防护

```java
/**
 * XSS过滤工具方法
 */
public class StringUtil {

    /**
     * 转义HTML特殊字符
     */
    public static String escapeHtml(String input) {
        if (input == null) {
            return null;
        }
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;");
    }
}

// 在Servlet中使用
String userInput = request.getParameter("content");
String safeContent = StringUtil.escapeHtml(userInput);
request.setAttribute("content", safeContent);
```

#### 7.3 密码安全

```java
// 密码加密使用DESUtil
String rawPassword = "admin123";
String encryptedPassword = DESUtil.encrypt(rawPassword);

// 解密验证
String decryptedPassword = DESUtil.decrypt(encryptedPassword);
boolean isMatch = rawPassword.equals(decryptedPassword);
```

### 8. 开发流程

#### 8.1 新增功能开发步骤

```
1. 数据库设计
   - 在 docs/database.md 中更新表结构文档
   - 执行SQL创建/修改表
   - 导出SQL备份到 database/ 目录

2. 创建Model
   - 在 model/ 包下创建实体类
   - 字段与数据库表一一对应
   - 添加Getter/Setter/toString

3. 创建DAO
   - 在 dao/ 包下创建DAO类
   - 实现CRUD基本操作
   - 使用PreparedStatement
   - 正确处理资源关闭

4. 创建Servlet
   - 在 servlet/ 包下创建Servlet
   - 配置URL映射（在web.xml中）
   - 实现doGet/doPost方法
   - 调用DAO，返回视图

5. 创建JSP页面
   - 在 WebContent/ 下创建JSP
   - 使用JSTL标签
   - 引用公共头部/底部
   - 实现表单和列表展示

6. 测试
   - 本地Tomcat部署测试
   - 验证所有功能正常
   - 检查日志无异常

7. 更新文档
   - 更新 AI_README.md（如需要）
   - 更新其他相关文档
```

### 9. 常用代码模板

#### 9.1 DAO模板

```java
package dao;

import model.Entity;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity数据访问对象
 */
public class EntityDAO {

    public boolean save(Entity entity) { /* ... */ }

    public boolean update(Entity entity) { /* ... */ }

    public boolean delete(Integer id) { /* ... */ }

    public boolean softDelete(Integer id) { /* ... */ }

    public Entity findById(Integer id) { /* ... */ }

    public List<Entity> findAll() { /* ... */ }

    public List<Entity> findByPage(int page, int size) { /* ... */ }

    public long count() { /* ... */ }

    private Entity mapResultSetToEntity(ResultSet rs) throws SQLException {
        Entity entity = new Entity();
        entity.setId(rs.getInt("id"));
        // ... 映射其他字段
        return entity;
    }
}
```

#### 9.2 Servlet模板

```java
package servlet;

import dao.XxxDAO;
import model.Xxx;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class XxxServlet extends HttpServlet {

    private XxxDAO xxxDAO = new XxxDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");

        if ("list".equals(action)) {
            list(req, resp);
        } else if ("detail".equals(action)) {
            detail(req, resp);
        } else if ("create".equals(action)) {
            req.getRequestDispatcher("/xxx/create.jsp").forward(req, resp);
        } else if ("edit".equals(action)) {
            edit(req, resp);
        } else {
            list(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");

        if ("create".equals(action)) {
            create(req, resp);
        } else if ("update".equals(action)) {
            update(req, resp);
        } else if ("delete".equals(action)) {
            delete(req, resp);
        }
    }

    // 具体方法实现...
}
```

### 10. 注意事项

1. **不要修改disposable目录下的文件**，这些是一次性工具
2. **不要删除现有的DAO/Model/Servlet**，可以扩展但不要破坏已有功能
3. **新增功能必须遵循MVC架构**，保持代码整洁
4. **所有数据库操作必须使用PreparedStatement**
5. **所有输出到页面的数据必须进行XSS转义**
6. **密码必须使用DESUtil加密后存储**
7. **使用Git提交前检查不要提交敏感信息**

### 11. 联系与支持

- 项目文档：查看 `docs/` 目录
- 数据库文档：`docs/database.md`
- 配置指南：`docs/CONFIG_GUIDE.md`
- 安装指南：`docs/INSTALL.md`

---

**最后更新**：2026-03-10
**文档版本**：v1.0
**维护者**：AI智能体 + 开发团队
