import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useRestaurantStore = defineStore('restaurant', () => {
  // State
  const restaurantId = ref(null); // 현재 편집 중인 식당 ID
  const menus = ref([]);

  // Actions
  /**
   * 수정 모드에서 식당 데이터를 로드합니다.
   * 스토어에 이미 같은 식당 데이터가 로드되어 있다면 메뉴를 덮어쓰지 않습니다.
   * @param {object} data - API로부터 받은 식당 데이터
   */
  function loadRestaurant(data) {
    if (restaurantId.value === data.restaurantId) {
      // 이미 로드된 식당이므로 메뉴는 그대로 둡니다.
      return;
    }
    // 새로운 식당 데이터를 로드합니다.
    restaurantId.value = data.restaurantId;
    menus.value = [...(data.menus || [])];
  }

  /**
   * 스토어를 초기 상태로 리셋합니다.
   */
  function clearRestaurant() {
    restaurantId.value = null;
    menus.value = [];
  }

  function setMenus(newMenus) {
    menus.value = [...newMenus];
  }

  function addMenu(menu) {
    menus.value.push(menu);
  }

  function updateMenu(updatedMenu) {
    const index = menus.value.findIndex(menu => menu.id === updatedMenu.id);
    if (index !== -1) {
      menus.value[index] = updatedMenu;
    }
  }

  function deleteMenu(menuId) {
    menus.value = menus.value.filter(menu => menu.id !== menuId);
  }

  function getMenuById(menuId) {
    const id = Number(menuId);
    return menus.value.find(menu => menu.id === id);
  }
  
  /**
   * 임시 클라이언트 측 ID를 생성합니다.
   * @returns {number} 새로운 메뉴 ID
   */
  function getNextId() {
    return Date.now();
  }

  return { 
    restaurantId,
    menus, 
    loadRestaurant,
    clearRestaurant,
    setMenus,
    addMenu, 
    updateMenu, 
    deleteMenu,
    getMenuById,
    getNextId,
  };
});
