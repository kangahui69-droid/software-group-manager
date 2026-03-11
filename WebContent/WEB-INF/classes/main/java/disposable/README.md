# Disposable Tools - 一次性工具程序

## 说明

本目录存放的是一次性使用的工具程序，主要用于：
- 数据库修复和维护
- 密码诊断和修复
- 系统测试和调试
- 数据迁移

## 文件列表

### 密码相关工具
| 文件名 | 用途 | 使用时机 |
|--------|------|----------|
| `CheckAdminPassword.java` | 验证管理员密码是否正确 | 怀疑密码问题时 |
| `PasswordGenerator.java` | 生成标准加密密码 | 需要重置密码时 |
| `PasswordVerify.java` | 验证密码加密/解密 | 测试DES工具时 |
| `DirectPasswordFix.java` | 直接修复数据库密码 | 登录失败时 |
| `FinalPasswordFix.java` | 最终密码修复方案 | 其他方法无效时 |
| `FixAdminPassword.java` | 修复管理员密码 | 管理员无法登录时 |

### 诊断工具
| 文件名 | 用途 | 使用时机 |
|--------|------|----------|
| `LoginDiagnostic.java` | 登录问题全面诊断 | 登录出现问题时 |
| `LoginDiagnosticTool.java` | 详细的登录诊断 | 需要详细日志时 |

### 测试工具
| 文件名 | 用途 | 使用时机 |
|--------|------|----------|
| `DecryptTest.java` | DES解密测试 | 测试加密功能时 |
| `PasswordVerifyTest.java` | 密码验证测试 | 单元测试时 |

### 编译文件
| 文件名 | 说明 |
|--------|------|
| `*.class` | Java编译后的字节码文件 |

## 使用建议

1. **不要将这些工具类包含在生产环境构建中**
2. **使用前请备份数据库**
3. **仔细阅读每个工具的注释说明**
4. **执行前确保理解工具的作用**

## 迁移记录

- 迁移日期：2026-03-10
- 迁移原因：整理项目结构，将一次性工具与核心代码分离
- 原位置：src/main/java/util/ 和 src/main/java/test/
- 新位置：src/main/java/disposable/
