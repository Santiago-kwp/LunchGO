import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '../views/HomeView.vue';

import { useRestaurantStore } from '@/stores/restaurant';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/business/dashboard',
      name: 'business-dashboard',
      component: () =>
        import('../views/business/dashboard/BusinessDashboardPage.vue'),
    },
    {
      path: '/business/reservations',
      name: 'business-reservations',
      component: () =>
        import('../views/business/reservations/BusinessReservationsPage.vue'),
    },
    {
      path: '/business/restaurant-info/add',
      name: 'business-restaurant-info-add',
      component: () =>
        import(
          '../views/business/restaurant-info/edit/RestaurantInfoEditPage.vue'
        ),
    },
    {
      path: '/business/restaurant-info/edit/:id',
      name: 'business-restaurant-info-edit',
      component: () =>
        import(
          '../views/business/restaurant-info/edit/RestaurantInfoEditPage.vue'
        ),
    },
    {
      path: '/business/restaurant-info/menu/add',
      name: 'business-restaurant-menu-add',
      component: () =>
        import('../views/business/restaurant-info/menu/MenuEditPage.vue'),
    },
    {
      path: '/business/restaurant-info/menu/edit/:id',
      name: 'business-restaurant-menu-edit',
      component: () =>
        import('../views/business/restaurant-info/menu/MenuEditPage.vue'),
      props: true,
    },
    {
      path: '/business/restaurant-info/:id',
      name: 'business-restaurant-info',
      component: () =>
        import('../views/business/restaurant-info/RestaurantInfoPage.vue'),
      props: true,
    },
    {
      path: '/business/restaurant-info/:id/menus',
      name: 'business-restaurant-menus',
      component: () =>
        import('../views/business/restaurant-info/menu/MenusInfoPage.vue'),
      props: true,
    },
    {
      path: '/business/staff',
      name: 'business-staff',
      component: () => import('../views/business/staff/BusinessStaffPage.vue'),
    },
    {
      path: '/business/reviews',
      name: 'business-reviews',
      component: () =>
        import('../views/business/reviews/BusinessReviewsPage.vue'),
    },
    {
      path: '/business/promotion',
      name: 'business-promotion',
      component: () =>
        import('../views/business/promotion/BusinessPromotionPage.vue'),
    },
    {
      path: '/admin/dashboard',
      name: 'admin-dashboard',
      component: () =>
        import('../views/admin/dashboard/AdminDashboardPage.vue'),
    },
    {
      path: '/admin/reservations',
      name: 'admin-reservations',
      component: () =>
        import('../views/admin/reservations/AdminReservationsPage.vue'),
    },
    {
      path: '/admin/franchises',
      name: 'admin-franchises',
      component: () =>
        import('../views/admin/franchises/AdminFranchisesPage.vue'),
    },
    {
      path: '/admin/reviews',
      name: 'admin-reviews',
      component: () => import('../views/admin/reviews/AdminReviewsPage.vue'),
    },
    {
      path: '/admin/members',
      name: 'admin-members',
      component: () => import('../views/admin/members/AdminMembersPage.vue'),
    },
    {
      path: '/admin/finance',
      name: 'admin-finance',
      component: () => import('../views/admin/finance/AdminFinancePage.vue'),
    },
    {
      path: '/admin/owners',
      name: 'admin-owners',
      component: () => import('../views/admin/owners/AdminOwnerApprovalPage.vue'),
    },
    {
      path: '/intro',
      name: 'intro',
      component: () => import('../views/intro/ServiceIntroPage.vue'),
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/login/LoginPage.vue'),
    },
    {
      path: '/my-reservations',
      name: 'my-reservations',
      component: () =>
        import('../views/my-reservations/MyReservationsPage.vue'),
    },
    {
      path: '/mypage/reviews',
      name: 'mypage-reviews',
      component: () => import('../views/mypage/reviews/ReviewsPage.vue'),
    },
    {
      path: '/mypage',
      name: 'mypage',
      component: () => import('../views/mypage/UserMyPage.vue'),
    },
    {
      path: '/business/mypage',
      name: 'business-mypage',
      component: () => import('../views/mypage/OwnerMyPage.vue'),
    },
    {
      path: '/partner',
      name: 'partner',
      component: () => import('../views/partner/PartnerPage.vue'),
    },
    {
      path: '/restaurant/:id/booking',
      name: 'restaurant-booking',
      component: () =>
        import('../views/restaurant/id/booking/RestaurantBookingPage.vue'),
      props: true,
    },
    {
      path: '/restaurant/:id/confirmation',
      name: 'restaurant-confirmation',
      component: () =>
        import(
          '../views/restaurant/id/confirmation/RestaurantConfirmationPage.vue'
        ),
      props: true,
    },
    {
      path: '/restaurant/:id/menu',
      name: 'restaurant-menu',
      component: () =>
        import('../views/restaurant/id/menu/MenuSelectionPage.vue'),
      props: true,
    },
    {
      path: '/restaurant/:id/menus',
      name: 'restaurant-menus',
      component: () =>
        import('../views/restaurant/id/menus/RestaurantMenusPage.vue'),
      props: true,
    },
    {
      path: '/restaurant/:id',
      name: 'restaurant-detail',
      component: () =>
        import('../views/restaurant/id/RestaurantDetailPage.vue'),
      props: true,
    },
    {
      path: '/restaurant/:id/payment',
      name: 'restaurant-payment',
      component: () =>
        import('../views/restaurant/id/payment/RestaurantPaymentPage.vue'),
      props: true,
    },
    {
      path: '/restaurant/:id/summary',
      name: 'restaurant-summary',
      component: () =>
        import('../views/restaurant/id/summary/RestaurantSummaryPage.vue'),
      props: true,
    },
    {
      path: '/restaurant/:id/reviews/write',
      name: 'write-review',
      component: () =>
        import('../views/restaurant/id/reviews/WriteReviewPage.vue'),
      props: true,
    },
    {
      path: '/restaurant/:id/reviews/:reviewId/edit',
      name: 'edit-review',
      component: () =>
        import('../views/restaurant/id/reviews/WriteReviewPage.vue'),
      props: true,
    },
    {
      path: '/restaurant/:id/reviews/:reviewId',
      name: 'review-detail',
      component: () =>
        import('../views/restaurant/id/reviews/reviewId/ReviewDetailPage.vue'),
      props: true,
    },
    {
      path: '/restaurant/:id/reviews',
      name: 'restaurant-reviews',
      component: () =>
        import('../views/restaurant/id/reviews/RestaurantReviewsPage.vue'),
      props: true,
    },
    {
      path: '/signup',
      name: 'signup',
      component: () => import('../views/signup/SignupChoicePage.vue'),
    },
    {
      path: '/signup/user',
      name: 'signup-user',
      component: () => import('../views/signup/UserSignupPage.vue'),
    },
    {
      path: '/signup/owner',
      name: 'signup-owner',
      component: () => import('../views/signup/OwnerSignupPage.vue'),
    },
    //방문 선택 확인 페이지
    {
      path: '/business/notifications',
      name: 'business-notifications',
      component: () => import('../views/business/notifications/BusinessNotificationsPage.vue'),
    },
    //예약 상세보기
    {
      path: '/business/reservations/:id',
      name: 'reservation-detail',
      component: () => import('@/views/business/reservations/ReservationDetailPage.vue'),
      props: true,
      alias: '/reservations/:id',
    },
    {
      path: '/staff/list',
      name : 'staff-list',
      component: () => import('@/views/staff/StaffListPage.vue'),
    },
    // Wildcard route for 404 - make sure this is the last route
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      redirect: '/', // Redirect to home for any unmatched routes
    },
  ],
});

router.beforeEach((to, from) => {
  // Pinia 스토어는 Vue 앱이 실행된 후에 사용 가능하므로, 가드 내에서 직접 호출합니다.
  const store = useRestaurantStore();

  // 식당 정보 등록/수정 워크플로우에 해당하는 라우트 이름 목록
  const workflowRoutes = [
    'business-restaurant-info-add',
    'business-restaurant-info-edit',
    'business-restaurant-menu-add',
    'business-restaurant-menu-edit',
  ];

  const isFromWorkflow = workflowRoutes.includes(from.name);
  const isToWorkflow = workflowRoutes.includes(to.name);

  // 워크플로우를 벗어나는 경우에만 store를 초기화합니다.
  if (isFromWorkflow && !isToWorkflow) {
    store.clearRestaurant();
  }
});

export default router;
