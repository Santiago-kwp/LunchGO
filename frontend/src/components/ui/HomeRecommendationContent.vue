<script setup>
import CafeteriaRecommendationSection from "@/components/ui/CafeteriaRecommendationSection.vue";
import TrendingRecommendationSection from "@/components/ui/TrendingRecommendationSection.vue";
import HomeRecommendationHeader from "@/components/ui/HomeRecommendationHeader.vue";
import RestaurantCardList from "@/components/ui/RestaurantCardList.vue";
import RestaurantCardSkeletonList from "@/components/ui/RestaurantCardSkeletonList.vue";

defineProps({
  isLoggedIn: {
    type: Boolean,
    default: false,
  },
  cafeteriaRecommendations: {
    type: Array,
    default: () => [],
  },
  recommendationButtons: {
    type: Array,
    default: () => [],
  },
  selectedRecommendation: {
    type: String,
    default: null,
  },
  recommendWeatherKey: {
    type: String,
    required: true,
  },
  recommendTasteKey: {
    type: String,
    required: true,
  },
  recommendBudgetKey: {
    type: String,
    required: true,
  },
  isTrendingSort: {
    type: Boolean,
    default: false,
  },
  isTrendingLoading: {
    type: Boolean,
    default: false,
  },
  trendingError: {
    type: String,
    default: "",
  },
  trendingCards: {
    type: Array,
    default: () => [],
  },
  isWeatherLoading: {
    type: Boolean,
    default: false,
  },
  tagMappingNotice: {
    type: String,
    default: "",
  },
  tasteRecommendationSummary: {
    type: String,
    default: "",
  },
  filterPerPersonBudgetDisplay: {
    type: String,
    default: "",
  },
  paginatedRestaurants: {
    type: Array,
    default: () => [],
  },
  onSelectRecommendation: {
    type: Function,
    required: true,
  },
  onOpenSearch: {
    type: Function,
    required: true,
  },
  onClearCafeteria: {
    type: Function,
    required: true,
  },
  onClearTrending: {
    type: Function,
    required: true,
  },
  onClearWeather: {
    type: Function,
    required: true,
  },
  onClearTaste: {
    type: Function,
    required: true,
  },
  onClearBudget: {
    type: Function,
    required: true,
  },
  isCafeteriaModalOpen: {
    type: Boolean,
    default: false,
  },
  isCafeteriaOcrLoading: {
    type: Boolean,
    default: false,
  },
  cafeteriaOcrResult: {
    type: Object,
    default: null,
  },
  cafeteriaDaysDraft: {
    type: Array,
    default: () => [],
  },
  cafeteriaOcrError: {
    type: String,
    default: "",
  },
  cafeteriaImageUrl: {
    type: String,
    default: "",
  },
  onCafeteriaModalClose: {
    type: Function,
    required: true,
  },
  onCafeteriaFileChange: {
    type: Function,
    required: true,
  },
  onCafeteriaOcr: {
    type: Function,
    required: true,
  },
  onCafeteriaConfirm: {
    type: Function,
    required: true,
  },
});
</script>

<template>
  <div class="space-y-4">
    <CafeteriaRecommendationSection
      v-if="isLoggedIn"
      :recommendations="cafeteriaRecommendations"
      :recommendation-buttons="recommendationButtons"
      :active-recommendation="selectedRecommendation"
      :onSelectRecommendation="onSelectRecommendation"
      :show-buttons="false"
      :onOpenSearch="onOpenSearch"
      :onClearRecommendations="onClearCafeteria"
      :isModalOpen="isCafeteriaModalOpen"
      :isProcessing="isCafeteriaOcrLoading"
      :ocrResult="cafeteriaOcrResult"
      :days="cafeteriaDaysDraft"
      :errorMessage="cafeteriaOcrError"
      :initialImageUrl="cafeteriaImageUrl"
      :onModalClose="onCafeteriaModalClose"
      :onFileChange="onCafeteriaFileChange"
      :onRunOcr="onCafeteriaOcr"
      :onConfirm="onCafeteriaConfirm"
    />
    <div
      v-if="!cafeteriaRecommendations.length && tagMappingNotice"
      class="rounded-2xl border border-[#e9ecef] bg-white px-4 py-3 text-sm text-gray-700"
    >
      {{ tagMappingNotice }}
    </div>

    <TrendingRecommendationSection
      :isActive="!cafeteriaRecommendations.length && isTrendingSort"
      :isLoading="isTrendingLoading"
      :error="trendingError"
      :cards="trendingCards"
      :onClear="onClearTrending"
    />

    <HomeRecommendationHeader
      v-if="!cafeteriaRecommendations.length && selectedRecommendation === recommendWeatherKey"
      title="날씨 추천"
      :onClear="onClearWeather"
    />

    <HomeRecommendationHeader
      v-if="!cafeteriaRecommendations.length && selectedRecommendation === recommendTasteKey"
      title="취향 맞춤 추천"
      :subtitle="tasteRecommendationSummary"
      description="나와 팀원의 특이사항 태그를 기반으로 매칭 점수가 높은 식당을 골랐어요."
      :onClear="onClearTaste"
    />

    <HomeRecommendationHeader
      v-if="!cafeteriaRecommendations.length && selectedRecommendation === recommendBudgetKey"
      title="예산 맞춤 추천"
      :subtitle="`1인당 ${filterPerPersonBudgetDisplay}`"
      :onClear="onClearBudget"
    />

    <div
      v-if="!cafeteriaRecommendations.length && selectedRecommendation === recommendWeatherKey && isWeatherLoading"
      class="px-4"
    >
      <RestaurantCardSkeletonList :count="3" />
    </div>

    <div
      v-else-if="!cafeteriaRecommendations.length && !isTrendingSort && !paginatedRestaurants.length"
      class="w-full px-4 py-10 text-center text-sm text-gray-700"
    >
      해당 검색 결과가 없습니다.
    </div>
    <RestaurantCardList
      v-else-if="!cafeteriaRecommendations.length && !isTrendingSort"
      :restaurants="paginatedRestaurants"
    />
  </div>
</template>
