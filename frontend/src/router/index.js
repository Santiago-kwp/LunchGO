import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '../views/HomeView.vue';

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
      path: '/business/restaurant-info',
      name: 'business-restaurant-info',
      component: () =>
        import('../views/business/restaurant-info/RestaurantInfoPage.vue'),
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
      path: '/mypage/favorites',
      name: 'mypage-favorites',
      component: () => import('../views/mypage/favorites/FavoritesPage.vue'),
    },
    {
      path: '/mypage/reviews',
      name: 'mypage-reviews',
      component: () => import('../views/mypage/reviews/ReviewsPage.vue'),
    },
    {
      path: '/mypage',
      name: 'mypage',
      component: () => import('../views/mypage/MyPage.vue'),
    },
    {
      path: '/partner',
      name: 'partner',
      component: () => import('../views/partner/PartnerPage.vue'),
    },
    {
      path: '/partner/signup',
      name: 'partner-signup',
      component: () => import('../views/partner/signup/PartnerSignupPage.vue'),
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
    //사업자 - 오늘의 예약 현황 (상세보기)
    {
      path: '/business/reservations/:id',
      name: 'reservation-detail',
      component: () =>
        import('@/views/business/reservations/ReservationDetailPage.vue'),
    },
    // Wildcard route for 404 - make sure this is the last route
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      redirect: '/', // Redirect to home for any unmatched routes
    },
  ],
});

export default router;
