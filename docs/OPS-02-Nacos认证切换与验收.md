# OPS-02 Nacos 2.2.3 认证切换与验收（已修复：本地配置，生产待验收）

## 本地配置范围

Compose 为 `nacos/nacos-server:v2.2.3` 显式开启认证，并从 VM 私有 `.env` 读取 `NACOS_AUTH_TOKEN`、`NACOS_AUTH_IDENTITY_KEY`、`NACOS_AUTH_IDENTITY_VALUE`。八个微服务从同一私有文件读取专用的 `NACOS_USERNAME` 和 `NACOS_PASSWORD`，直接绑定到 `spring.cloud.nacos.discovery.username/password`；不再回退到公开的 `nacos/nacos`。Nacos 不发布宿主端口，沿用 OPS-01 的 Docker 内网访问策略。

Nacos 2.2.3 的[官方鉴权说明](https://nacos.io/news/faq/nacos-user-question-history13533/)要求先准备服务身份和客户端账号，再开启认证。`NACOS_AUTH_TOKEN` 必须是 Base64，解码后至少 32 字节；服务身份键和值必须为本部署专用值。Nacos 仍是内部组件，不能仅凭认证就对公网开放；参见 [Nacos 2.x 认证文档](https://www.nacos.io/en/docs/v2.3/guide/user/auth/)。

## 生产切换前置条件

1. 先完成 4.6 生产数据库 baseline 和独立数据库账号切换；备份 `nacos_config`，确认可在隔离环境恢复。核对当前 VM 运行的 Nacos 镜像、用户表、现有注册服务和 `docker compose ps`。OPS-01 的端口收敛也应同步完成，避免认证切换期间控制台暴露。
2. 在受控管理入口中为微服务创建**非默认** Nacos 用户，例如 `sg_nacos_client`，并为当前 `public` 命名空间授予服务注册和发现所需的权限。先在隔离副本验证其读写边界和八服务注册行为。不要把 Nacos 管理员账号交给业务服务。
3. 为默认 `nacos` 管理员设置独立强密码并存入受控凭据库。若当前无鉴权状态下无法安全完成改密，则在 Nacos 仅对 Docker 内网可达后先开启鉴权、立即改密，验收脚本必须确认旧 `nacos/nacos` 不能登录。生产完成前不得保留默认密码。
4. 在私有 `.env` 设置五个新变量。`NACOS_USERNAME` 为已创建的专用用户名；`NACOS_PASSWORD` 使用 32–128 位随机字母、数字、`_` 或 `-`。token 用安全随机数生成并 Base64 编码，解码后至少 32 字节；身份键至少 16 字符、身份值至少 32 字符，且不得使用 `serverIdentity`、`security` 等默认值。不要把这些值写进 Git、手册或终端截图。保持 `.env` 权限为 `600`。
5. 在隔离环境先运行 `bash scripts/check-deployment-secrets.sh`，确认必填值、格式及八个客户端的一致性；随后验证 Nacos 启动、客户端注册和旧凭据失效。生产容器切换在可回滚发布窗口执行，不直接停止整台 VM。

## 生产切换后验收

在 VM 项目目录中运行：

```bash
bash scripts/check-deployment-secrets.sh
docker compose ps
bash scripts/verify-infrastructure.sh
python3 scripts/verify-nacos-auth-runtime.py
```

最后一个脚本从私有 Compose 配置读取专用客户端凭据，读取 Nacos 容器的 Docker 桥接地址，验证匿名命名请求被拒绝、默认 `nacos/nacos` 不能登录、专用客户端能登录并能看到八个已注册服务。脚本不打印密码或 accessToken；`verify-infrastructure.sh` 也会调用它。还应检查八个服务的日志中没有 `403`、认证失败或注册失败，并从 VM 外确认 8848/9848 不能直接连接。

本项目当前使用 Nacos **服务发现**，没有启用 Nacos Config Starter，也没有已知的 Nacos 配置项拉取链路；因此本项只能验证注册/发现。未来启用配置中心时，必须另行配置配置客户端凭据并增加真实配置读取验收。生产 VM 尚未应用本地代码，在线认证与账号轮换结果需另行记录。
