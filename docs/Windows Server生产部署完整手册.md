# Windows Server 生产部署完整手册

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

1. **配置 HTTPS**（可选但强烈推荐）
   - 使用 Nginx 反向代理 + Let's Encrypt 免费证书
   - 或使用云服务商提供的 SSL 证书

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

## 十九、微服务部署进度与后续步骤（追加：2026-09-28）

> **🔴 当前断点（2026-10-02 更新）：步骤 2 已完成，当前在步骤 3「构建 8 个微服务」。**
> 步骤 2 长期卡在 nacos 起不来，根因是**内嵌 Derby 在 Hyper-V 慢盘上首次建库 >30s，越过 Derby 默认 30s 登录超时**；
> 已改为 **nacos 持久化切 MySQL**（详见 §19.4 步骤 2 的「✅ 已解决」与「步骤 2 收尾改动」），nacos 现为 `(healthy)` + `200`。
> **下一步**：按 §19.4 步骤 3 构建 8 个微服务镜像（`COMPOSE_PARALLEL_LIMIT=2`，最慢一步，首次 20~40 分钟）。
>
> 追加日期：2026-09-28。**本节是当前唯一有效的进度断点**，§18.5 的"下一条动作"已过时，**以本节为准**。
> 详细修复过程与逐条证据记录在 `docs/旧WAR_Tomcat10修复与部署方案.md`（同目录）。
> 本节结构：19.1 环境事实 → 19.2 已完成 → 19.3 已知待办 → 19.4 接下来（步骤 0-6）→ 19.5 风险 → 19.6 快速定位。

### 19.1 环境事实（2026-09-28 实测）

| 项 | 值 |
|---|---|
| 部署执行环境 | Windows Server 主机上的 **Hyper-V Ubuntu VM**，VM 名 `sg-linux` |
| VM IP | `192.168.0.15` |
| 项目目录 | VM 内 `/opt/software-group` |
| 传输链路 | 开发机 → 向日葵 → Windows Server `C:\software-group\` → scp → VM |
| 当前访问入口 | `http://192.168.0.15:8080/software-group/...`（旧 WAR 直连，**不含 nginx**） |

### 19.2 已完成（截至 2026-09-28）

#### ① 旧 WAR 的 jakarta 迁移遗留 —— 已全部修复并验证跑通

背景：2026-08-02 那次 jakarta 迁移（commit `4ad3474`）只改了 Java 源码和根 `pom.xml`，WAR 的**运行环境三处没跟上**，导致 `app` 容器必然起不来。

| 处 | 修复前 | 修复后 |
|---|---|---|
| `Dockerfile.tomcat` | `FROM tomcat:9-jdk11` | `FROM tomcat:10.1-jdk17-temurin` |
| `pom-war.xml` | `javax.servlet:jstl:1.2` | `jakarta.servlet.jsp.jstl:*-api:3.0.2` + `org.glassfish.web:jakarta.servlet.jsp.jstl:3.0.1` |
| 86 个 JSP / 122 处 taglib URI | `http://java.sun.com/jsp/jstl/*` | `jakarta.tags.core` / `jakarta.tags.fmt` |

同批还修掉（原计划阶段 B）：

| 文件 | 改动 |
|---|---|
| `src/main/java/config/Config.java` | 新增**运行期** `${VAR:-default}` 解析，兜住此前没被替换的 `REDIS_*` / `RABBITMQ_*` / `AI_API_SECRET` 等占位符 |
| `docker-compose.yml` | `app` 的 `build.args` 改到 `environment:`（配合上一条）；`DB_PASSWORD` 变量名统一为 `MYSQL_ROOT_PASSWORD` |
| `.dockerignore` | `target/` → `target/*`，让 WAR 能进构建上下文 |
| `src/main/webapp/WEB-INF/web.xml` | 描述符升到 jakarta `web-app_6_0.xsd` + `version="6.0"` |

**验证结果（阶段 C 通过，2026-09-23）**：容器内 `Starting Servlet engine: [Apache Tomcat/10.1.60]`；历史测试账号登录后 **302 → /index.jsp**；会话落 Redis（`session:*` 前缀，`dbsize=1`）；DB **45 张表**齐全；Windows Server 浏览器可访问 `192.168.0.15:8080`。历史凭据已弃用，生产凭据不得写入手册。

#### ② 当时排查过的两个「非缺陷」（记下来，避免以后重复排查）

- **`/software-group/admin/index.jsp` 报 403** —— 是 `AuthFilter` 的**正确行为**：未登录是 302，登录了但角色非 ADMIN 才是 403。用 `admin` 账号登录后正常。
- **日志里 25 条 `activity_group doesn't exist`** —— 发生在零表窗口期，由 `GroupMuteScheduler` 每 60 秒探测产生，属**历史错误**；现在计数不再增长，表也已存在。

#### ③ 当前容器实况

`docker ps` 只有 4 个容器，且 `docker ps -a` 输出与之**完全一致** → 其余服务**从未创建过**（不是"停了"）：

```
sg_app        software-group-app              Up 4 days
sg_rabbitmq   rabbitmq:3-management-alpine    Up 5 days (healthy)
sg_redis      redis:7-alpine                  Up 5 days (healthy)
sg_mysql      mysql:8                         Up 5 days (healthy)
```

镜像仓库里**中间件已齐**（`nacos/nacos-server:v2.2.3`、`bladex/sentinel-dashboard:latest`、`minio/minio:latest`、`nginx:alpine`），但 **8 个微服务一个镜像都没有**（compose 里全是 `build:`，必须现场构建）。构建所需的 `maven:3.9-eclipse-temurin-17` 与 `eclipse-temurin:17-jre-alpine` 两个基础镜像也**尚未拉取**。

> 待解疑点：本机镜像列表里**没有 `tomcat:10.1-*`**，但运行中的 `sg_app` 自报 Tomcat 10.1.60。下次可用 `docker history software-group-app --no-trunc | tail -5` 看基础层。

### 19.3 已知待办（均未修；不阻塞当前部署，但影响「可重复部署」）

| # | 问题 | 影响 |
|---|---|---|
| 1 | `sql/` 目录按**字母序**执行，而 `db_users.sql` 排在 `software_group.sql` **之前**，且含表级 `GRANT` → 首条即 `ERROR 1146`，**mysql entrypoint 中止整个初始化**；`restart: always` 带非空 datadir 重启后初始化**永久跳过、表数 0** | **全新部署必复现**；当前环境因是既有数据卷，没踩到 |
| 2 | `sql/db_users.sql` 引用了 5 个**单数**表名（`resume`/`resume_award`/`resume_education`/`resume_project`/`resume_skill`），schema 实际是**复数** | 授权不生效。但 compose 里 9 个 `DB_USER` **全是 `root`**，这些 `*_svc` 账号**根本没被使用**，故无实际影响 |
| 3 | `mysql` healthcheck 缺 `start_period` | 冷启动可能被误判不健康 |
| 4 | `mysql:8` 是浮动 tag（已漂到 8.4.11） | 不可复现 |
| 5 | `src/main/webapp/META-INF/context.xml` 里的 `<Context path="/"/>` | 死代码（Tomcat 忽略 WAR 内的 `path` 属性），可删 |
| 6 | `sql/software_group.sql` 混了坏 UTF-8（部分 COMMENT / 种子行） | 显示乱码 |
| 7 | 108 个测试失败（疑似本机 JDK 23 与 Mockito 4.11 不兼容，**未验证**） | 不阻塞部署 |
| 8 | **96 个改动尚未 commit** —— 阶段 B 的全部产出目前**只存在于工作区** | **风险最高**，一次误操作即全丢 |
| 9 | `.env` **不在 `.gitignore` 里** | 明文 `MYSQL_ROOT_PASSWORD` 有被误提交的风险 |

可回收空间：`docker rmi tomcat:9-jdk11`（652MB）+ `docker rmi nacos/nacos-server:v2.2.0`（1.63GB）≈ **2.28GB**。

### 19.4 接下来：部署 8 个微服务（步骤 0 → 6）

> **总前提：绝对不要执行 `docker compose down -v`。** 数据卷一删就会触发待办 #1，45 张表全没。

#### 执行进度（每完成一步在此打勾）

| 步骤 | 状态 | 完成日期 | 结果摘要 |
|---|---|---|---|
| 步骤 0 确认源码与资源 | ✅ 已完成 | 2026-09-29 | 8 个 `*-service/` + `common/` + `nginx/` + `static/` 全在，14 项关键文件零 `MISS`；VM 内存上限由 8GB 提到 **12GB**；清掉 2 个僵尸镜像后磁盘可用 **≥15G** |
| 步骤 1 修 `sql/` 文件名顺序（可选） | ✅ 已完成 | 2026-09-29 | 10 处表名全改复数；`db_users.sql` → `zz_db_users.sql`（排序置后）；`verify-infrastructure.sh` 同步更新，md5 与本地复现值字节级一致 |
| 步骤 1.5 行尾规整（CRLF→LF） | ✅ 已完成 | 2026-09-29 | VM 整棵树是 CRLF（仓库是 LF，传输链路转的）。已修 `scripts/*.sh`（尺寸 6619/6313/7848 全对）与 **8 个 `*-service/Dockerfile`**（避免 `HEALTHCHECK ... \` 续行被 CR 打断而搞崩步骤 3）；`Dockerfile.tomcat` 本就是 LF。`.env` **实测 compose 会剥掉 CR**（文件 11 字节 → 容器内 10 字节），故 CRLF 无害，保持不动 |
| 步骤 2 起中间件层（nacos/sentinel/minio） | ✅ 已完成 | 2026-10-02 | sentinel ✅ 200、minio ✅ 200、**nacos ✅ `(healthy)` + 200**。nacos 长期卡死的根因＝**内嵌 Derby 在 Hyper-V 动态扩展 VHDX（慢盘）上首次建库 >30s，越过 Derby 默认 30s 登录超时**（`SQLTimeoutException: Login timeout exceeded`），建库失败留下缺 `service.properties` 的残破 `derby-data` → 崩溃循环。调超时参数 / 删目录 / 重建 volume 均无效，**最终改为 nacos 持久化切 MySQL 后一次起通**（改动见 §19.4「步骤 2 收尾改动」） |
| 步骤 3 构建 8 个微服务 | ✅ 已完成 | 2026-10-02 | 8 个镜像齐全（`docker images \| grep service \| wc -l` = **8**），SIZE **467–511MB**（=`eclipse-temurin:17-jre-alpine`≈180MB + Spring Boot fat jar），JAR 确已打入。本次 `docker compose build` 全命中缓存（`Building 8/8`、每条 `Built 0.0s`）——即**镜像已存在且与当前源码一致**，无须真编译 |
| 步骤 4 启动 8 个微服务 | ✅ 已完成 | 2026-10-07 | 8 个容器 `Up (healthy)`，Nacos 服务列表 **`count=8`**（`hr/user/content/file/monitor/ai/activity/project-award` 全齐）。三个坑逐一定性并修复：① healthcheck 误用 `curl`（镜像里只有 wget）→ 改 `wget -q --spider`+`127.0.0.1`；② **真根因：5 个服务 `spring.application.name` 只写在被忽略的 `bootstrap.yml`、运行期为空 → `NacosServiceRegistry` 打 `No service to register for nacos client...` 直接跳过注册**（**并不是 server-addr**）；③ 用 `docker-compose.override.yml` 补 `environment` 被本机 compose 拒（`additional properties 'environment' not allowed`）。最终改法：在 base `docker-compose.yml` 的 5 个服务各插一行 `SPRING_APPLICATION_NAME: <svc>-service`（详见下方「步骤 4 修复 A / B」） |
| 步骤 5 起 nginx | ✅ 已完成 | 2026-10-07 | `sg_nginx` `Up (healthy)`；四条 curl 全 200（`root`/`login`/`sg-login`/`sg-css`）。两个坑：① 探针 `localhost`→`127.0.0.1`（`nginx:alpine` 内 localhost 解析到 `::1`，而 `listen 80` 仅绑 IPv4）；② **旧站内链双前缀 404** —— 应用链接带 `${contextPath}`=`/software-group/...`，而 `location /` 把 `/` 映射成 `/software-group/` → 二次拼接成 `/software-group/software-group/...`（首页能开、CSS/JS/导航全 404）。修法：**新增独立前缀 location 原样透传** `location /software-group/ { proxy_pass http://tomcat_backend; ... }`（**rewrite 方向错、勿用**），并保留 `location /`（`e2e-smoke-test.sh` 要求 `/` 与 `/login.jsp`=200）。详见 §19.4「步骤 5」 |
| 步骤 6 验收（3 个脚本 + 2 个 curl） | ⬜ 待执行 | — | — |

> 状态用：⬜ 待执行 / 🔵 执行中 / ✅ 已完成 / ❌ 失败待查

**端口与容器名速查**（8083 / 8088 空缺，是设计如此）

| 服务 | 容器名 | 宿主端口 |
|---|---|---|
| file-service | `sg_file_service` | 8081 |
| user-service | `sg_user_service` | 8082 |
| activity-service | `sg_activity_service` | 8084 |
| project-award-service | `sg_project_award_service` | 8085 |
| monitor-service | `sg_monitor_service` | 8086 |
| content-service | `sg_content_service` | 8087 |
| hr-service | `sg_hr_service` | 8089 |
| ai-service | `sg_ai_service` | 8094 |

#### 步骤 0｜确认源码与资源（只读，不改动任何东西）

```bash
cd /opt/software-group
ls -d */ | head -30
ls common/pom.xml user-service/Dockerfile nginx/nginx.conf static/css 2>&1
free -h && df -h /var/lib/docker && nproc
```

- **用途**：确认 8 个 `*-service/`、`common/`、根 `pom.xml`、`nginx/nginx.conf`、`static/{css,js,images}` 都在 VM 上；并核对内存 ≥8GB、`/var/lib/docker` 余量 >20GB。
- **这是目前最大的未知项 —— 源码若不在 VM 上，后续全部免谈。**

#### 步骤 1｜（可选，建议）修 `sql/` 文件名顺序

```bash
cd /opt/software-group/sql && mv db_users.sql zz_db_users.sql && ls
```

- **用途**：让 `software_group.sql` 排在前面（对应待办 #1）。
- 对**本次**部署**不阻塞**（数据卷已存在，初始化根本不会跑），但修了才算"可重复部署"。若 VM 上是 git 仓库用 `git mv`。

#### 步骤 2｜起中间件层

```bash
cd /opt/software-group
docker compose up -d nacos sentinel minio
sleep 60    # Nacos 单机模式首次启动慢，60 秒内显示 (health: starting) 属正常
curl -s -o /dev/null -w 'nacos=%{http_code}\n'    http://localhost:8848/nacos/
curl -s -o /dev/null -w 'sentinel=%{http_code}\n' http://localhost:8858/
curl -s -o /dev/null -w 'minio=%{http_code}\n'    http://localhost:9000/minio/health/live
```

- **预期**：三个都返回 `200`。
- **实测（2026-10-02）**：`sentinel` ✅ `200`、`minio` ✅ `200`、`nacos` ✅ `200`（nacos 已切 MySQL 持久化，见下方「✅ 步骤 2 已解决」）。

---

> ## ✅ 步骤 2 已解决（2026-10-02 记）
>
> **一句话**：nacos 起不来的根因是**内嵌 Derby 在慢盘上建库超时**，调参/删目录/重建 volume 全都治不了；**改成 MySQL 持久化后一次起通**，`(healthy)` + `200`。
> 下方"历史命令（M/N/O、P/Q）"仅作记录，**已不需要再执行**。

### 最终根因（2026-10-02 定性）

| 项 | 内容 |
|----|------|
| 现象 | `sg_nacos` `restart: always` 无限重启；`docker logs` 反复出现 `SQLTimeoutException: Login timeout exceeded`，随后 `... does not contain the expected 'service.properties' file` |
| 主因 | volume `software-group_nacos_data` 落在 **Hyper-V 动态扩展 VHDX** 上，磁盘慢；**内嵌 Derby 首次建库耗时 >35s，越过 Derby 默认 30s 登录超时** → 建库中途失败 |
| 次因（自续） | 失败留下**缺 `service.properties` 的残破 `derby-data`**，之后每次启动 Derby 都判定"上次建库没完成"而拒绝，崩溃循环不止 |
| 已排除 | 内存：`OOMKilled=false`；权限：镜像内进程实测 `uid=0(root)`，坏目录本身即 root 创建 |

### 试过但无效的路（**别重复**）

| 尝试 | 结果 |
|------|------|
| 删 `derby-data`（即旧 M/N/O） | 目录清了，但**下次启动仍在 30s 内剪不断**，重新生成残破目录 → 继续崩 |
| 重建 `software-group_nacos_data` volume（退路 A） | 同上，慢盘这个物理条件没变，复现 |
| `JAVA_OPT_EXT="-Dderby.loginTimeout=120 -Ddb.pool.config.connectionTimeout=60000 -Ddb.pool.config.initializationFailTimeout=60000"` | 参数确实进了 JVM（`grep -c` = 108），**内嵌 Derby 不认这个系统属性**，仍崩（且崩溃间隔反而缩到 2.2s） |

### 生效的解法：nacos 持久化切 MySQL

细节与改动清单见紧接着的 **`#### 步骤 2 收尾改动`**。要点：新建 `nacos_config` 库（12 张表）→ compose 里 nacos 加 `SPRING_DATASOURCE_PLATFORM=mysql` + 6 个 `MYSQL_SERVICE_*` → 加 `depends_on: mysql(service_healthy)`。**结果：`docker compose ps nacos` → `Up (healthy)`，`curl .../nacos/` → `nacos=200`。**

### 历史命令（M/N/O、P/Q —— 已作废，仅供追溯，不要重跑）

<details>
<summary>展开：2026-09-29 的 M/N/O 与 P/Q（Derby 路线的旧操作）</summary>

| 步 | 做了什么 | 命令 |
|----|----------|------|
| **M** | 摘 restart 策略停循环 | `docker update --restart=no sg_nacos` + `docker stop sg_nacos` |
| **N** | 查 volume 内目录 | `docker run --rm --entrypoint ls -v software-group_nacos_data:/data redis:7-alpine -la /data` |
| **O** | 删坏目录 | `docker run --rm --entrypoint rm -v software-group_nacos_data:/data redis:7-alpine -rf /data/derby-data` |
| **P** | 重新起 nacos 并等待 | `docker update --restart=always sg_nacos` + `docker compose up -d nacos` + `sleep 90` + `curl -s -o /dev/null -w 'nacos=%{http_code}\n' http://localhost:8848/nacos/` |
| **Q** | 复查四条 | `docker inspect sg_nacos \| grep -E '"(RestartCount\|Status)"'` + `docker compose exec nacos ls -la /home/nacos/data/derby-data`（期望 `RestartCount` 不再增长、`Status=running`、出现 `service.properties`、`nacos=200`） |

</details>

---

#### 步骤 2 收尾改动（2026-10-02，已完成，供"可重复部署"参考）

> 目的：把 nacos 从内嵌 Derby 切到 **MySQL 持久化**，彻底绕开慢盘建库超时。**新环境部署按本小节做一遍即可，不必再走上面的 Derby 老路。**

**① `docker-compose.yml` 里 nacos 服务的改动**

- **新增** 持久化环境变量（`environment:` 下）：

  ```yaml
        SPRING_DATASOURCE_PLATFORM: mysql
        MYSQL_SERVICE_HOST: mysql
        MYSQL_SERVICE_PORT: 3306
        MYSQL_SERVICE_DB_NAME: nacos_config
        MYSQL_SERVICE_USER: root
MYSQL_SERVICE_PASSWORD: ${MYSQL_ROOT_PASSWORD:?MYSQL_ROOT_PASSWORD is required}
        MYSQL_SERVICE_DB_PARAM: "characterEncoding=utf8&connectTimeout=3000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
  ```

- **新增** 启动门禁（nacos 段末尾）：

  ```yaml
        depends_on:
          mysql:
            condition: service_healthy
  ```

- **删除** 曾经的 `JAVA_OPT_EXT: "-Dderby.loginTimeout=120 ..."`（已验证无效，**别再加回来**）。

**② 建 `nacos_config` 库 + 建表**

- 先建空库（MySQL 已在跑）：

  ```bash
  docker exec sg_mysql mysql -uroot -p"$MYSQL_ROOT_PASSWORD" \
      -e "CREATE DATABASE IF NOT EXISTS nacos_config DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
  ```

- 从 nacos 镜像里导出官方建表脚本（**12 张表**，无 `CREATE DATABASE`/`USE`，需先 `USE nacos_config` 再执行）：

  ```bash
  docker run --rm --entrypoint cat nacos/nacos-server:v2.2.3 \
      /home/nacos/conf/mysql-schema.sql > nacos-mysql-schema.sql   # 实测 10825 字节
  ```

- 导入：

  ```bash
  docker exec -i sg_mysql mysql -uroot -p"$MYSQL_ROOT_PASSWORD" nacos_config \
      < nacos-mysql-schema.sql
  docker exec sg_mysql mysql -uroot -p"$MYSQL_ROOT_PASSWORD" \
      -e "SHOW TABLES IN nacos_config;"        # 期望 12 行
  ```

  > ⚠️ **`nacos-mysql-schema.sql` 放在项目根目录，不要混入业务库初始化文件。** DB-02 起 Compose 不再将 `sql/software_group.sql` 挂载到 MySQL 初始化目录，业务库由独立 Flyway `db-migrate` 任务管理；Nacos 的 12 张表仍须按本节单独导入 `nacos_config`，不能导入 `software_group`。已有业务库首次使用新 Compose 前，先按《业务数据库Flyway迁移说明》核对结构并显式 baseline。

**③ 重建 nacos 容器并验证**

```bash
cd /opt/software-group
docker compose up -d nacos
sleep 60
docker compose ps nacos                                  # 期望 Up ... (healthy)
curl -s -o /dev/null -w 'nacos=%{http_code}\n' http://localhost:8848/nacos/   # 期望 nacos=200
```

**④ 附带说明**

- `software-group_nacos_data` 卷现在**已无实际作用**（配置存在 MySQL 里），可留可删，不必再折腾它。
- 备份/迁移只需带 **`mysql_data` 卷**，nacos 配置随 `nacos_config` 库一起走。

---

#### 步骤 3｜构建 8 个微服务（最慢的一步）

```bash
cd /opt/software-group
COMPOSE_PARALLEL_LIMIT=2 docker compose build \
    file-service user-service activity-service project-award-service \
    hr-service monitor-service content-service ai-service
```

- **用途**：生成 8 个镜像。每个 Dockerfile 内含 **3 次 Maven 构建**（根 pom → `common/` → 本服务），`common` 层有 Docker 缓存可复用。
- **为什么限并发**：VM 只有 **4 核 / 12GB**，而每个 Maven 构建进程约吃 0.8–1.5GB。默认并发会同时跑 4 个 → 叠加已在跑的服务，**极易 OOM**。`COMPOSE_PARALLEL_LIMIT=2` 是折中；若构建中内存吃紧（`free -h` 的 available 掉到 1GB 以下），改用 `COMPOSE_PARALLEL_LIMIT=1`。
- **预期**：8 行 `Successfully tagged software-group-<svc>:latest`。
- **风险**：① 首次需拉 `maven:3.9-eclipse-temurin-17` + `eclipse-temurin:17-jre-alpine` ≈1.5GB；② 容器内直连 Maven Central，**校园网可能慢或超时**，必要时挂国内镜像。
- **单独重试某个服务**：`docker compose build user-service`（不影响其它，失败后优先用这条重试）。

#### 步骤 4｜启动 8 个微服务

```bash
cd /opt/software-group
docker compose up -d file-service user-service activity-service project-award-service \
                     hr-service monitor-service content-service ai-service
curl -s "http://localhost:8848/nacos/v1/ns/service/list?pageNo=1&pageSize=20" | python3 -m json.tool
curl -s -o /dev/null -w 'user-svc=%{http_code}\n' http://localhost:8082/actuator/health
docker compose logs --tail=40 user-service
```

- **预期**：Nacos 服务列表出现 8 个服务名；`user-svc=200`；日志有 `Tomcat started on port(s): 8082` 与 `Started UserServiceApplication`。
- **实测（2026-10-07）**：`up -d` 后 8 个容器都 `Up`，但**暴露出两个此前没发现的坑**（均属 compose/配置层，不是应用代码问题），逐个修完细节见下。

##### 步骤 4 修复 A：healthcheck 误用 `curl`（已修，8 个全 `(healthy)`）

- **现象**：8 个服务容器全部 `Up ... (unhealthy)`，但 `curl http://localhost:8082/actuator/health` 从宿主看是 **`{"status":"UP"}`**（`db`/`discoveryComposite`/`diskSpace`/`sentinel` 全 UP），日志也显示注册成功。
- **根因**：compose 里 8 个 java 服务的 `healthcheck.test` 写的是 `["CMD", "curl", "-f", "http://localhost:<port>/actuator/health"]`，但运行镜像 `eclipse-temurin:17-jre-alpine` **只有 busybox 的 `wget`、没有 `curl`**（`docker exec sg_user_service which curl` 无输出）；compose 的 `healthcheck.test` 会**覆盖** Dockerfile 里原本正确的 `wget -q --spider` HEALTHCHECK → 探针永远"命令不存在" → 恒 `unhealthy`。
- **口诀**：**"应用 curl 得到 UP、容器却 unhealthy" → 先查探针用的二进制在镜像里存不存在。**
- **修复（已应用在 VM 与仓库 `docker-compose.yml`）**：

  ```bash
  sed -i '/actuator.health/ s/"curl", "-f"/"wget", "-q", "--spider"/' docker-compose.yml
  sed -i '/actuator.health/ s/localhost/127.0.0.1/' docker-compose.yml
  ```

  （用 `127.0.0.1` 而非 `localhost`，避免容器内 `localhost` 解析到 `::1` 而 Tomcat 只听 IPv4。）改完 `docker compose up -d <8 svc>` 重建 → **8 个全部 `(healthy)`**。

##### 步骤 4 修复 B：5 个服务未注册 —— **真因是 `spring.application.name` 为空**（已解决，`count=8`）

- **现象**：`curl .../nacos/v1/ns/service/list` 只返回 **3 个**（`ai-service`、`content-service`、`project-award-service`），其余 5 个（file/user/activity/hr/monitor）不注册。
- **根因（2026-10-07 纠正；此前误判为 `server-addr`）**：**真正的阻塞是 `spring.application.name` 缺失**。这 5 个服务把 `spring.application.name` 只写在 `bootstrap.yml` 里，而它们**没有 `spring-cloud-starter-bootstrap`** → 整份 `bootstrap.yml` 被忽略 → 运行期 `spring.application.name` 为**空** → `NacosServiceRegistry.register()` 打印 `No service to register for nacos client...` 并**直接 return、跳过注册**。此时 `server-addr` 根本没被用到——所以补 `server-addr` 的环境变量**设了也白设**。
  - **日志实证**：`WARN c.a.n.c.r.NacosServiceRegistry - No service to register for nacos client...`
  - **相关性 100%**：`ai-service` 的 `spring.application.name` 在 `application.yml`（正常）；`content`/`project-award` 有 bootstrap starter，`bootstrap.yml` 生效（正常）。
  - 附带第二个问题：`application.yml` 把 nacos 配置挂在顶层 `alibaba:`（应为 `spring.cloud.nacos`），这段被忽略（但不是本次阻塞点）。
- **修复（最终生效，**不重建镜像**，改 base `docker-compose.yml`）**：给这 5 个服务各补一行 `SPRING_APPLICATION_NAME: <svc>-service`（`file/user/activity/hr/monitor` → `file-service`…，与已注册的 `ai-service` 命名一致）：

  ```bash
  for s in file user activity hr monitor
  do
  sed -i "/container_name: sg_${s}_service/,/NACOS_HOST: nacos/ s|^\( *\)\(NACOS_HOST: nacos\)|&\n\1SPRING_APPLICATION_NAME: ${s}-service|" docker-compose.yml
  done
  ```

  - 范围锚点＝每个服务独有的 `container_name: sg_<x>_service`；用 `&`+`\1` 复用（不重复长片段、不写死缩进）。
  - 校验：`grep -c SPRING_APPLICATION_NAME docker-compose.yml` → **5**。改完 `docker compose up -d <8 svc>` 重建这 5 个 → `curl .../service/list` → **`count=8`**。
  - **VM 与仓库 `docker-compose.yml` 均已改**（仓库由同一 loop 写入，`grep -c` = 5）。
- **走过但无效 / 被拒的路（别再走）**：
  - `SPRING_CLOUD_NACOS_DISCOVERY_SERVER_ADDR: nacos:8848`（8 处，`sed` 插入）：**没解决问题**——app 名先空，注册在更早处就 `return` 了。它现仍在 compose 里（无害多余，可留可删）。
  - `docker-compose.override.yml` 补 `environment`：被本机 compose 拒——`validating .../docker-compose.override.yml: services.monitor-service additional properties 'environment' not allowed`（override 里的 `environment` 被判成"不许出现的属性"，反常）。**override 路在本机走不通，改用 base 文件。**
  - ⚠️ **粘贴坑（2026-10-07）**：下发 sed 别在替换段**重复长片段**（会被整段吞 → `unterminated 's'`），缩进别写死、用 `\(...\)`+`\1` 反向引用。见记忆 `feedback_paste_mangling`。

##### ✅ 步骤 4 断点已解决（2026-10-07）

- 断点（"重建容器 + 复查 Nacos 注册"）已跑完：`docker compose up -d <8 svc>` 后 `curl .../service/list` → **`count=8`**，8 个服务名齐全。
- **关键教训**：卡住的真因是 `spring.application.name` 为空（见「修复 B」），**不是** server-addr。只盯着 `count=3` 会误判成"环境变量没生效"——**一定要抓启动日志里的 `c.a.n.c.r.NacosServiceRegistry` 那行**（`No service to register for nacos client...`）。
- **重启后提醒**：VM/容器重启后，`docker compose ps` 先确认 mysql/redis/rabbitmq/nacos/sentinel/minio 与 8 个服务都 `Up`；若某服务没起，`docker compose up -d` 一次即可（`restart: always` 通常会自动拉起）。


#### 步骤 5｜最后起 nginx

```bash
cd /opt/software-group
mkdir -p static/css static/js static/images
docker compose up -d nginx
```

- **用途**：nginx 是统一入口（宿主 `${NGINX_PORT:-80}:80`），`depends_on` 了 `app` + 全部 8 个服务，**必须最后起**（否则依赖未就绪）。
- **✅ 已完成（2026-10-07）**：`sg_nginx` `Up (healthy)`，四条 curl 全 200。
- **两个坑（务必照做，否则旧站经 `:80` 会 404）**：
  1. **探针恒 unhealthy**：healthcheck 用 `http://localhost/health`，`nginx:alpine` 内 `localhost` 解析到 `::1`，而 `listen 80;` 只绑 IPv4 → 改 `127.0.0.1`。
  2. **旧站内链双前缀 404**：应用链接由 `${pageContext.request.contextPath}` 生成为 `/software-group/...`，而 `location / { proxy_pass http://tomcat_backend/software-group/; }` 把 `/` 映射成 `/software-group/` → 二次拼接成 `/software-group/software-group/...` → **首页能开、CSS/JS/导航全 404**。修法＝**新增一个原样透传的前缀 location**（`rewrite` 走不通，别试 —— 见下方注）：
     ```nginx
     location /software-group/ {
         proxy_pass http://tomcat_backend;
         proxy_set_header Host $host;
         proxy_set_header X-Real-IP $remote_addr;
         proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
         proxy_set_header X-Forwarded-Proto $scheme;
         proxy_http_version 1.1;
         proxy_set_header Connection "";
     }
     ```
     并**保留** `location /`（`scripts/e2e-smoke-test.sh` 硬性要求 `/` 与 `/login.jsp`=200）。
     > 为什么不用 `rewrite`：① `rewrite ^/software-group/(.*)$ /$1 break;` 放在带 URI 的 `proxy_pass` 里得不到预期路径（实测 backend 收到错误路径 → 仍 404）；② 即便剥成功，`/software-group/css/x.css` → `/css/x.css` 会撞上 nginx 的 `location /css/` 静态 alias（空目录）→ 还是 404。
     > 另注：`location ~* \.(woff|woff2|ttf|eot|otf)$` 是**正则**、优先于前缀，会截走 `/software-group/**/*.woff2`（字体可能 404）；要覆盖就写 `location ^~ /software-group/`。
     > 改 `nginx.conf` 时的粘贴坑：`sed` 的 `&`/`\$`/`{}` 与 `python` 里的 `\n` 都会被工具改写 → 用 `python3 - <<'PY'` + `chr(10)` 拼行、`awk 'NR!=<n>'` 删行、改完 `docker compose up -d --force-recreate nginx`（见记忆 `deploy-windows-dockerfile-gotchas`）。
- **验收 curl**：
  ```bash
  curl -s -o /dev/null -w 'root=%{http_code}\n'     http://localhost/
  curl -s -o /dev/null -w 'login=%{http_code}\n'    http://localhost/login.jsp
  curl -s -o /dev/null -w 'sg-login=%{http_code}\n' http://localhost/software-group/login.jsp
  curl -s -o /dev/null -w 'sg-css=%{http_code}\n'   http://localhost/software-group/css/design-system.css
  ```
  四条都应为 `200`（`sg-css` 是关键，证明内链不再双前缀）。

#### 步骤 6｜验收

```bash
cd /opt/software-group
bash scripts/verify-infrastructure.sh   # 只能在整栈起齐后跑（会查 sg_nacos/sg_minio/sg_nginx）
bash scripts/verify-routes.sh
bash scripts/e2e-smoke-test.sh
curl -s -o /dev/null -w 'app-direct=%{http_code}\n' http://localhost:8080/software-group/login.jsp
curl -s -o /dev/null -w 'via-nginx=%{http_code}\n'  http://localhost/software-group/login.jsp
```

- **预期**：三个脚本全绿；两个 curl 都 `200`（证明微服务上了、且旧站没被影响）。

> 2026-11-15 的 4.3 补充：`verify-routes.sh` 和 `e2e-smoke-test.sh` 现只验证网关安全回退、页面可达及明确禁用的接口，不能证明微服务业务已切流。部署 4.3 候选版本并完成数据库迁移后，另运行 `bash scripts/verify-mig43-production-readiness.sh`，再做登录、项目、奖项、新闻和群组的实际读写验收。旧 WAR 的 8080 直连入口暂保留。

### 19.5 风险清单

| 风险 | 说明 |
|---|---|
| 源码不在 VM | 步骤 0 会暴露；**最大未知项** |
| 内存吃紧 | nacos 512m + sentinel + minio + 8×JVM(256–512m) + app/mysql/redis/rabbit，建议 VM ≥8GB；留意 `dmesg \| grep -i oom` |
| Maven 拉包慢/失败 | 校园网直连 Maven Central |
| 首次构建慢 | 8 服务 × 3 次 Maven 构建，首次约 20–40 分钟 |
| 误删数据卷 | `docker compose down -v` 会触发待办 #1，45 张表全没 |

### 19.6 快速定位（供下次读取本文件）

- **手册状态**：一~十七 = 原始手册（仍有效）；十八 = Linux VM 路线（历史，已过时）；**十九 = 本文，当前唯一有效断点**。
- **VM**：`sg-linux`，IP `192.168.0.15`，项目在 `/opt/software-group`。
- **旧 WAR 状态**：**已跑通** —— `http://192.168.0.15:8080/software-group/login.jsp`（历史测试账号；生产凭据不在手册中展示）。
- **进度一览（2026-10-07）**：步骤 0 ✅、步骤 1 ✅（sql 顺序+表名复数）、步骤 1.5 ✅（CRLF→LF）、步骤 2 ✅（nacos 切 MySQL）、步骤 3 ✅（8 个镜像 467–511MB）、**步骤 4 ✅（8 容器 `Up (healthy)`，Nacos `count=8`）**、**步骤 5 ✅（nginx `Up (healthy)`；`root`/`login`/`sg-login`/`sg-css` 四条 curl 全 200）**、**步骤 6 🔵 验收（下一动作）**。
- **🔴 下一条动作：步骤 6（整栈验收）** —— 跑 `scripts/verify-infrastructure.sh`、`verify-routes.sh`、`e2e-smoke-test.sh` 三个脚本 + 两个 curl（`app-direct` 直连 `:8080`、`via-nginx` 走 `:80`）都应为 200。
- **步骤 5 的两个坑（细节见 §19.4「步骤 5」）**：① 探针 `localhost`→`127.0.0.1`；② 旧站内链双前缀 404 → **新增 `location /software-group/ { proxy_pass http://tomcat_backend; ... }` 原样透传**（**rewrite 方向错、勿用**），保留 `location /`（`e2e-smoke-test.sh` 要 `/`、`/login.jsp`=200）。
- **步骤 4 的三个坑与真因（细节见 §19.4「步骤 4 修复 A / B」）**：
  - **A**＝healthcheck 误用镜像里没有的 `curl` → 改 `wget -q --spider` + `127.0.0.1`。
  - **B（真因）＝5 个服务 `spring.application.name` 为空**——只写在被忽略的 `bootstrap.yml`（它们无 `spring-cloud-starter-bootstrap`）→ `NacosServiceRegistry` 打 `No service to register for nacos client...` 直接跳过注册。**不是 server-addr！** 修法：base `docker-compose.yml` 里给这 5 个各加 `SPRING_APPLICATION_NAME: <svc>-service`。
  - **C**＝`docker-compose.override.yml` 补 `environment` 被本机 compose 拒（`additional properties 'environment' not allowed`）→ 改走 base 文件。
- **步骤 2 的历史**：Derby 慢盘建库超时的完整定性、试错与最终 MySQL 方案，见 §19.4 步骤 2 的「✅ 步骤 2 已解决」+「步骤 2 收尾改动」（**新环境照"收尾改动"做即可，别再走 Derby**）。
- **协作方式**：用户把 VM 终端输出存到桌面 `终端输出.txt` 再发我；**每次只给一到两条命令的输出**，所以诊断步骤要能独立判断。**粘贴易失真**（中文变 `⬢`、空格/分号丢、命令被改写），验证优先用纯 ASCII + 计数判定（详见记忆 `feedback_paste_mangling`）。
- **详细证据与修复过程**：`docs/旧WAR_Tomcat10修复与部署方案.md`。
- **微服务逐服务的命令与预期值**：`docs/阶段三_微服务拆分实施计划.md` §3.3–§3.8。

### 19.7 DB-03 运行账号切换补充（2026-11-17）

前文 19.3 中“9 个 `DB_USER` 全是 root”和 19.4 中 Nacos 使用 root 的记录是当时的部署历史。DB-03 的本地配置已改为 10 个独立运行账号，生产尚未切换。正式更新 VM 前，先完成 DB-02 生产库 baseline，再按 [数据库运行账号切换说明](数据库运行账号切换说明.md) 设置私有 `.env`、运行 `db-migrate` 与 `db-users-init` 并验收权限。不能再执行旧的 `migrations/20261114_db_user_grants.sql`。
