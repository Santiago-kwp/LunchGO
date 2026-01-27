# 로컬 부하 테스트 관련 트러블슈팅

## 진행 배경

- ncp 원격 서버의 사용 기한이 종료되었으나, AWS를 사용하면서 수천 건의 예약 요청에 관한 부하 테스트를 진행하기에는 유료 계정이란 부담이 존재
- 결국 비용 문제로 인해 부득이하게 로컬 환경에서 부하 테스트를 진행
    - 인텔리제이 터미널에서 바로 k6로 부하테스트를 진행하기 위해 필요한 최소한의 요건만 정리

## 로컬 부하 테스트 결과 활용 시 주의사항

- 구현한 로직의 정밀함을 검증하는 상황에서는 유용하게 활용 가능
    - 어플리케이션상의 병목 지점 확인 - 기본적인 응답 속도를 확인
    - 멱등성 충족 여부 확인 - 중복 예약 방지 처리 여부를 확인(상태코드값 및 DB 데이터 확인)
    - 에러 핸들링 - 부하 발생 시 서버가 반환하는 상태코드값을 확인
- **로컬 부하 테스트 결과를 바탕으로 실제 운영 환경의 최대 성능을 추정하기에는 한계가 존재**
    - 다음 요인들로 인해 로컬 기기 사양과 실제 운영 서버의 사양 간 차이가 존재하기 때문
        - 자원 경합
            - 실제 운영 환경에서는 실제 부하 발생기와 서버가 분리되어 있지만, 로컬 환경에서는 부하 발생기와 서버가 CPU, RAM 등의 리소스를 공유
            - 실제 운영 환경보다 성능이 낮게 측정될 수 있음
        - 네트워크 지연의 부재
            - 실제 운영 환경에서는 패킷 전송 시 수십~수백 ms의 네트워크 지연(Latency)이 발생하지만, 로컬 환경에서는 패킷이 외부 망을 타지 않는 루프백(`127.0.0.1`) 방식을 사용
            - 즉, 로컬 부하 테스트 진행 시 실제 운영 환경보다 네트워크 지연 시간이 더 짧게 측정될 수 있음.
        - 가상화 및 OS의 제약
            - Docker 오버헤드 발생
                - 윈도우/Mac에서 도커는 가상 머신(VM) 위에서 돌아가므로 네이티브 리눅스 서버보다 입출력(I/O) 성능이 하락
            - 포트 고갈
                - 로컬 PC의 OS 설정에 따라서는, 단시간에 수천 개의 연결을 생성할 시 `Ephemeral Port` 부족으로 인해 실제 서버 성능과 상관없이 요청이 실패할 가능성이 존재
- **종합 정리**
    - 로컬 부하 테스트 결과에서, 실제 출력된 수치보다는 주요 성능 지표의 변화량에 주목하여 결과를 분석해야 함
    - 따라서, 로컬 부하 테스트는 아래와 같이 활용되어야 함.
        - 회귀 테스트: 코드 수정 전후의 성능 변화 비교
        - 설정 최적화: DB/WAS 설정 변경 전후의 성능(응답 속도) 변화 비교 → 상대적인 효율 측정

## 사전 준비 - 공통

### Redis 설치 or Docker 이미지 활용

- 로컬 환경에서 실행할 때, Redis가 설치되어 있지 않을 경우 실행 불가능
- 다음 2가지 방법 중 택1
    - 로컬 기기에 직접 Redis 설치 후 실행
    - Docker 이미지 활용 → 로컬 기기 내 Redis 설치 불필요

### k6 설치 - Windows 기준

1. Powershell을 관리자 권한으로 실행
2. Chocolatey 패키지 매니저 설치 및 확인
    - Chocolatey: 윈도우용 오픈소스 시스템 패키지 매니저
    - 아래의 명령어들을 순서대로 실행

    ```bash
    # chocolatey 설치
    Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
    
    # chocolatey 설치 확인
    choco
    ```

    - powershell 또는 cmd에서 `choco` 입력 시, 아래와 같이 출력되면 정상적으로 설치 완료된 것

   ![stress-test-local-1.png](/docs/images/stress-test-local-1.png)

3. k6 설치 및 확인

    ```bash
    # k6 설치
    choco install k6 [--version 원하는_버전]
    
    # k6 설치 확인
    k6
    ```

    - powershell 또는 cmd에서 `k6` 입력 시 아래와 같이 출력되면 정상적으로 설치 완료된 것

   ![stress-test-local-2.png](/docs/images/stress-test-local-2.png)


### k6 테스트 코드 수정

- 프로젝트 폴더 내 `scripts/k6_reservation_loadtest_idempotence.js` 에서 상수 `baseUrl` 의 기본값을 원격 private 서버의 IP에서 [`http://127.0.0.1:8080`](http://127.0.0.1:8080) 으로 변경 필수

    ```jsx
    import http from "k6/http";
    import { check, sleep } from "k6";
    
    const baseUrl = __ENV.BASE_URL || "http://127.0.0.1:8080";
    
    ... 코드 생략
    ```


## 사전 준비 - 예약 부하 테스트

### 인텔리제이 설정 수정

1. 인텔리제이 화면 상단에서 `Edit Configuration` 클릭
2. `Run/Debug Configuration`에서 Environment variable의 값으로 `LOGIN_QUEUE_ENABLED=false` 추가
    - 바로 Environment variable 입력창이 보이지 않을 경우, `Modify options` 클릭 후 `Operating System` 의 하위 항목 중 `Environment variable` 클릭

   ![stress-test-local-3.png](/docs/images/stress-test-local-3.png)


### 테스트 입력값 수정

- 백엔드 서버 실행 후, 인텔리제이 터미널에서 아래의 명령어를 실행(Windows 기준으로 작성)
    - 윈도우에서는 git bash가 `/scripts`와 같은 경로를 `C:/Program Files/Git/scripts`로 자동 변환 → `MSYS_NO_PATHCONV=1` 옵션을 사용하여 자동 변환을 방지
    - Mac 사용자일 경우, 아래 명령어에서 `MSYS_NO_PATHCONV=1` 옵션만 삭제하고 실행하면 됨

    ```bash
    MSYS_NO_PATHCONV=1 docker run --rm -i \
        -v "/$(pwd)/scripts:/scripts" \
        --network="host" \
        grafana/k6 run /scripts/k6_reservation_loadtest_idempotence.js \
        -e BASE_URL=http://host.docker.internal:8080 \
        -e EMAIL_PREFIX=loadtest.user \
        -e EMAIL_DOMAIN=example.com \
        -e PASSWORD='Passw0rd!123' \
        -e RESTAURANT_ID=67 \
        -e SLOT_DATE=2026-01-29 \
        -e SLOT_TIME=11:00 \
        -e PARTY_SIZE=4 \
        -e RESERVATION_TYPE=RESERVATION_DEPOSIT \
        -e LOAD_VUS=2000 \
        -e USE_LOGIN_QUEUE=true \
        -e LOGIN_QUEUE_POLL_MS=1000 \
        -e LOGIN_QUEUE_MAX_WAIT_MS=180000 \
        -e SETUP_TIMEOUT=30m \
        -e LOAD_DURATION=30s
    ```


## 로컬 부하 테스트 시나리오

- 예약은 모든 사용자들이 로그인한 상태에서 동시에 진행
- `RESTAURANT_ID=67` 또는 `RESTAURANT_ID=117`만을 사용
    - 예약 정원이 40명인 식당을 대상으로, 2000명의 사용자가 동시에 예약 요청을 전송
    - 각 사용자별 예약인원수(`PARTY_SIZE`)는 4명
        - 정상 종료 시 오직 10건의 예약만 접수, 나머지는 실패 처리
    - 각 사용자는 동일한 예약 요청을 3회씩 전달
        - 최초 1회의 요청만 선착순으로 접수, 이후의 중복된 예약 요청은 차단
    - 위 작업을 30초(`LOAD_DURATION=30s`) 이내에 완수해야 부하 테스트 최종 통과

## 초기 로컬 부하 테스트 수행 결과 및 문제 상황

<details>

  <summary>부하 테스트 수행 결과</summary>

```java
running (4m08.8s), 1928/2000 VUs, 303 complete and 0 interrupted iterations
reservations   [   5% ] 2000 VUs  00.7s/30s  0303/6000 iters, 3 per VU

running (4m09.8s), 1558/2000 VUs, 1364 complete and 0 interrupted iterations
reservations   [  23% ] 2000 VUs  01.6s/30s  1364/6000 iters, 3 per VU

running (4m10.8s), 1222/2000 VUs, 2381 complete and 0 interrupted iterations
reservations   [  40% ] 2000 VUs  02.7s/30s  2381/6000 iters, 3 per VU

running (4m11.8s), 0930/2000 VUs, 3258 complete and 0 interrupted iterations
reservations   [  54% ] 2000 VUs  03.7s/30s  3258/6000 iters, 3 per VU

running (4m12.8s), 0607/2000 VUs, 4218 complete and 0 interrupted iterations
reservations   [  70% ] 2000 VUs  04.7s/30s  4218/6000 iters, 3 per VU

running (4m13.8s), 0290/2000 VUs, 5163 complete and 0 interrupted iterations
reservations   [  86% ] 2000 VUs  05.6s/30s  5163/6000 iters, 3 per VU

running (4m14.8s), 0002/2000 VUs, 5994 complete and 0 interrupted iterations
reservations   [ 100% ] 2000 VUs  06.6s/30s  5994/6000 iters, 3 per VU

running (4m15.8s), 0002/2000 VUs, 5994 complete and 0 interrupted iterations
reservations   [ 100% ] 2000 VUs  07.6s/30s  5994/6000 iters, 3 per VU

running (4m16.8s), 0002/2000 VUs, 5994 complete and 0 interrupted iterations
reservations   [ 100% ] 2000 VUs  08.6s/30s  5994/6000 iters, 3 per VU

running (4m17.8s), 0002/2000 VUs, 5994 complete and 0 interrupted iterations
reservations   [ 100% ] 2000 VUs  09.6s/30s  5994/6000 iters, 3 per VU

running (4m18.8s), 0002/2000 VUs, 5994 complete and 0 interrupted iterations
reservations   [ 100% ] 2000 VUs  10.6s/30s  5994/6000 iters, 3 per VU

running (4m19.8s), 0002/2000 VUs, 5994 complete and 0 interrupted iterations
reservations   [ 100% ] 2000 VUs  11.6s/30s  5994/6000 iters, 3 per VU

running (4m20.8s), 0002/2000 VUs, 5994 complete and 0 interrupted iterations
reservations   [ 100% ] 2000 VUs  12.6s/30s  5994/6000 iters, 3 per VU

running (4m21.8s), 0002/2000 VUs, 5994 complete and 0 interrupted iterations
reservations   [ 100% ] 2000 VUs  13.6s/30s  5994/6000 iters, 3 per VU

running (4m22.8s), 0002/2000 VUs, 5994 complete and 0 interrupted iterations
reservations   [ 100% ] 2000 VUs  14.6s/30s  5994/6000 iters, 3 per VU

running (4m23.8s), 0002/2000 VUs, 5994 complete and 0 interrupted iterations
reservations   [ 100% ] 2000 VUs  15.6s/30s  5994/6000 iters, 3 per VU

running (4m24.8s), 0002/2000 VUs, 5994 complete and 0 interrupted iterations
reservations   [ 100% ] 2000 VUs  16.6s/30s  5994/6000 iters, 3 per VU

running (4m25.8s), 0002/2000 VUs, 5994 complete and 0 interrupted iterations
reservations   [ 100% ] 2000 VUs  17.6s/30s  5994/6000 iters, 3 per VU

running (4m26.8s), 0002/2000 VUs, 5994 complete and 0 interrupted iterations
reservations   [ 100% ] 2000 VUs  18.6s/30s  5994/6000 iters, 3 per VU

running (4m27.8s), 0002/2000 VUs, 5994 complete and 0 interrupted iterations
reservations   [ 100% ] 2000 VUs  19.6s/30s  5994/6000 iters, 3 per VU

running (4m28.8s), 0002/2000 VUs, 5994 complete and 0 interrupted iterations
reservations   [ 100% ] 2000 VUs  20.6s/30s  5994/6000 iters, 3 per VU

running (4m29.8s), 0002/2000 VUs, 5994 complete and 0 interrupted iterations
reservations   [ 100% ] 2000 VUs  21.6s/30s  5994/6000 iters, 3 per VU

running (4m30.8s), 0002/2000 VUs, 5994 complete and 0 interrupted iterations
reservations   [ 100% ] 2000 VUs  22.6s/30s  5994/6000 iters, 3 per VU

running (4m31.8s), 0002/2000 VUs, 5994 complete and 0 interrupted iterations
reservations   [ 100% ] 2000 VUs  23.6s/30s  5994/6000 iters, 3 per VU

running (4m32.8s), 0002/2000 VUs, 5994 complete and 0 interrupted iterations
reservations   [ 100% ] 2000 VUs  24.6s/30s  5994/6000 iters, 3 per VU

running (4m33.8s), 0001/2000 VUs, 5997 complete and 0 interrupted iterations
reservations   [ 100% ] 2000 VUs  25.6s/30s  5997/6000 iters, 3 per VU

running (4m34.8s), 0001/2000 VUs, 5997 complete and 0 interrupted iterations
reservations   [ 100% ] 2000 VUs  26.6s/30s  5997/6000 iters, 3 per VU

running (4m35.8s), 0001/2000 VUs, 5997 complete and 0 interrupted iterations
reservations   [ 100% ] 2000 VUs  27.6s/30s  5997/6000 iters, 3 per VU

running (4m36.8s), 0001/2000 VUs, 5997 complete and 0 interrupted iterations
reservations   [ 100% ] 2000 VUs  28.6s/30s  5997/6000 iters, 3 per VU

running (4m37.8s), 0001/2000 VUs, 5997 complete and 0 interrupted iterations
reservations   [ 100% ] 2000 VUs  29.6s/30s  5997/6000 iters, 3 per VU

running (4m38.8s), 0001/2000 VUs, 5997 complete and 0 interrupted iterations
reservations ↓ [ 100% ] 2000 VUs  30s  5997/6000 iters, 3 per VU

running (4m39.8s), 0001/2000 VUs, 5997 complete and 0 interrupted iterations
reservations ↓ [ 100% ] 2000 VUs  30s  5997/6000 iters, 3 per VU

running (4m40.8s), 0001/2000 VUs, 5997 complete and 0 interrupted iterations
reservations ↓ [ 100% ] 2000 VUs  30s  5997/6000 iters, 3 per VU
time="2026-01-26T12:12:56Z" level=warning msg="Request Failed" error="Post \"http://host.docker.internal:8080/api/reservations\": dial: i/o timeout"

        █ THRESHOLDS

        http_req_failed
        ✗ 'rate<0.01' rate=59.89%

        █ TOTAL RESULTS

checks_total.......: 13996  49.765044/s
checks_succeeded...: 99.99% 13995 out of 13996
checks_failed......: 0.00%  1 out of 13996

        ✓ login status 200
        ✗ first request is 2xx or 4xx
          ↳  99% — ✓ 5997 / ✗ 1
                  ✓ subsequent requests are blocked (4xx)

HTTP
http_req_duration..............: avg=63.13ms min=0s     med=54.98ms max=649.1ms p(90)=130.92ms p(95)=154.52ms
{ expected_response:true }...: avg=62.66ms min=1.3ms  med=80.8ms  max=649.1ms p(90)=156.16ms p(95)=172.03ms
http_req_failed................: 59.89% 5988 out of 9998
http_reqs......................: 9998   35.549508/s

        EXECUTION
dropped_iterations.............: 2      0.007111/s
iteration_duration.............: avg=83.78ms min=5.35ms med=61.87ms max=30s     p(90)=119.28ms p(95)=154.87ms
iterations.....................: 5998   21.32686/s
vus............................: 1      min=0            max=1930
vus_max........................: 2000   min=2000         max=2000

NETWORK
data_received..................: 5.5 MB 20 kB/s
data_sent......................: 4.3 MB 15 kB/s

running (4m41.2s), 0000/2000 VUs, 5998 complete and 0 interrupted iterations
reservations ✓ [ 100% ] 2000 VUs  30s  5998/6000 iters, 3 per VU
time="2026-01-26T12:12:56Z" level=error msg="thresholds on metrics 'http_req_failed' have been crossed"
```

</details>

- **문제 : `dial: i/o timeout` 에러 발생**
    - 6.6초 지점에 이미 대부분의 작업(5,994 iters)이 완료되었으나, 나머지 20.6초 동안 단 1~2명의 VU가 종료되지 않고 계속 `running` 상태를 유지하다가 30초를 채운 후 타임아웃이 발생
- 동시 예약 진행 시, 멱등성은 어느 정도(2,000명 동시 접속 시 약 99.95%의 성공률) 보장됨을 확인
    - `checks_failed: 1 out of 13996` :
        - `first request is 2xx or 4xx` 체크에서 **✗ 1**이 발생
        - 이 1건의 실패는 서버가 멱등성을 보장하지 못해서 실패한 것이 아닌, 네트워크 연결 자체가 실패(`i/o timeout`)해서 응답을 아예 받지 못한 것이 원인이 되어 발생
    - 서버 로그에서는 타임아웃 관련 로그가 출력되지 않음

## 문제 원인 분석 및 해결 방안

### 문제 원인 분석

- **추정 원인 1: TCP 커넥션 고갈 또는 드랍**
    - OS의 네트워크 스택에서 특정 패킷이 유실되거나 핸드셰이크 과정에서 밀려나는 현상이 발생할 수 있음
    - 로컬 환경에서는 네트워크 카드 없이 요청을 처리하므로, 요청이 너무 몰리면 특정 소켓 연결이 '먹통'이 되어 응답도 에러도 없이 무한 대기에 빠지는 현상이 발생할 수 있음
- **추정 원인 2: 백엔드 스레드 풀/DB 커넥션 풀 병목**
    - 서버 측에서 2,000개의 요청을 처리하는 과정에서 마지막 한두 개의 요청이 커넥션 풀을 할당받지 못한 채 큐에서 대기하다가, k6/Docker에서 설정한 네트워크 타임아웃에 도달
    - 콘솔창에서 아래와 같이 `dial: i/o timeout`에 관한 에러 메시지가 출력된 것이 그 근거

        ```bash
        time="2026-01-26T12:12:56Z" level=warning msg="Request Failed" error="Post \"http://host.docker.internal:8080/api/reservations\": dial: i/o timeout"
        ```

    - 백엔드 서버에서는 타임아웃 관련 에러 로그가 출력되지 않았지만 k6에서는 출력됨
        - 요청이 서버의 애플리케이션 계층(Spring Boot/Tomcat)에 도달하기도 전에 네트워크 레벨에서 차단된 것으로 추정
            - 서버 로그(Spring Boot)에 기록이 남으려면 Tomcat의 Acceptor 스레드가 해당 요청을 수신해야 하지만, 그러지 못했기 때문에 서버 로그가 출력되지 않은 것
- **추정 원인 3: 로컬 자원의 해제 속도**
    - 윈도우는 리눅스 서버에 비해 동적 포트(Ephemeral Port) 자원이 부족하고 회수 속도가 느림
    - 2,000명에 달하는 대규모 인원이 짧은 시간에 몰렸다가 빠지는 과정에서 OS와 Docker가 뒷정리를 다 하지 못한 상태로 다음 테스트를 진행할 시, 이전 테스트 진행 과정에서 누적된 데이터로 인해 아래와 같은 병목 현상이 발생
        - **TCP TIME_WAIT 및 소켓 고갈**
            - **무한 대기로 인한 타임아웃이 발생했다는 점에서, 유력한 원인으로 추정**
            - 테스트 완료 시 대부분의 TCP 연결은 종료되지만, OS에서는 패킷 유실을 대비해 일정 시간 동안 일부 TCP 연결을 `TIME_WAIT` 상태로 유지
            - 두번째 테스트부터는 직전의 테스트에서 사용된 `TIME_WAIT` 상태의 소켓들이 TCP 연결을 미리 점유하게 되어, 새로운 요청이 소켓을 할당받기 위해 대기하는 과정에서 `dial i/o timeout`이나 응답 지연이 발생
        - **JVM 및 OS의 리소스 누적**
            - 백엔드가 인텔리제이에서 직접 실행 중이므로, 반복된 부하로 인해 JVM의 Heap 메모리가 가득 차거나 GC(Garbage Collection) 실행 빈도가 증가했을 가능성이 존재
        - **Docker 가상 네트워크 병목**
            - 사용자의 요청은 `[k6 컨테이너] → [도커 네트워크 브릿지] → [윈도우 호스트 통로] → [백엔드]` 순으로 이동
            - `host.docker.internal`을 통해 도커에서 호스트로 통신할 때 사용하는 가상 네트워크 브릿지도 짧은 간격으로 부하가 반복되면 성능 저하가 발생
                - 도커가 호스트로 연결을 넘겨주는 단계에서 대량의 요청이 동시에 집중되면서 도커 엔진과 호스트 간 통신을 관리하는 사용자 공간 프록시(userland-proxy)에 과부하가 발생하여 타임아웃을 유발할 수 있음
    - 따라서, 로컬에서 부하 테스트를 진행할 경우, 마지막 부하 테스트 완료 시점으로부터 약 2~3분 뒤에 다음 부하 테스트를 진행하는 것이 좋음

### 종합 결론 & 해결 방안

- **TCP TIME_WAIT 및 소켓 고갈 관련 해결 방안**
    - k6 스크립트 수정: k6 HTTP 타임아웃 설정 추가 및 미세 지연(Jitter) 로직 추가
- **백엔드 스레드 풀 및 DB 커넥션 풀 병목 현상 관련 해결 방안**
    - tomcat 스레드 풀 관련 설정 추가
        - tomcat 스레드 풀의 크기 확장
        - 연결 유지 시간(keep-alive) 및 최대 연결 수(max-connection) 관련 설정 추가
    - DB 커넥션 풀 설정 추가: DB 커넥션 풀 확장 및 커넥션 유지 시간 조정
- 로컬 부하 테스트 간격 조정
    - 테스트 완료 후 다음 테스트 진행 전 휴지기(최소 2~3분, 최대 7분)를 부여

## 1차 개선 시도: 테스트 코드 수정

### 주요 변경 사항

- `scripts/k6_reservation_loadtest_idempotence.js` 의 테스트 코드 수정
    - k6 HTTP 타임아웃 설정 추가
    - 미세 지연 로직 추가
    - timeout 관련 테스트 항목 추가

    ```jsx
    ... 코드 생략
    
    export default function (users) {
      const vu = __VU;
      const iter = __ITER;
      const user = users[vu - 1];
    
      // 첫 번째 요청(중요한 요청) 전에 VU 번호에 비례해 아주 미세한 대기 시간(Jitter) 부여
      // 500명 기준 최대 0.5초 내외로 분산됨
      if (iter === 0) {
        sleep(Math.random() * 0.5);
      }
    
      if (!user) {
        console.error(`VU ${vu} has no user data.`);
        return;
      }
    
      const { token, userId } = user;
    
      const reservePayload = JSON.stringify({
        userId,
        restaurantId,
        slotDate,
        slotTime,
        partySize,
        reservationType,
        requestMessage: null,
      });
    
      const reserveRes = http.post(`${baseUrl}/api/reservations`, reservePayload, {
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        timeout: '10s', // 30초나 기다리지 않고 10초 만에 실패 처리하여 지표 왜곡 방지
      });
    
      check(reserveRes, {
        // 통신 자체가 실패(status 0)한 경우를 대비해 상태 코드 존재 확인 추가
        "is not timed out": (r) => r.status !== 0,
        "first request is 2xx or 4xx": (r) => iter === 0 ? (r.status >= 200 && r.status < 500) : true,
        "subsequent requests are blocked (4xx)": (r) => iter > 0 ? (r.status >= 400 && r.status < 500) : true,
      });
    }
    
    ... 코드 생략
    ```


### 부하 테스트 수행 결과 요약

- 초기 로컬 부하 테스트와는 달리, 총 실행 시간이 30초를 초과하기 전에 부하 테스트를 종료
- 총 응답 시간은 11.1초
- 부하 테스트 진행 도중 타임아웃이 2회 발생
    - 이는 여전히 DB 커넥션 및 네트워크상의 병목 현상이 해소되지 않았음을 의미

<details>
  <summary>인텔리제이 콘솔창 출력 결과</summary>

```bash
running (3m33.8s), 2000/2000 VUs, 100 complete and 0 interrupted iterations
reservations   [   2% ] 2000 VUs  00.4s/30s  0100/6000 iters, 3 per VU

running (3m34.8s), 1689/2000 VUs, 1015 complete and 0 interrupted iterations
reservations   [  17% ] 2000 VUs  01.4s/30s  1015/6000 iters, 3 per VU

running (3m35.8s), 1372/2000 VUs, 1940 complete and 0 interrupted iterations
reservations   [  32% ] 2000 VUs  02.4s/30s  1940/6000 iters, 3 per VU

running (3m36.8s), 1100/2000 VUs, 2742 complete and 0 interrupted iterations
reservations   [  46% ] 2000 VUs  03.4s/30s  2742/6000 iters, 3 per VU

running (3m37.8s), 0794/2000 VUs, 3668 complete and 0 interrupted iterations
reservations   [  61% ] 2000 VUs  04.4s/30s  3668/6000 iters, 3 per VU

running (3m38.8s), 0460/2000 VUs, 4671 complete and 0 interrupted iterations
reservations   [  78% ] 2000 VUs  05.4s/30s  4671/6000 iters, 3 per VU

running (3m39.8s), 0186/2000 VUs, 5487 complete and 0 interrupted iterations
reservations   [  91% ] 2000 VUs  06.4s/30s  5487/6000 iters, 3 per VU
time="2026-01-26T12:57:25Z" level=warning msg="Request Failed" error="Post \"http://host.docker.internal:8080/api/reservations\": request timeout"

running (3m40.8s), 0001/2000 VUs, 5997 complete and 0 interrupted iterations
reservations   [ 100% ] 2000 VUs  07.4s/30s  5997/6000 iters, 3 per VU

running (3m41.8s), 0001/2000 VUs, 5997 complete and 0 interrupted iterations
reservations   [ 100% ] 2000 VUs  08.4s/30s  5997/6000 iters, 3 per VU

running (3m42.8s), 0001/2000 VUs, 5997 complete and 0 interrupted iterations
reservations   [ 100% ] 2000 VUs  09.4s/30s  5997/6000 iters, 3 per VU

running (3m43.8s), 0001/2000 VUs, 5997 complete and 0 interrupted iterations
reservations   [ 100% ] 2000 VUs  10.4s/30s  5997/6000 iters, 3 per VU
time="2026-01-26T12:57:29Z" level=warning msg="Request Failed" error="Post \"http://host.docker.internal:8080/api/reservations\": request timeout"

  █ THRESHOLDS

    http_req_failed
    ✗ 'rate<0.01' rate=59.90%

  █ TOTAL RESULTS

    checks_total.......: 14000  62.348383/s
    checks_succeeded...: 99.98% 13998 out of 14000
    checks_failed......: 0.01%  2 out of 14000

    ✓ login status 200
    ✗ first request is 2xx or 4xx
      ↳  99% — ✓ 5998 / ✗ 2
    ✓ subsequent requests are blocked (4xx)

    HTTP
    http_req_duration..............: avg=61.4ms  min=0s     med=59.85ms max=900.61ms p(90)=112.02ms p(95)=139.72ms
      { expected_response:true }...: avg=54.36ms min=1.35ms med=80.56ms max=900.61ms p(90)=135.02ms p(95)=157.83ms
    http_req_failed................: 59.90% 5990 out of 10000
    http_reqs......................: 10000  44.534559/s

    EXECUTION
    iteration_duration.............: avg=77.93ms min=6.25ms med=66.51ms max=5s       p(90)=119.23ms p(95)=144.27ms
    iterations.....................: 6000   26.720736/s
    vus............................: 1      min=0             max=2000
    vus_max........................: 2000   min=2000          max=2000

    NETWORK
    data_received..................: 5.5 MB 25 kB/s
    data_sent......................: 4.2 MB 19 kB/s

running (3m44.5s), 0000/2000 VUs, 6000 complete and 0 interrupted iterations
reservations ✓ [ 100% ] 2000 VUs  11.1s/30s  6000/6000 iters, 3 per VU
time="2026-01-26T12:57:29Z" level=error msg="thresholds on metrics 'http_req_failed' have been crossed"
```

</details>


## 2차 개선 시도: tomcat 및 DB 커넥션 관련 설정 추가

### application.yml 관련 주요 변경사항

- tomcat 서버 스레드 관련 설정 추가
    - 식당 한 곳을 대상으로 동시에 발생한 2,000 건의 예약 요청을 처리할 수 있도록 `max-connections`, `keep-alive-timeout`,  `accept-count` 등의 값을 최대한 넉넉하게 설정
- DB 커넥션 풀에 관한 설정 추가
    - 커넥션 풀의 크기(`maximum-pool-size`)를 10 → 50으로 확장

```yaml
server:
  tomcat:
    # 1. 동시에 처리할 스레드 수 (기본값 200)
    # 로컬 CPU 코어 수의 2~4배 정도가 적당하지만, 2000명을 쳐내기 위해 500~800까지 늘려봅니다.
    threads:
      max: 800
      min-spare: 100
    
    # 2. 스레드가 꽉 찼을 때 대기할 수 있는 요청 수 (기본값 100)
    # 이 수치가 작으면 OS가 요청을 거절하여 'dial timeout' 발생
    accept-count: 2000
    
    # 3. 서버가 동시에 유지할 수 있는 최대 연결 수 (기본값 8,192)
    max-connections: 10000
    
    # 4. 연결 유지(Keep-Alive) 설정 (소켓 고갈 방지를 위한 핵심)
    # 하나의 소켓으로 여러 번의 예약 요청을 처리하게 하여 TIME_WAIT 발생 빈도를 낮춤
    keep-alive-timeout: 60000 # 60초
    max-keep-alive-requests: 200
    

spring:
  datasource:
    hikari:
      maximum-pool-size: 50 # 기본값 10에서 확장
      connection-timeout: 30000 # 30초(기본값 유지)

```

### 부하 테스트 결과 요약

- 최초로 타임아웃 없이 `checks_succeeded` 100% 달성
- 총 응답 시간은 7.4초로 단축

<details>

  <summary>콘솔창 출력 결과</summary>

```bash
running (3m36.9s), 1917/2000 VUs, 379 complete and 0 interrupted iterations
reservations   [   6% ] 2000 VUs  00.9s/30s  0379/6000 iters, 3 per VU

running (3m37.8s), 1684/2000 VUs, 1057 complete and 0 interrupted iterations
reservations   [  18% ] 2000 VUs  01.8s/30s  1057/6000 iters, 3 per VU

running (3m38.8s), 1339/2000 VUs, 2070 complete and 0 interrupted iterations
reservations   [  34% ] 2000 VUs  02.8s/30s  2070/6000 iters, 3 per VU

running (3m39.8s), 1054/2000 VUs, 2897 complete and 0 interrupted iterations
reservations   [  48% ] 2000 VUs  03.8s/30s  2897/6000 iters, 3 per VU

running (3m40.8s), 0724/2000 VUs, 3875 complete and 0 interrupted iterations
reservations   [  65% ] 2000 VUs  04.8s/30s  3875/6000 iters, 3 per VU

running (3m41.8s), 0420/2000 VUs, 4814 complete and 0 interrupted iterations
reservations   [  80% ] 2000 VUs  05.8s/30s  4814/6000 iters, 3 per VU

running (3m42.8s), 0180/2000 VUs, 5508 complete and 0 interrupted iterations
reservations   [  92% ] 2000 VUs  06.8s/30s  5508/6000 iters, 3 per VU
    
      █ THRESHOLDS

        http_req_failed
        ✗ 'rate<0.01' rate=59.90%

        █ TOTAL RESULTS

checks_total.......: 20000   89.520188/s
checks_succeeded...: 100.00% 20000 out of 20000
checks_failed......: 0.00%   0 out of 20000

        ✓ login status 200
        ✓ is not timed out
        ✓ first request is 2xx or 4xx
        ✓ subsequent requests are blocked (4xx)

HTTP
http_req_duration..............: avg=64.52ms min=189.19µs med=61.89ms max=1.29s p(90)=124.13ms p(95)=152.28ms
{ expected_response:true }...: avg=55.78ms min=189.19µs med=80.84ms max=1.29s p(90)=135.4ms  p(95)=157.99ms
http_req_failed................: 59.90% 5990 out of 10000
http_reqs......................: 10000  44.760094/s

        EXECUTION
iteration_duration.............: avg=169.3ms min=4.96ms   med=83.29ms max=1.37s p(90)=464.94ms p(95)=537.31ms
iterations.....................: 6000   26.856056/s
vus............................: 180    min=0             max=1950
vus_max........................: 2000   min=2000          max=2000

NETWORK
data_received..................: 5.5 MB 25 kB/s
data_sent......................: 4.2 MB 19 kB/s

running (3m43.4s), 0000/2000 VUs, 6000 complete and 0 interrupted iterations
reservations ✓ [ 100% ] 2000 VUs  07.4s/30s  6000/6000 iters, 3 per VU
time="2026-01-26T13:40:32Z" level=error msg="thresholds on metrics 'http_req_failed' have been crossed"
```

</details>


## 3차 개선 시도(최종): tomcat 및 DB 커넥션 설정 최적화

### application.yml 관련 주요 변경사항

- tomcat 서버 및 DB 커넥션 풀 모두 Blocking I/O를 기반으로 동작하기 때문에, 스레드 풀을 지나치게 확장할 경우 context-switching으로 인한 비용이 증가
- tomcat 서버 스레드 관련 설정을 아래와 같이 조정
    - `max-connections` : 기본값인 8192로 변경 → 삭제
    - `threads-max` : 800 → 400으로 하향
    - `accept-count`는 2000 → 1000으로 하향
- DB 커넥션 풀 관련 설정을 아래와 같이 조정
    - `maximum-pool-size` : 50 → 30으로 하향
    - `connection-timeout` : 30초 → 3초로 단축

```yaml
server:
  tomcat:
    # 1. 동시에 처리할 스레드 수 (기본값 200)
    threads:
      max: 400
      min-spare: 100
    # 2. 스레드가 꽉 찼을 때 대기할 수 있는 요청 수
    # 이 수치가 작으면 OS가 요청을 거절하여 'dial timeout'이 발생
    accept-count: 1000

    # 3. 연결 유지(Keep-Alive) 설정 (소켓 고갈 방지 핵심)
    # 하나의 소켓으로 여러 번의 예약 요청을 처리하게 하여 TIME_WAIT 발생 빈도를 낮춤
    keep-alive-timeout: 60000 # 60초
    max-keep-alive-requests: 200

spring:
  datasource:
    hikari:
      # 기본값 10에서 확장
      # DB 커넥션 풀의 크기를 지나치게 크게 늘릴 경우, Context-Switching 비용이 증가
      maximum-pool-size: 30 
      # 빠른 실패(Fail-Fast)"를 통한 스레드 고갈 방지 -> 커넥션을 얻지 못한 스레드를 빨리 반환하여 다른 작업을 수행할 때 활용
      # timeout은 3초로 설정: Redisson 분산 락의 waitTime이 2초이므로, 여유시간 1초를 추가
      connection-timeout: 3000
```

### 부하 테스트 결과 요약

- 총 3회의 부하 테스트를 진행
    - 서버 재기동 직후 첫번째 부하 테스트 진행
    - 2번째, 3번째 부하테스트는 예약 만료 시간(7분)만큼의 휴지기를 두고, 동일한 식당을 대상으로 진행
- 각각의 부하 테스트마다 동시 예약 요청 발생 시 10건의 예약만 접수됨을 확인
- 로컬 부하 테스트 3회 진행 결과, 총 응답 시간은 10.6s → 7.9초로 단축
    - tomcat 스레드 풀 및 DB 커넥션 풀 관련 설정을 하향 조정했음에도 불구하고, 2차 테스트 당시의 총 응답 시간(7.4초)에 근접
- 3회 진행된 부하 테스트 모두 타임아웃 없이 `checks_succeeded` 100% 달성

<details>

  <summary>콘솔창 출력 결과 1 - 서버 재기동 직후 부하 테스트 진행</summary>

```bash
    running (3m34.8s), 2000/2000 VUs, 22 complete and 0 interrupted iterations
    reservations   [   0% ] 2000 VUs  00.4s/30s  0022/6000 iters, 3 per VU
    
    running (3m35.8s), 1975/2000 VUs, 188 complete and 0 interrupted iterations
    reservations   [   3% ] 2000 VUs  01.3s/30s  0188/6000 iters, 3 per VU
    
    running (3m36.8s), 1975/2000 VUs, 188 complete and 0 interrupted iterations
    reservations   [   3% ] 2000 VUs  02.3s/30s  0188/6000 iters, 3 per VU
    
    running (3m37.8s), 1975/2000 VUs, 188 complete and 0 interrupted iterations
    reservations   [   3% ] 2000 VUs  03.3s/30s  0188/6000 iters, 3 per VU
    
    running (3m38.8s), 1943/2000 VUs, 662 complete and 0 interrupted iterations
    reservations   [  11% ] 2000 VUs  04.3s/30s  0662/6000 iters, 3 per VU
    
    running (3m39.8s), 1927/2000 VUs, 1522 complete and 0 interrupted iterations
    reservations   [  25% ] 2000 VUs  05.3s/30s  1522/6000 iters, 3 per VU
    
    running (3m40.8s), 1907/2000 VUs, 1982 complete and 0 interrupted iterations
    reservations   [  33% ] 2000 VUs  06.3s/30s  1982/6000 iters, 3 per VU
    
    running (3m41.8s), 1598/2000 VUs, 2822 complete and 0 interrupted iterations
    reservations   [  47% ] 2000 VUs  07.3s/30s  2822/6000 iters, 3 per VU
    
    running (3m42.8s), 0839/2000 VUs, 4143 complete and 0 interrupted iterations
    reservations   [  69% ] 2000 VUs  08.3s/30s  4143/6000 iters, 3 per VU
    
    running (3m43.8s), 0287/2000 VUs, 5170 complete and 0 interrupted iterations
    reservations   [  86% ] 2000 VUs  09.3s/30s  5170/6000 iters, 3 per VU
    
    running (3m44.8s), 0058/2000 VUs, 5861 complete and 0 interrupted iterations
    reservations   [  98% ] 2000 VUs  10.3s/30s  5861/6000 iters, 3 per VU
    
      █ THRESHOLDS
    
        http_req_failed
        ✗ 'rate<0.01' rate=59.90%
    
      █ TOTAL RESULTS
    
        checks_total.......: 20000   88.883403/s
        checks_succeeded...: 100.00% 20000 out of 20000
        checks_failed......: 0.00%   0 out of 20000
    
        ✓ login status 200
        ✓ is not timed out
        ✓ first request is 2xx or 4xx
        ✓ subsequent requests are blocked (4xx)
    
        HTTP
        http_req_duration..............: avg=498.35ms min=1.27ms med=95.96ms max=7.05s    p(90)=1.7s     p(95)=2.05s
          { expected_response:true }...: avg=54.9ms   min=1.27ms med=81.02ms max=816.36ms p(90)=125.38ms p(95)=147.67ms
        http_req_failed................: 59.90% 5990 out of 10000
        http_reqs......................: 10000  44.441701/s
    
        EXECUTION
        iteration_duration.............: avg=1.34s    min=6.94ms med=1.22s   max=10.1s    p(90)=2.83s    p(95)=3.46s
        iterations.....................: 6000   26.665021/s
        vus............................: 58     min=0             max=1975
        vus_max........................: 2000   min=2000          max=2000
    
        NETWORK
        data_received..................: 5.5 MB 25 kB/s
        data_sent......................: 4.3 MB 19 kB/s
    
    running (3m45.0s), 0000/2000 VUs, 6000 complete and 0 interrupted iterations
    reservations ✓ [ 100% ] 2000 VUs  10.6s/30s  6000/6000 iters, 3 per VU
    time="2026-01-27T02:43:53Z" level=error msg="thresholds on metrics 'http_req_failed' have been crossed"   
```

</details>


<details>

  <summary>콘솔창 출력 결과 2, 3 - 예약시간 만료 → 잔여석 반환 후 동일 식당으로 부하 테스트 진행</summary>

```bash
running (4m12.8s), 2000/2000 VUs, 0 complete and 0 interrupted iterations
reservations   [   0% ] 2000 VUs  00.1s/30s  0000/6000 iters, 3 per VU

running (4m13.8s), 2000/2000 VUs, 0 complete and 0 interrupted iterations
reservations   [   0% ] 2000 VUs  01.1s/30s  0000/6000 iters, 3 per VU

running (4m14.8s), 2000/2000 VUs, 24 complete and 0 interrupted iterations
reservations   [   0% ] 2000 VUs  02.1s/30s  0024/6000 iters, 3 per VU

running (4m15.8s), 2000/2000 VUs, 109 complete and 0 interrupted iterations
reservations   [   2% ] 2000 VUs  03.2s/30s  0109/6000 iters, 3 per VU

running (4m16.8s), 1994/2000 VUs, 350 complete and 0 interrupted iterations
reservations   [   6% ] 2000 VUs  04.1s/30s  0350/6000 iters, 3 per VU

running (4m17.8s), 1990/2000 VUs, 537 complete and 0 interrupted iterations
reservations   [   9% ] 2000 VUs  05.1s/30s  0537/6000 iters, 3 per VU

running (4m18.8s), 1922/2000 VUs, 1769 complete and 0 interrupted iterations
reservations   [  29% ] 2000 VUs  06.1s/30s  1769/6000 iters, 3 per VU

running (4m19.8s), 1590/2000 VUs, 3454 complete and 0 interrupted iterations
reservations   [  58% ] 2000 VUs  07.1s/30s  3454/6000 iters, 3 per VU

running (4m20.8s), 1022/2000 VUs, 4642 complete and 0 interrupted iterations
reservations   [  77% ] 2000 VUs  08.1s/30s  4642/6000 iters, 3 per VU

running (4m21.8s), 0679/2000 VUs, 5149 complete and 0 interrupted iterations
reservations   [  86% ] 2000 VUs  09.1s/30s  5149/6000 iters, 3 per VU

  █ THRESHOLDS

    http_req_failed
    ✗ 'rate<0.01' rate=59.90%

  █ TOTAL RESULTS

    checks_total.......: 20000   76.271118/s
    checks_succeeded...: 100.00% 20000 out of 20000
    checks_failed......: 0.00%   0 out of 20000

    ✓ login status 200
    ✓ is not timed out
    ✓ first request is 2xx or 4xx
    ✓ subsequent requests are blocked (4xx)

    HTTP
    http_req_duration..............: avg=743.54ms min=1.31ms  med=872.54ms max=3.83s p(90)=1.53s    p(95)=1.82s
      { expected_response:true }...: avg=67.27ms  min=1.31ms  med=81.2ms   max=3.05s p(90)=153.95ms p(95)=169.17ms
    http_req_failed................: 59.90% 5990 out of 10000
    http_reqs......................: 10000  38.135559/s

    EXECUTION
    iteration_duration.............: avg=1.95s    min=10.64ms med=1.43s    max=6.85s p(90)=3.97s    p(95)=4.6s
    iterations.....................: 6000   22.881335/s
    vus............................: 679    min=0             max=2000
    vus_max........................: 2000   min=2000          max=2000

    NETWORK
    data_received..................: 5.5 MB 21 kB/s
    data_sent......................: 4.2 MB 16 kB/s

running (4m22.2s), 0000/2000 VUs, 6000 complete and 0 interrupted iterations
reservations ✓ [ 100% ] 2000 VUs  09.6s/30s  6000/6000 iters, 3 per VU
time="2026-01-27T03:06:00Z" level=error msg="thresholds on metrics 'http_req_failed' have been crossed"
```

```bash
running (3m44.2s), 1725/2000 VUs, 883 complete and 0 interrupted iterations
reservations   [  15% ] 2000 VUs  01.3s/30s  0883/6000 iters, 3 per VU

running (3m44.8s), 1570/2000 VUs, 1334 complete and 0 interrupted iterations
reservations   [  22% ] 2000 VUs  01.8s/30s  1334/6000 iters, 3 per VU

running (3m45.8s), 1331/2000 VUs, 2054 complete and 0 interrupted iterations
reservations   [  34% ] 2000 VUs  02.8s/30s  2054/6000 iters, 3 per VU

running (3m46.8s), 1173/2000 VUs, 2605 complete and 0 interrupted iterations
reservations   [  43% ] 2000 VUs  03.9s/30s  2605/6000 iters, 3 per VU

running (3m47.8s), 0905/2000 VUs, 3421 complete and 0 interrupted iterations
reservations   [  57% ] 2000 VUs  04.8s/30s  3421/6000 iters, 3 per VU

running (3m48.8s), 0540/2000 VUs, 4433 complete and 0 interrupted iterations
reservations   [  74% ] 2000 VUs  05.8s/30s  4433/6000 iters, 3 per VU

running (3m50.1s), 0236/2000 VUs, 5369 complete and 0 interrupted iterations
reservations   [  89% ] 2000 VUs  07.1s/30s  5369/6000 iters, 3 per VU

running (3m50.8s), 0008/2000 VUs, 5985 complete and 0 interrupted iterations
reservations   [ 100% ] 2000 VUs  07.8s/30s  5985/6000 iters, 3 per VU

  █ THRESHOLDS

    http_req_failed
    ✗ 'rate<0.01' rate=59.90%

  █ TOTAL RESULTS

    checks_total.......: 20000   86.613759/s
    checks_succeeded...: 100.00% 20000 out of 20000
    checks_failed......: 0.00%   0 out of 20000

    ✓ login status 200
    ✓ is not timed out
    ✓ first request is 2xx or 4xx
    ✓ subsequent requests are blocked (4xx)

    HTTP
    http_req_duration..............: avg=91.94ms  min=1.28ms med=85.49ms  max=762.54ms p(90)=176.01ms p(95)=212.89ms
      { expected_response:true }...: avg=56.29ms  min=1.28ms med=81ms     max=762.54ms p(90)=138.27ms p(95)=159.37ms
    http_req_failed................: 59.90% 5990 out of 10000
    http_reqs......................: 10000  43.30688/s

    EXECUTION
    iteration_duration.............: avg=232.18ms min=5.68ms med=136.95ms max=1.47s    p(90)=566.12ms p(95)=684.15ms
    iterations.....................: 6000   25.984128/s
    vus............................: 8      min=0             max=1725
    vus_max........................: 2000   min=2000          max=2000

    NETWORK
    data_received..................: 5.5 MB 24 kB/s
    data_sent......................: 4.2 MB 18 kB/s

running (3m50.9s), 0000/2000 VUs, 6000 complete and 0 interrupted iterations
reservations ✓ [ 100% ] 2000 VUs  07.9s/30s  6000/6000 iters, 3 per VU
time="2026-01-27T03:28:27Z" level=error msg="thresholds on metrics 'http_req_failed' have been crossed"
```

</details>

    

## 대안: Nginx의 활용

- Nginx가 커넥션 제어를 전반적으로 수행함으로써, tomcat 서버는 비즈니스 로직 처리에 집중 가능
- `사용자 - Nginx - Tomcat` 의 구조가 되어 Nginx가 사용자와 tomcat 사이를 중개
    - Nginx-Tomcat 간 커넥션은 항상 유지하면서 사용자-Nginx 간 연결만 유지/해제하는 방식으로 HTTP Keep-Alive를 효율적으로 처리 가능
- 현재의 로컬 환경에서는 Nginx를 도입하지 않았기 때문에 각각의 사용자들이 Tomcat 서버와 직접적으로 연결되면서 DB 커넥션 풀, 소켓 고갈 등을 유발
    - Nginx 도입 시 Tomcat 서버는 Nginx와 연결된 소켓만 관리하면 되므로 소켓 고갈 방지 가능