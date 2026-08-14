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

应看到 `Containers` 已安装。

> **注意**：如果需要运行 Linux 容器（推荐），还需要启用 Hyper-V。但 Hyper-V 与某些硬件不兼容，可跳过并使用 Windows 容器模式。

### 1.3 可选：启用 Hyper-V（支持 Linux 容器模式）

> 如果计划使用 Linux 容器模式（推荐），需要启用 Hyper-V。

```powershell
# 安装 Hyper-V
Install-WindowsFeature -Name Hyper-V -IncludeManagementTools

# 重启服务器
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

### 2.3 验证安装

```powershell
docker --version
docker compose version
```

预期输出：
```
Docker version 27.x.x, build xxxxxx
Docker Compose version v2.x.x
```

### 2.4 配置 Docker daemon（可选）

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

### 2.5 Linux 容器模式（推荐）

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

## 三、停止 IIS（释放 80 端口）

Windows Server 默认启用 IIS，会占用 80 端口。

```powershell
# 停止 IIS
Stop-Service W3SVC

# 禁用 IIS（可选，防止重启后再次占用）
Set-Service -Name W3SVC -StartupType Disabled
```

验证 80 端口已释放：

```powershell
netstat -ano | findstr ":80"
```

应无输出（或只有其他服务的 80）。

---

## 四、上传项目代码

### 4.1 创建项目目录

```powershell
mkdir D:\software-group
cd D:\software-group
```

### 4.2 上传代码

**方式一：Git 克隆（推荐）**

```powershell
cd D:\software-group
git clone <你的GitHub仓库地址> .
```

**方式二：远程桌面复制**

使用 MSTSC 远程连接，直接复制粘贴文件。

**方式三：SMB 共享**

```powershell
# 本地设置共享后，服务器访问
\\本地电脑IP\share
```

### 4.3 创建静态资源目录

```powershell
cd D:\software-group
mkdir -p static/css static/js static/images
```

---

## 五、创建配置文件

### 5.1 创建 .env 文件

```powershell
cd D:\software-group
notepad .env
```

粘贴以下内容（根据实际情况修改密码）：

```env
# 数据库
MYSQL_ROOT_PASSWORD=YourSecurePassword123
MYSQL_DATABASE=software_group
MYSQL_PORT=3307

# Redis
REDIS_PASSWORD=YourRedisPassword123
REDIS_PORT=6379

# RabbitMQ
RABBITMQ_USER=admin
RABBITMQ_PASSWORD=YourRabbitMQPassword123
RABBITMQ_PORT=5672
RABBITMQ_MANAGE_PORT=15672

# Nacos
NACOS_PORT=8848
NACOS_GRPC_PORT=9848

# Sentinel
SENTINEL_PORT=8858

# MinIO
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=YourMinioPassword123
MINIO_PORT=9000
MINIO_CONSOLE_PORT=9001

# 微服务端口
FILE_SERVICE_PORT=8081
USER_SERVICE_PORT=8082
ACTIVITY_SERVICE_PORT=8084
PROJECT_AWARD_SERVICE_PORT=8085
HR_SERVICE_PORT=8089
MONITOR_SERVICE_PORT=8086
CONTENT_SERVICE_PORT=8087
AI_SERVICE_PORT=8094

# 旧WAR (Tomcat)
APP_PORT=8080

# Nginx
NGINX_PORT=80

# JWT (至少32字符)
JWT_SECRET=YourVeryLongAndSecureJWTSecretKey2024ForProduction

# DES加密 (与旧系统一致，不要修改)
DES_KEY=(^&%gasie_%^)

# AI配置
AI_PROVIDER=minimax
AI_API_KEY=your-api-key-here
AI_API_URL=
AI_MODEL=MiniMax-M2.7

# HikariCP连接池
HIKARI_MAX_POOL_SIZE=10
HIKARI_MIN_IDLE=5
SESSION_TIMEOUT=30

# Grafana 密码
GRAFANA_PASSWORD=admin123

# Prometheus 端口
PROMETHEUS_PORT=9090

# Loki 端口
LOKI_PORT=3100

# Portainer 端口
PORTAINER_PORT=9443
PORTAINER_AGENT_PORT=9001
```

保存文件。

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
cd D:\software-group
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
| **Nacos** | http://服务器IP:8848/nacos | 用户名/密码: nacos/nacos |
| **MinIO** | http://服务器IP:9001 | 用户名: minioadmin，密码: 见 .env |
| **RabbitMQ** | http://服务器IP:15672 | 用户名/密码: admin/见.env |
| **Sentinel** | http://服务器IP:8858 | 无默认账号 |

### 8.3 默认登录账号

| 账号 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 成员 | member1 | member123 |

---

## 九、生产级优化（监控/日志/运维）

> 基础服务运行正常后，执行以下配置以满足生产环境需求。

### 9.1 创建监控配置文件

#### 9.1.1 创建 Prometheus 配置目录和文件

```powershell
cd D:\software-group
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
      - GF_SECURITY_ADMIN_PASSWORD=${GRAFANA_PASSWORD:-admin123}
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
| **Grafana** | http://服务器IP:3000 | 用户名: admin，密码: admin123 |
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
cd D:\software-group

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
cd D:\software-group

# 创建备份目录
mkdir backup

# 备份 MySQL
docker compose exec mysql mysqldump -uroot -pYourPassword software_group > backup\backup_$(Get-Date -Format "yyyyMMdd_HHmmss").sql
```

### 12.2 恢复数据库

```powershell
cd D:\software-group
docker compose exec -i mysql mysql -uroot -pYourPassword software_group < backup\backup.sql
```

### 12.3 备份数据卷

```powershell
# 查看数据卷
docker volume ls

# 备份 MySQL 数据卷
docker run --rm -v software_group_mysql_data:/data -v D:\software-group\backup:/backup alpine tar czf /backup/mysql_data_$(Get-Date -Format "yyyyMMdd_HHmmss").tar.gz /data
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
D:\software-group\
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
