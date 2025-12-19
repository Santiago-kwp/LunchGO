<script setup lang="ts">
import { ref, watch, computed, onUnmounted, onMounted } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import {
  ArrowLeft,
  User,
  Camera,
  CalendarDays,
  Star,
  X,
  FileText,
} from 'lucide-vue-next';
import ReservationHistory from '@/components/ui/ReservationHistory.vue';
import UsageHistory from '@/components/ui/UsageHistory.vue';
import CheckEmailModal from '@/components/ui/CheckEmailModal.vue';

const router = useRouter();

// 이메일 인증 모달 표시 여부
const showEmailModal = ref(false);

// 이미지 상태 관리
const profileImage = ref<string | null>(null); // 미리보기용 (Base64 URL)
const selectedImageFile = ref<File | null>(null); // [추가] 실제 업로드할 파일 객체
const fileInput = ref<HTMLInputElement | null>(null);

// 네비게이션 상태
const activeNav = ref('info');

const showSuccess = ref(false);
const specialInterests = ref(['']); //특이사항

// Form fields state
const email = ref('popsicle0404@test.com');
const isEmailVerified = ref(false);
const name = ref('테스터');
const nickname = ref('');
const birthDate = ref('');
const gender = ref('공개하지 않음');

// 주소 관련 상태
const companyAddress = ref('서울시 강남구 테헤란로 123'); // 기존 통합 주소
const companyFrontAddress = ref(''); // 도로명 주소
const companyBackAddress = ref(''); // 상세 주소
const isAddressEditable = ref(false); // 주소 수정 모드 여부

const companyName = ref('xx회사');
const hideCompanyName = ref(false);

// 전화번호 관련 상태
const phoneNumber = ref('010-1234-5678');
const isPhoneEditable = ref(false); // 전화번호 수정 가능 여부
const showPhoneVerification = ref(false); // 인증번호 입력란 표시 여부
const isVerificationRequested = ref(false); // 인증 요청을 한 번이라도 했는지 여부

// 인증번호 관련 상태
const verificationCode = ref(''); //사용자가 입력한 인증번호
const verifyCode = ref(''); // 시스템이 보낸 인증번호
const isTimeout = ref(false);
const isVerified = ref(false);

//ref
const companyBackAddressRef = ref<any>(null);

// 타이머 상태
const timer = ref(180);
const timerInterval = ref<ReturnType<typeof setInterval> | null>(null);

// 생년월일 자동 포맷팅
watch(birthDate, (newVal) => {
  const cleaned = newVal.replace(/[^0-9]/g, '');
  let formatted = cleaned;

  if (cleaned.length > 8) {
    formatted = cleaned.slice(0, 8);
  }

  if (cleaned.length > 4 && cleaned.length <= 6) {
    formatted = `${cleaned.slice(0, 4)}-${cleaned.slice(4)}`;
  } else if (cleaned.length > 6) {
    formatted = `${cleaned.slice(0, 4)}-${cleaned.slice(4, 6)}-${cleaned.slice(
      6
    )}`;
  }

  if (newVal !== formatted) {
    birthDate.value = formatted;
  }
});

// 휴대폰 번호 자동 포맷팅
watch(phoneNumber, (newVal) => {
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
    phoneNumber.value = formatted;
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
    }
  }, 1000);
};

onUnmounted(() => {
  if (timerInterval.value) clearInterval(timerInterval.value);
});

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

// 주소 검색 핸들러
const handleAddressSearch = () => {
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

      //setTimeout을 사용하여 API가 창을 닫는 작업을 마칠 시간 줘야함
      setTimeout(() => {
        if (companyBackAddressRef.value) {
          // 상세주소 입력창으로 포커스 이동
          companyBackAddressRef.value.focus();
        }
      }, 100);
    },
  }).open();
};

const triggerFileInput = () => {
  fileInput.value?.click();
};

const handleImageUpload = (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0];
  if (file) {
    //실제 전송을 위해 파일 객체 저장
    selectedImageFile.value = file;

    //화면 미리보기를 위해 FileReader 사용
    const reader = new FileReader();
    reader.onload = (e) => {
      profileImage.value = e.target?.result as string;
    };
    reader.readAsDataURL(file);
  }
};

const handleVerifyCode = async () => {
  if (!verificationCode.value) return alert('인증번호를 입력해주세요.');

  //여기에 API를 통한 인증번호 체크 로직
  // 예: await api.checkVerificationCode(...)

  // [인증 성공 시 로직]
  isVerified.value = true;

  //인증번호 입력란 숨기기
  showPhoneVerification.value = false;

  //전화번호 입력창을 다시 Readonly로 변경
  isPhoneEditable.value = false;

  //버튼 상태 초기화 (다시 '번호변경' 버튼으로 돌아가기)
  isVerificationRequested.value = false;

  //타이머 정지
  if (timerInterval.value) clearInterval(timerInterval.value);
};

const checkInputElement = () => {
  if (isPhoneEditable.value) {
    if (!isVerified.value) return alert('전화번호 수정 시 인증은 필수입니다.');
  }

  if (!companyName.value) return alert('회사명은 필수 입력입니다.');
  if (isAddressEditable.value) {
    if (!companyFrontAddress.value || !companyBackAddress.value)
      return alert('주소를 입력해주세요.');
  }
  return null;
};

const handleVerifyEmail = () => {
  if (!email.value) return alert('이메일을 먼저 입력해주세요.');

  showEmailModal.value = true;
};

const handleEmailSuccess = () => {
  isEmailVerified.value = true;

  showEmailModal.value = false;
};

// 전화번호 변경 및 인증 요청 핸들러
const handlePhoneBtn = async () => {
  if (!isPhoneEditable.value) {
    isVerified.value = false; //변경하는 순간 다시 인증해야함
    isPhoneEditable.value = true;
    isVerificationRequested.value = false;
    showPhoneVerification.value = false;
  } else {
    if (phoneNumber.value.length < 12) {
      alert('올바른 전화번호를 입력해주세요.');
      return;
    }
    //인증번호 받아오기 api 이용
    alert('인증번호가 발송되었습니다.');
    showPhoneVerification.value = true;
    isVerificationRequested.value = true;
    startTimer();
  }
};

// 주소 변경 버튼 핸들러
const handleAddressChangeBtn = () => {
  isAddressEditable.value = true;
};

const addInterestField = () => {
  specialInterests.value.push('');
};

const updateInterest = (event: Event, index: number) => {
  const target = event.target as HTMLInputElement;
  specialInterests.value[index] = target.value;
};

const removeInterest = (index: number) => {
  specialInterests.value.splice(index, 1);
};

const handleSave = async () => {
  if (checkInputElement() !== null) return;

  // 주소가 수정 중이라면 저장 시 병합하는 로직 예시
  if (isAddressEditable.value) {
    companyAddress.value = `${companyFrontAddress.value} ${companyBackAddress.value}`;
    isAddressEditable.value = false;
  }
  try {
    let finalImageUrl = profileImage.value; // 기본값은 현재 이미지 경로

    //새로 선택된 이미지가 있다면 먼저 업로드하여 경로를 받아오기
    if (selectedImageFile.value) {
      const formData = new FormData();
      formData.append('profileImage', selectedImageFile.value);
      // 'profileImage'는 백엔드에서 받는 키 값과 일치해야 합니다.

      // [가상 코드] 이미지 업로드 API 호출
      // const uploadRes = await api.post('/api/upload/image', formData, {
      //   headers: { 'Content-Type': 'multipart/form-data' }
      // });

      // 서버가 반환해준 저장된 이미지 경로 (예: '/uploads/abc.jpg' 또는 'https://s3...')
      // finalImageUrl = uploadRes.data.filePath;
    }

    //받아온 이미지 경로와 나머지 폼 데이터를 DB에 저장 요청-payload (profileImageURL: finalImageUrl)

    // [가상 코드] 회원 정보 수정 API 호출
    // await api.put('/api/user/profile', payload);

    //성공 처리
    showSuccess.value = true;
    isAddressEditable.value = false;
    isPhoneEditable.value = false;

    //파일 객체 초기화 (저장 후에는 다시 선택 전까지 비워둠)
    selectedImageFile.value = null;

    setTimeout(() => (showSuccess.value = false), 3000);
  } catch (error) {
    console.error('저장 실패:', error);
    alert('저장 중 오류가 발생했습니다.');
  }
};

//회원 탈퇴 핸들러
const handleWithdraw = () => {
  if (
    confirm(
      '정말로 탈퇴하시겠습니까?\n탈퇴 시 모든 데이터가 삭제되며 복구할 수 없습니다.'
    )
  ) {
    // 여기에 탈퇴 API 호출
    alert('회원 탈퇴가 완료되었습니다.');
    router.push('/');
  }
};
</script>

<template>
  <div class="min-h-screen bg-[#F8F9FA] flex flex-col font-pretendard pb-10">
    <header class="bg-white border-b border-[#E9ECEF] sticky top-0 z-20">
      <div
        class="max-w-[500px] mx-auto px-4 h-14 flex items-center justify-between"
      >
        <RouterLink
          to="/"
          class="p-2 -ml-2 text-[#495057] hover:text-[#1E3A5F]"
        >
          <ArrowLeft class="w-6 h-6" />
        </RouterLink>
        <h1 class="text-lg font-bold text-[#1E3A5F]">마이페이지</h1>
        <div class="w-10" />
      </div>
    </header>

    <main class="flex-1 max-w-[500px] mx-auto w-full px-4 py-6 space-y-6">
      <section class="flex flex-col items-center pt-4">
        <div class="relative">
          <div
            class="w-24 h-24 bg-[#E9ECEF] rounded-2xl flex items-center justify-center border-2 border-white shadow-sm overflow-hidden"
          >
            <img
              v-if="profileImage"
              :src="profileImage"
              class="w-full h-full object-cover"
            />
            <User v-else class="w-12 h-12 text-[#ADB5BD]" />
          </div>
          <input
            type="file"
            ref="fileInput"
            class="hidden"
            accept="image/*"
            @change="handleImageUpload"
          />
          <button
            @click="triggerFileInput"
            class="absolute -bottom-2 -right-2 bg-white border border-[#E9ECEF] p-2 rounded-xl shadow-md hover:bg-[#F8F9FA] transition-all active:scale-90"
          >
            <Camera class="w-4 h-4 text-[#495057]" />
          </button>
        </div>
        <p class="mt-4 text-base font-bold text-[#1E3A5F]">{{ name }} 님</p>

        <nav
          class="w-full mt-6 flex bg-white border border-[#E9ECEF] rounded-xl overflow-hidden shadow-sm"
        >
          <button
            @click="activeNav = 'info'"
            :class="['nav-item', activeNav === 'info' ? 'active' : '']"
          >
            <User class="w-4 h-4 mr-1.5" />
            내 정보
          </button>
          <div class="w-[1px] h-4 bg-[#E9ECEF] self-center"></div>
          <button
            @click="activeNav = 'history'"
            :class="['nav-item', activeNav === 'history' ? 'active' : '']"
          >
            <CalendarDays class="w-4 h-4 mr-1.5" />
            예약내역
          </button>
          <div class="w-[1px] h-4 bg-[#E9ECEF] self-center"></div>
          <button
            @click="activeNav = 'usage'"
            :class="['nav-item', activeNav === 'usage' ? 'active' : '']"
          >
            <FileText class="w-4 h-4 mr-1.5" />
            이용내역
          </button>
          <div class="w-[1px] h-4 bg-[#E9ECEF] self-center"></div>
          <button
            @click="activeNav = 'favorite'"
            :class="['nav-item', activeNav === 'favorite' ? 'active' : '']"
          >
            <Star class="w-4 h-4 mr-1.5" />
            즐겨찾기
          </button>
        </nav>
      </section>

      <div v-if="activeNav === 'info'" class="space-y-6">
        <div class="info-card">
          <div class="card-title">기본 정보</div>
          <div class="p-6 space-y-5">
            <div class="input-group">
              <label>이메일</label>
              <div class="flex gap-2">
                <input
                  v-model="email"
                  type="email"
                  readonly
                  :class="[
                    'input-field flex-1 text-sm',
                    isEmailVerified
                      ? 'bg-[#F8F9FA] text-[#6C757D]'
                      : 'bg-white',
                  ]"
                />
                <button
                  @click="handleVerifyEmail"
                  :disabled="isEmailVerified"
                  :class="[
                    'whitespace-nowrap transition-colors',
                    isEmailVerified ? 'btn-disabled' : 'btn-outline-sm',
                  ]"
                >
                  {{ isEmailVerified ? '인증완료' : '인증하기' }}
                </button>
              </div>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div class="input-group">
                <label>이름</label>
                <input
                  v-model="name"
                  type="text"
                  readonly
                  class="input-field bg-[#F8F9FA] text-sm"
                />
              </div>
              <div class="input-group">
                <label>닉네임</label>
                <input
                  v-model="nickname"
                  type="text"
                  class="input-field"
                  maxlength="10"
                />
              </div>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div class="input-group">
                <label>생년월일</label>
                <input
                  v-model="birthDate"
                  type="text"
                  placeholder="YYYY-MM-DD"
                  maxlength="10"
                  class="input-field"
                />
              </div>
              <div class="input-group">
                <label>성별</label>
                <select v-model="gender" class="input-field">
                  <option>공개하지 않음</option>
                  <option>남</option>
                  <option>여</option>
                </select>
              </div>
            </div>
          </div>
        </div>

        <div class="info-card">
          <div class="card-title">연락처 및 소속</div>
          <div class="p-6 space-y-5">
            <div class="input-group">
              <label>전화번호</label>
              <div class="flex gap-2 mb-2">
                <input
                  v-model="phoneNumber"
                  type="tel"
                  maxlength="13"
                  :readonly="!isPhoneEditable"
                  :class="[
                    'input-field flex-1 transition-colors',
                    !isPhoneEditable
                      ? 'bg-[#F8F9FA] text-[#6C757D]'
                      : 'bg-white',
                  ]"
                />
                <button
                  @click="handlePhoneBtn"
                  class="btn-outline-sm whitespace-nowrap"
                >
                  {{
                    !isPhoneEditable
                      ? '번호변경'
                      : isVerificationRequested
                      ? '재전송'
                      : '인증요청'
                  }}
                </button>
              </div>
              <p
                v-if="!isTimeout && isVerified"
                class="text-xs text-red-500 mt-1 pl-1"
              >
                인증되었습니다.
              </p>

              <div v-if="showPhoneVerification">
                <div class="flex gap-2">
                  <input
                    v-model="verificationCode"
                    type="text"
                    placeholder="인증번호 입력"
                    class="input-field flex-1"
                    maxlength="6"
                  />
                  <button
                    class="btn-outline-sm whitespace-nowrap"
                    @click="handleVerifyCode"
                  >
                    확인
                  </button>
                </div>
                <p
                  v-if="!isTimeout"
                  class="text-[12px] text-[#FF6B4A] mt-1.5 pl-1 font-medium"
                >
                  {{ `남은 시간: ${formattedTimer}` }}
                </p>
                <p v-if="isTimeout" class="text-xs text-red-500 mt-1 pl-1">
                  입력 시간이 초과되었습니다. 재발송 버튼을 눌러주세요.
                </p>
              </div>
            </div>

            <div class="input-group">
              <label>회사명</label>
              <input
                v-model="companyName"
                type="text"
                class="input-field mb-2"
              />
              <label
                v-if="isEmailVerified"
                class="flex items-center gap-2 text-xs text-[#6C757D] cursor-pointer"
              >
                <input
                  v-model="hideCompanyName"
                  type="checkbox"
                  class="accent-[#FF6B4A] w-4 h-4"
                />
                회사명 숨기기
              </label>
            </div>

            <div class="input-group">
              <label>직장주소</label>

              <div v-if="!isAddressEditable" class="flex gap-2">
                <input
                  v-model="companyAddress"
                  type="text"
                  class="input-field flex-1"
                  readonly
                />
                <button
                  @click="handleAddressChangeBtn"
                  class="btn-outline-sm whitespace-nowrap"
                >
                  주소변경
                </button>
              </div>

              <div v-else class="space-y-2">
                <input
                  v-model="companyFrontAddress"
                  type="text"
                  placeholder="도로명주소 입력하세요"
                  readonly
                  @click="handleAddressSearch"
                  class="input-field"
                />

                <input
                  ref="companyBackAddressRef"
                  v-model="companyBackAddress"
                  type="text"
                  placeholder="상세주소를 입력하세요"
                  class="input-field"
                />
              </div>
            </div>
          </div>
        </div>

        <div class="info-card">
          <div class="card-title">기타</div>

          <p class="px-6 pt-4 text-xs text-[#868E96] leading-relaxed">
            팀원의 특이사항도 입력해보세요! 함께 반영됩니다.
          </p>

          <div class="p-6 space-y-5">
            <div class="input-group">
              <label>특이사항</label>
              <div class="space-y-2">
                <div
                  v-for="(interest, index) in specialInterests"
                  :key="index"
                  class="flex items-center gap-2"
                >
                  <input
                    :value="interest"
                    @input="updateInterest($event, index)"
                    placeholder="특이사항을 입력해주세요."
                    class="input-field flex-1"
                  />
                  <button
                    @click="removeInterest(index)"
                    class="p-2 text-[#ADB5BD] hover:text-[#FF6B4A] transition-colors rounded-lg hover:bg-[#FFF9F8]"
                    title="삭제"
                  >
                    <X class="w-5 h-5" />
                  </button>
                </div>

                <button
                  @click="addInterestField"
                  class="w-full h-10 border-2 border-dashed border-[#E9ECEF] rounded-lg text-[#ADB5BD] hover:bg-[#F8F9FA] transition-colors"
                >
                  + 추가하기
                </button>
              </div>
            </div>
          </div>
        </div>

        <CheckEmailModal
          v-if="showEmailModal"
          :email="email"
          @close="showEmailModal = false"
          @verified="handleEmailSuccess"
        />

        <button @click="handleSave" class="btn-primary-gradient">
          수정 사항 저장하기
        </button>

        <div class="flex justify-center mt-4 pb-4">
          <button
            @click="handleWithdraw"
            class="text-[#ADB5BD] text-xs underline underline-offset-4 hover:text-[#868E96] transition-colors"
          >
            회원 탈퇴
          </button>
        </div>
      </div>

      <ReservationHistory v-else-if="activeNav === 'history'" />

      <UsageHistory v-else-if="activeNav === 'usage'" />

      <div
        v-else-if="activeNav === 'favorite'"
        class="py-20 flex flex-col items-center justify-center text-[#ADB5BD]"
      >
        <Star class="w-12 h-12 mb-3 opacity-20" />
        <p>즐겨찾기한 항목이 없습니다.</p>
      </div>
    </main>

    <Transition name="fade">
      <div v-if="showSuccess" class="toast">
        <div class="flex items-center gap-2">
          <div class="bg-white rounded-full p-1">
            <svg
              class="w-3 h-3 text-[#FF6B4A]"
              fill="currentColor"
              viewBox="0 0 20 20"
            >
              <path
                fill-rule="evenodd"
                d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                clip-rule="evenodd"
              />
            </svg>
          </div>
          <span>성공적으로 수정되었습니다.</span>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/static/pretendard.min.css');

.font-pretendard {
  font-family: 'Pretendard', sans-serif;
}

/* Nav Styles */
.nav-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 14px 0;
  font-size: 13px;
  font-weight: 600;
  color: #adb5bd;
  transition: all 0.2s;
  background: white;
  border: none;
  cursor: pointer;
  white-space: nowrap;
}

.nav-item.active {
  color: #ff6b4a;
  background: #fff9f8;
}

.nav-item:hover:not(.active) {
  color: #6c757d;
  background: #fafafa;
}

/* Card Styles */
.info-card {
  background: #ffffff;
  border-radius: 16px;
  border: 1px solid #e9ecef;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
  overflow: hidden;
}

.card-title {
  padding: 16px 24px 0;
  font-size: 15px;
  font-weight: 700;
  color: #1e3a5f;
}

/* Input Styles */
.input-group label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: #6c757d;
  margin-bottom: 6px;
  padding-left: 2px;
}

.input-field {
  width: 100%;
  height: 44px;
  padding: 0 14px;
  font-size: 14px;
  border: 1.5px solid #e9ecef;
  border-radius: 10px;
  transition: all 0.2s;
  color: #1e3a5f;
}

.input-field:focus {
  outline: none;
  border-color: #ff6b4a;
  box-shadow: 0 0 0 3px rgba(255, 107, 74, 0.1);
}

/* Button Styles */
.btn-outline-sm {
  height: 44px;
  padding: 0 16px;
  font-size: 13px;
  font-weight: 600;
  color: #495057;
  background: white;
  border: 1.5px solid #e9ecef;
  border-radius: 10px;
  cursor: pointer;
}

.btn-disabled {
  height: 44px;
  padding: 0 16px;
  font-size: 13px;
  font-weight: 600;
  color: #adb5bd;
  background: #e9ecef;
  border: 1.5px solid #e9ecef;
  border-radius: 10px;
  cursor: default;
}

.btn-primary-gradient {
  width: 100%;
  height: 54px;
  background: linear-gradient(135deg, #ff6b4a 0%, #ff8e72 100%);
  color: white;
  font-size: 16px;
  font-weight: 700;
  border: none;
  border-radius: 14px;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(255, 107, 74, 0.3);
  transition: transform 0.2s, opacity 0.2s;
}

.btn-primary-gradient:active {
  transform: scale(0.98);
  opacity: 0.9;
}

/* Toast */
.toast {
  position: fixed;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  background: #343a40;
  color: white;
  padding: 12px 24px;
  border-radius: 50px;
  font-size: 14px;
  z-index: 100;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);

  white-space: nowrap; /* 텍스트가 절대 줄바꿈되지 않게 강제함 */
  width: max-content; /* 내용물(텍스트) 길이만큼 가로 사이즈를 확보함 */
  max-width: 90vw; /* 화면이 너무 작을 경우를 대비해 화면 너비의 90%까지만 커지게 제한 */
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translate(-50%, 20px);
}
</style>
