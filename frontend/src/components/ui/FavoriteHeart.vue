<script setup lang="ts">
import { ref, watch } from 'vue';
import { Heart } from 'lucide-vue-next';
import axios from 'axios'; 
import { data } from 'autoprefixer';

//userId를 pinia에서 가져와야함
const userId = ref('');

const props = defineProps<{
  restaurantId: number;
  initialFavorite?: boolean; // 초기 즐겨찾기 상태
}>();

// 부모에게 상태 변경을 알리기 위한 이벤트 정의
const emit = defineEmits<{
  (e: 'update:favorite', status: boolean): void; // 상태가 바뀔 때
  (e: 'remove'): void; // 해제 시 목록에서 제거
}>();

// 내부 상태 관리
const isActive = ref(props.initialFavorite || false);

// props가 외부에서 변할 경우 동기화
watch(() => props.initialFavorite, (newVal) => {
  isActive.value = newVal || false;
});

const toggleFavorite = async () => {
  const previousState = isActive.value;

  // 1. 채워진 상태 -> 빈 상태
  if (isActive.value) {
    if (!confirm('즐겨찾기를 해제하시겠습니까?')) return;
    
    // UI 낙관적 업데이트 (API 응답 전 미리 변경)
    isActive.value = false;
    
    try {
      await axios.delete(`/api/bookmark`,{ 
      data: { userId: userId.value, restaurantId: props.restaurantId }
    });
      
      emit('update:favorite', false);
      emit('remove'); // 부모에게 삭제 알림 (목록에서 제거)
      
    } catch (error) {
      isActive.value = previousState; // 실패 시 롤백
      const status = error.response.status;

      switch(status){
        case 400:
            alert("[400 Bad Request] 잘못된 요청입니다 입력값을 확인해주세요.");
            break;
        case 404:
            alert("[404 Not Found] 이미 삭제되었거나 즐겨찾기에 없는 맛집입니다.");
            isActive.value = false;
            break;
        default:
            alert(`오류가 발생했습니다. (Code: ${status})`);
      }
    }
  } 
  // 2. 빈 상태 -> 채워진 상태 (등록)
  else {
    isActive.value = true;
    
    try {
      await axios.post('/api/bookmark', {
        userId: userId.value, restaurantId: props.restaurantId 
      });
      
      emit('update:favorite', true);
      
    } catch (error) {
      isActive.value = previousState; //롤백
      
      const status = error.response.status;

      switch(status){
        case 400:
            alert("[400 Bad Request] 잘못된 요청입니다 입력값을 확인해주세요.");
            break;
        case 409:
            alert("[409 Conflict] 이미 즐겨찾기에 추가되어 있습니다.");
            isActive.value = true;
            break;
        default:
            alert(`오류가 발생했습니다. (Code: ${status})`);
      }
    }
  }
};
</script>

<template>
  <button 
    @click.prevent="toggleFavorite"
    class="w-6 h-6 flex items-center justify-center hover:bg-gray-50 rounded-full transition-colors focus:outline-none"
    :title="isActive ? '즐겨찾기 해제' : '즐겨찾기 등록'"
  >
    <Heart 
      class="w-4 h-4 transition-all duration-200" 
      :class="isActive 
        ? 'fill-[#ff6b4a] text-[#ff6b4a]' 
        : 'fill-none text-gray-300'"
    />
  </button>
</template>