#!/bin/sh

export SCOUTER_OBJ_NAME=${SCOUTER_OBJ_NAME:-lunchgo-prod-$(hostname)}

exec java -javaagent:/app/scouter/agent.java/scouter.agent.jar \
  -Dscouter.config=/app/scouter/conf/scouter.conf \
  -Dscouter.obj_name=${SCOUTER_OBJ_NAME} \
  -jar /app/app.jar
