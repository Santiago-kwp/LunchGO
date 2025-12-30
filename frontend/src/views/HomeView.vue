<script setup>
import {
  ref,
  computed,
  watch,
  onMounted,
  onBeforeUnmount,
  reactive,
  nextTick,
} from "vue";
import {
  MapPin,
  Calendar,
  User,
  Star,
  X,
  Minus,
  Plus,
  SlidersHorizontal,
  ChevronLeft,
  ChevronRight,
  Home,
} from "lucide-vue-next"; // Import Lucide icons for Vue
import Button from "@/components/ui/Button.vue"; // Import our custom Button component
import AppFooter from "@/components/ui/AppFooter.vue";
import BottomNav from "@/components/ui/BottomNav.vue";
import { RouterLink } from "vue-router"; // Import Vue RouterLink
import { loadKakaoMaps, geocodeAddress } from "@/utils/kakao";
import { restaurants as restaurantData } from "@/data/restaurants";
import AppHeader from "@/components/ui/AppHeader.vue";
import CafeteriaRecommendationSection from "@/components/ui/CafeteriaRecommendationSection.vue";
import RestaurantCardList from "@/components/ui/RestaurantCardList.vue";
import { useCafeteriaRecommendation } from "@/composables/useCafeteriaRecommendation";
import TrendingRecommendationSection from "@/components/ui/TrendingRecommendationSection.vue";
import { useTrendingRestaurants } from "@/composables/useTrendingRestaurants";

const DEFAULT_USER_ID = 2;

// State management (React's useState -> Vue's ref)
const isFilterOpen = ref(false);
const selectedSort = ref("추천순");
const selectedPriceRange = ref(null);
const selectedRecommendation = ref(null);
const {
  cafeteriaRecommendations,
  cafeteriaOcrResult,
  cafeteriaOcrError,
  cafeteriaDaysDraft,
  cafeteriaImageUrl: cafeteriaImageUrlRef,
  isCafeteriaOcrLoading,
  isCafeteriaModalOpen,
  hasConfirmedMenus,
  isCheckingMenus,
  loadRecommendationsFromStorage,
  clearCafeteriaRecommendations,
  handleCafeteriaFileChange,
  handleCafeteriaOcr,
  handleCafeteriaConfirm,
  checkCafeteriaMenuStatus,
  openCafeteriaModal,
  openCafeteriaModalWithExisting,
  requestCafeteriaRecommendations,
} = useCafeteriaRecommendation({ userId: DEFAULT_USER_ID });
const cafeteriaImageUrl = computed(() => cafeteriaImageUrlRef.value);
const homeListStateStorageKey = "homeListState";
const filterForm = reactive({
  sort: selectedSort.value,
  priceRange: selectedPriceRange.value,
  recommendation: null,
});

const isSearchOpen = ref(false);
const searchDate = ref("");
const searchTime = ref("");
const searchCategories = ref([]);
const searchPartySize = ref(4);
const searchTags = ref([]);
const avoidIngredients = ref([]);
const searchDistance = ref("");
const budget = ref(500000);
const {
  isTrendingLoading,
  trendingRestaurants,
  trendingError,
  fetchTrendingRestaurants,
  clearTrendingRestaurants,
} = useTrendingRestaurants();
const isGeocodeExportMode = ref(false);
const isGeocodeExporting = ref(false);
const geocodeExportProgress = ref({ done: 0, total: 0 });
const geocodeExportMissing = ref([]);

const selectedDistanceKm = computed(() => {
  if (!searchDistance.value) return null;
  const kmMatch = searchDistance.value.match(/([\d.]+)\s*km/);
  if (kmMatch) return Number(kmMatch[1]);
  return null;
});

const restaurants = restaurantData;
const restaurantsPerPage = 10;
const currentPage = ref(1);
const isTrendingSort = computed(() => selectedRecommendation.value === "인기순");
const restaurantIndexById = new Map(
  restaurants.map((restaurant) => [String(restaurant.id), restaurant])
);
const processedRestaurants = computed(() => {
  let result = restaurants.slice();

  const distanceLimit = selectedDistanceKm.value;
  if (distanceLimit) {
    result = result.filter((restaurant) =>
      isWithinDistance(restaurant.coords, distanceLimit)
    );
  }

  const activeRange = selectedPriceRange.value;
  if (activeRange && activeRange !== "전체") {
    const range = priceRangeMap[activeRange];
    if (range) {
      result = result.filter((restaurant) => {
        const priceValue = extractPriceValue(restaurant.price);
        if (priceValue == null) return false;
        return priceValue >= range.min && priceValue <= range.max;
      });
    }
  }

  const center = defaultMapCenter;
  const getDistance = (restaurant) =>
    haversineDistance(restaurant.coords, center);

  const sorters = {
    추천순: (a, b) => {
      const ratingDiff = (b.rating ?? 0) - (a.rating ?? 0);
      if (ratingDiff !== 0) return ratingDiff;
      return (b.reviews ?? 0) - (a.reviews ?? 0);
    },
    거리순: (a, b) => getDistance(a) - getDistance(b),
    평점순: (a, b) => (b.rating ?? 0) - (a.rating ?? 0),
    가격순: (a, b) => {
      const priceA = extractPriceValue(a.price) ?? Number.POSITIVE_INFINITY;
      const priceB = extractPriceValue(b.price) ?? Number.POSITIVE_INFINITY;
      return priceA - priceB;
    },
  };

  const sorter = sorters[selectedSort.value] || sorters.추천순;
  result.sort(sorter);

  return result;
});
const trendingCards = computed(() => {
  return trendingRestaurants.value.map((restaurant) => {
    const fallback = restaurantIndexById.get(String(restaurant.restaurantId));
    const addressParts = [restaurant.roadAddress, restaurant.detailAddress]
      .filter(Boolean)
      .join(" ");
    const apiTags = Array.isArray(restaurant.tags)
      ? restaurant.tags.map((tag) => ({ name: tag.content }))
      : [];
    return {
      id: String(restaurant.restaurantId),
      name: restaurant.name || fallback?.name || "",
      address: addressParts || fallback?.address || "",
      image: restaurant.imageUrl || fallback?.image || "/placeholder.svg",
      rating: restaurant.rating ?? fallback?.rating ?? null,
      reviews: restaurant.reviewCount ?? restaurant.reviews ?? fallback?.reviews ?? 0,
      price: restaurant.price ?? fallback?.price ?? "가격 정보 없음",
      reviewTags: restaurant.reviewTags || [],
      restaurantTags: apiTags.length ? apiTags : fallback?.topTags || [],
    };
  });
});
const trendingRestaurantIdSet = computed(
  () =>
    new Set(
      trendingRestaurants.value.map((restaurant) =>
        String(restaurant.restaurantId)
      )
    )
);
const availableRestaurants = computed(() => {
  if (!isTrendingSort.value) {
    return processedRestaurants.value;
  }
  return processedRestaurants.value.filter(
    (restaurant) => !trendingRestaurantIdSet.value.has(String(restaurant.id))
  );
});
const displayRestaurants = computed(() => availableRestaurants.value);
const totalPages = computed(() =>
  Math.max(1, Math.ceil(displayRestaurants.value.length / restaurantsPerPage))
);
const paginatedRestaurants = computed(() => {
  const start = (currentPage.value - 1) * restaurantsPerPage;
  return displayRestaurants.value.slice(start, start + restaurantsPerPage);
});
const pageNumbers = computed(() =>
  Array.from({ length: totalPages.value }, (_, index) => index + 1)
);
const canGoPrevious = computed(() => currentPage.value > 1);
const canGoNext = computed(() => currentPage.value < totalPages.value);
const restaurantGeocodeCache = new Map();

const mapContainer = ref(null);
const mapInstance = ref(null);
const kakaoMapsApi = ref(null);
const isMapReady = ref(false);
const mapMarkers = [];
const defaultMapCenter = restaurants[0]?.coords || {
  lat: 37.394374,
  lng: 127.110636,
};
const currentLocation = ref("경기도 성남시 분당구 판교동");
const mapDistanceSteps = Object.freeze([
  { label: "100m", level: 2 },
  { label: "250m", level: 3 },
  { label: "500m", level: 4 },
  { label: "1km", level: 5 },
  { label: "2km", level: 6 },
  { label: "3km", level: 7 },
]);
const defaultMapDistanceIndex = mapDistanceSteps.findIndex(
  (step) => step.label === "500m"
);
const mapDistanceStepIndex = ref(
  defaultMapDistanceIndex === -1 ? 0 : defaultMapDistanceIndex
);
const currentDistanceLabel = computed(
  () => mapDistanceSteps[mapDistanceStepIndex.value].label
);
const distanceSliderFill = computed(() => {
  if (mapDistanceSteps.length <= 1) return 0;
  return (
    ((mapDistanceSteps.length - 1 - mapDistanceStepIndex.value) /
      (mapDistanceSteps.length - 1)) *
    100
  );
});

const isCalendarOpen = ref(false);
const calendarMonth = ref(new Date());
const weekdayLabels = ["일", "월", "화", "수", "목", "금", "토"];
const selectedMapRestaurant = ref(null);
const favoriteRestaurantIds = ref([]);

const perPersonBudget = computed(() => {
  if (budget.value <= 0 || !searchPartySize.value) {
    return 0;
  }
  return Math.floor(budget.value / searchPartySize.value);
});

const perPersonBudgetDisplay = computed(() => {
  if (!perPersonBudget.value) {
    return "0원";
  }
  return `${perPersonBudget.value.toLocaleString()}원`;
});

const formattedSearchDate = computed(() => {
  if (!searchDate.value) {
    return "날짜를 선택하세요";
  }
  const date = new Date(searchDate.value);
  if (Number.isNaN(date.getTime())) {
    return "날짜를 선택하세요";
  }
  return date.toLocaleDateString("ko-KR", {
    month: "long",
    day: "numeric",
    weekday: "short",
  });
});

const formattedCalendarMonth = computed(() => {
  return calendarMonth.value.toLocaleDateString("ko-KR", {
    year: "numeric",
    month: "long",
  });
});

const calendarDays = computed(() => {
  const year = calendarMonth.value.getFullYear();
  const month = calendarMonth.value.getMonth();
  const firstDay = new Date(year, month, 1).getDay();
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const totalCells = Math.ceil((firstDay + daysInMonth) / 7) * 7;
  const days = [];

  for (let i = 0; i < firstDay; i++) {
    days.push(null);
  }

  for (let day = 1; day <= daysInMonth; day++) {
    days.push(day);
  }

  while (days.length < totalCells) {
    days.push(null);
  }

  return days;
});

const formatDateValue = (date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
};

const toggleCalendar = () => {
  if (!isCalendarOpen.value) {
    calendarMonth.value = searchDate.value
      ? new Date(searchDate.value)
      : new Date();
  }
  isCalendarOpen.value = !isCalendarOpen.value;
};

const selectCalendarDay = (day) => {
  if (!day) return;

  const selected = new Date(
    calendarMonth.value.getFullYear(),
    calendarMonth.value.getMonth(),
    day
  );
  searchDate.value = formatDateValue(selected);
  isCalendarOpen.value = false;
};

const previousCalendarMonth = () => {
  calendarMonth.value = new Date(
    calendarMonth.value.getFullYear(),
    calendarMonth.value.getMonth() - 1,
    1
  );
};

const nextCalendarMonth = () => {
  calendarMonth.value = new Date(
    calendarMonth.value.getFullYear(),
    calendarMonth.value.getMonth() + 1,
    1
  );
};

const isCalendarSelectedDay = (day) => {
  if (!day || !searchDate.value) return false;
  const selectedDate = new Date(searchDate.value);
  return (
    selectedDate.getFullYear() === calendarMonth.value.getFullYear() &&
    selectedDate.getMonth() === calendarMonth.value.getMonth() &&
    selectedDate.getDate() === day
  );
};

watch(searchDate, (value) => {
  if (!value) return;
  const parsed = new Date(value);
  if (!Number.isNaN(parsed.getTime())) {
    calendarMonth.value = new Date(parsed.getFullYear(), parsed.getMonth(), 1);
  }
});

watch(totalPages, (newTotal) => {
  if (currentPage.value > newTotal) {
    currentPage.value = newTotal;
  }
});

watch(selectedSort, () => {
  currentPage.value = 1;
});

watch(selectedPriceRange, () => {
  currentPage.value = 1;
});

const goToPage = (page) => {
  if (page < 1 || page > totalPages.value) return;
  currentPage.value = page;
  if (typeof window !== "undefined") {
    window.scrollTo({ top: 0, behavior: "smooth" });
  }
};

const goToPreviousPage = () => {
  if (!canGoPrevious.value) return;
  goToPage(currentPage.value - 1);
};

const goToNextPage = () => {
  if (!canGoNext.value) return;
  goToPage(currentPage.value + 1);
};

const resolveRestaurantCoords = async (restaurant) => {
  if (restaurant.coords?.lat && restaurant.coords?.lng) {
    return restaurant.coords;
  }

  const cacheKey = restaurant.address?.trim() || restaurant.name;
  if (!cacheKey) {
    return null;
  }

  if (restaurantGeocodeCache.has(cacheKey)) {
    return restaurantGeocodeCache.get(cacheKey);
  }

  const rawAddress = restaurant.address?.trim();
  const candidates = [];
  if (rawAddress) {
    candidates.push(rawAddress);
    const withoutParens = rawAddress.split(" (")[0].trim();
    if (withoutParens && withoutParens !== rawAddress) {
      candidates.push(withoutParens);
    }
    const withoutSemicolon = rawAddress.split(";")[0].trim();
    if (withoutSemicolon && !candidates.includes(withoutSemicolon)) {
      candidates.push(withoutSemicolon);
    }
  }

  try {
    for (const candidate of candidates) {
      try {
        const coords = await geocodeAddress(candidate);
        restaurantGeocodeCache.set(cacheKey, coords);
        restaurant.coords = coords;
        return coords;
      } catch (candidateError) {
        continue;
      }
    }
    throw new Error("geocode-failed");
  } catch (error) {
    console.error("주소 지오코딩 실패:", cacheKey, error);
    return null;
  }
};

const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

const downloadJsonFile = (data, filename) => {
  if (typeof window === "undefined") return;
  const blob = new Blob([JSON.stringify(data, null, 2)], {
    type: "application/json",
  });
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement("a");
  anchor.href = url;
  anchor.download = filename;
  anchor.click();
  URL.revokeObjectURL(url);
};

const startGeocodeExport = async () => {
  if (isGeocodeExporting.value) return;
  isGeocodeExporting.value = true;
  geocodeExportMissing.value = [];
  geocodeExportProgress.value = {
    done: 0,
    total: restaurants.length,
  };

  for (const restaurant of restaurants) {
    try {
      const coords = await resolveRestaurantCoords(restaurant);
      if (!coords) {
        geocodeExportMissing.value.push({
          id: restaurant.id,
          name: restaurant.name,
          address: restaurant.address,
        });
      }
    } catch (error) {
      geocodeExportMissing.value.push({
        id: restaurant.id,
        name: restaurant.name,
        address: restaurant.address,
      });
    } finally {
      geocodeExportProgress.value.done += 1;
      await delay(200);
    }
  }

  const exportPayload = restaurants.map((restaurant) => ({
    id: restaurant.id,
    name: restaurant.name,
    address: restaurant.address,
    coords: restaurant.coords ?? null,
  }));

  downloadJsonFile(exportPayload, "restaurant_coords.json");
  if (geocodeExportMissing.value.length) {
    console.warn("좌표 변환 실패 목록:", geocodeExportMissing.value);
  }
  isGeocodeExporting.value = false;
};

const renderMapMarkers = async (kakaoMaps) => {
  if (!mapInstance.value) return;

  mapMarkers.forEach((marker) => marker.setMap(null));
  mapMarkers.length = 0;

  const distanceLimit = selectedDistanceKm.value;

  for (const restaurant of restaurants) {
    const coords = await resolveRestaurantCoords(restaurant);
    if (!isValidCoords(coords)) continue;
    if (distanceLimit && !isWithinDistance(coords, distanceLimit)) {
      continue;
    }

    const marker = new kakaoMaps.Marker({
      position: new kakaoMaps.LatLng(coords.lat, coords.lng),
      title: restaurant.name,
    });

    try {
      marker.setMap(mapInstance.value);
      kakaoMaps.event.addListener(marker, "click", () => {
        selectedMapRestaurant.value = restaurant;
      });
      mapMarkers.push(marker);
    } catch (error) {
      console.error("지도 마커 표시 실패:", restaurant?.name, error);
    }
  }
};

const levelForDistance = (stepIndex) => {
  const step = mapDistanceSteps[stepIndex] ?? mapDistanceSteps[0];
  return step.level;
};

let mapZoomRafId = 0;
let mapZoomRetryId = 0;
let mapZoomAttempts = 0;
let pendingMapZoomLevel = null;
const maxMapZoomAttempts = 3;
const scheduleMapZoom = (force = false) => {
  if (mapZoomRafId) {
    cancelAnimationFrame(mapZoomRafId);
  }
  if (mapZoomRetryId) {
    clearTimeout(mapZoomRetryId);
    mapZoomRetryId = 0;
  }
  mapZoomAttempts = 0;
  mapZoomRafId = requestAnimationFrame(() => {
    mapZoomRafId = 0;
    if (!mapInstance.value) return;
    if (!force && !isMapReady.value) return;
    if (isSearchOpen.value) return;
    if (!mapContainer.value?.offsetWidth || !mapContainer.value?.offsetHeight) {
      return;
    }
    if (!mapContainer.value?.isConnected || !mapContainer.value?.offsetParent) {
      return;
    }
    const targetLevel =
      pendingMapZoomLevel ?? levelForDistance(mapDistanceStepIndex.value);
    const attemptZoom = () => {
      if (!mapInstance.value) return;
      try {
        mapInstance.value.relayout?.();
        if (typeof mapInstance.value.getBounds === "function") {
          const bounds = mapInstance.value.getBounds();
          if (!bounds) throw new Error("map-bounds-unavailable");
        }
        if (typeof mapInstance.value.getLevel === "function") {
          const currentLevel = mapInstance.value.getLevel();
          if (currentLevel === targetLevel) {
            pendingMapZoomLevel = null;
            return;
          }
        }
        mapInstance.value.setLevel(targetLevel);
        pendingMapZoomLevel = null;
      } catch (error) {
        mapZoomAttempts += 1;
        if (mapZoomAttempts < maxMapZoomAttempts) {
          mapZoomRetryId = setTimeout(attemptZoom, 120);
        }
      }
    };
    attemptZoom();
  });
};

const applyHomeMapZoom = (force = false) => {
  pendingMapZoomLevel = levelForDistance(mapDistanceStepIndex.value);
  scheduleMapZoom(force);
};

const changeMapDistance = (delta) => {
  const next = Math.min(
    mapDistanceSteps.length - 1,
    Math.max(0, mapDistanceStepIndex.value + delta)
  );
  mapDistanceStepIndex.value = next;
};

const resetMapToHome = () => {
  if (!mapInstance.value) return;
  const kakaoMaps = window?.kakao?.maps;
  if (!kakaoMaps?.LatLng) return;

  const targetCenter = new kakaoMaps.LatLng(
    defaultMapCenter.lat,
    defaultMapCenter.lng
  );
  mapInstance.value.panTo(targetCenter);
  mapDistanceStepIndex.value =
    defaultMapDistanceIndex === -1 ? 0 : defaultMapDistanceIndex;
  applyHomeMapZoom();
};

const initializeMap = async () => {
  if (!mapContainer.value) return;
  try {
    const kakaoMaps = await loadKakaoMaps();
    kakaoMapsApi.value = kakaoMaps;
    const center = new kakaoMaps.LatLng(
      defaultMapCenter.lat,
      defaultMapCenter.lng
    );
    mapInstance.value = new kakaoMaps.Map(mapContainer.value, {
      center,
      level: levelForDistance(mapDistanceStepIndex.value),
    });
    kakaoMaps.event.addListener(mapInstance.value, "idle", () => {
      if (pendingMapZoomLevel != null) {
        scheduleMapZoom(true);
      }
    });

    await renderMapMarkers(kakaoMaps);
    applyHomeMapZoom(true);
    isMapReady.value = true;
  } catch (error) {
    console.error("카카오 지도 초기화에 실패했습니다.", error);
  }
};

onMounted(() => {
  initializeMap();
  if (typeof window !== "undefined") {
    const params = new URLSearchParams(window.location.search);
    if (params.get("geocode") === "1") {
      isGeocodeExportMode.value = true;
      startGeocodeExport();
    }
  }
});

onBeforeUnmount(() => {
  mapMarkers.forEach((marker) => marker.setMap(null));
  mapMarkers.length = 0;
  mapInstance.value = null;
  isMapReady.value = false;
  if (mapRenderRafId) {
    cancelAnimationFrame(mapRenderRafId);
    mapRenderRafId = 0;
  }
  if (mapZoomRafId) {
    cancelAnimationFrame(mapZoomRafId);
    mapZoomRafId = 0;
  }
  if (mapZoomRetryId) {
    clearTimeout(mapZoomRetryId);
    mapZoomRetryId = 0;
  }
});

watch(mapDistanceStepIndex, () => {
  applyHomeMapZoom();
});

watch(isSearchOpen, (isOpen) => {
  if (!isOpen) {
    nextTick(() => {
      setTimeout(() => {
        applyHomeMapZoom(true);
      }, 120);
    });
  }
});

// Static data (constants)
const categories = ["한식", "중식", "일식", "양식"];
const timeSlots = ["11:00", "12:00", "13:00", "14:00"];
const priceRanges = [
  "전체",
  "1만원 이하",
  "1만원~1.5만원",
  "1.5만원~2만원",
  "2만원~3만원",
  "3만원 이상",
];
const priceRangeMap = Object.freeze({
  전체: null,
  "1만원 이하": { min: 0, max: 10000 },
  "1만원~1.5만원": { min: 10000, max: 15000 },
  "1.5만원~2만원": { min: 15000, max: 20000 },
  "2만원~3만원": { min: 20000, max: 30000 },
  "3만원 이상": { min: 30000, max: Number.POSITIVE_INFINITY },
});
const recommendationOptions = [
  "구내식당 대체 추천",
  "예산 맞춤",
  "취향 맞춤",
  "인기순",
  "날씨 추천",
];
const distances = ["1km 이내", "2km 이내", "3km 이내"];
const sortOptions = ["추천순", "거리순", "평점순", "낮은 가격순"];
const restaurantTags = [
  "조용한",
  "깔끔한",
  "셀프바",
  "주차장 제공",
  "이국적/이색적",
  "칸막이",
  "룸",
  "단체",
];
const ingredients = [
  "견과류",
  "우유",
  "계란",
  "밀",
  "대두",
  "갑각류",
  "조개류",
  "생선",
  "메밀",
  "고수",
  "오이",
  "파프리카",
  "미나리",
  "당근",
];

const extractPriceValue = (priceText = "") => {
  const match = priceText.match(/([\d.,]+)\s*원/);
  const target = match?.[1] ?? priceText;
  const digits = target.replace(/[^0-9]/g, "");
  if (!digits) return null;
  return Number(digits);
};

const haversineDistance = (coordsA = {}, coordsB = {}) => {
  if (!coordsA.lat || !coordsA.lng || !coordsB.lat || !coordsB.lng) {
    return Number.POSITIVE_INFINITY;
  }
  const toRad = (value) => (value * Math.PI) / 180;
  const earthRadius = 6371; // km
  const dLat = toRad(coordsB.lat - coordsA.lat);
  const dLng = toRad(coordsB.lng - coordsA.lng);
  const lat1 = toRad(coordsA.lat);
  const lat2 = toRad(coordsB.lat);

  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.sin(dLng / 2) * Math.sin(dLng / 2) * Math.cos(lat1) * Math.cos(lat2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return earthRadius * c;
};

const isValidCoords = (coords) =>
  Number.isFinite(coords?.lat) && Number.isFinite(coords?.lng);

const isWithinDistance = (coords, limitKm) => {
  if (!limitKm) return true;
  if (!isValidCoords(coords)) return false;
  return haversineDistance(coords, defaultMapCenter) <= limitKm;
};

let mapRenderRafId = 0;
const scheduleMapMarkerRender = () => {
  if (mapRenderRafId) {
    cancelAnimationFrame(mapRenderRafId);
  }
  mapRenderRafId = requestAnimationFrame(() => {
    mapRenderRafId = 0;
    if (!isMapReady.value || !kakaoMapsApi.value || !mapInstance.value) {
      return;
    }
    if (!mapContainer.value?.offsetWidth || !mapContainer.value?.offsetHeight) {
      return;
    }
    renderMapMarkers(kakaoMapsApi.value);
  });
};

watch(selectedDistanceKm, (distanceLimit) => {
  if (distanceLimit) {
    const label = `${distanceLimit}km`;
    const stepIndex = mapDistanceSteps.findIndex(
      (step) => step.label === label
    );
    if (stepIndex !== -1) {
      mapDistanceStepIndex.value = stepIndex;
    }
  }

  scheduleMapMarkerRender();
});

// Event handlers
const openFilterModal = () => {
  filterForm.sort = selectedSort.value;
  filterForm.priceRange = selectedPriceRange.value;
  filterForm.recommendation = selectedRecommendation.value;
  isFilterOpen.value = true;
};

const toggleFilterPriceRange = (range) => {
  if (filterForm.priceRange === range) {
    filterForm.priceRange = null;
    return;
  }
  filterForm.priceRange = range;
};

const isRestaurantFavorite = (restaurantId) => {
  return favoriteRestaurantIds.value.includes(restaurantId);
};

const toggleRestaurantFavorite = (restaurantId) => {
  const index = favoriteRestaurantIds.value.indexOf(restaurantId);
  if (index > -1) {
    favoriteRestaurantIds.value.splice(index, 1);
  } else {
    favoriteRestaurantIds.value.push(restaurantId);
  }
};

const resetFilters = () => {
  filterForm.sort = "추천순";
  filterForm.priceRange = null;
  filterForm.recommendation = null;
};

const applyFilters = () => {
  selectedSort.value = filterForm.sort || "추천순";
  selectedPriceRange.value = filterForm.priceRange || null;
  selectedRecommendation.value = filterForm.recommendation || null;
  currentPage.value = 1;
  isFilterOpen.value = false;
  persistHomeListState();
};

const clearTrendingRecommendation = () => {
  selectedRecommendation.value = null;
  filterForm.recommendation = null;
  clearTrendingRestaurants();
  currentPage.value = 1;
  persistHomeListState();
};

const toggleRecommendationOption = (option) => {
  if (option === "구내식당 대체 추천") {
    filterForm.recommendation = option;
    const baseDate = resolveCafeteriaBaseDate();
    checkCafeteriaMenuStatus(baseDate);
    return;
  }

  filterForm.recommendation =
    filterForm.recommendation === option ? null : option;
};

const handleCafeteriaMenuEdit = () => {
  if (hasConfirmedMenus.value) {
    openCafeteriaModalWithExisting(resolveCafeteriaBaseDate());
    return;
  }
  openCafeteriaModal();
};

const handleCafeteriaRecommendNow = async () => {
  await requestCafeteriaRecommendations(resolveCafeteriaBaseDate());
  isFilterOpen.value = false;
};

const handleCafeteriaConfirmAndClose = async (days) => {
  const saved = await handleCafeteriaConfirm(
    days,
    resolveCafeteriaBaseDate()
  );
  if (saved) {
    isFilterOpen.value = false;
  }
};

const toggleSearchCategory = (category) => {
  if (searchCategories.value.includes(category)) {
    searchCategories.value = [];
  } else {
    searchCategories.value = [category];
  }
};

const toggleSearchTag = (tag) => {
  const index = searchTags.value.indexOf(tag);
  if (index > -1) {
    searchTags.value.splice(index, 1);
  } else {
    searchTags.value.push(tag);
  }
};

const toggleAvoidIngredient = (ingredient) => {
  const index = avoidIngredients.value.indexOf(ingredient);
  if (index > -1) {
    avoidIngredients.value.splice(index, 1);
  } else {
    avoidIngredients.value.push(ingredient);
  }
};

const resetSearch = () => {
  searchDate.value = "";
  searchTime.value = "";
  searchCategories.value = [];
  searchPartySize.value = 4;
  searchTags.value = [];
  avoidIngredients.value = [];
  searchDistance.value = "";
  budget.value = 500000;
  isCalendarOpen.value = false;
  calendarMonth.value = new Date();
};

const applySearch = () => {
  isSearchOpen.value = false;
  isCalendarOpen.value = false;
  // 검색 적용 로직 (Vue version)
};

const closeMapRestaurantModal = () => {
  selectedMapRestaurant.value = null;
};

const resolveCafeteriaBaseDate = () => {
  if (searchDate.value) {
    return searchDate.value;
  }
  return new Date().toISOString().slice(0, 10);
};

onMounted(() => {
  loadRecommendationsFromStorage();

  const storedHomeState = sessionStorage.getItem(homeListStateStorageKey);
  if (storedHomeState) {
    try {
      const parsed = JSON.parse(storedHomeState);
      selectedSort.value = parsed.selectedSort ?? selectedSort.value;
      selectedPriceRange.value =
        parsed.selectedPriceRange ?? selectedPriceRange.value;
      selectedRecommendation.value =
        parsed.selectedRecommendation ?? selectedRecommendation.value;
      currentPage.value = parsed.currentPage ?? currentPage.value;
      nextTick(() => {
        if (Number.isFinite(parsed.scrollY)) {
          window.scrollTo(0, parsed.scrollY);
        }
      });
    } catch (error) {
      console.error("홈 리스트 상태 복원 실패:", error);
      sessionStorage.removeItem(homeListStateStorageKey);
    }
  }
});

watch(isTrendingSort, (isActive) => {
  if (isActive) {
    fetchTrendingRestaurants({ days: 7, limit: restaurantsPerPage });
    return;
  }
  clearTrendingRestaurants();
});

const persistHomeListState = () => {
  sessionStorage.setItem(
    homeListStateStorageKey,
    JSON.stringify({
      selectedSort: selectedSort.value,
      selectedPriceRange: selectedPriceRange.value,
      selectedRecommendation: selectedRecommendation.value,
      currentPage: currentPage.value,
      scrollY: window.scrollY,
    })
  );
};

watch([selectedSort, selectedPriceRange, selectedRecommendation], () => {
  persistHomeListState();
});

watch(currentPage, () => {
  persistHomeListState();
});

onBeforeUnmount(() => {
  persistHomeListState();
});
</script>

<template>
  <div class="min-h-screen bg-[#f8f9fa]">
    <AppHeader />
    <div
      v-if="isGeocodeExportMode"
      class="bg-white border-b border-[#e9ecef]"
    >
      <div class="max-w-[500px] mx-auto px-4 py-3 text-xs text-[#495057]">
        <p class="font-semibold text-[#1e3a5f]">주소 좌표 생성 모드</p>
        <p v-if="isGeocodeExporting">
          진행 중: {{ geocodeExportProgress.done }} /
          {{ geocodeExportProgress.total }}
        </p>
        <p v-else>완료되었습니다. 다운로드된 파일을 확인하세요.</p>
        <p v-if="geocodeExportMissing.length">
          실패 {{ geocodeExportMissing.length }}건 (콘솔 확인)
        </p>
      </div>
    </div>

    <main class="max-w-[500px] mx-auto pb-20">
      <div class="bg-white px-4 py-4 border-b border-[#e9ecef]">
        <div class="flex items-center gap-2">
          <MapPin class="w-5 h-5 text-[#ff6b4a]" />
          <div>
            <h2 class="text-base font-semibold text-[#1e3a5f]">
              {{ currentLocation }}
            </h2>
            <p class="text-xs text-[#6c757d]">현재 위치 기준</p>
          </div>
        </div>
      </div>

      <div class="relative h-64">
        <div ref="mapContainer" class="w-full h-full" />
        <div
          class="absolute inset-0 pointer-events-none bg-gradient-to-t from-black/20 via-transparent to-transparent"
        />
        <div
          class="absolute top-4 right-4 z-10 pointer-events-auto flex flex-col items-center gap-2"
        >
          <button
            @click="changeMapDistance(-1)"
            class="w-8 h-8 rounded-sm bg-white shadow-card flex items-center justify-center text-[#1e3a5f] hover:bg-[#f8f9fa]"
          >
            <Plus class="w-4 h-4" />
          </button>
          <div
            class="h-20 w-[6px] bg-white/80 rounded relative shadow-card overflow-hidden"
          >
            <div
              class="absolute top-0 left-0 right-0 bg-[#ff6b4a] transition-all"
              :style="{ height: `${distanceSliderFill}%` }"
            ></div>
          </div>
          <button
            @click="changeMapDistance(1)"
            class="w-8 h-8 rounded-sm bg-white shadow-card flex items-center justify-center text-[#1e3a5f] hover:bg-[#f8f9fa]"
          >
            <Minus class="w-4 h-4" />
          </button>
          <div
            class="mt-2 px-3 py-1 rounded-full bg-white text-xs font-semibold text-[#1e3a5f] shadow-card"
          >
            반경 {{ currentDistanceLabel }}
          </div>
        </div>
        <div
          class="absolute bottom-4 left-4 bg-white/90 backdrop-blur px-4 py-2 rounded-full shadow-card flex items-center gap-2 text-sm text-[#1e3a5f]"
        >
          <MapPin class="w-4 h-4 text-[#ff6b4a]" />
          <span>{{ currentLocation }} · {{ currentDistanceLabel }} 반경</span>
        </div>
      </div>

      <div class="px-4 py-5">
        <CafeteriaRecommendationSection
          :recommendations="cafeteriaRecommendations"
          :favoriteRestaurantIds="favoriteRestaurantIds"
          :onToggleFavorite="toggleRestaurantFavorite"
          :onOpenSearch="() => (isSearchOpen = true)"
          :onClearRecommendations="clearCafeteriaRecommendations"
          :isModalOpen="isCafeteriaModalOpen"
          :isProcessing="isCafeteriaOcrLoading"
          :ocrResult="cafeteriaOcrResult"
          :days="cafeteriaDaysDraft"
          :errorMessage="cafeteriaOcrError"
          :initialImageUrl="cafeteriaImageUrl"
          :onModalClose="() => (isCafeteriaModalOpen = false)"
          :onFileChange="handleCafeteriaFileChange"
          :onRunOcr="() => handleCafeteriaOcr(resolveCafeteriaBaseDate())"
          :onConfirm="handleCafeteriaConfirmAndClose"
        />

        <div
          v-if="!cafeteriaRecommendations.length"
          class="flex items-center gap-2 mb-4"
        >
          <button
            @click="openFilterModal"
            class="flex items-center gap-1.5 text-sm text-[#6c757d] hover:text-[#ff6b4a] transition-colors"
          >
            <SlidersHorizontal class="w-4 h-4" />
            <span>필터</span>
          </button>
        </div>

        <div v-if="!cafeteriaRecommendations.length" class="space-y-6">
          <TrendingRecommendationSection
            :isActive="isTrendingSort"
            :isLoading="isTrendingLoading"
            :error="trendingError"
            :cards="trendingCards"
            :favoriteRestaurantIds="favoriteRestaurantIds"
            :onToggleFavorite="toggleRestaurantFavorite"
            :onClear="clearTrendingRecommendation"
          />

          <RestaurantCardList
            :restaurants="paginatedRestaurants"
            :favoriteRestaurantIds="favoriteRestaurantIds"
            :onToggleFavorite="toggleRestaurantFavorite"
          />
        </div>

        <div
          v-if="!cafeteriaRecommendations.length && totalPages > 1"
          class="mt-6"
        >
          <nav
            class="flex flex-wrap items-center justify-center gap-2 text-sm"
            aria-label="페이지네이션"
          >
            <button
              type="button"
              class="min-w-[56px] h-9 px-4 rounded-2xl border border-[#e9ecef] bg-white text-[#495057] font-medium transition-colors disabled:text-[#c7cdd3] disabled:border-[#f1f3f5] disabled:cursor-not-allowed"
              :disabled="!canGoPrevious"
              @click="goToPreviousPage"
            >
              이전
            </button>

            <button
              v-for="page in pageNumbers"
              :key="page"
              type="button"
              @click="goToPage(page)"
              :aria-current="page === currentPage ? 'page' : undefined"
              :class="[
                'min-w-[36px] h-9 px-3 rounded-2xl border font-medium transition-colors',
                page === currentPage
                  ? 'bg-gradient-to-r from-[#ff6b4a] via-[#ff805f] to-[#ff987d] text-white border-transparent shadow-button-hover'
                  : 'bg-white text-[#495057] border-[#e9ecef] hover:text-[#ff6b4a] hover:border-[#ff6b4a]',
              ]"
            >
              {{ page }}
            </button>

            <button
              type="button"
              class="min-w-[56px] h-9 px-4 rounded-2xl border border-[#e9ecef] bg-white text-[#495057] font-medium transition-colors disabled:text-[#c7cdd3] disabled:border-[#f1f3f5] disabled:cursor-not-allowed"
              :disabled="!canGoNext"
              @click="goToNextPage"
            >
              다음
            </button>
          </nav>
        </div>
      </div>

      <div
        v-if="selectedMapRestaurant"
        class="fixed bottom-20 left-0 right-0 z-[60] px-4"
      >
        <div class="max-w-[500px] mx-auto">
          <RouterLink
            :to="`/restaurant/${selectedMapRestaurant.id}`"
            class="block bg-white border border-[#e9ecef] shadow-card rounded-2xl p-4"
            @click="closeMapRestaurantModal"
          >
            <div class="flex gap-3">
              <img
                :src="selectedMapRestaurant.image || '/placeholder.svg'"
                :alt="selectedMapRestaurant.name"
                class="w-20 h-20 rounded-xl object-cover flex-shrink-0"
              />
              <div class="flex-1 min-w-0">
                <div class="flex items-start justify-between gap-2 mb-1">
                  <div>
                    <p class="text-sm font-semibold text-[#1e3a5f]">
                      {{ selectedMapRestaurant.name }}
                    </p>
                    <p class="text-xs text-[#6c757d] truncate">
                      {{ selectedMapRestaurant.address }}
                    </p>
                  </div>
                  <button
                    @click.stop.prevent="closeMapRestaurantModal"
                    class="text-[#adb5bd] hover:text-[#495057]"
                  >
                    <X class="w-4 h-4" />
                  </button>
                </div>
                <div class="flex items-center gap-2 text-xs text-[#6c757d]">
                  <span
                    class="flex items-center gap-1 text-[#1e3a5f] font-semibold"
                  >
                    <Star class="w-3.5 h-3.5 fill-[#ffc107] text-[#ffc107]" />
                    {{ selectedMapRestaurant.rating }}
                  </span>
                  <span>리뷰 {{ selectedMapRestaurant.reviews }}개</span>
                  <span>{{ selectedMapRestaurant.category }}</span>
                </div>
                <p class="text-sm font-semibold text-[#1e3a5f] mt-2">
                  {{ selectedMapRestaurant.price }}
                </p>
              </div>
            </div>
          </RouterLink>
        </div>
      </div>

      <AppFooter />
    </main>

    <BottomNav @home="resetMapToHome" />

    <!-- Filter Modal -->
    <div
      v-if="isFilterOpen"
      class="fixed inset-0 z-[100] bg-black/50 flex items-end"
    >
      <div
        class="w-full max-w-[500px] mx-auto bg-white rounded-t-2xl max-h-[85vh] overflow-y-auto animate-in slide-in-from-bottom duration-300"
      >
        <div
          class="sticky top-0 bg-white border-b border-[#e9ecef] px-4 py-4 flex items-center justify-between"
        >
          <h3 class="text-lg font-semibold text-[#1e3a5f]">필터 및 정렬</h3>
          <button
            @click="isFilterOpen = false"
            class="text-[#6c757d] hover:text-[#1e3a5f]"
          >
            <X class="w-6 h-6" />
          </button>
        </div>

        <div class="p-4 space-y-6">
          <!-- Sort Options -->
          <div>
            <h4 class="text-sm font-semibold text-[#1e3a5f] mb-3">정렬</h4>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="option in sortOptions"
                :key="option"
                @click="filterForm.sort = option"
                :class="`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                  filterForm.sort === option
                    ? 'gradient-primary text-white'
                    : 'bg-[#f8f9fa] text-[#495057] hover:bg-[#e9ecef]'
                }`"
              >
                {{ option }}
              </button>
            </div>
          </div>

          <!-- Price Range Filter -->
          <div>
            <h4 class="text-sm font-semibold text-[#1e3a5f] mb-3">
              1인당 가격대
            </h4>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="range in priceRanges"
                :key="range"
                @click="toggleFilterPriceRange(range)"
                :class="`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                  filterForm.priceRange === range
                    ? 'gradient-primary text-white'
                    : 'bg-[#f8f9fa] text-[#495057] hover:bg-[#e9ecef]'
                }`"
              >
                {{ range }}
              </button>
            </div>
          </div>

          <!-- TODO: 로그인 사용자만 노출 (백엔드 연동 후 v-if 적용) -->
          <div>
            <h4 class="text-sm font-semibold text-[#1e3a5f] mb-3">추천옵션</h4>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="option in recommendationOptions"
                :key="option"
                type="button"
                @click="toggleRecommendationOption(option)"
                :class="`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                  filterForm.recommendation === option
                    ? 'gradient-primary text-white'
                    : 'bg-[#f8f9fa] text-[#495057] hover:bg-[#e9ecef]'
                }`"
              >
                {{ option }}
              </button>
            </div>
            <div
              v-if="filterForm.recommendation === '구내식당 대체 추천'"
              class="mt-3 flex items-center justify-end gap-2"
            >
              <div class="flex items-center gap-2">
                <button
                  type="button"
                  class="px-3 py-2 rounded-lg text-xs font-semibold border border-[#dee2e6] text-[#495057] bg-white hover:bg-[#f8f9fa] disabled:opacity-60 disabled:cursor-not-allowed"
                  :disabled="isCheckingMenus"
                  @click="handleCafeteriaMenuEdit"
                >
                  {{ hasConfirmedMenus ? '구내식당 메뉴 수정' : '구내식당 메뉴 입력' }}
                </button>
                <button
                  v-if="hasConfirmedMenus"
                  type="button"
                  class="px-3 py-2 rounded-lg text-xs font-semibold gradient-primary text-white disabled:opacity-60 disabled:cursor-not-allowed"
                  :disabled="isCheckingMenus"
                  @click="handleCafeteriaRecommendNow"
                >
                  추천받기
                </button>
              </div>
            </div>
          </div>
        </div>

        <div
          class="sticky bottom-0 bg-white border-t border-[#e9ecef] p-4 flex gap-3"
        >
          <Button
            @click="resetFilters"
            variant="outline"
            class="flex-1 h-12 text-[#495057] border-[#dee2e6] hover:bg-[#f8f9fa] rounded-xl bg-transparent"
          >
            초기화
          </Button>
          <Button
            @click="applyFilters"
            class="flex-1 h-12 gradient-primary text-white rounded-xl"
          >
            적용하기
          </Button>
        </div>
      </div>
    </div>

    <!-- Search Modal -->
    <div
      v-if="isSearchOpen"
      class="fixed inset-0 z-[100] bg-black/50 flex items-end"
    >
      <div
        class="w-full max-w-[500px] mx-auto bg-white rounded-t-2xl max-h-[85vh] overflow-y-auto animate-in slide-in-from-bottom duration-300"
      >
        <div
          class="sticky top-0 bg-white border-b border-[#e9ecef] px-4 py-4 flex items-center justify-between"
        >
          <h3 class="text-lg font-semibold text-[#1e3a5f]">검색 필터</h3>
          <button
            @click="isSearchOpen = false"
            class="text-[#6c757d] hover:text-[#1e3a5f]"
          >
            <X class="w-6 h-6" />
          </button>
        </div>

        <div class="p-4 space-y-6">
          <!-- Reservation Date -->
          <div>
            <h4 class="text-sm font-semibold text-[#1e3a5f] mb-3">예약 날짜</h4>
            <div class="relative">
              <button
                type="button"
                @click="toggleCalendar"
                class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg text-sm flex items-center justify-between text-left text-[#495057] focus:outline-none focus:ring-2 focus:ring-[#ff6b4a] focus:border-transparent"
              >
                <span
                  :class="searchDate ? 'text-[#1e3a5f]' : 'text-[#adb5bd]'"
                  >{{ formattedSearchDate }}</span
                >
                <Calendar class="w-5 h-5 text-[#ff6b4a]" />
              </button>

              <div
                v-if="isCalendarOpen"
                class="absolute right-0 top-full mt-2 w-72 bg-white border border-[#e9ecef] rounded-xl shadow-card z-20"
              >
                <div
                  class="flex items-center justify-between px-4 py-3 border-b border-[#e9ecef]"
                >
                  <button
                    @click.stop="previousCalendarMonth"
                    class="p-1 rounded-full hover:bg-[#f8f9fa] transition-colors"
                  >
                    <ChevronLeft class="w-4 h-4 text-[#495057]" />
                  </button>
                  <span class="text-sm font-semibold text-[#1e3a5f]">{{
                    formattedCalendarMonth
                  }}</span>
                  <button
                    @click.stop="nextCalendarMonth"
                    class="p-1 rounded-full hover:bg-[#f8f9fa] transition-colors"
                  >
                    <ChevronRight class="w-4 h-4 text-[#495057]" />
                  </button>
                </div>
                <div class="px-4 py-3">
                  <div
                    class="grid grid-cols-7 text-center text-xs text-[#6c757d] mb-2"
                  >
                    <span
                      v-for="dayName in weekdayLabels"
                      :key="dayName"
                      class="py-1"
                      >{{ dayName }}</span
                    >
                  </div>
                  <div class="grid grid-cols-7 gap-1">
                    <button
                      v-for="(day, index) in calendarDays"
                      :key="index"
                      :disabled="!day"
                      @click.stop="selectCalendarDay(day)"
                      :class="[
                        'w-9 h-9 rounded-full text-sm transition-colors',
                        !day ? 'cursor-default opacity-0' : '',
                        day && isCalendarSelectedDay(day)
                          ? 'bg-[#ff6b4a] text-white font-semibold'
                          : 'text-[#495057] hover:bg-[#f1f3f5]',
                      ]"
                    >
                      {{ day }}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Time Slot Selection -->
          <div>
            <h4 class="text-sm font-semibold text-[#1e3a5f] mb-3">시간대</h4>
            <div class="grid grid-cols-4 gap-2">
              <button
                v-for="time in timeSlots"
                :key="time"
                @click="searchTime = time"
                :class="`px-4 py-3 rounded-lg text-sm font-medium transition-colors ${
                  searchTime === time
                    ? 'gradient-primary text-white'
                    : 'bg-[#f8f9fa] text-[#495057] hover:bg-[#e9ecef]'
                }`"
              >
                {{ time }}
              </button>
            </div>
          </div>

          <!-- Category -->
          <div>
            <h4 class="text-sm font-semibold text-[#1e3a5f] mb-3">음식 종류</h4>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="category in categories"
                :key="category"
                @click="toggleSearchCategory(category)"
                :class="`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                  searchCategories.includes(category)
                    ? 'gradient-primary text-white'
                    : 'bg-[#f8f9fa] text-[#495057] hover:bg-[#e9ecef]'
                }`"
              >
                {{ category }}
              </button>
            </div>
          </div>

          <!-- Party Size -->
          <div>
            <h4 class="text-sm font-semibold text-[#1e3a5f] mb-3">인원 수</h4>
            <div class="flex items-center gap-4">
              <button
                @click="searchPartySize = Math.max(4, searchPartySize - 1)"
                class="w-10 h-10 rounded-lg border border-[#dee2e6] flex items-center justify-center hover:bg-[#f8f9fa] transition-colors disabled:opacity-50"
                :disabled="searchPartySize <= 4"
              >
                <Minus class="w-4 h-4 text-[#495057]" />
              </button>
              <div class="flex-1 text-center">
                <span class="text-2xl font-semibold text-[#1e3a5f]">{{
                  searchPartySize
                }}</span>
                <span class="text-sm text-[#6c757d] ml-1">명</span>
              </div>
              <button
                @click="searchPartySize = Math.min(12, searchPartySize + 1)"
                class="w-10 h-10 rounded-lg border border-[#dee2e6] flex items-center justify-center hover:bg-[#f8f9fa] transition-colors disabled:opacity-50"
                :disabled="searchPartySize >= 12"
              >
                <Plus class="w-4 h-4 text-[#495057]" />
              </button>
            </div>
          </div>

          <!-- Budget Slider -->
          <div>
            <h4 class="text-sm font-semibold text-[#1e3a5f] mb-3">
              총 회식 가격
            </h4>
            <div class="space-y-3">
              <div class="text-center">
                <span class="text-2xl font-semibold text-[#1e3a5f]">
                  {{
                    budget >= 500000
                      ? "50만원 이상"
                      : `${(budget / 10000).toFixed(0)}만원`
                  }}
                </span>
              </div>
              <input
                type="range"
                min="0"
                max="500000"
                step="10000"
                v-model.number="budget"
                class="w-full h-2 bg-[#e9ecef] rounded-lg appearance-none cursor-pointer [&::-webkit-slider-thumb]:appearance-none [&::-webkit-slider-thumb]:w-5 [&::-webkit-slider-thumb]:h-5 [&::-webkit-slider-thumb]:rounded-full [&::-webkit-slider-thumb]:bg-[#ff6b4a] [&::-webkit-slider-thumb]:cursor-pointer"
              />
              <div class="flex justify-between text-xs text-[#6c757d]">
                <span>0원</span>
                <span>50만원 이상</span>
              </div>
              <div
                v-if="perPersonBudget"
                class="mt-2 p-3 bg-[#f8f9fa] border border-[#e9ecef] rounded-lg text-center text-sm text-[#495057]"
              >
                <p>1인당 예상 금액</p>
                <p class="text-xl font-semibold text-[#1e3a5f]">
                  {{ perPersonBudgetDisplay }}
                </p>
              </div>
            </div>
          </div>

          <!-- Distance Filter -->
          <div>
            <h4 class="text-sm font-semibold text-[#1e3a5f] mb-3">거리</h4>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="distance in distances"
                :key="distance"
                @click="searchDistance = distance"
                :class="`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                  searchDistance === distance
                    ? 'gradient-primary text-white'
                    : 'bg-[#f8f9fa] text-[#495057] hover:bg-[#e9ecef]'
                }`"
              >
                {{ distance }}
              </button>
            </div>
          </div>

          <!-- Restaurant Tags -->
          <div>
            <h4 class="text-sm font-semibold text-[#1e3a5f] mb-3">식당 태그</h4>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="tag in restaurantTags"
                :key="tag"
                @click="toggleSearchTag(tag)"
                :class="`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                  searchTags.includes(tag)
                    ? 'gradient-primary text-white'
                    : 'bg-[#f8f9fa] text-[#495057] hover:bg-[#e9ecef]'
                }`"
              >
                {{ tag }}
              </button>
            </div>
          </div>

          <!-- Avoid Ingredients -->
          <div>
            <h4 class="text-sm font-semibold text-[#1e3a5f] mb-3">기피 재료</h4>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="ingredient in ingredients"
                :key="ingredient"
                @click="toggleAvoidIngredient(ingredient)"
                :class="`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                  avoidIngredients.includes(ingredient)
                    ? 'bg-red-500 text-white'
                    : 'bg-[#f8f9fa] text-[#495057] hover:bg-[#e9ecef]'
                }`"
              >
                {{ ingredient }}
              </button>
            </div>
          </div>
        </div>

        <div
          class="sticky bottom-0 bg-white border-t border-[#e9ecef] p-4 flex gap-3"
        >
          <Button
            @click="resetSearch"
            variant="outline"
            class="flex-1 h-12 text-[#495057] border-[#dee2e6] hover:bg-[#f8f9fa] rounded-xl bg-transparent"
          >
            초기화
          </Button>
          <Button
            @click="applySearch"
            class="flex-1 h-12 gradient-primary text-white rounded-xl"
          >
            검색하기
          </Button>
        </div>
      </div>
    </div>
  </div>
</template>

<style>
.wrap_btn_zoom,
.wrap_scale {
  display: none !important;
}
</style>
