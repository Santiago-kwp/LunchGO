import { ref } from 'vue';
import { useRouter } from 'vue-router';

export function useReservationQueue() {
  const router = useRouter();
  
  const isWaiting = ref(false);
  const modalType = ref('waiting'); // 'waiting' | 'error'
  const modalMessage = ref('');
  const queueErrorMessage = ref(''); // 컴포넌트의 에러 메시지와 구분하기 위해 이름 변경
  const currentWaitingCount = ref(0);
  const estimatedWaitTime = ref(0);

  const MAX_RETRIES = 10;
  
  /**
   * 예약 대기열 처리 로직
   * @param {Function} apiCall 예약 생성 API 호출 함수 (Promise 반환)
   * @param {Function} onSuccess 성공 시 실행할 콜백
   * @param {Ref<Boolean>} isSubmitting 외부 로딩 상태 Ref
   */
  const processQueue = async (apiCall, onSuccess, isSubmitting) => {
    queueErrorMessage.value = '';
    modalType.value = 'waiting';
    let retryCount = 0;

    const attempt = async () => {
      try {
        const result = await apiCall();
        if (onSuccess) onSuccess(result);
      } catch (e) {
        const msg = e.response?.data?.message || e?.message || '';
        const waitingCount = e.response?.data?.waitingCount || 0;

        // 1. 중복 예약 등 에러 모달 ("대기 중" 없는 409)
        if (e.response?.status === 409 && !msg.includes('대기 중')) {
          modalType.value = 'error';
          modalMessage.value = (msg || '이미 처리된 예약이거나 잔여석이 부족합니다.').replace('. ', '.\n');
          isWaiting.value = true;
          return;
        }

        // 2. 대기열 진입 ("대기 중" 포함)
        if (e.response?.status === 409 && msg.includes('대기 중')) {
          if (retryCount < MAX_RETRIES) {
            retryCount++;
            currentWaitingCount.value = waitingCount;
            // 대기 시간 시뮬레이션: 인원당 약 3초 + 기본 2초
            estimatedWaitTime.value = waitingCount > 0 ? (waitingCount * 3) : 5;
            
            modalType.value = 'waiting';
            modalMessage.value = '예약 요청이 많아 대기 중입니다.\n잠시만 기다려주세요...';
            isWaiting.value = true;
            setTimeout(attempt, 2000); // 2초 후 재시도
            return;
          } else {
            modalType.value = 'error';
            modalMessage.value = '현재 예약 요청이 많습니다.\n잠시 후 다시 시도해 주세요.';
            isWaiting.value = true;
          }
        } else {
          // 3. 기타 에러
          queueErrorMessage.value = msg || '예약 생성 중 오류가 발생했습니다.';
        }
      } finally {
        // 재시도 중이 아니고, 에러 모달 상태도 아닐 때만 로딩 해제
        if (modalType.value !== 'error' && (!isWaiting.value || retryCount >= MAX_RETRIES)) {
          isWaiting.value = false;
          if (isSubmitting) isSubmitting.value = false;
        }
      }
    };

    await attempt();
  };

  const handleQueueModalClose = (isSubmitting) => {
    isWaiting.value = false;
    if (modalType.value === 'error') {
      if (isSubmitting) isSubmitting.value = false;
      router.push('/');
    }
  };

  return {
    isWaiting,
    modalType,
    modalMessage,
    queueErrorMessage,
    currentWaitingCount,
    estimatedWaitTime,
    processQueue,
    handleQueueModalClose
  };
}
