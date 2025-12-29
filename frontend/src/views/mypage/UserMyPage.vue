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
import UserFavorites from '@/components/ui/UserFavorites.vue';
import axios from 'axios';

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
const specialInterests =ref<(number | null)[]>([null]); //특이사항 id 저장

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
const isTimeout = ref(false);
const isPhoneVerified = ref(false);

//특이사항 list
const preferenceOptions = [
  { id: 1, label: '오이를 못 먹어요' },
  { id: 2, label: '오이를 좋아해요' },
  { id: 3, label: '고수를 못 먹어요' },
  { id: 4, label: '고수를 좋아해요' },
  { id: 5, label: '해물을 못 먹어요' },
  { id: 6, label: '해물을 좋아해요' },
  { id: 7, label: '치즈를 못 먹어요' },
  { id: 8, label: '치즈를 좋아해요' },
  { id: 9, label: '매운 음식을 좋아해요' },
  { id: 10, label: '매운 음식을 못 먹어요' },
  { id: 11, label: '느끼한 음식을 못 먹어요' },
  { id: 12, label: '인스타 감성을 좋아해요' },
  { id: 13, label: '땅콩을 못 먹어요' },
  { id: 14, label: '견과류를 못 먹어요' },
  { id: 15, label: '갑각류를 못 먹어요' },
  { id: 16, label: '우유를 못 먹어요' },
  { id: 17, label: '계란을 못 먹어요' },
  { id: 18, label: '밀가루를 못 먹어요' },
  { id: 19, label: '해산물을 못 먹어요' },
  { id: 20, label: '비건 음식을 좋아해요' },
  { id: 21, label: '돼지고기를 못 먹어요' },
  { id: 22, label: '소고기를 못 먹어요' }
];

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

// 특이사항: 현재 인덱스(currentIndex)에 보여줄 옵션 목록 계산
const getAvailableOptions = (currentIndex: number) => {
  const allSelectedIds = specialInterests.value;

  return preferenceOptions.filter((option) => {
    // 1. 아직 아무도 선택하지 않은 옵션이거나
    // 2. 현재 내 줄(currentIndex)에서 선택된 옵션인 경우
    return !allSelectedIds.includes(option.id) || allSelectedIds[currentIndex] === option.id;
  });
};

// 특이사항 추가 버튼
const addInterestField = () => {
  // 모든 옵션을 다 썼으면 추가 못하게 막기 (선택사항)
  if (specialInterests.value.length >= preferenceOptions.length) {
    return alert("더 이상 선택할 수 있는 옵션이 없습니다.");
  }
  specialInterests.value.push(null); // 빈 선택지 추가
};

//사용자 정보 가져오기
const fetchUserInfo = async() => {
  try{
    const userId = 5; //pinia에서 가져오기

    const response = await axios.get(`/api/info/user/${userId}`);
    const data = response.data;

    email.value = data.email;
    name.value = data.name;
    nickname.value = data.nickname || '';
    birthDate.value = data.birth || '';
    gender.value = data.gender || '공개하지 않음';
    phoneNumber.value = data.phone;

    companyName.value = data.companyName;
    companyAddress.value = data.companyAddress;

    isEmailVerified.value = data.emailAuthentication;

    if(data.image){
      profileImage.value = data.image;
    }

    // 서버에서 온 객체 리스트 -> ID 리스트로 변환
    // data.specialities = [{ specialityId: 1, keyword: '...', ... }, ...] 형태임
    if (data.specialities && data.specialities.length > 0) {
      specialInterests.value = data.specialities.map((item: any) => Number(item.specialityId));
    } else {
      specialInterests.value = [null]; // 비어있으면 빈 칸 하나 생성
    }
  }catch(error){
     const status = error.response.status;

      if(status === 404){
        alert("[404 Not Found] 해당 사용자가 존재하지 않습니다.");
      }else{
        alert(`오류가 발생했습니다. (Code: ${status})`);
      }
  }
}

//컴포넌트 마운트 시 스크립트 로드
onMounted(() => {
  loadDaumPostcodeScript();
  fetchUserInfo();
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

  try {
    const response = await axios.post('/api/sms/verify', {
      phone: phoneNumber.value,
      verifyCode: verificationCode.value
    });

    if(response.data === true){
      alert('인증이 완료되었습니다.');
      
      // [인증 성공 시 로직]
      isPhoneVerified.value = true;

      //인증번호 입력란 숨기기
      showPhoneVerification.value = false;

      //전화번호 입력창을 다시 Readonly로 변경
      isPhoneEditable.value = false;

      //버튼 상태 초기화 (다시 '번호변경' 버튼으로 돌아가기)
      isVerificationRequested.value = false;

      //타이머 정지
      if (timerInterval.value) clearInterval(timerInterval.value);

    } else{
      alert("인증번호가 일치하지 않습니다. 다시 확인해주세요.");

      isPhoneVerified.value = false;
    }
  } catch (error) {
    // 에러 처리
    const status = error.response.status;
    if (status === 400) alert("[400 Bad Request] 잘못된 요청입니다. 입력값을 확인해주세요.");
    else alert(`오류가 발생했습니다. (Code: ${status})`);
    
    isPhoneVerified.value = false;
  }
};

const checkInputElement = () => {
  if (isPhoneEditable.value) {
    if (!isPhoneVerified.value) return alert('전화번호 수정 시 인증은 필수입니다.');
  }

  if (!companyName.value) return alert('회사명은 필수 입력입니다.');
  if (isAddressEditable.value) {
    if (!companyFrontAddress.value || !companyBackAddress.value)
      return alert('주소를 입력해주세요.');
  }
  return null;
};

const handleVerifyEmail = async () => {
  showEmailModal.value = true;
};

const handleEmailSuccess = () => {
  isEmailVerified.value = true;
  showEmailModal.value = false;
};

// 전화번호 변경 및 인증 요청 핸들러
const handlePhoneBtn = async () => {
  if (!isPhoneEditable.value) {
    isPhoneVerified.value = false; //변경하는 순간 다시 인증해야함
    isPhoneEditable.value = true;
    isVerificationRequested.value = false;
    showPhoneVerification.value = false;
  } else {
    if (phoneNumber.value.length < 12) {
      alert('올바른 전화번호를 입력해주세요.');
      return;
    }

    isPhoneVerified.value = false;
    verificationCode.value = '';

  alert(`인증번호를 발송했습니다: ${phoneNumber.value}`);
  try {
    await axios.post('/api/sms/send', {phone: phoneNumber.value});

    showPhoneVerification.value = true;
    isVerificationRequested.value = true;
    startTimer(); 
  }catch(error){
    const status = error.response.status;

    if(status === 400) alert("[400 Bad Request] 잘못된 요청입니다. 입력값을 확인해주세요.");
    else alert(`메시지 전송에 오류가 발생했습니다. (Code: ${status})`);
  }  
}
};

// 주소 변경 버튼 핸들러
const handleAddressChangeBtn = () => {
  isAddressEditable.value = true;
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

    try{
      const formData = new FormData();

      const specialityIds = specialInterests.value
      .filter((id) => id !== null) // null 제거
      .map((id) => Number(id));    // 숫자로 확실하게 변환
      
      const infoData = {
        nickname: nickname.value,
        birth: birthDate.value ? birthDate.value:null,
        gender: gender.value,
        phone: phoneNumber.value,
        companyName: companyName.value,
        companyAddress: companyAddress.value,
        image: selectedImageFile.value ? null: profileImage.value,
        emailAuthentication: isEmailVerified.value,
        specialities: specialityIds
      }

      const jsonBlob = new Blob([JSON.stringify(infoData)], {type: "application/json"});
      formData.append("info",jsonBlob);

      if(selectedImageFile.value){
        formData.append('image', selectedImageFile.value);
      }

      const userId = 5; //pinia 수정 필요
      await axios.put(`/api/info/user/${userId}`,formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      
      //성공 처리
    showSuccess.value = true;
    isAddressEditable.value = false;
    isPhoneEditable.value = false;

    //파일 객체 초기화 (저장 후에는 다시 선택 전까지 비워둠)
    selectedImageFile.value = null;

    setTimeout(() => (showSuccess.value = false), 3000);

    }catch(error){
      const status = error.response.status;

      if(status === 404){
        alert("[404 Not Found] 해당 사용자가 존재하지 않습니다.");
      }else{
        alert(`오류가 발생했습니다. (Code: ${status})`);
      }
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
                v-if="!isTimeout && isPhoneVerified"
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
          v-for="(interestId, index) in specialInterests"
          :key="index"
          class="flex items-center gap-2"
        >
          <select
            v-model="specialInterests[index]"
            class="input-field flex-1 appearance-none bg-white cursor-pointer"
          >
            <option :value="null" disabled>특이사항을 선택해주세요.</option>
            <option
              v-for="option in getAvailableOptions(index)"
              :key="option.id"
              :value="option.id"
            >
              {{ option.label }}
            </option>
          </select>

          <button
            @click="removeInterest(index)"
            class="p-2 text-[#ADB5BD] hover:text-[#FF6B4A] transition-colors rounded-lg hover:bg-[#FFF9F8] border border-transparent hover:border-[#ffece9]"
            title="삭제"
          >
            <X class="w-5 h-5" />
          </button>
        </div>

        <button
          @click="addInterestField"
          class="w-full h-10 border-2 border-dashed border-[#E9ECEF] rounded-lg text-[#ADB5BD] hover:bg-[#F8F9FA] transition-colors flex items-center justify-center gap-1 font-medium text-sm"
        >
          <span>+</span> 추가하기
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

      <UserFavorites v-else-if="activeNav === 'favorite'" />
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
