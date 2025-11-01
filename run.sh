#!/bin/bash
# -------------------------------------------------
#  FindYourDreamHouseAI – Max Performance + All Env Vars
# -------------------------------------------------

# === FULL ENVIRONMENT VARIABLES ===
export AI_API_KEY=verySecretAPIKey
export AWS_REGION=us-east-2
export CLOUDWATCH_NAMESPACE="DreamHouseAI/Prod"
export LLM_EMBEDDING_MODEL=nomic-embed-text
export LLM_MODEL=qwen3:4b-instruct
export LLM_NATIVE_BASE_URL=http://localhost:11434
export LLM_TEMPERATURE=0.2
export LLM_URL=http://localhost:11434/v1
export LOGS_FILE_LOCATION=logs/application.log
export ORACLE_PASSWORD=AppSecurePwd456
export ORACLE_USERNAME=appuser
export POSTGRESQL_PASSWORD=admin
export POSTGRESQL_URL=jdbc:postgresql://localhost:5432/postgres
export POSTGRESQL_USERNAME=admin
export REDIS_SERVER=redis://localhost:6379
export S3_SECRET_KEY=dev/dreamai/backend
export SONAR_HOST_URL=http://localhost:9000
export SONAR_LOGIN_KEY=sqp_358a9072f25d96dd99d951b0f78d336f8bb46734

# === ENSURE LOGS DIRECTORY EXISTS ===
mkdir -p logs

# === JVM TUNING (8-core, 24GB RAM) ===
JAVA_OPTS=(
  -Xms8g
  -Xmx16g
  -XX:MaxMetaspaceSize=512m
  -XX:+UseZGC
  -XX:+AlwaysPreTouch
  -XX:+UseStringDeduplication
  -XX:ReservedCodeCacheSize=512m
  -XX:CICompilerCount=6
  -XX:TieredStopAtLevel=1
  -Djava.awt.headless=true
  -Dspring.jmx.enabled=false
  -Dlogging.level.root=WARN
)

# === KILL ANY EXISTING APP ON 8080 ===
echo "Killing any process on port 8080..."
kill -9 $(lsof -t -i :8080) 2>/dev/null || echo "No process found on 8080."

# Warm up Ollama

echo "Warming up Ollama..."
curl -s http://localhost:11434/api/generate -d '{
  "model": "qwen3:4b-instruct",
  "prompt": "hello",
  "stream": false
}' > /dev/null


# === BUILD THE APP ===
mvn clean install package -U

# === RUN THE APP ===
echo "Starting FindYourDreamHouseAI on port 8080..."
java "${JAVA_OPTS[@]}" -jar target/FindYourDreamHouseAI-1.0.0.jar \
  --server.port=8080 \
  --spring.profiles.active=prod

# === OPTIONAL: Keep terminal open on crash ===
echo "App exited with status $?"