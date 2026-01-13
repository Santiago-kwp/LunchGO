import { ref, computed, watch, nextTick, onBeforeUnmount } from "vue";
import { loadKakaoMaps } from "@/utils/kakao";

export const useHomeMap = ({
  isLoggedIn,
  fetchUserAddress,
  geocodeAddress,
  isSearchOpen,
  selectedDistanceKm,
  resolveRestaurantCoords,
  onMarkerClick,
  restaurants,
}) => {
  const mapContainer = ref(null);
  const mapInstance = ref(null);
  const kakaoMapsApi = ref(null);
  const isMapReady = ref(false);
  const mapMarkers = [];
  const fallbackMapCenter = {
    lat: 37.394374,
    lng: 127.110636,
  };
  const fallbackAddress = "경기도 성남시 분당구 판교역로 235";
  const mapCenter = ref({ ...fallbackMapCenter });
  const currentLocation = ref(fallbackAddress);
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

  const getRestaurants = () =>
    typeof restaurants === "function" ? restaurants() : restaurants || [];

  const haversineDistance = (coordsA = {}, coordsB = {}) => {
    if (!coordsA.lat || !coordsA.lng || !coordsB.lat || !coordsB.lng) {
      return Number.POSITIVE_INFINITY;
    }
    const toRad = (value) => (value * Math.PI) / 180;
    const earthRadius = 6371;
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
    return haversineDistance(coords, mapCenter.value) <= limitKm;
  };

  const renderMapMarkers = async (kakaoMaps) => {
    if (!mapInstance.value) return;

    mapMarkers.forEach((marker) => marker.setMap(null));
    mapMarkers.length = 0;

    const markerSvg =
      "data:image/svg+xml;utf8," +
      "<svg xmlns='http://www.w3.org/2000/svg' width='32' height='46' viewBox='0 0 32 46'>" +
      "<path d='M16 1C8.8 1 3 6.8 3 14c0 9.3 13 30 13 30s13-20.7 13-30C29 6.8 23.2 1 16 1z' fill='%23ff6b4a' stroke='white' stroke-width='2'/>" +
      "<circle cx='16' cy='14' r='5' fill='white'/>" +
      "</svg>";
    const markerImage = new kakaoMaps.MarkerImage(
      markerSvg,
      new kakaoMaps.Size(32, 46),
      { offset: new kakaoMaps.Point(16, 46) }
    );

    const distanceLimit = selectedDistanceKm.value;

    for (const restaurant of getRestaurants()) {
      const coords = await resolveRestaurantCoords(restaurant);
      if (!isValidCoords(coords)) continue;
      if (distanceLimit && !isWithinDistance(coords, distanceLimit)) {
        continue;
      }

      const marker = new kakaoMaps.Marker({
        position: new kakaoMaps.LatLng(coords.lat, coords.lng),
        title: restaurant.name,
        image: markerImage,
      });

      try {
        marker.setMap(mapInstance.value);
        kakaoMaps.event.addListener(marker, "click", () => {
          onMarkerClick?.(restaurant);
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
      mapCenter.value.lat,
      mapCenter.value.lng
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
        mapCenter.value.lat,
        mapCenter.value.lng
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

  const applyUserMapCenter = async () => {
    if (!isLoggedIn.value) return;
    const address = await fetchUserAddress();
    if (!address) {
      currentLocation.value = fallbackAddress;
      mapCenter.value = { ...fallbackMapCenter };
      return;
    }
    currentLocation.value = address;
    try {
      const coords = await geocodeAddress(address);
      mapCenter.value = coords;
      if (mapInstance.value && kakaoMapsApi.value?.LatLng) {
        const center = new kakaoMapsApi.value.LatLng(coords.lat, coords.lng);
        mapInstance.value.setCenter(center);
        scheduleMapMarkerRender();
      }
    } catch (error) {
      // keep fallback center if geocode fails
    }
  };

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

  return {
    mapContainer,
    mapCenter,
    currentLocation,
    isMapReady,
    mapDistanceStepIndex,
    currentDistanceLabel,
    distanceSliderFill,
    applyUserMapCenter,
    initializeMap,
    resetMapToHome,
    changeMapDistance,
    applyHomeMapZoom,
    isWithinDistance,
    calculateDistanceKm: haversineDistance,
  };
};
