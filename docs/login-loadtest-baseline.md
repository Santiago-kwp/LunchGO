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

---

## 대기열 적용 후 결과
### k6 요약 (표)
| 항목 | 값 |
| --- | --- |
| VUs | 20 |
| Duration | 2m |
| 총 요청 수 | 569,673 |
| 처리량 | 4,747.17986 req/s |
| 실패율 | 100.00% |
| 응답 시간 avg | 4.12 ms |
| 응답 시간 min | 0.49182 ms |
| 응답 시간 med | 2.95 ms |
| 응답 시간 p90 | 8.03 ms |
| 응답 시간 p95 | 10 ms |
| 응답 시간 max | 476.33 ms |

### xlog 요약 (표)
| 대상 | Count | Total Elapsed (ms) | Avg Elapsed (ms) | Total SQL Time (ms) | Avg SQL Time (ms) |
| --- | --- | --- | --- | --- | --- |
| `/api/login` POST | 474,578 | 236,704 | 0 | 0 | 0 |

### 비고
- 로그인 상태 체크(200) 실패율 100%
- 대기열 적용으로 200 응답 대신 대기열 응답(429 등)이 대부분인 것으로 추정

---

## 비교 요약 (baseline vs queue)
| 항목 | 대기열 전 | 대기열 후 |
| --- | --- | --- |
| 총 요청 수 | 3,391 | 569,673 |
| 처리량 (req/s) | 28.176 | 4,747.17986 |
| 실패율 | 0.00% | 100.00% |
| 응답 시간 avg (ms) | 709.11 | 4.12 |
| 응답 시간 p95 (ms) | 823.60 | 10 |

---

## 대기열 적용 (1000 VU, one-shot) 결과 비교
### capacity=20
| 항목 | 값 |
| --- | --- |
| 완료 시간 | 약 53초 |
| queue_allowed_rate | 100.00% |
| `/api/login` avg (ms) | 614 |
| `/api/login/queue` POST avg (ms) | 95 |
| `/api/login/queue` GET avg (ms) | 22 |
| `/api/login/queue` GET count | 26,450 |

### capacity=15
| 항목 | 값 |
| --- | --- |
| 완료 시간 | 약 1분 06.5초 |
| queue_allowed_rate | 100.00% |
| `/api/login` avg (ms) | 424 |
| `/api/login/queue` POST avg (ms) | 146 |
| `/api/login/queue` GET avg (ms) | 27 |
| `/api/login/queue` GET count | 33,579 |

### capacity=10
| 항목 | 값 |
| --- | --- |
| 완료 시간 | 약 1분 26.4초 |
| queue_allowed_rate | 100.00% |
| `/api/login` avg (ms) | 273 |
| `/api/login/queue` POST avg (ms) | 134 |
| `/api/login/queue` GET avg (ms) | 9 |
| `/api/login/queue` GET count | 44,135 |

### 요약
- capacity를 20 → 15 → 10으로 낮추면 완료 시간이 늘어남
- 대기열 GET 폴링 횟수는 증가

---

## 폴링 간격 비교 (capacity=10, 1000 VU, one-shot)
### poll=1000ms
| 항목 | 값 |
| --- | --- |
| 완료 시간 | 약 1분 26.4초 |
| queue_allowed_rate | 100.00% |
| `/api/login` avg (ms) | 273 |
| `/api/login/queue` POST avg (ms) | 134 |
| `/api/login/queue` GET avg (ms) | 9 |
| `/api/login/queue` GET count | 44,135 |
| CPU 관찰 | 최대 약 80% (안정화) |

### poll=1500ms
| 항목 | 값 |
| --- | --- |
| 완료 시간 | 약 2분 21.7초 |
| queue_allowed_rate | 100.00% |
| `/api/login` avg (ms) | 339 |
| `/api/login/queue` POST avg (ms) | 121 |
| `/api/login/queue` GET avg (ms) | 10 |
| `/api/login/queue` GET count | 47,534 |
| CPU 관찰 | 최대 약 65% |

### poll=2000ms
| 항목 | 값 |
| --- | --- |
| 완료 시간 | 약 2분 54.5초 |
| queue_allowed_rate | 100.00% |
| `/api/login` avg (ms) | 미수집 |
| `/api/login/queue` GET count | 미수집 |
| CPU 관찰 | 최대 약 30% |

### 요약
- 폴링 간격을 늘리면 CPU 피크가 낮아지지만 완료 시간이 늘어남
- 1500ms는 1000ms 대비 CPU는 낮고, 2000ms 대비 완료 시간이 짧음
## 관찰 포인트
- 로그인 성공률 100%
- 평균 응답 시간 약 709ms 수준
- 동시 20 VUs, 28 rps 처리

## 다음 단계
- 대기열 적용 후 동일 조건으로 재측정
- 처리량/지연 분포/CPU 지표 비교
