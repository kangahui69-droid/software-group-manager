# ========== project-award-service Dockerfile ==========
# 阶段三微服务 - 项目奖项服务（合并 Project + Award）

# ========== 第一阶段：Maven 构建 ==========
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# 1. 先复制父 POM 并安装到本地仓库
COPY pom.xml .
RUN mvn install -N -DskipTests

# 2. 复制 common 模块并安装
COPY common/pom.xml common/
COPY common/src ./common/src
RUN mvn install -f common/pom.xml -DskipTests

# 3. 复制当前服务的 pom.xml 和源码
COPY project-award-service/pom.xml project-award-service/
COPY project-award-service/src ./project-award-service/src

# 4. 构建当前服务
WORKDIR /build/project-award-service
RUN mvn clean package -DskipTests

# ========== 第二阶段：运行阶段 ==========
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

COPY --from=builder /build/project-award-service/target/project-award-service-1.0.0.jar app.jar

RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8085

ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError"

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:8085/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
