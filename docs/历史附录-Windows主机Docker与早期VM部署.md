# 历史附录：Windows 主机 Docker 与早期 VM 部署记录

> 本文保存 2026 年早期排查与部署过程，不作为当前生产操作步骤。当前唯一部署路线请看 [Windows Server 生产部署完整手册](Windows%20Server生产部署完整手册.md)。Windows 主机 Docker、DockerCli 切换、Windows 容器挂载和旧端口命令均已过时。


> **项目**: 高校软件小组管理系统
> **版本**: 2.0.0（合并版）
> **更新日期**: 2026-08-14
> **服务器系统**: Windows Server 2019/2022
> **对应迁移阶段**: 阶段一~阶段四（微服务迁移完成，生产部署落地）

---

## 一、环境准备

### 1.1 系统要求

| 资源 | 最低配置 | 推荐配置 |
|------|----------|----------|
| CPU | 2 核 | 4 核 |
| 内存 | 4 GB | 8 GB |
| 磁盘 | 50 GB | 100 GB SSD |
| 系统 | Windows Server 2019+ | Windows Server 2022 |

### 1.2 启用容器功能

以管理员身份打开 PowerShell，执行：

```powershell
# 安装容器功能
Install-WindowsFeature -Name Containers -IncludeManagementTools
Install-WindowsFeature -Name Containers -IncludeManagementTools
# 重启服务器（如提示）
Restart-Computer
```

重启后验证：

```powershell
# 检查容器功能是否安装成功
Get-WindowsFeature | Where-Object {$_.InstallState -eq "Installed"}
```

应看到 `Containers` （已安装）。

> **注意**：如果需要运行 Linux 容器（推荐），还需要启用 Hyper-V。但 Hyper-V 与某些硬件不兼容，可跳过并使用 Windows 容器模式。

### 1.3 可选：启用 Hyper-V（支持 Linux 容器模式）（已安装）

> 如果计划使用 Linux 容器模式（推荐），需要启用 Hyper-V。

```powershell
# 安装 Hyper-V
Install-WindowsFeature -Name Hyper-V -IncludeManagementTools

# 重启服务器（已重启）
Restart-Computer
```

---

## 二、安装 Docker Engine

> **重要**：Docker Desktop 不支持 Windows Server！Windows Server 需要安装 **Docker Engine (Docker EE)**。

### 2.1 方法一：在线安装（推荐）

以管理员身份打开 PowerShell，执行：

```powershell
# 安装 Docker Engine
Install-Module DockerMsftProvider -Force
Install-Package Docker -ProviderName DockerMsftProvider -Force

# 启动 Docker 服务
Start-Service Docker

# 设置开机自启
Set-Service -Name Docker -StartupType Automatic
```

### 2.2 方法二：离线安装

如果服务器无法联网，下载 Docker Engine 包后手动安装：
1. 在有网络的机器上下载：https://download.docker.com/win/static/stable/x86_64/
2. 拷贝到服务器
3. 解压后运行 `dockerd.exe`
4. 或使用 `start-service docker` 启动服务

### 2.3 验证安装（已完成）

```powershell
docker --version
docker compose version
```

预期输出：
```
Docker version 27.x.x, build xxxxxx
Docker Compose version v2.x.x
```

### 2.4 配置 Docker daemon（可选）（没有配置，先不用配置，容器的系统有问题）                                                         ）

如需配置日志轮转、存储驱动等，创建配置文件：

```powershell
# 创建配置目录
New-Item -ItemType Directory -Force -Path "C:\ProgramData\docker\config"

# 创建 daemon.json
notepad "C:\ProgramData\docker\config\daemon.json"
```

```json
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m",
    "max-file": "3"
  },
  "storage-opts": ["size=120GB"]
}
```

修改后重启：

```powershell
Restart-Service Docker
```

### 2.5 Linux 容器模式（推荐，必须要在这个Hyper-V开Linux不然镜像跑不了）

> **推荐**：使用 Linux 容器可以获得更好的兼容性和更多镜像选择。

```powershell
# 检查当前容器模式
docker version -f '{{.Server.Os}}'

# 如果显示 windows，运行以下命令切换到 Linux 容器模式
# 注意：需要先启用 Hyper-V
& 'C:\Program Files\Docker\Docker\DockerCli.exe' -SwitchDaemon
```

如果切换成功，再次检查应显示 `linux`。

---

## 三、停止 IIS（释放 80 端口）（已释放）

Windows Server 默认启用 IIS，会占用 80 端口。

```powershell
# 停止 IIS
Stop-Service W3SVC

# 禁用 IIS（可选，防止重启后再次占用）
Set-Service -Name W3SVC -StartupType Disabled
```

验证 80 端口已释放：（已释放）

```powershell
netstat -ano | findstr ":80"
```

应无输出（或只有其他服务的 80）。

---

## 四、上传项目代码

### 4.1 创建项目目录

```powershell
mkdir C:\software-group
cd C:\software-group
```

### 4.2 上传代码（用的是：打包解压得方式）

**方式一：Git 克隆（推荐）**

```powershell
cd C:\software-group
git clone <你的GitHub仓库地址> .
```

**方式二：远程桌面复制**

使用 MSTSC 远程连接，直接复制粘贴文件。

**方式三：SMB 共享**

```powershell
# 本地设置共享后，服务器访问
\\本地电脑IP\share
```

### 4.3 创建静态资源目录（到这一步了）

```powershell
cd C:\software-group
mkdir -p static/css static/js static/images
```

---

## 五、创建配置文件

### 5.1 创建 .env 文件（已创建；按下列安全规则维护）

当前服务运行于 Ubuntu VM。请在 `/opt/software-group` 维护私有 `.env`；已有文件时直接编辑，不能用模板覆盖。

```bash
cd /opt/software-group
test -e .env || cp .env.example .env
chmod 600 .env
vi .env
bash scripts/check-deployment-secrets.sh
```

模板只列变量名，不包含密码。每个必需变量须填入唯一的随机值；预检脚本不会打印值。`DES_KEY` 在 DATA-02 旧 DES 账户全部迁移前必须保持与历史数据一致。不要运行会打印解析后密钥的 `docker compose config`，只使用 `config --quiet`。
---

## 六、开放端口

### 6.1 Windows 防火墙规则

以管理员身份打开 PowerShell，执行：

```powershell
# 主应用端口（必须）
New-NetFirewallRule -DisplayName "Software Group HTTP" -Direction Inbound -Protocol TCP -LocalPort 80 -Action Allow

# Grafana 监控仪表盘（运维）
New-NetFirewallRule -DisplayName "Software Group Grafana" -Direction Inbound -Protocol TCP -LocalPort 3000 -Action Allow

# Prometheus 指标采集（运维）
New-NetFirewallRule -DisplayName "Software Group Prometheus" -Direction Inbound -Protocol TCP -LocalPort 9090 -Action Allow

# Portainer Docker管理界面（运维）
New-NetFirewallRule -DisplayName "Software Group Portainer" -Direction Inbound -Protocol TCP -LocalPort 9443 -Action Allow

# Nacos 服务注册中心（运维）
New-NetFirewallRule -DisplayName "Software Group Nacos" -Direction Inbound -Protocol TCP -LocalPort 8848 -Action Allow

# MinIO 控制台（运维）
New-NetFirewallRule -DisplayName "Software Group MinIO" -Direction Inbound -Protocol TCP -LocalPort 9001 -Action Allow

# RabbitMQ 管理界面（运维）
New-NetFirewallRule -DisplayName "Software Group RabbitMQ" -Direction Inbound -Protocol TCP -LocalPort 15672 -Action Allow
```

### 6.2 云服务商安全组（如果使用阿里云/腾讯云/华为云等）

在云控制台安全组中添加入站规则：

| 协议 | 端口 | 来源 | 说明 |
|------|------|------|------|
| TCP | 80 | 0.0.0.0/0 | 主应用 |
| TCP | 3000 | 你的IP | Grafana |
| TCP | 9090 | 你的IP | Prometheus |
| TCP | 9443 | 你的IP | Portainer |
| TCP | 8848 | 你的IP | Nacos |
| TCP | 9001 | 你的IP | MinIO |
| TCP | 15672 | 你的IP | RabbitMQ |

---

## 七、启动基础服务

### 7.1 进入项目目录

```powershell
cd C:\software-group
```

### 7.2 启动所有服务

```powershell
docker compose up -d
```

### 7.3 等待服务启动

首次启动需要下载镜像，约 5-10 分钟。

```powershell
# 检查服务状态
docker compose ps
```

### 7.4 查看日志

```powershell
# 查看所有服务日志
docker compose logs

# 查看指定服务日志
docker compose logs nginx
docker compose logs app
docker compose logs mysql
docker compose logs nacos
```

---

## 八、验证基础部署

### 8.1 检查服务状态

```powershell
docker compose ps
```

所有服务应显示 `Up` 状态。

### 8.2 访问应用

| 服务 | 地址 | 说明 |
|------|------|------|
| **主应用** | http://服务器IP/ | 登录页面 |
| **Nacos** | http://服务器IP:8848/nacos | 当前认证配置见 OPS-02，生产启用认证后使用受控凭据 |
| **MinIO** | http://服务器IP:9001 | 使用受控凭据 |
| **RabbitMQ** | http://服务器IP:15672 | 使用受控凭据 |
| **Sentinel** | http://服务器IP:8858 | 无默认账号 |

### 8.3 默认登录账号

| 账号 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | 由运维受控创建 | 不提供仓库内默认密码 |
| 成员 | 由业务流程创建 | 不提供仓库内默认密码 |

---

## 九、生产级优化（监控/日志/运维）

> 基础服务运行正常后，执行以下配置以满足生产环境需求。

### 9.1 创建监控配置文件

#### 9.1.1 创建 Prometheus 配置目录和文件

```powershell
cd C:\software-group
mkdir -p prometheus grafana/provisioning/datasources loki promtail
```

```yaml
# prometheus/prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  # Spring Boot 微服务（需要 actuator/prometheus 端点）
  - job_name: 'microservices'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets:
          - 'file-service:8081'
          - 'user-service:8082'
          - 'activity-service:8084'
          - 'project-award-service:8085'
          - 'hr-service:8089'
          - 'monitor-service:8086'
          - 'content-service:8087'
          - 'ai-service:8094'
        labels:
          group: 'software-group'

  # Prometheus 自身
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  # Nginx（需要 nginx-vts 模块，如未安装可移除此job）
  # 如需监控Nginx，需在nginx.conf中添加vhost_traffic_status模块
  # 或使用nginx-prometheus-exporter作为替代方案
  # - job_name: 'nginx'
  #   static_configs:
  #     - targets: ['nginx:80']
```

#### 9.1.2 创建 Grafana Loki 数据源配置

```yaml
# grafana/provisioning/datasources/loki.yml
apiVersion: 1

datasources:
  - name: Loki
    type: loki
    access: proxy
    url: http://loki:3100
    jsonData:
      httpHeaderName1: "X-Scope-OrgID"
      httpHeaderValue1: "1"
    isDefault: true
    editable: false
```

#### 9.1.3 创建 Loki 配置

```yaml
# loki/loki-config.yml
auth_enabled: false

server:
  http_listen_port: 3100

ingester:
  lifecycler:
    address: 127.0.0.1
  ring:
    kvstore:
      store: inmemory
    replication_factor: 1
  chunk_idle_period: 15m
  max_transfer_retries: 0

schema_config:
  configs:
    - from: 2026-01-01
      store: boltdb
      object_store: filesystem
      schema: v11
      index:
        prefix: index_
        period: 168h

storage_config:
  boltdb:
    directory: /loki/index
  filesystem:
    directory: /loki/chunks

limits_config:
  enforce_metric_name: false
  reject_old_samples: true
  reject_old_samples_max_age: 168h
```

#### 9.1.4 创建 Promtail 配置（Linux only）

> **注意**：Promtail 不支持 Windows，Windows 用户请跳过此步骤。

```yaml
# promtail/promtail-config.yml
server:
  http_listen_port: 9080
  grpc_listen_port: 0

positions:
  filename: /tmp/positions.yaml

clients:
  - url: http://loki:3100/loki/api/v1/push

scrape_configs:
  - job_name: docker
    docker targets:
      - /var/lib/docker/containers/*.log
    pipeline_stages:
      - json:
          expressions:
            log: log
            stream: stream
      - labels:
          stream:
          container_name:
      - multiline:
          firstline: ^\d{4}-\d{2}-\d{2}
          max_wait_time: 3s
```

### 9.2 创建 docker-compose.monitoring.yml

```yaml
# docker-compose.monitoring.yml
# 监控服务：Prometheus + Grafana

services:
  # ========== Prometheus（指标采集）==========
  prometheus:
    image: prom/prometheus:latest
    container_name: sg_prometheus
    restart: always
    ports:
      - "${PROMETHEUS_PORT:-9090}:9090"
    volumes:
      - ./prometheus/prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - prometheus_data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--storage.tsdb.retention.time=15d'
      - '--web.enable-lifecycle'
    networks:
      - software-group-net
    healthcheck:
      test: ["CMD", "wget", "-q", "--spider", "http://localhost:9090/-/healthy"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 30s

  # ========== Grafana（可视化仪表盘）==========
  grafana:
    image: grafana/grafana:latest
    container_name: sg_grafana
    restart: always
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_USER=admin
      - GF_SECURITY_ADMIN_PASSWORD=${GRAFANA_PASSWORD:?GRAFANA_PASSWORD is required}
      - GF_USERS_ALLOW_SIGN_UP=false
      - GF_INSTALL_PLUGINS=grafana-piechart-panel
    volumes:
      - grafana_data:/var/lib/grafana
      - ./grafana/provisioning:/etc/grafana/provisioning:ro
    networks:
      - software-group-net
    depends_on:
      - prometheus
    healthcheck:
      test: ["CMD", "wget", "-q", "--spider", "http://localhost:3000/api/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 30s

volumes:
  prometheus_data:
  grafana_data:

networks:
  software-group-net:
    external: true
```

### 9.3 创建 docker-compose.logging.yml

> **Windows 说明**：Promtail 不支持 Windows，仅保留 Loki 服务，容器日志通过 Docker 自身日志驱动收集。

```yaml
# docker-compose.logging.yml
# 日志服务：Loki（Windows简化版，不含Promtail）

services:
  # ========== Loki（日志存储）==========
  loki:
    image: grafana/loki:latest
    container_name: sg_loki
    restart: always
    ports:
      - "${LOKI_PORT:-3100}:3100"
    volumes:
      - ./loki/loki-config.yml:/etc/loki/local-config.yaml:ro
      - loki_data:/loki
    command: -config.file=/etc/loki/local-config.yaml
    networks:
      - software-group-net
    healthcheck:
      test: ["CMD", "wget", "-q", "--spider", "http://localhost:3100/ready"]
      interval: 30s
      timeout: 10s
      retries: 3

volumes:
  loki_data:

networks:
  software-group-net:
    external: true
```

### 9.4 创建 docker-compose.ops.yml

> **Windows 说明**：使用 `//./pipe/docker_engine` 代替 `/var/run/docker.sock`。

```yaml
# docker-compose.ops.yml
# 运维工具：Portainer

services:
  # ========== Portainer（Docker 可视化管理）==========
  portainer:
    image: portainer/portainer:latest
    container_name: sg_portainer
    restart: always
    ports:
      - "${PORTAINER_PORT:-9443}:9443"
      - "${PORTAINER_AGENT_PORT:-9001}:9001"
    volumes:
      - //./pipe/docker_engine:/var/run/docker.sock
      - portainer_data:/data
    networks:
      - software-group-net
    command: --http-enabled
    # 注意：Portainer 默认健康检查端口是 9000，但 Web UI 在 9443
    healthcheck:
      test: ["CMD", "wget", "-q", "--spider", "http://localhost:9443/api/system/status"]
      interval: 30s
      timeout: 10s
      retries: 3

volumes:
  portainer_data:

networks:
  software-group-net:
    external: true
```

### 9.5 启动监控/日志/运维服务

```powershell
# 启动监控服务
docker compose -f docker-compose.monitoring.yml up -d

# 启动日志服务
docker compose -f docker-compose.logging.yml up -d

# 启动运维工具
docker compose -f docker-compose.ops.yml up -d
```

### 9.6 验证监控/日志/运维服务

```powershell
# 验证 Prometheus
curl http://localhost:9090/api/v1/status/config

# 验证 Grafana
curl http://localhost:3000/api/health

# 验证 Loki
curl http://localhost:3100/ready

# 查看所有服务状态
docker compose ps
docker compose -f docker-compose.monitoring.yml ps
docker compose -f docker-compose.logging.yml ps
docker compose -f docker-compose.ops.yml ps
```

### 9.7 访问运维界面

| 服务 | 地址 | 说明 |
|------|------|------|
| **Grafana** | http://服务器IP:3000 | 使用私有环境文件中的受控凭据 |
| **Prometheus** | http://服务器IP:9090 | 指标查询 |
| **Portainer** | https://服务器IP:9443 | Docker Web管理界面 |

### 9.8 Grafana 添加 Loki 日志源

1. 登录 Grafana (http://服务器IP:3000)
2. 点击 **Configuration** → **Data Sources**
3. 点击 **Add data source**
4. 选择 **Loki**
5. URL 填写 `http://loki:3100`
6. 点击 **Save & Test**

查询日志示例：
```
{service="user-service"}
{service="user-service"} |= "ERROR"
```

---

## 十、Docker Compose 配置优化

> 为所有服务添加健康检查、重启策略、日志轮转等生产级配置。

### 10.1 日志轮转配置（daemon.json）

修改 `C:\ProgramData\docker\config\daemon.json`：

```json
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m",
    "max-file": "3"
  }
}
```

修改后重启：

```powershell
Restart-Service Docker
```

### 10.2 资源限制查看（Windows 限制）

> **注意**：`deploy.resources` 仅在 Docker Swarm 模式下生效，Docker Compose 在 Windows 上会忽略此配置。

Windows 容器资源限制需要在创建容器时指定：

```powershell
# 查看当前容器资源使用
docker stats
```

### 10.3 容器健康检查说明

所有微服务已配置 `/actuator/health` 健康检查：
- file-service: http://localhost:8081/actuator/health
- user-service: http://localhost:8082/actuator/health
- activity-service: http://localhost:8084/actuator/health
- project-award-service: http://localhost:8085/actuator/health
- hr-service: http://localhost:8089/actuator/health
- monitor-service: http://localhost:8086/actuator/health
- content-service: http://localhost:8087/actuator/health
- ai-service: http://localhost:8094/actuator/health

---

## 十一、日常运维

### 11.1 常用命令

```powershell
# 进入项目目录
cd C:\software-group

# 停止服务
docker compose stop

# 启动服务
docker compose start

# 重启服务
docker compose restart

# 更新部署（代码更新后）
git pull
docker compose up -d --build

# 同时更新监控/日志/运维服务
docker compose -f docker-compose.monitoring.yml up -d --build
docker compose -f docker-compose.logging.yml up -d --build
docker compose -f docker-compose.ops.yml up -d --build

# 查看资源使用
docker stats

# 查看所有服务状态
docker compose ps
```

### 11.2 日志查看

```powershell
# 实时查看所有日志
docker compose logs -f

# 查看指定服务日志
docker compose logs -f nginx
docker compose logs -f app
docker compose logs -f mysql

# 查看监控服务日志
docker compose -f docker-compose.monitoring.yml logs -f prometheus
docker compose -f docker-compose.monitoring.yml logs -f grafana

# 查看最近100行日志
docker compose logs --tail=100
```

### 11.3 完全清理

```powershell
# 停止并删除容器（保留数据卷）
docker compose down

# 停止并删除所有容器和数据卷（数据会丢失！）
docker compose down -v
docker compose -f docker-compose.monitoring.yml down -v
docker compose -f docker-compose.logging.yml down -v
docker compose -f docker-compose.ops.yml down -v

# 删除未使用的镜像
docker image prune -a
```

---

## 十二、备份与恢复

### 12.1 备份数据库

```powershell
cd C:\software-group

# 创建备份目录
mkdir backup

# 备份 MySQL
docker exec -it sg_mysql mysqldump -uroot -p --result-file=/tmp/software_group.sql software_group
docker cp sg_mysql:/tmp/software_group.sql backup\backup.sql
```

### 12.2 恢复数据库

```powershell
cd C:\software-group
docker cp backup\backup.sql sg_mysql:/tmp/restore.sql
docker exec -it sg_mysql mysql -uroot -p software_group
```

进入 MySQL 后执行 `SOURCE /tmp/restore.sql;`。交互输入密码，不写入命令参数或终端历史。

### 12.3 备份数据卷

```powershell
# 查看数据卷
docker volume ls

# 备份 MySQL 数据卷
docker run --rm -v software_group_mysql_data:/data -v C:\software-group\backup:/backup alpine tar czf /backup/mysql_data_$(Get-Date -Format "yyyyMMdd_HHmmss").tar.gz /data
```

---

## 十三、故障排查

### 13.1 常见问题

| 问题 | 原因 | 解决方法 |
|------|------|----------|
| Docker 无法启动 | 容器功能未启用 | 检查步骤 1.2，重新启用容器功能 |
| 容器启动失败 | 配置错误或端口占用 | `docker compose logs 服务名` 查看日志 |
| MySQL 连接失败 | 密码错误或未就绪 | 等待 30 秒，或检查 .env 密码 |
| Nacos unhealthy | 启动较慢 | 等待 1-2 分钟后再检查 |
| 访问 localhost 404 | nginx 配置问题 | `docker compose logs nginx` 查看 |
| 端口被占用 | 端口冲突 | 修改 .env 中端口配置 |
| 80 端口被占用 | IIS 未停止 | `Stop-Service W3SVC` |
| Prometheus 无数据 | 微服务 actuator 未启用 | 检查微服务是否暴露 /actuator/prometheus 端点 |
| Grafana 无日志 | Loki 未启动或数据源配置错误 | 检查 Loki 容器状态和 Grafana Loki 数据源 |
| Portainer 无法访问 | 端口 9443 未开放 | 检查防火墙规则 |

### 13.2 快速诊断命令

```powershell
# 1. 检查 Docker 是否运行
docker version

# 2. 检查所有服务状态
docker compose ps
docker compose -f docker-compose.monitoring.yml ps
docker compose -f docker-compose.logging.yml ps
docker compose -f docker-compose.ops.yml ps

# 3. 检查端口占用
netstat -ano | findstr ":80"
netstat -ano | findstr ":8080"

# 4. 检查容器网络
docker network ls
docker network inspect software_group_software-group-net

# 5. 检查磁盘空间
Get-PSDrive C

# 6. 查看容器资源使用
docker stats

# 7. 检查健康检查状态
docker inspect --format='{{.State.Health.Status}}' sg_mysql
docker inspect --format='{{.State.Health.Status}}' sg_redis
```

---

## 十四、安全建议

### 14.1 必做安全措施

1. **修改所有默认密码**
   - [ ] MySQL root 密码
   - [ ] Redis 密码
   - [ ] RabbitMQ 密码
   - [ ] JWT Secret
   - [ ] MinIO 密码
   - [ ] Grafana 密码
   - [ ] Nacos 密码（生产环境）

2. **限制端口访问**
   - 数据库（3306）不对外开放
   - Redis（6379）不对外开放
   - Nacos/Sentinel 仅对运维 IP 开放
   - Grafana/Prometheus 仅对运维 IP 开放
   - Portainer 仅对运维 IP 开放

3. **定期备份**
   - 数据库每日备份
   - 配置文件备份
   - 数据卷定期备份

### 14.2 生产环境建议

1. **配置 HTTPS**（当前版本生产必需；详见 19.10）
   - 内网 IP 使用受控内部 CA 签发的 IP SAN 证书，并向授权客户端分发 CA 证书
   - 将来使用域名时，可使用公开 CA 证书

2. **启用防火墙**
   - 只开放必要的 80/443 端口
   - 数据库、Redis 等使用内网访问

3. **监控告警**
   - 在 Grafana 中配置告警规则
   - 设置邮件/钉钉/飞书通知

---

## 十五、服务端口汇总

### 15.1 应用服务

| 服务 | 端口 | 访问范围 |
|------|------|----------|
| app (旧 WAR) | 8080 | 仅内网 |
| file-service | 8081 | 仅内网 |
| user-service | 8082 | 仅内网 |
| activity-service | 8084 | 仅内网 |
| project-award-service | 8085 | 仅内网 |
| hr-service | 8089 | 仅内网 |
| monitor-service | 8086 | 仅内网 |
| content-service | 8087 | 仅内网 |
| ai-service | 8094 | 仅内网 |

### 15.2 基础设施服务

| 服务 | 端口 | 访问范围 |
|------|------|----------|
| nginx | 80 | 对外 |
| mysql | 3307 | 仅内网 |
| redis | 6379 | 仅内网 |
| rabbitmq | 5672/15672 | 运维/内网 |
| nacos | 8848/9848 | 运维 |
| sentinel | 8858 | 运维 |
| minio | 9000/9001 | 运维 |

### 15.3 运维服务

| 服务 | 端口 | 访问范围 |
|------|------|----------|
| prometheus | 9090 | 运维 |
| grafana | 3000 | 运维 |
| loki | 3100 | 运维 |
| portainer | 9443 | 运维 |

---

## 十六、完整文件结构

```
C:\software-group\
├── docker-compose.yml              # 基础服务（MySQL/Redis/微服务/Nginx等）
├── docker-compose.monitoring.yml   # 监控服务（Prometheus/Grafana）
├── docker-compose.logging.yml      # 日志服务（Loki）
├── docker-compose.ops.yml          # 运维工具（Portainer）
├── .env                            # 环境变量配置
├── Dockerfile.tomcat              # 旧WAR构建文件
├── sql/                            # 数据库初始化脚本
├── nginx/nginx.conf                # Nginx配置
├── static/                         # 静态资源
├── prometheus/
│   └── prometheus.yml             # Prometheus配置
├── grafana/
│   └── provisioning/
│       └── datasources/
│           └── loki.yml           # Grafana Loki数据源
├── loki/
│   └── loki-config.yml            # Loki配置
└── backup/                        # 备份目录（手动创建）
```

---

## 十七、相关文档

| 文档 | 位置 |
|------|------|
| 微服务迁移计划 | docs/微服务迁移计划.md |
| 阶段一 Docker容器化实施计划 | docs/阶段一_Docker容器化实施计划.md |
| 阶段二 中间件引入实施计划 | docs/阶段二_中间件引入实施计划.md |
| 阶段三 微服务拆分实施计划 | docs/阶段三_微服务拆分实施计划.md |
| 项目说明文档 | docs/README.md |
| 开发文档 | docs/development.md |

---

**部署过程中如有疑问，请联系开发者。**

---

## 十八、Linux VM 部署路线（新增：Windows 主机无法跑 Linux 镜像的替代方案）

> 追加日期：2026-09-08。以下是在**实机部署**过程中发现原手册假设不成立后，补充的路线与进度记录。**原手册 一~十七章 为原始内容，继续有效**。区别仅在：原 §7 `docker compose up -d` 的执行环境由"Windows 主机 Docker"改为"本机 Hyper-V 里的 Linux(Ubuntu)虚拟机"，其余（端口、防火墙、.env、目录、备份）基本不变。

### 18.1 为什么必须改用 Linux VM（实机验证结论）

- 本机 Docker 为 **Docker EE（经 DockerMsftProvider 安装）＝ Windows 容器模式**：`docker version -f '{{.Server.Os}}'` 返回 `windows`。
- `DockerCli.exe -SwitchDaemon`（原手册 §2.5）**不存在** → 该命令是 Docker Desktop 专属，而 Windows Server 不支持 Docker Desktop。
- 原手册整套 compose（mysql/redis/nginx/nacos/minio + 8 微服务 + 旧 WAR + 监控）全部是 **Linux 镜像**，Windows 容器下无法运行；且 MySQL/Redis/Nacos/MinIO/Nginx 等均无官方 Windows 容器镜像，"改成 Windows 镜像"不现实（工作量大且核心组件无解）。
- **结论**：在本机 Hyper-V 中开一台 Ubuntu 虚拟机，VM 内安装 Docker Engine，把整套 compose 原样搬进 VM 运行。Windows 主机那套 Docker 弃用、不再启动。

### 18.2 服务器环境事实记录

| 项 | 值 |
|----|----|
| 项目目录 | `C:\software-group`（服务器无 D 盘；本手册路径已全部由 D 改 C） |
| Docker | Docker EE，`Server.Os = windows`（仅 Windows 容器） |
| Hyper-V | 已安装且可用：`Get-WindowsFeature Hyper-V`=Installed、`HypervisorPresent`=True、`vmms`=Running |
| 网络 | 校园网（可上网；拉 Docker Hub / Maven 可能慢，必要时配国内镜像） |
| 内存 | ≥16GB（VM 分 8GB，动态 4–8） |

### 18.3 已完成的部署步骤（进度记录）

1. **§2.4 daemon.json 事故**：daemon.json 含 `storage-opts`（Linux 专属）导致 Docker 无法重启 → 已删除该文件恢复。**以后重建 daemon.json 必须去掉 storage-opts；§2.4 可选，可直接跳过。**
2. **§2.5 判定**：`DockerCli.exe` 不存在，确认主机只能跑 Windows 容器。
3. **Hyper-V 确认可用**：功能 Installed + HypervisorPresent=True + vmms Running。
4. **创建 Linux VM（管理员 PowerShell）**：
   - 交换机（桥接，VM 直接拿校园网 IP）：`New-VMSwitch -Name "External" -NetAdapterName <物理网卡名> -AllowManagementOS $true`
   - VM：`New-VM -Name "sg-linux" -MemoryStartupBytes 8GB -Generation 2 -NewVHDPath "C:\software-group\vm\sg-linux.vhdx" -NewVHDSizeBytes 60GB -SwitchName "External"`
   - CPU/内存：`Set-VM -Name "sg-linux" -ProcessorCount 4`；`Set-VMMemory -VMName "sg-linux" -DynamicMemoryEnabled $true -MinimumBytes 4GB -MaximumBytes 8GB`
   - **踩坑修复（重要）**：
     - VM 默认**没有 DVD 光驱**，`Set-VMDvdDrive` 无效 → 用 `Add-VMDvdDrive -VMName sg-linux -Path "C:\ubuntu-24.04.4-live-server-amd64.iso"`
     - Gen2 安全启动模板默认 `MicrosoftWindows` 会拒绝 Ubuntu → `Set-VMFirmware -VMName sg-linux -EnableSecureBoot Off`
     - DVD 设第一启动项：`$dvd=Get-VMDvdDrive sg-linux; Set-VMFirmware sg-linux -FirstBootDevice $dvd`
   - ISO：`C:\ubuntu-24.04.4-live-server-amd64.iso`（3.17GB，下载完好）
5. **Ubuntu 24.04.4 live-server 安装完成**：语言可选中文（简体）；键盘 English/US；存储 60GB 整盘；**勾选 Install OpenSSH server**；用户名/密码由使用者设定。
   - ⚠️ 若重启后又回到安装界面（DVD 还挂着）：`Stop-VM sg-linux -Force; Set-VMDvdDrive sg-linux -Path $null; Start-VM sg-linux`
6. **当前断点（2026-09-08）**：VM 内 Ubuntu **已安装完成**，停在**登录界面（输入用户名 + 密码）**，尚未登录。

### 18.4 下一步（段 B / C / D）

**B. VM 内安装 Docker Engine（Ubuntu 登录后执行）**
```bash

sudo systemctl enable --now docker
sudo usermod -aG docker $USER   # 让当前用户免 sudo 用 docker，重新登录生效
docker --version
docker compose version
```
> 校园网拉 Docker Hub 慢时再配 registry 镜像（如 `docker.m.daocloud.io`），到时再给命令。

**C. 代码 + 关键文件进 VM（建议放 /opt/software-group）**
```bash
sudo mkdir -p /opt/software-group && sudo chown $USER:$USER /opt/software-group
```
从 Windows 端 scp 或 git clone 仓库。**必须补上 3 个 git 不追踪的文件**：
1. `target/software-group.war` —— 由 `mvn -f pom-war.xml clean package` 生成（根 `pom.xml` 已是微服务父工程，默认不打 WAR）
2. `src/main/resources/config.local.properties` —— **必须是占位符格式**（`db.url=jdbc:mysql://${DB_HOST:-mysql}...`），`Dockerfile.tomcat` 构建时 COPY 并 envsubst 替换
3. `.env` —— 注意 `docker-compose.yml:558` 的 app(WAR) 用 `${MYSQL_PASSWORD}`，微服务用 `${MYSQL_ROOT_PASSWORD}`；**两个变量都要在 .env 里设置且密码一致**，否则 WAR 连不上库而微服务正常（现象隐蔽）

**D. 启动栈（在 /opt/software-group 内）**
```bash
cd /opt/software-group
docker compose up -d          # 首次构建镜像 + 拉镜像较久
docker compose ps
docker compose logs -f app mysql nginx   # 排错
```
浏览器访问 VM 的 IP（80 端口）。

### 18.5 部署进度快速定位（供下次读取本文件）

> ⚠️ **本节已过时（2026-09-28）**：下面写的"下一条要做的动作"（装 Docker）早已完成，且后续又推进了旧 WAR 修复与验证。**请直接读 §十九**，它以最新状态为准。18.5 保留作历史记录。

- 手册整体状态：一~十七 = 原始部署手册（仍用）；十八 = 本节（Linux VM 路线补充 + 进度）。
- 服务器路径 `C:\software-group`；VM 名 `sg-linux`；Ubuntu ISO 在 `C:\ubuntu-24.04.4-live-server-amd64.iso`。
- **下一条要做的动作**：VM 内 Ubuntu **登录** → 段 B 装 Docker（见 18.4 B）。
- 待使用者提供的值：VM 用户名、VM IP、`.env` 密码（MYSQL_ROOT_PASSWORD 与 MYSQL_PASSWORD 一致）。

---
