<script setup lang="ts">
import { ref, watch, computed, onUnmounted, onMounted } from 'vue';
import { RouterLink } from 'vue-router';
import { ArrowLeft, ChevronRight, Check, X } from 'lucide-vue-next'; // 아이콘 추가
import Button from '@/components/ui/Button.vue';
import Card from '@/components/ui/Card.vue';
import Input from '@/components/ui/Input.vue';

const name = ref('');
const email = ref('');
const isEmailUnique = ref(false);
const phone = ref('');
const verificationCode = ref('');
const password = ref('');
const passwordConfirm = ref('');
const companyName = ref('');
const companyFrontAddress = ref('');
const companyBackAddress = ref('');

//컴포넌트 ref 생성
const passwordConfirmRef = ref<any>(null);
//상세주소 focus를 위한 ref 생성
const companyBackAddressRef = ref<any>(null);

// 인증번호 관련 상태
const isCodeSent = ref(false);
const verifyCode = ref('');
const isTimeout = ref(false);

// 타이머 상태
const timer = ref(180);
const timerInterval = ref<ReturnType<typeof setInterval> | null>(null);

// 약관 동의 상태
const agreeTerms = ref(false);
const agreePrivacy = ref(false);
const agreeMarketing = ref(false);

// 전체 동의 체크박스 상태 (Computed 처럼 동작하게 하거나 watch로 제어)
const agreeAll = ref(false);

//도로명주소 api
const loadDaumPostcodeScript = () => {
  const scriptId = 'daum-postcode-script';
  const existingScript = document.getElementById(scriptId);

  if (!existingScript) {
    const script = document.createElement('script');
    script.src =
      '//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js';
    script.id = scriptId;
    document.body.appendChild(script);
  }
};

//컴포넌트 마운트 시 스크립트 로드
onMounted(() => {
  loadDaumPostcodeScript();
});

//주소 검색 핸들러
const handleAddressSearch = () => {
  //TypeScript에서 window객체의 daum 프로퍼티 접근을 위해 any 타입 단언 사용
  if (!(window as any).daum || !(window as any).daum.Postcode) {
    alert('주소 검색 서비스를 불러오는 중입니다. 잠시 후 다시 시도해주세요.');
    loadDaumPostcodeScript();
    return;
  }

  new (window as any).daum.Postcode({
    oncomplete: function (data: any) {
      let addr = '';
      if (data.userSelectedType === 'R') {
        addr = data.roadAddress;
      } else {
        addr = data.jibunAddress;
      }

      companyFrontAddress.value = addr;

      // (Vue의 ref를 통해 포커스)
      if (companyBackAddressRef.value) {
        // Input 컴포넌트 내부의 실제 input 요소에 접근하거나,
        // 컴포넌트가 expose한 focus 메서드를 호출해야 함.
        // 일반적인 HTML input ref라면 .focus() 바로 사용 가능
        companyBackAddressRef.value.$el.querySelector('input')?.focus() ||
          companyBackAddressRef.value.focus?.();
      }
    },
  }).open();
};

// 전체 동의 로직
watch(agreeAll, (newVal) => {
  if (newVal) {
    agreeTerms.value = true;
    agreePrivacy.value = true;
    agreeMarketing.value = true;
  } else {
    // 개별 해제가 아니라 사용자가 직접 '전체 해제'를 눌렀을 때만 동작하도록
    // (여기서는 단순화를 위해 전체 토글로 구현, 필요 시 로직 세분화 가능)
    if (agreeTerms.value && agreePrivacy.value && agreeMarketing.value) {
      agreeTerms.value = false;
      agreePrivacy.value = false;
      agreeMarketing.value = false;
    }
  }
});

// 개별 체크박스가 변경될 때 '전체 동의' 체크박스 상태 업데이트
watch(
  [agreeTerms, agreePrivacy, agreeMarketing],
  ([terms, privacy, marketing]) => {
    agreeAll.value = terms && privacy && marketing;
  }
);

//휴대폰 번호 자동 포맷팅
watch(phone, (newVal) => {
  // 숫자만 남기고 제거
  const cleaned = newVal.replace(/[^0-9]/g, '');
  let formatted = cleaned;

  // 11자리까지만 입력 가능하도록 제한
  if (cleaned.length > 11) {
    formatted = cleaned.slice(0, 11);
  }

  // 포맷팅 로직 apply
  if (cleaned.length > 3 && cleaned.length <= 7) {
    formatted = `${cleaned.slice(0, 3)}-${cleaned.slice(3)}`;
  } else if (cleaned.length > 7) {
    formatted = `${cleaned.slice(0, 3)}-${cleaned.slice(3, 7)}-${cleaned.slice(
      7
    )}`;
  }

  // 값이 변경되었을 때만 업데이트
  if (newVal !== formatted) {
    phone.value = formatted;
  }
});

// 타이머 포맷 (MM:SS)
const formattedTimer = computed(() => {
  const m = Math.floor(timer.value / 60)
    .toString()
    .padStart(2, '0');
  const s = (timer.value % 60).toString().padStart(2, '0');
  return `${m}:${s}`;
});

// 타이머 시작
const startTimer = () => {
  if (timerInterval.value) clearInterval(timerInterval.value);
  timer.value = 180;
  isTimeout.value = false;

  timerInterval.value = setInterval(() => {
    if (timer.value > 0) {
      timer.value--;
    } else {
      if (timerInterval.value) clearInterval(timerInterval.value);
      isTimeout.value = true;
      alert('인증번호 입력 시간이 초과되었습니다. 재발송이 필요합니다.');
      isCodeSent.value = false;
    }
  }, 1000);
};

onUnmounted(() => {
  if (timerInterval.value) clearInterval(timerInterval.value);
});

const checkInputElement = () => {
  if (!email.value) return alert('이메일을 입력해주세요.');
  if (!isEmailUnique.value) return alert('이메일 중복확인이 필요합니다.');
  if (!password.value) return alert('비밀번호를 입력해주세요.');
  if (!passwordConfirm.value) return alert('비밀번호 재입력이 필요합니다.');
  if (!name.value) return alert('이름을 입력해주세요.');
  if (!companyName.value) return alert('회사명을 입력해주세요.');
  if (!companyFrontAddress.value) return alert('도로명주소를 입력해주세요.');
  return null;
};

// 인증번호 발송
const handleSendVerifyCode = () => {
  if (checkInputElement() !== null) {
    return;
  }

  // API 호출 시뮬레이션
  alert(`인증번호를 발송했습니다: ${phone.value}`);

  isCodeSent.value = true;
  verifyCode.value = '';
  startTimer();
};

// 모달 관련 상태
const isTermsModalOpen = ref(false);
const modalTitle = ref('');
const modalContent = ref('');

const handleEmailDuplicateCheck = async () => {
  if (!email.value) return alert('이메일을 먼저 입력해주세요.');

  //백엔드 연동해서 이메일 unique한지 check
  alert('사용 가능한 이메일입니다.');

  isEmailUnique.value = true;
};

// 이용약관 모달 열기 -> 내용 수정 필수!!
const openModal = (type) => {
  isTermsModalOpen.value = true;
  if (type === 'terms') {
    modalTitle.value = '서비스 이용약관';
    modalContent.value =
      '제1조 (목적)\n이 약관은 런치고 서비스의 이용조건 및 절차...';
  } else if (type === 'privacy') {
    modalTitle.value = '개인정보 처리방침';
    modalContent.value = '런치고는 고객님의 개인정보를 소중히 다루며...';
  } else if (type === 'marketing') {
    modalTitle.value = '마케팅 정보 수신 동의';
    modalContent.value = '이벤트 및 혜택 정보를 받아보실 수 있습니다...';
  } else {
    modalTitle.value = '취소 및 환불 정책';
    modalContent.value = '예약 시간 1시간 전까지 취소 가능하며...';
  }
};

// 모달 닫기
const closeModal = () => {
  isTermsModalOpen.value = false;
};

const handleSignup = () => {
  const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[\W_]).{8,20}$/;

  if (checkInputElement() !== null) return;
  if (!isCodeSent.value) return alert('휴대전화 인증은 필수입니다.');
  if (!passwordRegex.test(password.value))
    return alert(
      '비밀번호는 8~20자이어야 하며, 영문 대문자, 소문자, 숫자, 특수문자를 모두 포함해야 합니다.'
    );

  if (password.value !== passwordConfirm.value) {
    alert('비밀번호가 일치하지 않습니다.');

    //비밀번호 확인 입력창 비우고 포커스 이동
    passwordConfirm.value = '';
    passwordConfirmRef.value?.focus(); // 포커스 함수 실행
    return;
  }
  if (!agreeTerms.value || !agreePrivacy.value) {
    alert('필수 약관에 동의해주세요.');
    return;
  }

  //회원가입 시 도로명주소와 상세주소를 합쳐서 백엔드로 전달
  let totalAddress = companyFrontAddress.value + ' ' + companyBackAddress.value;

  //인증번호가 잘못된 경우

  //타이머 실행되고 있으면 정지
  if (timerInterval.value) clearInterval(timerInterval.value);
  alert('회원가입 완료!');
};
</script>

<template>
  <div class="min-h-screen bg-[#f8f9fa] flex flex-col">
    <header class="bg-white border-b border-[#e9ecef]">
      <div class="max-w-[500px] mx-auto px-4 h-14 flex items-center">
        <RouterLink
          to="/signup"
          class="flex items-center gap-2 text-[#495057] hover:text-[#1e3a5f]"
        >
          <ArrowLeft class="w-5 h-5" />
          <span class="text-sm font-medium">돌아가기</span>
        </RouterLink>
      </div>
    </header>

    <main class="flex-1 max-w-[500px] mx-auto w-full px-4 py-8">
      <div class="flex flex-col items-center mb-8">
        <img
          src="/images/lunch-go-whitebg.png"
          alt="런치고"
          width="80"
          height="80"
          class="w-20 h-20 mb-4"
        />
        <h1 class="text-2xl font-bold text-[#1e3a5f] mb-2">회원가입</h1>
        <p class="text-sm text-[#6c757d]">
          런치고 회원이 되어 편리한 회식 예약을 경험하세요
        </p>
      </div>

      <Card class="border-[#e9ecef] rounded-xl bg-white shadow-card p-6">
        <form @submit.prevent="handleSignup" class="space-y-4">
          <div>
            <label
              for="email"
              class="block text-sm font-medium text-[#1e3a5f] mb-2"
              >이메일</label
            >
            <div class="flex gap-2">
              <Input
                id="email"
                type="email"
                placeholder="example@email.com"
                v-model="email"
                class="flex-1 h-12 px-4 border-[#dee2e6] rounded-lg focus:border-[#ff6b4a] focus:ring-1 focus:ring-[#ff6b4a] text-[#1e3a5f]"
              />
              <Button
                type="button"
                @click="handleEmailDuplicateCheck"
                variant="outline"
                class="h-12 px-4 border-[#dee2e6] text-[#495057] bg-white hover:bg-[#f8f9fa] rounded-lg whitespace-nowrap"
              >
                중복확인
              </Button>
            </div>
          </div>

          <div>
            <label
              for="password"
              class="block text-sm font-medium text-[#1e3a5f] mb-2"
              >비밀번호</label
            >
            <Input
              id="password"
              type="password"
              placeholder="비밀번호를 입력하세요 (8자 이상)"
              minlength="8"
              maxlength="20"
              v-model="password"
              class="w-full h-12 px-4 border-[#dee2e6] rounded-lg focus:border-[#ff6b4a] focus:ring-1 focus:ring-[#ff6b4a] text-[#1e3a5f]"
            />
          </div>

          <div>
            <label
              for="password-confirm"
              class="block text-sm font-medium text-[#1e3a5f] mb-2"
              >비밀번호 확인</label
            >
            <Input
              ref="passwordConfirmRef"
              id="password-confirm"
              type="password"
              placeholder="비밀번호를 다시 입력하세요"
              minlength="8"
              maxlength="20"
              v-model="passwordConfirm"
              class="w-full h-12 px-4 border-[#dee2e6] rounded-lg focus:border-[#ff6b4a] focus:ring-1 focus:ring-[#ff6b4a] text-[#1e3a5f]"
            />
          </div>

          <div>
            <label
              for="name"
              class="block text-sm font-medium text-[#1e3a5f] mb-2"
              >이름</label
            >
            <Input
              id="name"
              type="text"
              placeholder="이름을 입력하세요"
              maxlength="10"
              v-model="name"
              class="w-full h-12 px-4 border-[#dee2e6] rounded-lg focus:border-[#ff6b4a] focus:ring-1 focus:ring-[#ff6b4a] text-[#1e3a5f]"
            />
          </div>

          <div>
            <label
              for="companyName"
              class="block text-sm font-medium text-[#1e3a5f] mb-2"
              >회사명</label
            >
            <Input
              id="companyName"
              type="text"
              placeholder="회사명을 입력하세요"
              maxlength="100"
              v-model="companyName"
              class="w-full h-12 px-4 border-[#dee2e6] rounded-lg focus:border-[#ff6b4a] focus:ring-1 focus:ring-[#ff6b4a] text-[#1e3a5f]"
            />
          </div>

          <div>
            <label
              for="companyAddress"
              class="block text-sm font-medium text-[#1e3a5f] mb-2"
              >회사주소</label
            >
            <Input
              id="companyAddress"
              type="text"
              placeholder="도로명주소를 입력하세요."
              v-model="companyFrontAddress"
              readonly
              @click="handleAddressSearch"
              class="w-full h-12 px-4 border-[#dee2e6] rounded-lg focus:border-[#ff6b4a] focus:ring-1 focus:ring-[#ff6b4a] text-[#1e3a5f] cursor-pointer bg-white"
            />
            <Input
              ref="companyBackAddressRef"
              type="text"
              placeholder="상세주소를 입력하세요."
              maxlength="100"
              v-model="companyBackAddress"
              class="w-full h-12 px-4 border-[#dee2e6] rounded-lg focus:border-[#ff6b4a] focus:ring-1 focus:ring-[#ff6b4a] text-[#1e3a5f] mt-2"
            />
          </div>

          <div>
            <label
              for="phone"
              class="block text-sm font-medium text-[#1e3a5f] mb-2"
              >휴대폰 번호</label
            >
            <div class="flex gap-2">
              <Input
                id="phone"
                type="tel"
                placeholder="010-0000-0000"
                maxlength="13"
                v-model="phone"
                class="flex-1 h-12 px-4 border-[#dee2e6] rounded-lg focus:border-[#ff6b4a] focus:ring-1 focus:ring-[#ff6b4a] text-[#1e3a5f]"
              />
              <Button
                type="button"
                @click="handleSendVerifyCode"
                variant="outline"
                class="h-12 px-4 border-[#dee2e6] text-[#495057] bg-white hover:bg-[#f8f9fa] rounded-lg whitespace-nowrap"
              >
                {{ isCodeSent ? '재발송' : '인증번호 발송' }}
              </Button>
            </div>
          </div>

          <div
            v-if="isCodeSent"
            class="animate-in fade-in slide-in-from-top-2 duration-300"
          >
            <label
              for="verify"
              class="block text-sm font-medium text-[#1e3a5f] mb-2"
            >
              인증번호
            </label>
            <div class="relative">
              <Input
                id="verify"
                type="text"
                placeholder="인증번호를 입력하세요"
                v-model="verificationCode"
                class="w-full h-12 px-4 border-[#dee2e6] rounded-lg focus:border-[#ff6b4a] focus:ring-1 focus:ring-[#ff6b4a] text-[#1e3a5f] pr-16"
              />
              <span
                class="absolute right-4 top-1/2 -translate-y-1/2 text-sm font-medium"
                :class="isTimeout ? 'text-red-500' : 'text-[#ff6b4a]'"
              >
                {{ formattedTimer }}
              </span>
            </div>

            <p v-if="isTimeout" class="text-xs text-red-500 mt-1 pl-1">
              입력 시간이 초과되었습니다. 재발송 버튼을 눌러주세요.
            </p>
          </div>

          <div class="pt-4">
            <label
              class="flex items-center gap-3 p-4 bg-[#f8f9fa] rounded-xl cursor-pointer hover:bg-[#f1f3f5] transition-colors mb-3 border border-transparent hover:border-[#dee2e6]"
            >
              <div class="relative flex items-center justify-center w-5 h-5">
                <input
                  type="checkbox"
                  v-model="agreeAll"
                  class="peer appearance-none w-5 h-5 border-2 border-[#adb5bd] rounded checked:bg-[#ff6b4a] checked:border-[#ff6b4a] transition-colors"
                />
                <Check
                  class="absolute w-3.5 h-3.5 text-white pointer-events-none opacity-0 peer-checked:opacity-100"
                />
              </div>
              <span class="text-sm font-bold text-[#1e3a5f]"
                >약관 전체 동의</span
              >
            </label>

            <div class="flex flex-col gap-1 pl-1">
              <div class="flex items-center justify-between py-2 group">
                <label class="flex items-center gap-3 cursor-pointer flex-1">
                  <div
                    class="relative flex items-center justify-center w-5 h-5"
                  >
                    <input
                      type="checkbox"
                      v-model="agreeTerms"
                      class="peer appearance-none w-5 h-5 border border-[#dee2e6] rounded checked:bg-[#ff6b4a] checked:border-[#ff6b4a] transition-colors"
                    />
                    <Check
                      class="absolute w-3.5 h-3.5 text-white pointer-events-none opacity-0 peer-checked:opacity-100"
                    />
                  </div>
                  <span
                    class="text-sm text-[#495057] group-hover:text-[#1e3a5f] transition-colors"
                  >
                    <span class="text-[#ff6b4a] mr-1">(필수)</span>서비스
                    이용약관 동의
                  </span>
                </label>
                <button
                  type="button"
                  @click.stop="openModal('terms')"
                  class="p-1 text-[#adb5bd] hover:text-[#495057] hover:bg-[#f8f9fa] rounded-full transition-colors"
                >
                  <ChevronRight class="w-4 h-4" />
                </button>
              </div>

              <div class="flex items-center justify-between py-2 group">
                <label class="flex items-center gap-3 cursor-pointer flex-1">
                  <div
                    class="relative flex items-center justify-center w-5 h-5"
                  >
                    <input
                      type="checkbox"
                      v-model="agreePrivacy"
                      class="peer appearance-none w-5 h-5 border border-[#dee2e6] rounded checked:bg-[#ff6b4a] checked:border-[#ff6b4a] transition-colors"
                    />
                    <Check
                      class="absolute w-3.5 h-3.5 text-white pointer-events-none opacity-0 peer-checked:opacity-100"
                    />
                  </div>
                  <span
                    class="text-sm text-[#495057] group-hover:text-[#1e3a5f] transition-colors"
                  >
                    <span class="text-[#ff6b4a] mr-1">(필수)</span>개인정보
                    처리방침 동의
                  </span>
                </label>
                <button
                  type="button"
                  @click.stop="openModal('privacy')"
                  class="p-1 text-[#adb5bd] hover:text-[#495057] hover:bg-[#f8f9fa] rounded-full transition-colors"
                >
                  <ChevronRight class="w-4 h-4" />
                </button>
              </div>

              <div class="flex items-center justify-between py-2 group">
                <label class="flex items-center gap-3 cursor-pointer flex-1">
                  <div
                    class="relative flex items-center justify-center w-5 h-5"
                  >
                    <input
                      type="checkbox"
                      v-model="agreeMarketing"
                      class="peer appearance-none w-5 h-5 border border-[#dee2e6] rounded checked:bg-[#ff6b4a] checked:border-[#ff6b4a] transition-colors"
                    />
                    <Check
                      class="absolute w-3.5 h-3.5 text-white pointer-events-none opacity-0 peer-checked:opacity-100"
                    />
                  </div>
                  <span
                    class="text-sm text-[#495057] group-hover:text-[#1e3a5f] transition-colors"
                  >
                    <span class="text-[#adb5bd] mr-1">(선택)</span>마케팅 정보
                    수신 동의
                  </span>
                </label>
                <button
                  type="button"
                  @click.stop="openModal('marketing')"
                  class="p-1 text-[#adb5bd] hover:text-[#495057] hover:bg-[#f8f9fa] rounded-full transition-colors"
                >
                  <ChevronRight class="w-4 h-4" />
                </button>
              </div>
            </div>
          </div>

          <Button
            type="submit"
            class="w-full h-12 gradient-primary text-white font-semibold rounded-xl hover:opacity-90 transition-opacity shadow-button mt-6"
          >
            회원가입
          </Button>
        </form>

        <div class="mt-6 text-center">
          <p class="text-sm text-[#6c757d]">
            이미 회원이신가요?
            <RouterLink
              to="/login"
              class="text-[#ff6b4a] font-semibold hover:underline"
            >
              로그인
            </RouterLink>
          </p>
        </div>
      </Card>
    </main>
  </div>

  <Transition
    enter-active-class="transition duration-200 ease-out"
    enter-from-class="opacity-0"
    enter-to-class="opacity-100"
    leave-active-class="transition duration-150 ease-in"
    leave-from-class="opacity-100"
    leave-to-class="opacity-0"
  >
    <div v-if="isTermsModalOpen" class="fixed inset-0 z-[999]">
      <div
        class="absolute inset-0 bg-black/40 backdrop-blur-sm"
        @click="closeModal"
      ></div>

      <div
        class="absolute left-1/2 top-1/2 w-[calc(100%-32px)] max-w-[400px] -translate-x-1/2 -translate-y-1/2"
      >
        <Card
          class="flex flex-col max-h-[70vh] rounded-2xl bg-white shadow-2xl overflow-hidden animate-in fade-in zoom-in-95 duration-200"
        >
          <div
            class="px-5 py-4 border-b border-[#f1f3f5] flex items-center justify-between bg-white"
          >
            <h3 class="text-base font-bold text-[#1e3a5f]">
              {{ modalTitle }}
            </h3>
            <button
              type="button"
              class="text-[#adb5bd] hover:text-[#495057] transition-colors p-1"
              @click="closeModal"
            >
              <X class="w-5 h-5" />
            </button>
          </div>

          <div
            class="p-5 overflow-y-auto text-sm text-[#495057] leading-relaxed whitespace-pre-line bg-[#f8f9fa]"
          >
            {{ modalContent }}
          </div>

          <div class="p-4 bg-white border-t border-[#f1f3f5]">
            <Button
              type="button"
              class="w-full h-11 bg-[#1e3a5f] text-white font-semibold rounded-xl hover:bg-[#162c4b] transition-colors"
              @click="closeModal"
            >
              확인
            </Button>
          </div>
        </Card>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
/* 커스텀 체크박스 스타일링을 위해 appearance-none을 사용했으므로 별도 CSS 불필요 */
/* 모달 애니메이션을 위한 Transition 클래스는 Tailwind로 대체하거나 필요시 추가 */
</style>
