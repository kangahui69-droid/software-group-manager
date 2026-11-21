# OPS-03：内网 IP HTTPS 部署与验收

## 确认结果与边界

修复前仓库只发布 Nginx TCP 80，Nginx 只监听 HTTP；此前提供的生产 `docker compose ps` 也只显示 Nginx 80。当前入口只有内网 IP，尚无证书。本地配置现已准备好 80→443、TLS 1.2/1.3、私有证书挂载和续期脚本；**生产 VM 未发布本次修改，线上 HTTPS 尚未验收**。

内网 IP 不能使用公开 CA 的受信任证书（见 [Let's Encrypt 签发策略](https://letsencrypt.org/documents/isrg-cp-v3.4/)）；项目提供内部 CA 签发的 IP SAN 证书。必须把 `ca.crt` 导入每台授权客户端的受信任根证书存储，浏览器才会信任该 HTTPS 连接。**绝不可分发 `ca.key` 或 `privkey.pem`**。如果以后有域名，可改用域名证书；证书文件名仍为 `fullchain.pem`、`privkey.pem`。

## 生产准备

以下 `<VM_IP>` 必须替换成部署时确认的访问 IP，不能直接照抄历史 IP。先完成现有版本的备份、数据库迁移与账号准备，并安排可回滚的发布窗口；本节不要求现在重启或停止生产服务。

```bash
cd /opt/software-group
sudo bash scripts/issue-internal-ip-cert.sh <VM_IP> /opt/software-group-private/ca /opt/software-group-private/tls
sudo bash scripts/check-ip-tls-cert.sh <VM_IP> /opt/software-group-private/ca/ca.crt /opt/software-group-private/tls
sudo ls -l /opt/software-group-private/ca/ca.crt /opt/software-group-private/tls/fullchain.pem /opt/software-group-private/tls/privkey.pem
```

脚本只在首次签发或距到期不足 30 天时更新站点证书；内部 CA 保存在独立目录。将私有 `.env` 设置为 `TLS_CERT_DIR=/opt/software-group-private/tls`。不要把 CA 私钥、站点私钥或证书放入 Git 仓库、`nginx/` 或构建产物。若 VM IP 改变，要为新 IP 重新签发证书并更新客户端入口。

只复制 `/opt/software-group-private/ca/ca.crt` 到授权的 Windows 客户端，然后导入受信任根证书。可由管理员使用系统证书管理工具统一分发；个人测试可用 PowerShell 的 `Import-Certificate -FilePath 'C:\path\ca.crt' -CertStoreLocation Cert:\CurrentUser\Root`。导入前核对 CA 指纹，确认文件来自可信运维渠道。

## 发布与验证

先确认私有证书目录真实存在、`.env` 路径为绝对路径，并从 VM 控制台确认 443 入站策略及回退手段。80 只用于跳转，443 提供业务。切换 Nginx 容器会有短暂连接中断，应在发布窗口执行。

```bash
cd /opt/software-group
docker compose config --quiet
sudo ufw allow 443/tcp
docker compose up -d --no-deps nginx
docker exec sg_nginx nginx -t
docker compose ps nginx
```

从**已信任内部 CA 的 VM 外客户端**执行：

```bash
bash scripts/verify-https.sh <VM_IP> /path/to/ca.crt
CURL_CA_BUNDLE=/path/to/ca.crt BASE_URL=https://<VM_IP> bash scripts/verify-routes.sh
```

还需在浏览器打开 `https://<VM_IP>/software-group/`，确认无证书警告、登录后会话正常、页面没有混合内容；检查 `Set-Cookie` 带 `Secure`。`web.xml` 已将会话 Cookie 设为 Secure。外部同时核对 80 返回 308 且 Location 指向同一 IP 的 HTTPS、443 可达、内部服务端口不可达。Nginx `/health` 仍仅表示代理存活；上游 readiness 属 OPS-04。

## 续期与到期告警

内部证书有效期 90 天，脚本在剩余不足 30 天时自动重签。生产上由 root 的 cron 每天调用维护脚本；证书变更时脚本会先执行 `nginx -t` 再无中断重载：

```cron
MAILTO=运维告警邮箱
0 2 * * * /usr/bin/flock -n /run/software-group-tls.lock /bin/bash /opt/software-group/scripts/maintain-ip-tls.sh <VM_IP> /opt/software-group-private/ca /opt/software-group-private/tls
```

需确保该 VM 的 cron 邮件或监控系统确实把非零退出、证书剩余不足 7 天告警送达运维；上线前故意用错误 IP 运行 `check-ip-tls-cert.sh`，验证告警通道。CA 到期前需人工轮换并重新分发 `ca.crt`。备份 CA 私钥和证书时应使用受控加密备份，站点私钥不能进入普通代码备份。

## 重要限制

Nginx 在 HTTPS 响应中配置了 HSTS 头，所有自定义响应头的路由也单独配置了该头。但 [RFC 6797](https://datatracker.ietf.org/doc/rfc6797/) 要求浏览器**不把 IP 地址记为 HSTS 主机**，因此纯 IP 访问时 HSTS 头无法实现浏览器自动升级或防降级。当前依靠 80→443 跳转和用户使用 `https://`；如需 HSTS 真正生效，应给站点配置受信任域名。

发布前若没有可用证书，不能套用新的 Compose：Nginx 会因缺少证书而无法启动。生产 HTTPS、客户端信任、自动续期和告警送达均须现场验收，不能以本地测试代替。
