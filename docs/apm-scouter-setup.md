# Scouter APM 적용 방안 (로컬 + 클라우드)

## 목적
- 로컬 환경에서 성능 병목을 빠르게 파악하고, 배포 환경에서 서비스 상태를 지속 모니터링한다.
- LunchGO의 Spring Boot WAS에 Scouter APM을 적용한다.

## Scouter APM이란?
Scouter는 애플리케이션의 트랜잭션 흐름과 성능 지표를 수집해
병목, 오류, 느린 요청을 빠르게 찾도록 돕는 오픈소스 APM이다.
일반적으로 다음 상황에서 유용하다.

- 배포 후 응답이 느려졌을 때 원인 추적
- 특정 API의 지연/오류 비율을 확인해야 할 때
- 외부 API/DB 호출이 병목인지 확인할 때
- 배포 전후 성능 변화를 비교해야 할 때

Scouter 구성은 크게 3가지로 나뉜다.
- **Agent**: 애플리케이션 내부에 붙어 트랜잭션/메트릭을 수집
- **Collector(Server)**: Agent 데이터를 수집/저장
- **Client(UI)**: 수집된 데이터를 조회/분석하는 데스크톱 도구

## 적용 범위 요약
- 대상: Spring Boot WAS 컨테이너
- 수집 항목: 트랜잭션 트레이스, 응답 시간, 오류/예외, JDBC 쿼리 지연, 외부 호출 지연
- 배포 환경: NCP VPC (Public Subnet Nginx, Private Subnet WAS/DB/Redis, NAT Gateway Outbound)

## 투 트랙 운영 전략
1) 로컬 트랙
- 개발 머신에서 Spring Boot 실행 시 Scouter Java Agent를 주입한다.
- 로컬 전용 설정 파일/환경변수로 서비스명을 분리한다.

2) 클라우드 트랙
- Docker 이미지 또는 런타임 옵션으로 Java Agent를 주입한다.
- Collector는 VPC 내부에 구성하거나 외부 SaaS처럼 Public IP로 접근 가능한 위치에 둔다.
- Private Subnet WAS는 NAT Gateway를 통해 Collector로 Outbound 통신한다.

## 결정 사항
- Collector 위치: VPC 내부 배치
- Agent 주입 방식: Dockerfile 내장
- 서비스명/인스턴스명 분리: `[서비스명]-[환경]-[식별자]` 규칙 채택

## 아키텍처 적용 지점
- Java Agent: WAS 컨테이너 내부
- Collector: VPC 내부(권장) 또는 외부 네트워크
- Nginx: APM에는 직접 영향 없음. WAS 내부 트레이스에 집중한다.

## 설치/적용 흐름 (개요)
1) Scouter Server(Collector) 준비
- Collector 설치 후 포트 오픈(기본 TCP 6100/6105 등)
- 운영 환경에서는 VPC 내부 Private Subnet에 설치 권장

2) Scouter Agent 적용
- Java Agent jar를 WAS 컨테이너에 포함 또는 마운트
- 실행 옵션에 `-javaagent` 추가
- 설정 파일에 service name, host, collector address 등록

3) 로컬/클라우드 분리 설정
- 로컬: `scouter.conf`에 `obj_name`/`net_collector_ip` 로컬용 값
- 클라우드: `scouter.conf` 또는 환경변수로 서비스명/인스턴스명 분리

## 결정이 필요한 항목 (장단점)

### 1) Collector 배치 위치
- VPC 내부 배치
  - 장점: 보안/접근 통제가 용이, 외부 네트워크 의존 최소화
  - 단점: 별도 인스턴스 운영 비용/관리 필요
- 외부/Public 배치
  - 장점: 구축 간단, 접근 편의
  - 단점: Outbound 허용 필요, 보안 정책 강화 필요

### 2) Agent 주입 방식
- Docker 이미지에 내장
  - 장점: 배포 시점에 동일하게 적용, 재현성 높음
  - 단점: 이미지 변경/배포 필요
- 런타임 마운트/옵션 주입
  - 장점: 기존 이미지 유지 가능
  - 단점: 배포 스크립트 복잡도 증가

### 3) 서비스명/인스턴스명 전략
- 환경별 분리(예: lunchgo-local, lunchgo-prod)
  - 장점: 로컬/운영 데이터 혼선 방지
  - 단점: 설정 관리 항목 증가

## obj_name 분리 전략 (확정)
형식: `[서비스명]-[환경]-[식별자]`

### 구성 요소 정의
- [서비스명]
  - 역할: 모니터링 대상 애플리케이션 구분
  - 예시: `lunchgo`
  - 확장: MSA 전환 시 `lunchgo-order`, `lunchgo-payment` 등으로 세분화
- [환경]
  - 역할: 로컬/개발/운영 데이터 분리
  - 예시: `local`, `dev`, `prod`(또는 `stg`)
  - 장점: 환경별 필터링 및 알림 설정 용이
- [식별자]
  - 역할: 동일 환경 내 다중 인스턴스 식별
  - 로컬: 개발자 이름 또는 머신 이름 권장
  - 클라우드: hostname 또는 컨테이너 ID 등 동적 값 권장

### 예시
- 로컬: `lunchgo-local-gildong`, `lunchgo-local-my-macbook`
- 클라우드: `lunchgo-prod-lunchgo-was-6dbcf8c4d-abcde`, `lunchgo-prod-e8a3f2d1b4c7`

## 적용 방법 (로컬/클라우드)

### 1) 로컬 트랙: scouter.conf 직접 설정
개발자 PC에서는 고정된 이름을 사용하는 것이 관리가 쉽다.

```ini
# Scouter object name
obj_name=lunchgo-local-gildong

# Scouter collector IP address (localhost or local network IP)
net_collector_ip=127.0.0.1
```

### 2) 클라우드 트랙: 환경변수 + 엔트리포인트 스크립트
obj_name은 동적으로 생성하고, scouter.conf에는 공통 설정만 둔다.

`entrypoint.sh` 예시

```bash
#!/bin/sh

# 환경변수가 설정되지 않았다면 기본값 사용
export SCOUTER_OBJ_NAME=${SCOUTER_OBJ_NAME:-lunchgo-prod-$(hostname)}

java -javaagent:/app/scouter/agent.java/scouter.agent.jar \
     -Dscouter.config=/app/scouter/conf/scouter.conf \
     -Dscouter.obj_name=${SCOUTER_OBJ_NAME} \
     -jar /app/app.jar
```

클라우드용 `scouter.conf` 예시

```ini
# obj_name은 외부(환경변수)에서 주입
# obj_name=

# NCP VPC 내부 Collector Private IP
net_collector_ip=10.0.1.100
```

## Dockerfile/Entrypoint 반영 예시

### Dockerfile 예시 (Agent 내장)
현재 레포의 루트 `Dockerfile`을 기준으로, Scouter Agent와 entrypoint를 포함하는 예시다.

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# Scouter Agent/Config 복사 (레포 루트 기준)
COPY scouter/ /app/scouter/
COPY scripts/scouter-entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

EXPOSE 8080
ENTRYPOINT ["/app/entrypoint.sh"]
```

### entrypoint.sh 예시
Collector 주소는 `scouter.conf`에 고정, obj_name만 동적 주입한다.

```bash
#!/bin/sh

export SCOUTER_OBJ_NAME=${SCOUTER_OBJ_NAME:-lunchgo-prod-$(hostname)}

exec java -javaagent:/app/scouter/agent.java/scouter.agent.jar \
  -Dscouter.config=/app/scouter/conf/scouter.conf \
  -Dscouter.obj_name=${SCOUTER_OBJ_NAME} \
  -jar /app/app.jar
```

### 파일 배치 예시 (레포 기준)
- Scouter Agent: `scouter/agent.java/scouter.agent.jar`
- Scouter Config: `scouter/conf/scouter.conf`
- Entrypoint: `scripts/scouter-entrypoint.sh`

### 장점 요약
- 유연성: 이미지 재빌드 없이 obj_name 변경 가능
- 확장성: Auto Scaling 시 인스턴스 식별 문제 최소화
- 가독성: `lunchgo-prod-*` 패턴으로 운영 환경 필터링 가능

## NCP VPC 환경 적용 고려사항
- Private Subnet WAS에서 Collector로 Outbound 통신이 가능해야 한다.
- NAT Gateway 경유 시 포트/방화벽 규칙 점검 필요.
- ACG에 Collector 포트 허용 규칙 추가 필요(Collector 위치에 따라 달라짐).

## ACG 규칙 (Collector 기준)
필수 인바운드

| 프로토콜 | 접근 소스 | 허용 포트 | 용도 |
| --- | --- | --- | --- |
| TCP | image-vpc-prod-default-acg(326796) | 6100 | Scouter Agent -> Collector |
| UDP | image-vpc-prod-default-acg(326796) | 6100 | Scouter Agent -> Collector (옵션) |

옵션 인바운드 (웹 UI)

| 프로토콜 | 접근 소스 | 허용 포트 | 용도 |
| --- | --- | --- | --- |
| TCP | image-bastion-host-acg(326799) | 6180 | Scouter Web UI |

## 진행 과정 (단계별)
1) Scouter Agent jar 준비
- `scouter/agent.java/scouter.agent.jar` 배치
- 버전 정보와 출처 기록
- 다운로드 위치: `https://github.com/scouter-project/scouter/releases`
- 파일: `scouter-all-[버전].tar.gz`
- 압축 해제 후 `agent.java/scouter.agent.jar` 사용

2) Dockerfile 반영
- 기존 `Dockerfile`에 Scouter 디렉터리/entrypoint 복사 추가
- `ENTRYPOINT`를 `/app/entrypoint.sh`로 변경

3) entrypoint 적용
- `scripts/scouter-entrypoint.sh` 사용
- `SCOUTER_OBJ_NAME` 환경변수로 obj_name 동적 주입

4) scouter.conf 반영
- `scouter/conf/scouter.conf`에 Collector Private IP 입력
- obj_name은 비워두고 외부 주입 원칙 유지

5) 로컬 검증
- 로컬에서 Agent 적용 후 트레이스/메트릭 수집 확인

6) 클라우드 검증
- WAS 컨테이너 재배포
- 핵심 플로우 트레이스 수집 확인

## Scouter Agent 다운로드 절차 (결정 사항)
1) 최신 버전 확인
- `https://github.com/scouter-project/scouter/releases`의 Latest 확인

2) 압축 파일 다운로드
- Assets에서 `scouter-all-[버전].tar.gz` 다운로드

3) 압축 해제
```bash
tar -xvf scouter-all-[버전].tar.gz
```

4) Agent jar 확인 및 배치
- 압축 해제 후 `scouter/agent.java/scouter.agent.jar` 확인
- 프로젝트 경로 `scouter/agent.java/scouter.agent.jar`에 배치

## 참고 경로 (압축 해제 후)
- Agent: `scouter/agent.java/scouter.agent.jar`
- Collector(서버): `scouter/server/`
- 설정 파일:
  - 서버용: `scouter/server/conf/scouter.conf`
  - 에이전트용: `scouter/agent.java/conf/scouter.conf`

## 체크리스트 (로컬)
- Java Agent 적용 여부 확인
- 트랜잭션 트레이스/느린 쿼리 확인
- 로컬 API 호출 시 지표 수집 확인

## 로컬 Collector 실행 (Docker)
로컬에서 Scouter Server를 띄워 Agent 데이터를 직접 수집한다. (JDK 11 필요)

### 1) 로컬 Scouter Server 설정
`~/Documents/scouter/server/conf/scouter.conf`
```ini
net_tcp_listen_port=6100
net_udp_listen_port=6100
net_http_port=6180
```

### 2) 로컬 Collector 컨테이너 실행
```bash
docker rm -f scouter-local-server || true
docker run -d --name scouter-local-server \
  -p 6100:6100 -p 6100:6100/udp -p 6180:6180 \
  -v "$HOME/Documents/scouter/server:/opt/scouter/server" \
  -w /opt/scouter/server \
  eclipse-temurin:11-jre \
  bash -lc "nohup java -Dscouter.config=/opt/scouter/server/conf/scouter.conf -Xmx1024m -classpath ./scouter-server-boot.jar scouter.boot.Boot ./lib > nohup.out & tail -f nohup.out"
```

### 3) 로컬 터널링 주의
로컬 Collector를 쓰는 경우 SSH 터널에서 6100/6180 포워딩은 제거한다.

## 체크리스트 (클라우드)
- Collector 접근성 확인 (Private Subnet -> Collector)
- WAS 컨테이너에 Agent 적용 확인
- 핵심 플로우(로그인/예약/결제) 트레이스 수집 확인

## Scouter Client 설치/접속/확인
Scouter Client는 데이터를 시각적으로 확인하는 분석 도구다.

### 1) Client 설치
- 다운로드 위치: `https://github.com/scouter-project/scouter/releases`
- OS별 다운로드 파일(Assets)
  - M1/M2 Mac: `scouter-client-cocoa.macosx.aarch64.zip`
  - Intel Mac: `scouter-client-cocoa.macosx.x86_64.zip`
  - Windows: `scouter-client-win32.win32.x86_64.zip`
- 설치/실행
  - 압축 해제 후 실행 파일을 더블클릭
  - macOS 경고 시 우클릭 -> "열기"로 실행

### 2) Collector 접속 정보 (로컬)
- Server Address: `127.0.0.1` (또는 `localhost`)
- Port: `6100`
- ID: `admin`
- Password: `admin`

### 3) 데이터 확인 절차
1) 객체 확인
   - Object List에서 `/M1-MacBook-Pro.local` 호스트 아래
     `/lunchgo-local-my-macbook` Java 객체가 보이는지 확인
   - 아이콘이 녹색이면 정상 통신 상태

2) API 호출 후 트레이스/응답시간 확인
   - Postman/브라우저/curl로 API 1~2건 호출
   - Realtime > Active Service: 실시간 트랜잭션 확인
   - Dashboard > Application: TPS/Response Time/Active Service 차트 확인
   - Response Time 차트에서 범위 선택 후 X-Log 상세 분석

## 로컬 컨테이너 실행 (.apm-local.env)
로컬 DB/Redis는 SSH 터널(`3307/6379`)을 사용하므로 컨테이너 실행 시 URL을 오버라이드한다.

`.apm-local.env`
```env
SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3307/lunchgo?useSSL=false&serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true&characterEncoding=UTF-8
SPRING_DATA_REDIS_HOST=host.docker.internal
SCOUTER_OBJ_NAME=lunchgo-local-my-macbook
```

실행
```bash
docker rm -f lunchgo-backend-apm-test || true
docker run -d --name lunchgo-backend-apm-test -p 8080:8080 \
  --env-file /Users/user/devStudy/LunchGO/.apm-local.env \
  -v "/Users/user/devStudy/LunchGO/scouter/conf/scouter.local.conf:/app/scouter/conf/scouter.conf:ro" \
  lunchgo-backend:apm-test
```

### 줄바꿈 주의
`serverTimezone=Asia/Seoul`이 줄바꿈되면 JDBC URL 오류가 발생한다.

## Nginx 리다이렉트(영구 적용)
Scouter Webapp는 기본 루트(`/`)에 매핑된 페이지가 없어 403이 발생할 수 있다.
Nginx에서 `/scouter` 경로로 리다이렉트/프록시를 구성하면 안전하게 접근할 수 있다.

### 적용 흐름 (Bastion Docker Nginx 기준)
1) 기존 설정 백업
```bash
docker exec my-nginx cat /etc/nginx/conf.d/default.conf > /root/nginx-default.conf.bak
```

2) 호스트에 설정 파일 생성
```bash
cat <<'EOF' > /root/nginx-default.conf
server {
    listen       80;
    server_name  localhost;

    location / {
        root   /usr/share/nginx/html;
        index  index.html index.htm;
    }

    location /api/ {
        proxy_pass http://10.0.2.6:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location = /scouter {
        return 302 /scouter/extweb/index.html;
    }

    location /scouter/ {
        proxy_pass http://10.0.2.6:6180/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    error_page   500 502 503 504  /50x.html;
    location = /50x.html {
        root   /usr/share/nginx/html;
    }
}
EOF
```

3) 컨테이너 재생성(마운트 적용)
```bash
docker rm -f my-nginx
docker run -d --name my-nginx -p 80:80 \
  -v /root/nginx-default.conf:/etc/nginx/conf.d/default.conf:ro \
  nginx
```

4) 확인
```bash
docker exec my-nginx nginx -t
```

### 접속 경로
- `http://<bastion-ip>/scouter`
- `http://<bastion-ip>/scouter/extweb/index.html`

## 트러블슈팅 (운영)

### 1) WAS 컨테이너가 즉시 종료됨 (Scouter Agent 경로 누락)
**증상**
- 컨테이너가 `Exited (1)`로 바로 종료
- 로그: `Error opening zip file or JAR manifest missing : /app/scouter/agent.java/scouter.agent.jar`

**원인**
- `entrypoint.sh`가 `-javaagent`를 필수로 로드하는데, 이미지/마운트에 Agent JAR이 없음

**해결**
- Agent/Conf 마운트 후 재실행
```bash
docker rm -f lunchgo-backend || true
docker run --env-file /opt/lunchgo/.env \
  -e SCOUTER_OBJ_NAME=lunchgo-prod-$(hostname) \
  -p 8080:8080 --name lunchgo-backend -d \
  -v /opt/scouter/scouter/agent.java:/app/scouter/agent.java:ro \
  -v /opt/scouter/scouter/agent.java/conf/scouter.conf:/app/scouter/conf/scouter.conf:ro \
  pgw10243/lunchgo-backend:dev
```

**마운트 옵션 구분**
- `-v /opt/scouter/scouter/agent.java:/app/scouter/agent.java:ro`
  - Agent JAR이 포함된 디렉터리(필수). 없으면 컨테이너가 즉시 종료됨.
- `-v /opt/scouter/scouter/agent.java/conf/scouter.conf:/app/scouter/conf/scouter.conf:ro`
  - 환경별 설정 파일 주입(Collector IP/포트). Agent 디렉터리 마운트와 별도.

### 2) Collector UI에 객체가 보이지 않음
**증상**
- Agent 로그는 찍히는데 UI에 객체가 표시되지 않음

**원인**
- Collector가 UDP 6100 포트를 열지 않음 (컨테이너 포트/설정 누락)
- Agent `net_collector_ip` 미설정

**해결**
1) Agent 설정
```
net_collector_ip=10.0.2.6
net_collector_tcp_port=6100
net_collector_udp_port=6100
```

2) Collector 설정
```
net_tcp_listen_port=6100
net_udp_listen_port=6100
net_http_port=6180
```

3) Collector 컨테이너 재실행 (UDP 6100 포함, 6180은 webapp 컨테이너가 사용 중이면 제외)
```bash
docker rm -f scouter-server
docker run -d --name scouter-server \
  -p 6100:6100 -p 6100:6100/udp \
  -v /opt/scouter/scouter/server:/opt/scouter/server \
  -w /opt/scouter/server \
  eclipse-temurin:11-jre \
  bash -lc "nohup java -Dscouter.config=/opt/scouter/server/conf/scouter.conf -Xmx1024m -classpath ./scouter-server-boot.jar scouter.boot.Boot ./lib > nohup.out & tail -f nohup.out"
```

### 3) Today Visitor가 0으로 보임
**증상**
- IP Summary/XLog는 정상인데 `Today Visitor`만 0

**원인**
- Visitor는 HTTP Session 생성 기준으로 집계됨
- REST API가 Stateless로 동작하면 `JSESSIONID`가 생성되지 않아 Visitor가 0일 수 있음

**확인**
```bash
curl -i http://<bastion-ip>/api/login | rg -i "Set-Cookie|JSESSIONID"
```
- `JSESSIONID`가 없으면 Visitor 0이 정상 동작

**대응**
- Visitor 대신 `IP Summary`, `XLog`, `Active Service`, `TPS/Elapsed`를 기준으로 모니터링

## macOS Scouter Client 실행 이슈(압축 해제 문제)
macOS에서 Scouter Client가 SIGSEGV로 종료되는 경우, 잘못된 압축 해제가 원인일 수 있다.

### 해결 방법
- tar로 정확히 해제한다.
```bash
tar -xvzf scouter.client.product-macosx.cocoa.aarch64.tar.gz
```

### 추가 확인
- 기존에 잘못 풀린 앱이 있으면 삭제 후 재설치
```bash
rm -rf /Users/USER/Documents/scouter.client.app
```

## 다음 작업 제안
- Scouter Agent jar 확보 및 배치
- Dockerfile 반영 및 entrypoint 적용
- Collector Private IP 확정 후 `scouter.conf` 업데이트
