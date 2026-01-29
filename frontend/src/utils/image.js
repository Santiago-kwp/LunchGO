/**
 * 이미지 URL을 정규화하여 /images/ 접두사가 포함된 경로로 변환
 * @param {string|null|undefined} url - 원본 이미지 URL
 * @param {string} fallback - URL이 없을 때 반환할 기본값 (기본: '/placeholder.svg')
 * @returns {string} 정규화된 이미지 URL
 */
export function normalizeImageUrl(url, fallback = '/placeholder.svg') {
  if (!url) {
    return fallback;
  }

  // 1. 이미 정규화되었거나, 외부 링크(http),
  //    또는 프론트엔드 내부 리소스(?import), Base64 데이터인 경우 그대로 반환
  if (
      url.startsWith('/images/') ||
      url.startsWith('http') ||
      url.includes('?import') ||
      url.startsWith('data:') ||
      url.startsWith('/src/') // 추가: Vite 소스 경로인 경우
  ) {
    return url;
  }

  if (url.startsWith('/images/') || url.startsWith('http')) {
    return url;
  }
  return '/images/' + url;
}

/**
 * 이미지 URL 배열을 정규화
 * @param {Array<string|null>} urls - 원본 이미지 URL 배열
 * @returns {Array<string>} 정규화된 이미지 URL 배열 (null 제외)
 */
export function normalizeImageUrls(urls) {
  if (!Array.isArray(urls)) {
    return [];
  }
  return urls
    .map(url => url ? normalizeImageUrl(url, null) : null)
    .filter(Boolean);
}
