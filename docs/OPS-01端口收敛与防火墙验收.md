# OPS-01 端口收敛与防火墙验收

## 当前本地配置与生产边界

本地 `docker-compose.yml` 仅发布 Nginx `80:80`、`443:443`，其中 80 只负责跳转。数据库、中间件、管理控制台、旧 WAR 和八个微服务没有宿主端口映射；容器间继续使用 `software-group-net` 和服务名访问。发布前必须先按 [OPS-03 HTTPS 部署与验收](OPS-03-内网IP-HTTPS部署与验收.md) 准备证书。当前生产 VM 尚未部署此改动，其实际监听和网络 ACL 必须现场核查。

Docker 官方说明：Docker 发布端口的流量可能在经过 UFW 的 INPUT 规则前被转发，不能只用 UFW 规则来补救仍在 Compose 中发布的内部端口。因此先移除内部端口映射，再配置 VM 防火墙和上游网络 ACL。参考：[Docker 防火墙文档](https://docs.docker.com/engine/network/packet-filtering-firewalls/)；[Ubuntu UFW 文档](https://ubuntu.com/server/docs/firewalls/)。

## 发布前确认

1. 从 VM 控制台或当前 SSH 会话确认实际 VM IP、SSH 端口、运维来源 IP 和上游转发路径；不要照抄手册中的历史 IP。
2. 保存当前 `docker compose ps`、`docker ps --format '{{.Names}} {{.Ports}}'`、`sudo ufw status numbered`、`sudo ss -lntp` 输出和 Compose/.env 备份。检查 VM 内是否另有非 Compose 服务需要入站连接。
3. 先完成 DB-02/DB-03 的生产库结构核对、显式 Flyway baseline 和独立账号切换。直接把含 4.6 改动的新 Compose 套到旧生产库可能阻止服务启动。
4. 在可回滚的发布窗口重建受端口配置影响的容器，保留所有数据卷。旧容器不会因为本地仓库修改而自动取消既有端口映射；不执行 `down -v`。

## VM 防火墙和 SSH

先确认 Ubuntu VM 的 SSH 入站端口、实际运维出口 IP，并保持一个 VM 控制台或可用的现有 SSH 会话。以下命令中的占位符必须替换成现场核实的值；先使用 `--dry-run` 预览 UFW 规则。**添加定向 SSH 规则并从第二个会话验证成功后，才删除原有的宽泛 SSH 放行规则。**

```bash
sudo ufw status numbered
sudo ufw --dry-run allow proto tcp from <运维出口IP>/32 to any port <SSH端口>
sudo ufw allow proto tcp from <运维出口IP>/32 to any port <SSH端口>
sudo ufw allow 80/tcp
# 确认证书和回退路径就绪后，再执行：sudo ufw allow 443/tcp
sudo ufw status numbered
```

核对现有防火墙策略、转发规则和回退通道后，确认默认入站策略为拒绝（例如 `sudo ufw default deny incoming`），按现场要求保持出站策略，并启用 UFW。若 VM 目前未启用 UFW，不能远程盲目执行 `ufw enable`。从第二个会话确认定向 SSH 规则可用后，检查 `sudo ufw status numbered` 并删除所有宽泛的 SSH 放行规则（包括 IPv6）；从未经授权的来源复测 SSH 不可达。OPS-03 发布时准备好证书后再放行 443。Windows 宿主机防火墙不能代替拥有独立 IP 的 Ubuntu VM 防火墙；还应核对交换机、路由器或云侧 ACL。禁用 `docker` 自建防火墙规则会影响 Docker 网络，不作为本项修复手段。

## 切换后验收

在 VM 的项目目录中检查 Compose 解析和容器实际发布情况，并执行更新后的脚本：

```bash
docker compose config --quiet
docker ps --format '{{.Names}} {{.Ports}}'
bash scripts/verify-infrastructure.sh
CURL_CA_BUNDLE=/path/to/ca.crt BASE_URL=https://<VM_IP> CHECK_LOCAL_SERVICES=1 bash scripts/e2e-smoke-test.sh
CURL_CA_BUNDLE=/path/to/ca.crt BASE_URL=https://<VM_IP> bash scripts/verify-mig43-production-readiness.sh
docker exec sg_nginx wget -q --spider http://app:8080/software-group/login.jsp
```

预期只有 `sg_nginx` 发布宿主端口 80/443，80 返回 HTTPS 跳转；容器健康检查、Nginx 入口和容器内旧 WAR 直连均通过。相关脚本应使用 `BASE_URL=https://<VM_IP>` 和受信任 CA。再从**VM 外部**的运维终端测试 80/443 可达，3307、6379、5672、15672、8848、9848、8858、9000、9001、8080、8081、8082、8084、8085、8086、8087、8089、8094 均不可直接连接；核查 IPv4 和 IPv6，以及 SSH 只对授权来源可达。保存测试时间、来源 IP、VM IP、Git SHA、容器镜像 ID 和结果。不能只凭 `ufw status` 或 `ss` 判断 Docker 端口已经不可达。

生产验收完成前，OPS-01 仅代表本地 Compose 和验收脚本已修复，不能声称生产暴露范围已收敛。
