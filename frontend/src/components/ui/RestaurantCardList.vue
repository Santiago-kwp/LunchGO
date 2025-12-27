<script setup>
import { RouterLink } from "vue-router";
import { Star } from "lucide-vue-next";
import Card from "@/components/ui/Card.vue";

const props = defineProps({
  restaurants: {
    type: Array,
    default: () => [],
  },
  favoriteRestaurantIds: {
    type: Array,
    default: () => [],
  },
  onToggleFavorite: {
    type: Function,
    default: () => {},
  },
});

const isRestaurantFavorite = (restaurantId) =>
  props.favoriteRestaurantIds.includes(restaurantId);

const normalizeTag = (tag) => {
  if (!tag) return null;
  if (typeof tag === "string") {
    return { name: tag };
  }
  const name = tag.name ?? tag.content;
  if (!name) return null;
  return { name, count: tag.count };
};

const buildTagList = (tags) => (tags || []).map(normalizeTag).filter(Boolean);

const resolveDisplayTags = (restaurant) => {
  const reviewTags = buildTagList(restaurant.reviewTags || []).filter(
    (tag) => (tag.count ?? 0) > 0
  );
  const restaurantTags = buildTagList(
    restaurant.restaurantTags || restaurant.tags || restaurant.topTags || []
  );

  if (reviewTags.length >= 3) {
    return reviewTags.slice(0, 3);
  }
  if (restaurantTags.length) {
    return restaurantTags.slice(0, 3);
  }
  return reviewTags.slice(0, 3);
};

const hasDisplayTags = (restaurant) => resolveDisplayTags(restaurant).length > 0;

const formatTagLabel = (tag) => {
  if (!tag) return "";
  if (tag.count == null || tag.count === "") {
    return tag.name ?? "";
  }
  return `${tag.name} ${tag.count}`;
};
</script>

<template>
  <div class="space-y-3">
    <RouterLink
      v-for="restaurant in restaurants"
      :key="restaurant.id"
      :to="`/restaurant/${restaurant.id}`"
    >
      <Card
        class="relative overflow-hidden border-[#e9ecef] rounded-xl bg-white shadow-card hover:shadow-lg transition-shadow cursor-pointer"
      >
        <button
          type="button"
          class="absolute top-3 right-3 z-10 p-2 rounded-full bg-white/90 shadow-card text-[#c4c4c4] hover:text-[#ff6b4a] transition-colors"
          :aria-pressed="isRestaurantFavorite(restaurant.id)"
          @click.stop.prevent="onToggleFavorite(restaurant.id)"
        >
          <Star
            class="w-4 h-4"
            :class="
              isRestaurantFavorite(restaurant.id)
                ? 'fill-current text-[#ff6b4a]'
                : 'text-[#adb5bd] fill-white'
            "
          />
          <span class="sr-only">
            {{
              isRestaurantFavorite(restaurant.id)
                ? '즐겨찾기 해제'
                : '즐겨찾기에 추가'
            }}
          </span>
        </button>
        <div class="flex gap-3 p-2">
          <div
            class="relative w-24 h-24 flex-shrink-0 rounded-lg overflow-hidden"
          >
            <img
              :src="restaurant.image || '/placeholder.svg'"
              :alt="restaurant.name"
              class="w-full h-full object-cover"
            />
          </div>

          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 mb-1">
              <h4 class="font-semibold text-[#1e3a5f] text-sm">
                {{ restaurant.name }}
              </h4>
            </div>
            <p class="text-xs text-[#6c757d] mb-1 truncate">
              {{ restaurant.address }}
            </p>
            <div class="flex items-center gap-1 mb-1.5">
              <Star class="w-3.5 h-3.5 fill-[#ffc107] text-[#ffc107]" />
              <span class="text-sm font-medium text-[#1e3a5f]">{{
                restaurant.rating ?? "-"
              }}</span>
              <span class="text-xs text-[#6c757d]"
                >(리뷰수 : {{ restaurant.reviews ?? 0 }})</span
              >
            </div>
            <div
              v-if="hasDisplayTags(restaurant)"
              class="flex flex-wrap gap-1 mb-2"
            >
              <span
                v-for="(tag, index) in resolveDisplayTags(restaurant)"
                :key="index"
                class="inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded-full bg-gradient-to-r from-[#ff6b4a] to-[#ffc4b8] text-white"
              >
                {{ formatTagLabel(tag) }}
              </span>
            </div>
            <p class="text-sm font-semibold text-[#1e3a5f]">
              {{ restaurant.price || "가격 정보 없음" }}
            </p>
          </div>
        </div>
      </Card>
    </RouterLink>
  </div>
</template>
