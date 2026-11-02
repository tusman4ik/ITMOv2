# task-service image. Build context: repository root.
# Jar must be built on the host first:
#   ./gradlew :services:task-service:bootJar -x test
#   docker build -f services/task-service/Dockerfile -t spc/task-service .
FROM eclipse-temurin:26-jre-noble
RUN apt-get update && apt-get install -y --no-install-recommends \
        curl \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY services/task-service/build/libs/ libs/
RUN JAR=$(ls libs/task-service-*.jar | grep -v '\-plain\.jar$' | head -1) \
 && cp "$JAR" task-service.jar \
 && rm -rf libs
RUN useradd -m -u 10001 app && chown -R app:app /app
USER app
EXPOSE 8081
HEALTHCHECK --interval=10s --timeout=5s --start-period=60s --retries=18 \
    CMD curl -s -o /dev/null -X POST -H 'Content-Type: application/json' \
        --data '{"items":[]}' http://localhost:8081/api/v1/tasks/check || exit 1
ENTRYPOINT ["java", "-jar", "/app/task-service.jar"]
