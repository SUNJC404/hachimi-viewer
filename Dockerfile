# Dockerfile.local

# 最终运行只需要Java环境（JRE），不再需要完整的JDK和Maven
FROM eclipse-temurin:17-jre-focal@sha256:d7ec0d23cc1675eb16a266af1d4f5d41db60260af3fcd81c42de2e6ed3dc4dba

# 设置工作目录
WORKDIR /app

# 【关键步骤】
# 从构建上下文（项目根目录）的 target 文件夹中，
# 复制我们已经在本地打包好的 JAR 文件到容器中，并重命名为 app.jar。
# 使用通配符 `*.jar` 可以避免因版本号变化导致文件名对不上的问题。
COPY target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 容器启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]