const defaultMenuCategories = [
  {
    id: 'course',
    name: '코스 메뉴',
    items: [
      {
        id: 'course-a',
        name: 'A코스',
        price: '35,000원',
        description: '전채 + 메인 + 디저트',
      },
      {
        id: 'course-b',
        name: 'B코스',
        price: '45,000원',
        description: '전채 + 메인 + 디저트 + 와인',
      },
      {
        id: 'course-c',
        name: 'C코스',
        price: '55,000원',
        description: '전채 + 메인 + 디저트 + 프리미엄 와인',
      },
      {
        id: 'course-d',
        name: 'D코스',
        price: '65,000원',
        description:
          '최고급 전채 + 프리미엄 메인 + 특별 디저트 + 프리미엄 와인',
      },
    ],
  },
  {
    id: 'single',
    name: '단품 메뉴',
    items: [
      {
        id: 'single-pasta',
        name: '파스타',
        price: '18,000원',
        description: '크림/토마토 소스 선택',
      },
      {
        id: 'single-steak',
        name: '스테이크',
        price: '28,000원',
        description: '250g, 미디움 추천',
      },
      {
        id: 'single-risotto',
        name: '리조또',
        price: '16,000원',
        description: '해산물 리조또',
      },
      {
        id: 'single-pizza',
        name: '피자',
        price: '22,000원',
        description: '마르게리타/페퍼로니',
      },
      {
        id: 'single-salad',
        name: '샐러드',
        price: '12,000원',
        description: '시저 샐러드',
      },
      {
        id: 'single-soup',
        name: '수프',
        price: '10,000원',
        description: '버섯 크림 수프',
      },
    ],
  },
  {
    id: 'side',
    name: '사이드 메뉴',
    items: [
      { id: 'side-fries', name: '감자튀김', price: '8,000원' },
      { id: 'side-garlic', name: '마늘빵', price: '6,000원' },
      { id: 'side-cheese', name: '치즈스틱', price: '9,000원' },
      { id: 'side-shrimp', name: '새우튀김', price: '12,000원' },
    ],
  },
  {
    id: 'beverage',
    name: '음료',
    items: [
      {
        id: 'bev-red-wine',
        name: '와인(레드)',
        price: '45,000원',
        description: '보틀',
      },
      {
        id: 'bev-white-wine',
        name: '와인(화이트)',
        price: '42,000원',
        description: '보틀',
      },
      { id: 'bev-beer', name: '맥주', price: '6,000원' },
      { id: 'bev-soft', name: '소프트드링크', price: '3,000원' },
      { id: 'bev-espresso', name: '에스프레소', price: '4,000원' },
      { id: 'bev-americano', name: '아메리카노', price: '4,500원' },
    ],
  },
  {
    id: 'dessert',
    name: '디저트',
    items: [
      { id: 'des-tiramisu', name: '티라미수', price: '8,000원' },
      { id: 'des-pannacotta', name: '판나코타', price: '7,000원' },
      { id: 'des-icecream', name: '아이스크림', price: '5,000원' },
      { id: 'des-cheesecake', name: '치즈케이크', price: '9,000원' },
    ],
  },
];

const restaurantMenuCatalog = {
  1: defaultMenuCategories,
};

const cloneMenuCategories = (categories) =>
  categories.map((category) => ({
    ...category,
    items: category.items.map((item) => ({ ...item })),
  }));

export const getMenuCategoriesByRestaurant = (restaurantId) => {
  const categories =
    restaurantMenuCatalog[restaurantId] ?? defaultMenuCategories;
  return cloneMenuCategories(categories);
};

export { defaultMenuCategories, restaurantMenuCatalog };
