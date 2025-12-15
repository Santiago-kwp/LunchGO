<script setup lang="ts">
import { ReceiptTurkishLiraIcon } from 'lucide-vue-next';
import { ref, watch, computed, onUnmounted } from 'vue';

const props = defineProps<{
  isVisible: boolean;
  userType: string;
}>();

const emit = defineEmits(['close']);

// 도달 번호 관리 (1: 본인인증, 2: 비밀번호 재설정)
const step = ref(1);

// 첫번째 모달: 본인인증 관련 상태
const name = ref('');
// email 변수는 userType에 따라 '이메일' 또는 '아이디' 값을 담습니다.
const email = ref('');
const phone = ref('');
const isCodeSent = ref(false);
const verifyCode = ref('');
const isTimeout = ref(false);

// 두번째 모달: 비밀번호 재설정 관련 상태
const newPassword = ref('');
const confirmPassword = ref('');

// owner 여부 판단
const isOwner = computed(() => props.userType === 'owner');

// 모달이 닫힐 때 모든 상태 초기화
watch(
  () => props.isVisible,
  (newVal) => {
    if (!newVal) {
      if (timerInterval.value) clearInterval(timerInterval.value);

      setTimeout(() => {
        step.value = 1;
        name.value = '';
        email.value = '';
        phone.value = '';
        verifyCode.value = '';
        isCodeSent.value = false;
        isTimeout.value = false;
        newPassword.value = '';
        confirmPassword.value = '';
      }, 300);
    }
  }
);

// 휴대폰 번호 자동 포맷팅
watch(phone, (newVal) => {
  const cleaned = newVal.replace(/[^0-9]/g, '');
  let formatted = cleaned;

  if (cleaned.length > 11) {
    formatted = cleaned.slice(0, 11);
  }

  if (cleaned.length > 3 && cleaned.length <= 7) {
    formatted = `${cleaned.slice(0, 3)}-${cleaned.slice(3)}`;
  } else if (cleaned.length > 7) {
    formatted = `${cleaned.slice(0, 3)}-${cleaned.slice(3, 7)}-${cleaned.slice(
      7
    )}`;
  }

  if (newVal !== formatted) {
    phone.value = formatted;
  }
});

// 타이머 관련 상태
const timer = ref(180);
const timerInterval = ref(null);

const formattedTimer = computed(() => {
  const m = Math.floor(timer.value / 60)
    .toString()
    .padStart(2, '0');
  const s = (timer.value % 60).toString().padStart(2, '0');
  return `${m}:${s}`;
});

const startTimer = () => {
  if (timerInterval.value) clearInterval(timerInterval.value);

  timer.value = 180;
  isTimeout.value = false;

  timerInterval.value = setInterval(() => {
    if (timer.value > 0) {
      timer.value--;
    } else {
      clearInterval(timerInterval.value);
      isTimeout.value = true;
      alert('인증번호 입력 시간이 초과되었습니다. 재발송이 필요합니다.');
    }
  }, 1000);
};

onUnmounted(() => {
  if (timerInterval.value) clearInterval(timerInterval.value);
});

// 인증번호 발송 핸들러
const handleSendVerifyCode = async () => {
  // 입력값 검증
  if (!email.value) {
    const label = isOwner.value ? '아이디' : '이메일';
    alert(`${label}을(를) 입력해주세요.`);
    return;
  }
  if (!name.value) {
    alert('이름을 입력해주세요.');
    return;
  }
  if (!phone.value) {
    alert('휴대폰 번호를 입력해주세요.');
    return;
  }

  // 실제 API 호출 로직 (비동기)
  // await api.sendCode(...)

  alert(`인증번호를 발송했습니다: ${phone.value}`);

  isCodeSent.value = true;
  verifyCode.value = '';
  startTimer();
};

// 통합 폼 제출 핸들러
const handleSubmit = () => {
  if (step.value === 1) {
    handleVerifyUser();
  } else {
    handleResetPassword();
  }
};

const verifyUserFromData = () => {
  return null;
};

// 1단계: 인증 확인
const handleVerifyUser = () => {
  if (!email.value) {
    const label = isOwner.value ? '아이디' : '이메일';
    alert(`${label}을(를) 입력해주세요.`);
    return;
  }
  if (!name.value) {
    alert('이름을 입력해주세요.');
    return;
  }
  if (!isCodeSent.value) {
    alert('인증번호를 먼저 발송해주세요.');
    return;
  }
  if (!verifyCode.value) {
    alert('인증번호를 입력해주세요.');
    return;
  }

  if (verifyUserFromData() !== null) {
    alert(verifyUserFromData);
    return;
  }

  step.value = 2;
};

// 2단계: 비밀번호 변경 요청
const handleResetPassword = () => {
  if (!newPassword.value) {
    alert('새로운 비밀번호를 입력해주세요.');
    return;
  }
  const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[\W_]).{8,20}$/;

  if (!passwordRegex.test(newPassword.value)) {
    alert(
      '비밀번호는 8~20자이어야 하며, 영문 대문자, 소문자, 숫자, 특수문자를 모두 포함해야 합니다.'
    );
    return;
  }

  if (newPassword.value !== confirmPassword.value) {
    alert('비밀번호가 일치하지 않습니다.');
    return;
  }

  alert('비밀번호가 성공적으로 변경되었습니다.');
  emit('close');
};

const submitButtonText = computed(() => {
  return step.value === 1 ? '인증하기' : '비밀번호 변경하기';
});

const modalTitle = computed(() => {
  return step.value === 1 ? '비밀번호 찾기' : '비밀번호 재설정';
});
</script>

<template>
  <Teleport to="body">
    <div v-if="isVisible" class="modal-overlay" @click.self="$emit('close')">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ modalTitle }}</h3>
          <button class="close-btn" @click="$emit('close')">&times;</button>
        </div>

        <div class="modal-body">
          <p class="guide-text" v-if="step === 1">
            가입 시 등록한 정보를 입력해주세요.
          </p>
          <p class="guide-text" v-else>
            새롭게 사용할 비밀번호를 입력해주세요.
          </p>

          <form @submit.prevent="handleSubmit">
            <template v-if="step === 1">
              <div v-if="isOwner" class="input-group">
                <label for="find-id">아이디</label>
                <input
                  id="find-id"
                  v-model="email"
                  type="text"
                  placeholder="아이디를 입력하세요."
                  minlength="7"
                  maxlength="15"
                  required
                />
              </div>

              <div v-else class="input-group">
                <label for="find-email">이메일</label>
                <input
                  id="find-email"
                  v-model="email"
                  type="email"
                  placeholder="이메일을 입력하세요."
                  required
                />
              </div>

              <div class="input-group">
                <label for="find-name">이름</label>
                <input
                  id="find-name"
                  v-model="name"
                  type="text"
                  placeholder="이름을 입력하세요."
                  maxlength="10"
                  required
                />
              </div>

              <div class="input-group">
                <label for="find-phone">휴대폰 번호</label>
                <div class="input-with-button">
                  <input
                    id="find-phone"
                    v-model="phone"
                    type="tel"
                    placeholder="전화번호를 입력하세요."
                    required
                    class="flex-grow-input"
                    :disabled="isCodeSent"
                  />
                  <button
                    type="button"
                    class="btn-secondary"
                    @click="handleSendVerifyCode"
                  >
                    {{ isCodeSent ? '재전송' : '인증번호 발송' }}
                  </button>
                </div>
              </div>

              <div v-if="isCodeSent" class="input-group slide-in">
                <input
                  v-model="verifyCode"
                  type="text"
                  placeholder="인증번호를 6자리를 입력하세요."
                  class="input-field"
                  maxlength="6"
                />
                <p class="timer-text">{{ formattedTimer }}</p>
              </div>
            </template>

            <template v-else>
              <div class="input-group slide-in">
                <label for="new-password">새로운 비밀번호</label>
                <input
                  id="new-password"
                  v-model="newPassword"
                  type="password"
                  placeholder="새로운 비밀번호를 입력하세요."
                  required
                />
              </div>

              <div class="input-group slide-in">
                <label for="confirm-password">비밀번호 재입력</label>
                <input
                  id="confirm-password"
                  v-model="confirmPassword"
                  type="password"
                  placeholder="비밀번호를 재입력하세요."
                  required
                />
              </div>
            </template>

            <button type="submit" class="btn-confirm">
              {{ submitButtonText }}
            </button>

            <div v-if="step === 1 && isCodeSent" class="resend-link-container">
              <button
                type="button"
                class="btn-text-link"
                @click="handleSendVerifyCode"
              >
                인증번호가 오지 않나요? <span>재전송</span>
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
/* 기존 스타일 그대로 유지 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  backdrop-filter: blur(2px);
}

.modal-content {
  background: white;
  width: 90%;
  max-width: 400px;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  color: #1e3a5f;
  font-weight: 700;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #adb5bd;
  padding: 0;
  line-height: 1;
}

.guide-text {
  font-size: 14px;
  color: #6c757d;
  margin-bottom: 24px;
  line-height: 1.5;
}

.input-group {
  margin-bottom: 16px;
  position: relative;
}

.input-group label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: #1e3a5f;
  margin-bottom: 6px;
}

.input-group input {
  width: 100%;
  height: 48px;
  padding: 0 16px;
  border: 1.5px solid #e9ecef;
  border-radius: 8px;
  box-sizing: border-box;
  font-size: 14px;
  color: #1e3a5f;
  transition: border-color 0.2s;
}

.input-group input:focus {
  outline: none;
  border-color: #ff6b4a;
}

.input-group input:disabled {
  background-color: #f8f9fa;
  color: #adb5bd;
}

.input-with-button {
  display: flex;
  gap: 8px;
  width: 100%;
}

.input-with-button input.flex-grow-input {
  flex: 1;
}

.btn-secondary {
  height: 48px;
  padding: 0 16px;
  background-color: #f8f9fa;
  border: 1.5px solid #e9ecef;
  border-radius: 8px;
  color: #495057;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s;
}

.btn-secondary:hover {
  background-color: #e9ecef;
  border-color: #dee2e6;
  color: #1e3a5f;
}

.slide-in {
  animation: fadeIn 0.3s ease-out;
}
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.timer-text {
  position: absolute;
  right: 16px;
  top: 50%;
  top: 24px;
  transform: translateY(0);
  margin-top: 14px;
  font-size: 13px;
  color: #ff6b4a;
  pointer-events: none;
}
/* timer-text 위치 보정 (input-group 내부에 label이 없는 경우를 위해) */
.input-group:not(:has(label)) .timer-text {
  top: 50%;
  transform: translateY(-50%);
  margin-top: 0;
}

.btn-confirm {
  width: 100%;
  height: 48px;
  background-color: #ff6b4a;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 700;
  font-size: 16px;
  cursor: pointer;
  margin-top: 8px;
  transition: background-color 0.2s;
}

.btn-confirm:hover {
  background-color: #e5553a;
}

.resend-link-container {
  margin-top: 12px;
  text-align: center;
  animation: fadeIn 0.5s ease-out;
}

.btn-text-link {
  background: none;
  border: none;
  color: #adb5bd;
  font-size: 13px;
  cursor: pointer;
  padding: 0;
  font-family: inherit;
}

.btn-text-link span {
  color: #6c757d;
  text-decoration: underline;
  margin-left: 4px;
  font-weight: 600;
  transition: color 0.2s;
}

.btn-text-link:hover span {
  color: #1e3a5f;
}
</style>
