# 로그인 부하 테스트 (대기열 적용 전) 결과

## 테스트 개요
- 대상: `/api/login` POST
- 환경: 단일 인스턴스
- 도구: k6 (`grafana/k6`)
- 목적: 대기열 적용 전 베이스라인 성능/처리량 확보

## 실행 커맨드
```sh
docker run --rm -i grafana/k6 run - \
  -e BASE_URL=http://10.0.2.6:8080 \
  -e EMAIL_PREFIX=loadtest.user \
  -e EMAIL_DOMAIN=example.com \
  -e PASSWORD='Passw0rd!123' \
  -e LOAD_VUS=20 \
  -e LOAD_DURATION=2m \
  < /root/k6_login_loadtest.js
```

## k6 요약 (표)
| 항목 | 값 |
| --- | --- |
| VUs | 20 |
| Duration | 2m |
| 총 요청 수 | 3,391 |
| 처리량 | 28.176 req/s |
| 실패율 | 0.00% |
| 응답 시간 avg | 709.11 ms |
| 응답 시간 min | 376.46 ms |
| 응답 시간 med | 716.42 ms |
| 응답 시간 p90 | 806.33 ms |
| 응답 시간 p95 | 823.60 ms |
| 응답 시간 max | 945.99 ms |

## xlog 요약 (표)
| 대상 | Count | Total Elapsed (ms) | Avg Elapsed (ms) | Total SQL Time (ms) | Avg SQL Time (ms) |
| --- | --- | --- | --- | --- | --- |
| `/api/login` POST | 3,392 | 2,387,830 | 704 | 9,867 | 3 |
| `BaseMemberService#updateLastLoginAt()` | 3,197 | 19,352 | 6 | 2,377 | 1 |

## 관찰 포인트
- 로그인 성공률 100%
- 평균 응답 시간 약 709ms 수준
- 동시 20 VUs, 28 rps 처리

## 다음 단계
- 대기열 적용 후 동일 조건으로 재측정
- 처리량/지연 분포/CPU 지표 비교
