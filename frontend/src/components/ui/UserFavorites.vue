<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { RouterLink } from 'vue-router';
import { Heart, Star, Bell } from 'lucide-vue-next';
// import axios from 'axios'; // 실제 API 연동 시 주석 해제

// 타입 정의
interface Restaurant {
  id: number;
  name: string;
  category: string;
  rating: number;
  reviews: number;
  description: string;
  price: string;
  badgeColor: string;
  hasPromotion: boolean; // 프로모션 알림 수신 여부
}

//api 호출 시 필요한 정보(변수명과 매핑)
const userId = ref('');
const restaurantId = ref('');
const promotionAgree = ref('');

// 상태 관리
const favorites = ref<Restaurant[]>([]);
const isLoading = ref(true); // 로딩 상태

// 1. [API 로직] 즐겨찾기 목록 불러오기
const fetchFavorites = async () => {
  try {
    isLoading.value = true;
    
    // [실제 API 호출 예시]

    // 보통 헤더에 토큰을 담아서 보냄

    // const token = localStorage.getItem('accessToken');

    // const response = await axios.get('/api/user/favorites', {

    //   headers: { Authorization: `Bearer ${token}` }

    // });

    // favorites.value = response.data;
    
    // 더미 데이터에 hasPromotion 필드 포함
    favorites.value = [
      {
        id: 1,
        name: '판교 숯불갈비',
        category: '한식',
        rating: 4.8,
        reviews: 245,
        description: '회식 분위기 최고, 술 사실이 풍부해요',
        price: '18,000원 · 4인 ~ 12인',
        badgeColor: 'bg-emerald-500',
        hasPromotion: true, // 이미 알림 받는 중
      },
      {
        id: 2,
        name: '중화요리 맛집',
        category: '중식',
        rating: 4.7,
        reviews: 189,
        description: '단체석 완비, 룸 예약 가능',
        price: '15,000원 · 4인 ~ 10인',
        badgeColor: 'bg-emerald-600',
        hasPromotion: false, // 알림 안 받는 중
      },
      {
        id: 3,
        name: '이탈리안 레스토랑',
        category: '양식',
        rating: 4.9,
        reviews: 312,
        description: '분위기 좋고, 와인 선택 다양',
        price: '22,000원 · 4인 ~ 8인',
        badgeColor: 'bg-emerald-500',
        hasPromotion: false,
      },
    ];
  } catch (error) {
    console.error('데이터 로드 실패:', error);
  } finally {
    isLoading.value = false;
  }
};

// 컴포넌트 마운트 시 데이터 호출
onMounted(() => {
  fetchFavorites();
});

// 찜 해제 핸들러
const toggleFavorite = async (id: number) => {
  if (confirm('즐겨찾기에서 삭제하시겠습니까?')) {
    favorites.value = favorites.value.filter((item) => item.id !== id);
    // API 삭제 로직 추가 가능
  }
};

// [프로모션 토글 핸들러]
const handlePromotionToggle = async (restaurant: Restaurant) => {
  const previousState = restaurant.hasPromotion;
  restaurant.hasPromotion = !previousState;
  
  const actionName = restaurant.hasPromotion ? '설정' : '해제';

  try {
    // API 호출
    // if (restaurant.hasPromotion) {
    //   await axios.post(`/api/promotion/${restaurant.id}`);
    // } else {
    //   await axios.delete(`/api/promotion/${restaurant.id}`);
    // }
    
  } catch (error) {
    // 실패 시 롤백
    restaurant.hasPromotion = previousState;
    alert(`프로모션 ${actionName} 중 오류가 발생했습니다.`);
  }
};
</script>

<template>
  <div class="h-full">
    <div v-if="isLoading" class="py-20 flex justify-center items-center">
       <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-[#ff6b4a]"></div>
    </div>

    <template v-else>
      <div v-if="favorites.length > 0">
        <div class="px-2 py-4 border-b border-[#e9ecef] mb-2">
          <p class="text-sm text-[#495057]">
            총 <span class="font-semibold text-[#ff6b4a]">{{ favorites.length }}개</span>의 즐겨찾기
          </p>
        </div>

        <div class="space-y-4 pb-10">
          <div
            v-for="restaurant in favorites"
            :key="restaurant.id"
            class="bg-white border border-[#e9ecef] rounded-xl overflow-hidden shadow-sm hover:shadow-md transition-shadow"
          >
            <div class="flex gap-3 p-3">
              <div class="relative w-24 h-24 flex-shrink-0 rounded-lg overflow-hidden bg-gray-200">
                <div class="absolute top-2 left-2 z-10">
                  <span
                    :class="`${restaurant.badgeColor} text-white text-[10px] font-medium px-2 py-0.5 rounded shadow-sm`"
                  >
                    {{ restaurant.category }}
                  </span>
                </div>
                <div class="absolute inset-0 flex items-center justify-center text-gray-400 text-xs font-medium bg-gray-100">
                  이미지
                </div>
              </div>

              <div class="flex-1 min-w-0 flex flex-col justify-center">
                <div class="flex items-start justify-between mb-1">
                  <h4 class="font-bold text-[#1e3a5f] text-sm truncate pr-2">{{ restaurant.name }}</h4>
                  <button 
                    @click.prevent="toggleFavorite(restaurant.id)"
                    class="w-6 h-6 flex items-center justify-center hover:bg-gray-50 rounded-full -mt-1 -mr-1 transition-colors"
                  >
                    <Heart class="w-4 h-4 fill-[#ff6b4a] text-[#ff6b4a]" />
                  </button>
                </div>
                
                <div class="flex items-center gap-1 mb-1.5">
                  <Star class="w-3 h-3 fill-[#ffc107] text-[#ffc107]" />
                  <span class="text-sm font-bold text-[#1e3a5f]">{{ restaurant.rating }}</span>
                  <span class="text-xs text-[#868e96]">({{ restaurant.reviews }})</span>
                </div>
                
                <p class="text-xs text-[#868e96] mb-1.5 truncate">{{ restaurant.description }}</p>
                <p class="text-sm font-bold text-[#1e3a5f]">{{ restaurant.price }}</p>
              </div>
            </div>

            <div class="flex gap-2 px-3 pb-3">
              <button 
                @click="handlePromotionToggle(restaurant)"
                :class="[
                  'flex-1 h-9 flex items-center justify-center gap-1.5 text-xs font-semibold border rounded-lg transition-all',
                  restaurant.hasPromotion
                    ? 'bg-[#fff9f8] border-[#ff6b4a] text-[#ff6b4a] hover:bg-[#fff0ed]' 
                    : 'bg-white border-[#dee2e6] text-[#1e3a5f] hover:bg-[#f8f9fa] group'
                ]"
              >
                <Bell 
                  :class="[
                    'w-3.5 h-3.5 transition-colors',
                    restaurant.hasPromotion 
                      ? 'fill-[#ff6b4a] text-[#ff6b4a]' 
                      : 'text-[#1e3a5f] group-hover:text-[#ff6b4a]'
                  ]" 
                />
                {{ restaurant.hasPromotion ? '알림 받는 중' : '프로모션 받기' }}
              </button>

              <RouterLink :to="`/restaurant/${restaurant.id}?type=preorder`" class="flex-1">
                <button class="w-full h-9 text-xs font-bold text-white rounded-lg shadow-sm btn-gradient hover:opacity-90 transition-opacity">
                  예약하기
                </button>
              </RouterLink>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="py-20 flex flex-col items-center justify-center text-[#ADB5BD]">
        <Star class="w-12 h-12 mb-3 opacity-20" />
        <p class="text-sm">즐겨찾기한 항목이 없습니다.</p>
      </div>
    </template>
  </div>
</template>

<style scoped>
.btn-gradient {
  background: linear-gradient(135deg, #ff6b4a 0%, #ff8e72 100%);
}
</style>