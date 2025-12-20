<script setup>
import { ref, computed, onBeforeUnmount } from "vue";

const emit = defineEmits(["close", "recommend"]);

const previewUrl = ref("");
let currentObjectUrl = "";
const canRecommend = computed(() => Boolean(previewUrl.value));

const handleFileChange = (event) => {
  const file = event.target.files?.[0];
  if (!file) {
    if (currentObjectUrl) {
      URL.revokeObjectURL(currentObjectUrl);
      currentObjectUrl = "";
    }
    previewUrl.value = "";
    return;
  }

  if (currentObjectUrl) {
    URL.revokeObjectURL(currentObjectUrl);
  }

  currentObjectUrl = URL.createObjectURL(file);
  previewUrl.value = currentObjectUrl;
};

onBeforeUnmount(() => {
  if (currentObjectUrl) {
    URL.revokeObjectURL(currentObjectUrl);
  }
});
</script>

<template>
  <Teleport to="body">
    <div
      class="fixed inset-0 z-[110] bg-black/50 flex items-center justify-center"
      @click.self="emit('close')"
    >
      <div class="w-full max-w-[520px] bg-white rounded-2xl shadow-xl mx-4">
        <div class="px-5 py-4 border-b border-[#e9ecef] flex items-center justify-between">
          <h3 class="text-lg font-semibold text-[#1e3a5f]">
            구내식당 주간 메뉴
          </h3>
          <button
            type="button"
            class="text-[#6c757d] hover:text-[#1e3a5f]"
            @click="emit('close')"
          >
            닫기
          </button>
        </div>

        <div class="p-5 space-y-4">
          <p class="text-sm text-[#6c757d]">
            이번주 구내식당 메뉴 사진을 올리면 날짜별로 대체 식당을 추천해줄게요.
          </p>

          <div class="space-y-2">
            <label class="text-sm font-semibold text-[#1e3a5f]">
              메뉴 사진 업로드
            </label>
            <input
              type="file"
              accept="image/*"
              class="w-full text-sm file:mr-4 file:py-2 file:px-3 file:rounded-lg file:border-0 file:bg-[#f8f9fa] file:text-[#495057] hover:file:bg-[#e9ecef]"
              @change="handleFileChange"
            />
          </div>

          <div
            class="border border-dashed border-[#dee2e6] rounded-xl p-4 text-center bg-[#f8f9fa]"
          >
            <template v-if="previewUrl">
              <img
                :src="previewUrl"
                alt="주간 메뉴 미리보기"
                class="max-h-[280px] w-full object-contain rounded-lg bg-white"
              />
            </template>
            <template v-else>
              <p class="text-sm text-[#6c757d]">
                업로드된 사진이 여기에 표시됩니다.
              </p>
            </template>
          </div>
        </div>

        <div class="px-5 py-4 border-t border-[#e9ecef] flex justify-end gap-2">
          <button
            type="button"
            class="px-4 py-2 rounded-lg text-sm font-medium transition-colors bg-[#f8f9fa] text-[#495057] hover:bg-[#e9ecef]"
            @click="emit('close')"
          >
            닫기
          </button>
          <button
            type="button"
            class="px-4 py-2 rounded-lg text-sm font-medium transition-colors gradient-primary text-white disabled:opacity-60 disabled:cursor-not-allowed"
            :disabled="!canRecommend"
            @click="emit('recommend')"
          >
            추천받기
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>
