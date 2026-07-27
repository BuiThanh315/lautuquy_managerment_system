# =====================================================
# Stage 1: Build — dùng Maven + JDK 17 để build JAR
# =====================================================
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml trước để tận dụng Docker layer cache cho dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code và build JAR (bỏ qua tests)
COPY src ./src
RUN mvn clean package -DskipTests -B

# =====================================================
# Stage 2: Run — chỉ cần JRE 17 nhẹ hơn để chạy
# =====================================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy JAR từ stage build
COPY --from=build /app/target/management-1.0.0.jar app.jar

# Expose port (Render tự inject PORT qua env var)
EXPOSE 8080

# Tối ưu memory cho free tier Render (512MB RAM)
ENTRYPOINT ["java", \
  "-Xmx400m", \
  "-Xms128m", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
