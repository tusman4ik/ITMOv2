# Code-runner image. Build context: repository root.
# Jar must be built on the host first:
#   ./gradlew :services:code-runner:shadowJar -x test
#   docker build -f services/code-runner/Dockerfile -t spc/code-runner .
#
# Runs untrusted code via `isolate`, so the container must run privileged
# (see docker-compose.demo.yml). Never run this next to anything you care about.
FROM eclipse-temurin:26-jre-noble
LABEL authors="tusman4ik"

RUN apt-get update && apt-get install -y --no-install-recommends \
    curl vim \
    ca-certificates \
    python3 \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /etc/apt/keyrings \
    && curl -sS https://www.ucw.cz/isolate/debian/signing-key.asc > /etc/apt/keyrings/isolate.asc

RUN echo "Types: deb" > /etc/apt/sources.list.d/isolate.sources \
    && echo "URIs: http://www.ucw.cz/isolate/debian/" >> /etc/apt/sources.list.d/isolate.sources \
    && echo "Suites: trixie-isolate" >> /etc/apt/sources.list.d/isolate.sources \
    && echo "Components: main" >> /etc/apt/sources.list.d/isolate.sources \
    && echo "Architectures: amd64" >> /etc/apt/sources.list.d/isolate.sources \
    && echo "Signed-By: /etc/apt/keyrings/isolate.asc" >> /etc/apt/sources.list.d/isolate.sources

RUN apt-get update && apt-get install -y --no-install-recommends \
    isolate \
    && rm -rf /var/lib/apt/lists/*

RUN echo "isolate:100000:65536" >> /etc/subuid \
    && echo "isolate:100000:65536" >> /etc/subgid

WORKDIR /app

COPY services/code-runner/build/libs/*-all.jar app.jar
COPY --chmod=0777 services/code-runner/scripts/bootstrap.sh scripts/bootstrap.sh

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=12 \
    CMD curl -sf http://localhost:8080/openapi -o /dev/null || exit 1

# add -DLOG_FORMAT=ASYNC_JSON in java -jar... if needed
ENTRYPOINT ["./scripts/bootstrap.sh"]
