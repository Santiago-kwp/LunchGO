# Payment Troubleshooting (PortOne) - Deployment

## 증상
- 결제 화면에서 다음 경고가 표시되고 결제가 진행되지 않음
  - "PortOne 설정값이 누락되었습니다. 관리자에게 문의해 주세요."

## 영향 범위
- 프론트(브라우저)에서 PortOne 결제 호출이 차단됨
- 백엔드는 정상 동작하더라도 결제 요청이 시작되지 않음

## 원인
- 프론트는 `VITE_*` 환경변수를 **빌드 시점**에만 주입받음
- 운영 배포 시 `VITE_PORTONE_STORE_ID`, `VITE_PORTONE_CHANNEL_KEY`가 주입되지 않아
  `import.meta.env` 값이 빈 문자열로 남았음
- 결과적으로 프론트에서 설정 누락 오류가 발생

## 확인 방법
1) 백엔드 환경변수는 정상인지 확인
```bash
docker exec lunchgo-backend env | rg "PORTONE_"
```

2) 프론트에서 사용하는 키 확인
```bash
grep -Rni "VITE_PORTONE" frontend/src
```

3) 결제 화면 코드에서 누락 체크 확인
- `frontend/src/views/restaurant/id/payment/RestaurantPaymentPage.vue`
- `VITE_PORTONE_STORE_ID`, `VITE_PORTONE_CHANNEL_KEY`가 비어 있으면 오류 발생

## 해결 (권장: GitHub Actions에서 주입)
1) GitHub Secrets 등록
- `VITE_PORTONE_STORE_ID`
- `VITE_PORTONE_CHANNEL_KEY`
- `VITE_PORTONE_OPEN_TYPE` (예: `popup`)

2) 워크플로우에 env 주입
`.github/workflows/frontend-deploy.yml`
```yaml
env:
  VITE_API_BASE_URL: http://${{ secrets.SERVER_IP }}
  VITE_PORTONE_STORE_ID: ${{ secrets.VITE_PORTONE_STORE_ID }}
  VITE_PORTONE_CHANNEL_KEY: ${{ secrets.VITE_PORTONE_CHANNEL_KEY }}
  VITE_PORTONE_OPEN_TYPE: ${{ secrets.VITE_PORTONE_OPEN_TYPE }}
```

3) dev 브랜치로 푸시 → 프론트 빌드/배포 자동 실행

## 해결 (로컬 빌드 후 수동 업로드)
1) 빌드 시 환경변수 주입
```bash
cd frontend
export VITE_API_BASE_URL=http://<bastion-ip>
export VITE_PORTONE_STORE_ID=store-xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
export VITE_PORTONE_CHANNEL_KEY=channel-key-xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
export VITE_PORTONE_OPEN_TYPE=popup
npm ci
npm run build
```

2) Object Storage 업로드
```bash
export AWS_ACCESS_KEY_ID=...
export AWS_SECRET_ACCESS_KEY=...
export BUCKET=lunchgo-test-bucket
export ENDPOINT_URL=http://kr.object.ncloudstorage.com
export REGION=kr-standard

bash scripts/upload_frontend_object_storage.sh
```

## 참고
- `VITE_*` 값은 번들에 포함되므로 민감정보를 넣지 않는다.
- `VITE_PORTONE_SDK_URL`은 기본값(`https://cdn.portone.io/v2/browser-sdk.js`)이 있으므로
  별도 주입 없이도 동작한다.

## 웹훅 트러블슈팅

### 증상
- PortOne 콘솔에서 웹훅 발송 실패 (HTTP 400)
- 서버 로그: `결제 정보를 찾을 수 없습니다.`

### 원인
- 웹훅 테스트 payload의 결제 식별자가 실제 DB 결제 데이터와 불일치
- `/api/payments/portone/requested`가 저장되기 전에 테스트 웹훅이 도착

### 해결
1) 웹훅 URL을 운영 엔드포인트로 변경
   - 예: `http://<bastion-ip>/api/payments/portone/webhook`
2) **실 결제 플로우**로 테스트
   - 프론트에서 결제를 진행하면 `requested -> complete` 흐름이 DB에 저장됨
   - 이후 웹훅이 동일한 식별자로 도착하면 200 응답

### 확인 방법
1) 백엔드 로그에서 서명 검증 확인
   - `Webhook Signature Check`의 Received/Calculated가 일치해야 함
2) DB에 결제 레코드가 존재하는지 확인
