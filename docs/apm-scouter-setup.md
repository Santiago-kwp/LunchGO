# Scouter APM Setup (Private Server)

## Current Containers
- backend: `lunchgo-backend` (8080)
- scouter collector: `scouter-server` (6100 TCP/UDP)
- scouter webapp: `scouter-webapp` (6180)
- redis: `lunchgo-redis` (6379)
- mysql: `lunchgo-db` (3306)

## Host Agent Install (Docker)
Host metrics are required to see CPU/IO for the MySQL/Redis containers. The host agent runs once on the server.

1) Check host agent jar
```bash
ls /opt/scouter/scouter/agent.host
```

2) Run host agent container
```bash
docker rm -f scouter-host || true

docker run -d --name scouter-host --restart=always \
  --pid=host --net=host \
  -v /proc:/host/proc:ro \
  -v /sys:/host/sys:ro \
  -v /var/run/docker.sock:/var/run/docker.sock:ro \
  -v /opt/scouter/scouter/agent.host:/scouter/agent.host \
  eclipse-temurin:11-jre \
  java -Xms128m -Xmx128m \
    -Dscouter.config=/scouter/agent.host/conf/scouter.conf \
    -Dscouter.host.proc.dir=/host/proc \
    -Dscouter.host.sys.dir=/host/sys \
    -cp /scouter/agent.host/scouter.host.jar \
    scouter.boot.Boot
```

3) Minimum config (`/opt/scouter/scouter/agent.host/conf/scouter.conf`)
```
net_collector_ip=127.0.0.1
net_collector_udp_port=6100
net_collector_tcp_port=6100
obj_name=private-webserver-host
```

4) Verify logs
```bash
docker logs --tail=100 scouter-host
```

## Result
- Host agent starts successfully (Scouter Host Agent Version 2.21.1).
- Host/Container metrics are visible in Scouter Web under `private-webserver-host`.
