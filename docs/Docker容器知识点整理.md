---
title: "我用一个 16 容器的项目，把 Docker 从「能跑」学到了「真懂」"
aliases:
  - /posts/docker-16-containers/
  - /posts/learn-docker-by-real-project/
  - /posts/docker-mental-model/
description: "以一个真实的高校管理系统为标本——16 个容器：MySQL、Redis、RabbitMQ、Nacos、Sentinel、MinIO 六个中间件，外加 8 个微服务、1 个 Tomcat 旧应用、1 个 Nginx 网关——把镜像、容器、Dockerfile、多阶段构建、端口映射、数据卷、容器网络、Compose 编排、健康检查、配置注入这些 Docker 核心概念，用我自己的话从头讲一遍。"
---

# 我用一个 16 容器的项目，把 Docker 从「能跑」学到了「真懂」

> **摘要**：学 Docker 之前，我只会在教程里敲 `docker run hello-world`，真到项目里就一头雾水：镜像和容器到底啥区别？端口为什么写 `3307:3306`？数据怎么突然就没了？后来我拿自己那个"16 个容器"的高校管理系统当标本，把每个组件都拆开看了一遍，才真正把 Docker 的骨架想明白。这篇文章不是命令手册，而是**我脑子里的那张 Docker 地图**——如果你也卡在"会用不会懂"，希望它有用。

---

## 一、引子：我以前对 Docker 的三个糊涂

我最初对 Docker 的印象，是"一种把程序打包起来、然后在别的机器上跑的东西"。听起来很美好，但一落到地面就全是问号：

- **镜像和容器**，一个词是"模具"、一个是"做出来的蛋糕"，可他俩到底谁动谁不动？
- **端口**为什么有的写 `3306`、有的写 `3307`，容器里连数据库到底该用哪个？
- 我明明往容器里放了文件，**为什么一删容器就没了**？

这三个问题卡了我很久。直到我把项目里那份 `docker-compose.yml` 一条条读完、把 16 个容器都"解剖"了一遍，才豁然开朗。下面就把我理清它们的顺序，原样讲给你听。

---

## 二、一句话，先抓住 Docker 的灵魂

我现在是这么理解 Docker 的：

> **Docker 是把"一个服务 + 它运行所依赖的一整套环境"，打包成一个可以被原样复制、到处运行的盒子。**

我这套系统要跑起来，得同时伺候：MySQL 8、Redis 7、RabbitMQ、Nacos、MinIO、Sentinel、Tomcat 10.1（跑旧 WAR）、JDK 17，再加 8 个 Spring Boot 微服务、1 个 Nginx。手工在服务器上装这些，版本、配置、端口任何一处不对，就是"在我机器上能跑"的经典悲剧。

Docker 的做法是：**每个组件进一个盒子，盒子之间互不干扰；换台机器，一条 `docker compose up -d`，16 个盒子照样起来。**

这里还有个我知道但常被忽略的前提：**容器不是虚拟机。** 虚拟机要背一整套操作系统（几个 GB、启动一分钟）；容器和宿主机**共用同一个内核**，只是把进程隔离开（几百 MB、启动一两秒）。我项目里一个微服务镜像才 400~500MB，里面主要还是 JRE——换成等价的虚拟机，得好几个 GB。

---

## 三、镜像、容器、Dockerfile：先把三兄弟认清楚

这是我理解 Docker 的**第一块地基**，务必分清：

| 概念 | 是什么 | 一句话类比 |
|---|---|---|
| **镜像 image** | 只读的"文件系统快照 + 元数据"，由多层叠成，自己不会动 | 蛋糕的**模具** |
| **容器 container** | 镜像跑起来的**实例**，在只读层上盖了一层"可写薄层" | 用模具做出来的**蛋糕** |
| **Dockerfile** | 一份文本**配方**，描述怎么从零做出这个镜像 | 模具的**图纸** |
| **Compose** | 一张**装<br/>配图**：哪些盒子、怎么拼、谁先谁后 | **组装说明** |

三个我后来才想通的关键点：

1. **一个镜像能跑出很多容器；容器删了，镜像还在。**
2. **容器里写的文件，只活在那一层"可写薄层"上；容器一删，这层就没了。** —— 这就是"数据为什么会丢"的根源（第七章讲怎么救）。
3. **镜像分层，层能复用。** 做镜像时，`FROM / RUN / COPY / ENV` 每条指令都生成一层；某条指令和上次完全一样，就直接用缓存、不重跑。**所以指令的写法顺序，直接决定构建快不快。**

---

## 四、读懂一份 Dockerfile

我项目的旧 WAR 应用，配方就是这几行（`Dockerfile.tomcat`）：

```dockerfile
FROM tomcat:10.1-jdk17-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY target/software-group.war /usr/local/tomcat/webapps/software-group.war
ENV CATALINA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC"
EXPOSE 8080
CMD ["catalina.sh", "run"]
```

逐条对照，我的理解是这样的：

| 指令 | 我把它理解成 |
|---|---|
| `FROM` | 站在谁的肩上——基于官方 Tomcat 镜像，里面 OS、JDK、Tomcat 都齐了 |
| `RUN` | **做镜像时**执行命令，结果固化进镜像（这里是删示例应用） |
| `COPY` | 把**宿主机**的文件搬进镜像（这里把我的 WAR 搬进去） |
| `ENV` | 设一个会留在镜像里的环境变量 |
| `EXPOSE` | **只是声明**"我打算用 8080"——它**不打通任何端口**（这是我踩的第一个坑，第六章说） |
| `CMD` | 容器**启动时**默认跑的主进程 |

这里有两组极容易混的对比，我特意记牢了：

**① `RUN` vs `CMD`**：`RUN` 是"做模具时"执行（结果留在模具里）；`CMD` 是"起容器时"执行（是容器的命脉，主进程一结束容器就退出）。

**② `CMD` vs `ENTRYPOINT`**：`CMD` 是**默认命令、可被覆盖**（`docker run 镜像 别的命令` 就把它顶掉了）；`ENTRYPOINT` 是**固定入口、不易覆盖**。我项目里的微服务用的是：

```dockerfile
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

为什么要套一层 `sh -c`？因为**数组写法（exec form）不会展开环境变量**，裹上 `sh -c` 才能让 `$JAVA_OPTS` 生效。这是新手很容易一头撞上的墙。

---

## 五、多阶段构建：一个"我恍然大悟"的技巧

我项目的 8 个微服务，Dockerfile 长这样（以 user-service 为例）：

```dockerfile
# ---- 阶段一：构建 ----
FROM maven:3.9-eclipse-temurin-17 AS builder
COPY pom.xml .            && mvn install -N -DskipTests      # 先父 POM
COPY common/...           && mvn install -f common/pom.xml    # 再公共模块
COPY user-service/...     && mvn clean package -DskipTests    # 最后本服务，打出 jar

# ---- 阶段二：运行 ----
FROM eclipse-temurin:17-jre-alpine
COPY --from=builder /build/user-service/target/user-service-1.0.0.jar app.jar
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

**为什么要两个 `FROM`？** 因为：

- **构建**一个 Java 项目，需要 Maven + 完整 JDK + 一堆依赖（很大）；
- **运行**它，只需要 JRE + 那个 jar。

如果只用一段写，最终镜像里会塞满 Maven、源码、编译缓存——**又大又危险**（体积大、源码还泄露）。**多阶段构建让"构建那套工具"用完就扔**，最终镜像只从 builder 里 `COPY --from=builder` 拿一个 jar 走。

**还有一个我很佩服的细节：指令顺序就是缓存策略。** 它先 `COPY pom.xml` 再下依赖，最后才 `COPY src`。因为 Maven 下依赖最慢，把不常变的 `pom.xml` 放前面——**我只改业务代码时，下依赖那层命缓存，不用重下**。要是把 `COPY src` 放最前，改一行代码就得从头下一遍依赖。

**`-alpine` 的意义**：Alpine 是极小的 Linux（约 5MB），配 JRE 让镜像更小。代价是**工具少**——它**没有 `curl`，只有 `wget`**。这个细节，后来成了我健康检查翻车的直接原因。

---

## 六、容器怎么跑起来，端口怎么通

**容器的一生 = 主进程的一生**：主进程退出，容器就结束。


然后是新手第一大坎——**端口映射**。记住一条铁律：

> 镜像里的服务，监听的是**容器内的端口**；外面默认够不着。要暴露，得**发布端口**。

```yaml
ports:
  - "3307:3306"    # 左 = 宿主机端口，右 = 容器内端口
```

我项目的 MySQL 就是这么写的。于是产生两个"世界"：

- **宿主机世界**：连 MySQL 用 `localhost:3307`
- **容器世界**：别的容器连它用 `mysql:3306`（服务名 + 容器端口）

**为什么左边是 3307 不是 3306？** 为了避开宿主机上可能已经占着 3306 的本机 MySQL——这就是端口映射最实用的场景：**容器内保持标准端口，对外换一个不冲突的。**

最后纠正一个我自己误会很久的点：**`EXPOSE` 不打通端口**，它只是文档。真正打通的是 compose 的 `ports:`。所以哪怕某份 Dockerfile 里 `EXPOSE` 写了错端口，只要 compose 映射对，运行照样没问题。

---

## 七、数据为什么会丢：数据卷

回到开头那个扎心的问题——**容器里放的文件，容器一删就没了。** 因为文件写在"可写薄层"上，这层跟着容器一起消失。

可数据库的数据、用户上传的文件，绝对不能丢。于是要把这些磁盘目录**挂到容器外面**——这就是**数据卷（volume）**。我项目里用两种：

**① 命名卷（named volume）**——数据交给 Docker 管，存在宿主机 `/var/lib/docker/volumes/`：

```yaml
mysql:
  volumes:
    - mysql_data:/var/lib/mysql
volumes:
  mysql_data:      # 文件末尾统一声明
```

**② 绑定挂载（bind mount）**——直接挂宿主机上的某个目录/文件：

```yaml
nginx:
  volumes:
    - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro   # :ro = 只读
```

其中 `ro` 表示容器只能读、改不了宿主机文件。

**我最喜欢的一个设计**：项目的上传目录 `app_data` 这个命名卷，**同时挂给了两个容器**——`app`（可写，负责接收上传）和 `nginx`（只读，负责把文件发出去）。一个卷、两容器共享，用户上传的文件 nginx 就能直接吐给浏览器，不用绕经应用。

> **顺带一个坑**：绑定挂载是指向宿主机的某个文件。如果你用 `sed -i` 改这个文件，`sed` 会**换掉 inode**，而挂载还指着旧的 → 容器看到的还是旧内容。我当时就是用 `docker compose up -d --force-recreate <服务>` 重建容器才让它生效的。

---

## 八、容器之间怎么互相找到：网络

容器默认彼此隔离。要让它们说话，得放进**同一个网络**。我项目建了一个自定义 bridge 网络：

```yaml
networks:
  software-group-net:
    driver: bridge
```

**自定义网络最爽的一点：服务名就是域名。** 同一网络里，容器能直接用**服务名**互相找到（Docker 内置 DNS 帮你解析）。所以微服务的环境变量是这样：

```yaml
file-service:
  environment:
    DB_HOST: mysql     # 服务名，不用记 IP
    DB_PORT: 3306      # 容器端口（不是宿主机的 3307！）
    REDIS_HOST: redis
```

**新手最常踩的雷：在容器里写 `localhost`。** `localhost` 指的是**容器自己**，既不是宿主机、也不是别的容器。所以：

- 微服务连库 → `mysql:3306`
- 你在宿主机用工具连库 → `localhost:3307`

同一个 MySQL，**两个世界两套地址**。想通这点，网络就算入门了。

---

## 九、Compose：把 16 个容器管明白

如果一个个手敲 `docker run` 起 16 个容器，得记住一堆 `-p / -v / -e / --network`，还容易漏、容易乱序。**Compose 就是把我前面学的东西，写成一份 YAML，一条命令拉起来。**

结构就三层：`services`（有哪些容器）、`networks`（网络）、`volumes`（卷）。拿 MySQL 一个服务解剖，它几乎用全了前面所有知识点：

```yaml
mysql:
  image: mysql:8
  container_name: sg_mysql
  restart: always                                   # 挂了自动重启
  environment:
    MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-root123}
    MYSQL_DATABASE: ${MYSQL_DATABASE:-software_group}
  ports:
    - "${MYSQL_PORT:-3307}:3306"                    # 端口映射
  volumes:
    - mysql_data:/var/lib/mysql                     # 命名卷：数据不丢
    - ./sql:/docker-entrypoint-initdb.d:ro          # 绑定挂载：首次自动建库
  networks: [software-group-net]
  healthcheck:
    test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
    interval: 10s
    timeout: 5s
    retries: 5
```

几个我特别想讲的点：

- **`${VAR:-默认值}`**：用宿主机环境变量或 `.env` 覆盖，没有就用默认值。这让同一份 compose 能跨环境跑，不用改文件。
- **`./sql:/docker-entrypoint-initdb.d`**：MySQL 官方镜像有个约定——容器**第一次**启动时自动执行这个目录里的 `.sql`。我把建 45 张表的 `software_group.sql` 和建微服务用户的 `db_users.sql` 挂进去，库就自动初始化了。**只在数据卷为空时执行一次。**
- **`restart: always`**：自愈——容器挂了、Docker 重启了，都自动拉起来。
- **两种"名字"**：**服务名**（`mysql`）给网络当域名用；**容器名**（`sg_mysql`）给 `docker ps / exec` 用。

---

## 十、健康检查与启动顺序：让依赖等对时机

**问题**：应用起太快，MySQL 还没准备好就连接失败。光写 `depends_on` 只保证"容器**启动了**"，**不保证"服务**能用了**"——MySQL 容器起来了，内部初始化还得几秒。

**解法**：健康检查 + 按健康状态依赖。

```yaml
healthcheck:
  test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
  interval: 10s
  timeout: 5s
  retries: 5

# 依赖方这样等
depends_on:
  mysql:
    condition: service_healthy     # 等到 mysql 真的 healthy 才启动
```

这就串成一条启动链：

```
mysql / redis / nacos(healthy) → 8 个微服务 → nginx(依赖 app + 全部微服务)
```

**这里我栽过一跤**：微服务的运行镜像是 `-alpine`（第五章说了它没 `curl`），所以健康检查**必须用 `wget -q --spider`**，用 `curl` 会永远失败。`--spider` 是"只探不下载"，配 `127.0.0.1` 更稳。

> 补充：**Dockerfile 里的 `HEALTHCHECK` 会被 compose 的 `healthcheck:` 覆盖**。所以 Dockerfile 里写错端口也别慌，运行时以 compose 为准。

---

## 十一、配置怎么进容器：环境变量

我项目用环境变量把配置从镜像里"解耦"出来。好处是：**同一个镜像**，换套环境变量就能连不同的数据库，不用重新构建。这就是"配置与代码分离"。

微服务的 Dockerfile **不写死**任何环境相关的东西（库地址、密码），全部启动时由 compose 注入：

```yaml
environment:
  DB_HOST: mysql
  DB_PASSWORD: ${MYSQL_ROOT_PASSWORD:-root123}
```

而我那个旧 WAR 应用更特别：它的环境变量名**必须和 `config.local.properties` 里的 `${...}` 占位符同名**，因为应用启动时会在**运行期**读环境变量替换占位符——所以改配置不用重新打 WAR。这个设计我琢磨挺久才看懂。

---

## 十二、把踩过的坑，翻译成知识点

学 Docker 时，坑本身就是最好的老师。我把项目里真实撞过的墙，一一对应回概念：

| 我踩的坑 | 背后的 Docker 知识点 |
|---|---|
| 服务没注册到 Nacos | 环境变量注入：镜像缺的配置能靠 compose 补 |
| 健康检查用 `curl` 全挂 | Alpine 精简镜像**没有 curl**，要用 wget |
| `sed -i` 改了挂载的配置不生效 | 绑定挂载的 inode 语义 → 要 `--force-recreate` |
| 应用连不上库 | `depends_on` 要配 `condition: service_healthy` |
| 上传文件在 app 和 nginx 间共享 | 一个命名卷可挂多个容器 |
| 差点手滑敲了 `down -v` | `-v` 会删命名卷 → 数据库 + 上传全没 |

---

## 十三、我脑子里的那张 Docker 地图

写了这么多，如果你只让我留一样东西，我会留下这张"框架图"：

```
        Dockerfile  ──build──▶  镜像(image)  ──run──▶  容器(container)
      （配方/图纸）            （只读模具/分层）        （跑起来的实例/可写薄层）

   数据不丢 ──────────▶  卷(volume)      挂出去：命名卷 / 绑定挂载
   互相通信 ──────────▶  网络(network)   自定义 bridge + 服务名当域名
   端口对外 ──────────▶  端口映射         -p 宿主机:容器
   谁先谁后 ──────────▶  healthcheck + depends_on(condition)
   一堆盒子 ──────────▶  docker-compose  一条命令编排 N 个容器
```

**我现在的理解浓缩成三句话**：

1. **镜像是不变的模具，容器是会过期的一次性蛋糕**；所有要留下的东西，都必须放进"卷"里。
2. **端口有两个世界**：宿主机看左边（`3307`），容器之间看右边（`3306`）；容器里永远别写 `localhost`。
3. **Compose 的魔法不是"能一起启动"，而是"按健康顺序启动 + 统一注入配置 + 统一挂载数据"**——它把一堆散命令，收成了一份可复制的图纸。

从"能跑"到"真懂"，我最大的收获不是记住了多少命令，而是**能在脑子里给每个容器画出它的三个圈**：它的数据挂在哪、它的网络叫什么、它等谁先健康。画得出这三个圈，Docker 就不再是黑盒了。

---

*如果你也在用一个真实项目学 Docker，建议别停在 `hello-world`：找一份 `docker-compose.yml`，把每个服务的一条条读下来，问自己"它为什么要有这一行"。你会发现，Docker 的几乎所有概念，都藏在这些"为什么"里。*
