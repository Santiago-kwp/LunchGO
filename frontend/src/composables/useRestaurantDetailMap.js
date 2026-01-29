import { ref, computed, watch, nextTick, onBeforeUnmount } from 'vue';
import { loadKakaoMaps, geocodeAddress } from '@/utils/kakao';
import { buildMarkerBody, buildMarkerCircle, buildMarkerDataUri } from '@/utils/mapMarkerSvgs';

export const useRestaurantDetailMap = ({
  restaurantInfo,
  restaurantName,
  fetchCompanyAddress,
  isLoggedIn,
}) => {
  const detailMapContainer = ref(null);
  let detailMapInstance = null;
  let detailMarker = null;
  const detailKakaoMapsApi = ref(null);
  const detailRoutePolyline = ref(null);
  const detailRouteOriginMarker = ref(null);
  const routeInfo = ref(null);
  const routeError = ref('');
  const isRouteLoading = ref(false);

  const detailMapDistanceSteps = Object.freeze([
    { label: '100m', level: 2 },
    { label: '250m', level: 3 },
    { label: '500m', level: 4 },
    { label: '1km', level: 5 },
    { label: '2km', level: 6 },
    { label: '3km', level: 7 },
  ]);

  const detailDefaultMapDistanceIndex = detailMapDistanceSteps.findIndex(
    (step) => step.label === '500m',
  );
  const detailMapDistanceStepIndex = ref(
    detailDefaultMapDistanceIndex === -1 ? 0 : detailDefaultMapDistanceIndex,
  );

  const detailCurrentDistanceLabel = computed(
    () =>
      detailMapDistanceSteps[detailMapDistanceStepIndex.value]?.label ??
      detailMapDistanceSteps[0].label,
  );

  const detailDistanceSliderFill = computed(() => {
    if (detailMapDistanceSteps.length <= 1) return 0;
    return (
      ((detailMapDistanceSteps.length - 1 - detailMapDistanceStepIndex.value) /
        (detailMapDistanceSteps.length - 1)) *
      100
    );
  });

  const detailLevelForDistance = (stepIndex) => {
    const step = detailMapDistanceSteps[stepIndex] ?? detailMapDistanceSteps[0];
    return step.level;
  };

  const applyDetailMapZoom = () => {
    if (!detailMapInstance) return;
    detailMapInstance.setLevel(detailLevelForDistance(detailMapDistanceStepIndex.value), {
      animate: { duration: 300 },
    });
  };

  const changeDetailMapDistance = (delta) => {
    const next = Math.min(
      detailMapDistanceSteps.length - 1,
      Math.max(0, detailMapDistanceStepIndex.value + delta),
    );
    detailMapDistanceStepIndex.value = next;
  };

  const clearDetailRoute = () => {
    if (detailRoutePolyline.value) {
      detailRoutePolyline.value.setMap(null);
      detailRoutePolyline.value = null;
    }
    if (detailRouteOriginMarker.value) {
      detailRouteOriginMarker.value.setMap(null);
      detailRouteOriginMarker.value = null;
    }
  };

  const drawDetailRoute = (pathPoints = []) => {
    if (!detailMapInstance || !detailKakaoMapsApi.value) return;
    clearDetailRoute();
    if (!Array.isArray(pathPoints) || !pathPoints.length) return;

    const kakaoMaps = detailKakaoMapsApi.value;
    const path = pathPoints
      .filter((point) => Number.isFinite(point?.lat) && Number.isFinite(point?.lng))
      .map((point) => new kakaoMaps.LatLng(point.lat, point.lng));
    if (path.length < 2) return;

    detailRoutePolyline.value = new kakaoMaps.Polyline({
      path,
      strokeWeight: 6,
      strokeColor: '#d9480f',
      strokeOpacity: 0.95,
      strokeStyle: 'solid',
    });
    detailRoutePolyline.value.setMap(detailMapInstance);

    const bounds = new kakaoMaps.LatLngBounds();
    path.forEach((point) => bounds.extend(point));
    detailMapInstance.setBounds(bounds, 20, 20, 20, 20);
  };

  const renderRouteOriginMarker = (coords) => {
    if (!detailMapInstance || !detailKakaoMapsApi.value) return;
    const kakaoMaps = detailKakaoMapsApi.value;
    const markerSvg = buildMarkerDataUri(
      buildMarkerBody('#1e3a5f', buildMarkerCircle('white')),
    );
    const markerImage = new kakaoMaps.MarkerImage(
      markerSvg,
      new kakaoMaps.Size(32, 46),
      { offset: new kakaoMaps.Point(16, 46) },
    );
    if (detailRouteOriginMarker.value) {
      detailRouteOriginMarker.value.setMap(null);
    }
    detailRouteOriginMarker.value = new kakaoMaps.Marker({
      position: new kakaoMaps.LatLng(coords.lat, coords.lng),
      title: '회사',
      image: markerImage,
    });
    detailRouteOriginMarker.value.setMap(detailMapInstance);
  };

  const initializeDetailMap = async () => {
    if (!detailMapContainer.value || !restaurantInfo.value?.coords) {
      console.warn('지도 컨테이너 또는 좌표 정보가 없어 지도를 초기화할 수 없습니다.');
      return;
    }
    try {
      const kakaoMaps = await loadKakaoMaps();
      detailKakaoMapsApi.value = kakaoMaps;
      const center = new kakaoMaps.LatLng(
        restaurantInfo.value.coords.lat,
        restaurantInfo.value.coords.lng,
      );
      const markerSvg = buildMarkerDataUri(
        buildMarkerBody('#ff6b4a', buildMarkerCircle('white')),
      );
      const markerImage = new kakaoMaps.MarkerImage(
        markerSvg,
        new kakaoMaps.Size(32, 46),
        { offset: new kakaoMaps.Point(16, 46) },
      );
      detailMapInstance = new kakaoMaps.Map(detailMapContainer.value, {
        center,
        level: detailLevelForDistance(detailMapDistanceStepIndex.value),
      });
      detailMapInstance.setZoomable(false);
      detailMarker = new kakaoMaps.Marker({
        position: center,
        title: restaurantName.value,
        image: markerImage,
      });
      detailMarker.setMap(detailMapInstance);
      applyDetailMapZoom();
    } catch (error) {
      console.error('식당 위치 지도를 불러오지 못했습니다.', error);
    }
  };

  const handleCheckRoute = async (httpRequest) => {
    if (!restaurantInfo.value?.coords) {
      routeError.value = '식당 위치를 확인할 수 없습니다.';
      return;
    }
    if (!isLoggedIn.value) {
      routeError.value = '로그인 후 경로를 확인할 수 있습니다.';
      return;
    }

    isRouteLoading.value = true;
    routeError.value = '';
    routeInfo.value = null;

    try {
      if (!detailMapInstance && detailMapContainer.value) {
        await nextTick();
        await initializeDetailMap();
      }
      const companyAddress = await fetchCompanyAddress();
      if (!companyAddress) {
        routeError.value = '회사 주소를 등록해 주세요.';
        return;
      }
      const originCoords = await geocodeAddress(companyAddress);
      if (!originCoords) {
        routeError.value = '회사 위치를 확인할 수 없습니다.';
        return;
      }
      const destinationCoords = restaurantInfo.value.coords;
      const response = await httpRequest.post('/api/map/route', {
        origin: { lat: originCoords.lat, lng: originCoords.lng },
        destination: { lat: destinationCoords.lat, lng: destinationCoords.lng },
      });
      const data = response?.data || {};
      if (Array.isArray(data.path) && data.path.length) {
        drawDetailRoute(data.path);
      }
      renderRouteOriginMarker(originCoords);
      routeInfo.value = {
        distanceMeters: data.distanceMeters ?? null,
        durationSeconds: data.durationSeconds ?? null,
      };
    } catch (error) {
      routeError.value = '경로를 불러오지 못했습니다.';
    } finally {
      isRouteLoading.value = false;
    }
  };

  const resetMapState = () => {
    clearDetailRoute();
    routeInfo.value = null;
    routeError.value = '';
  };

  const cleanup = () => {
    if (detailMarker) {
      detailMarker.setMap(null);
    }
    clearDetailRoute();
    detailMarker = null;
    detailMapInstance = null;
  };

  watch(restaurantInfo, async (newValue) => {
    if (newValue && newValue.coords) {
      await nextTick();
      resetMapState();
      initializeDetailMap();
    }
  });

  watch(detailMapDistanceStepIndex, () => {
    applyDetailMapZoom();
  });

  onBeforeUnmount(() => {
    cleanup();
  });

  return {
    detailMapContainer,
    routeInfo,
    routeError,
    isRouteLoading,
    detailMapDistanceStepIndex,
    detailCurrentDistanceLabel,
    detailDistanceSliderFill,
    initializeDetailMap,
    handleCheckRoute,
    changeDetailMapDistance,
    cleanup,
  };
};
