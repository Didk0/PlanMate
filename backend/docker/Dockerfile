FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw && ./mvnw -B -ntp dependency:go-offline

COPY src/ src/

RUN ./mvnw -B -ntp package -DskipTests

RUN JAR=$(find target -maxdepth 1 -name '*.jar' ! -name '*.jar.original') && \
    [ "$(printf '%s\n' "$JAR" | wc -l)" -eq 1 ] || \
      { echo "ERROR: expected exactly one jar in target/, found: $JAR"; exit 1; } && \
    cp "$JAR" target/app.jar

FROM eclipse-temurin:25-jre-alpine

RUN addgroup -S app && adduser -S -G app app

WORKDIR /app

COPY --from=builder --chown=app:app /app/target/app.jar app.jar

USER app

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k"
ENV SPRING_MAIN_BANNER_MODE=off

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
