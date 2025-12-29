<script setup>
import Button from "@/components/ui/Button.vue";
import CafeteriaMenuUploadModal from "@/components/ui/CafeteriaMenuUploadModal.vue";
import CafeteriaRecommendationList from "@/components/ui/CafeteriaRecommendationList.vue";
import { Search } from "lucide-vue-next";

defineProps({
  recommendations: {
    type: Array,
    required: true,
  },
  favoriteRestaurantIds: {
    type: Array,
    required: true,
  },
  onToggleFavorite: {
    type: Function,
    required: true,
  },
  onOpenSearch: {
    type: Function,
    required: true,
  },
  onClearRecommendations: {
    type: Function,
    required: true,
  },
  isModalOpen: {
    type: Boolean,
    required: true,
  },
  isProcessing: {
    type: Boolean,
    required: true,
  },
  initialImageUrl: {
    type: String,
    default: "",
  },
  ocrResult: {
    type: Object,
    default: null,
  },
  days: {
    type: Array,
    default: () => [],
  },
  errorMessage: {
    type: String,
    default: "",
  },
  onModalClose: {
    type: Function,
    required: true,
  },
  onFileChange: {
    type: Function,
    required: true,
  },
  onRunOcr: {
    type: Function,
    required: true,
  },
  onConfirm: {
    type: Function,
    required: true,
  },
});
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-4">
      <h3 class="text-lg font-semibold text-[#1e3a5f]">
        {{ recommendations.length ? "구내식당 대체 추천" : "오늘의 추천 회식 맛집" }}
      </h3>
      <Button
        v-if="!recommendations.length"
        @click="onOpenSearch"
        variant="outline"
        size="sm"
        class="h-8 px-3 text-xs border-[#dee2e6] text-[#495057] bg-white hover:bg-[#f8f9fa] hover:text-[#1e3a5f] rounded-lg flex items-center gap-1"
      >
        <Search class="w-3.5 h-3.5" />
        검색
      </Button>
      <Button
        v-else
        @click="onClearRecommendations"
        variant="outline"
        size="sm"
        class="h-8 px-3 text-xs border-[#dee2e6] text-[#495057] bg-white hover:bg-[#f8f9fa] hover:text-[#1e3a5f] rounded-lg flex items-center gap-1"
      >
        추천 해제
      </Button>
    </div>

    <CafeteriaRecommendationList
      v-if="recommendations.length"
      :recommendations="recommendations"
      :favoriteRestaurantIds="favoriteRestaurantIds"
      :onToggleFavorite="onToggleFavorite"
      class="mb-6"
    />

    <CafeteriaMenuUploadModal
      v-if="isModalOpen"
      @close="onModalClose"
      @file-change="onFileChange"
      @run-ocr="onRunOcr"
      @confirm="onConfirm"
      :is-processing="isProcessing"
      :initial-image-url="initialImageUrl"
      :ocr-result="ocrResult"
      :days="days"
      :error-message="errorMessage"
    />
  </div>
</template>
