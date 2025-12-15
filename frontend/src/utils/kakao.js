const KAKAO_APP_KEY = '95594389d96723d3b307c14b21a9eeef';

let kakaoMapsLoader;

export const loadKakaoMaps = () => {
  if (typeof window === 'undefined') {
    return Promise.reject(new Error('Kakao Maps can only be loaded in the browser.'));
  }

  if (window.kakao && window.kakao.maps) {
    return Promise.resolve(window.kakao.maps);
  }

  if (!kakaoMapsLoader) {
    kakaoMapsLoader = new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${KAKAO_APP_KEY}&autoload=false`;
      script.async = true;
      script.onerror = () => reject(new Error('카카오 지도 SDK 로딩 실패'));
      script.onload = () => {
        window.kakao.maps.load(() => {
          resolve(window.kakao.maps);
        });
      };
      document.head.appendChild(script);
    });
  }

  return kakaoMapsLoader;
};
