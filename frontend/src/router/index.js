import { createRouter, createWebHistory } from "vue-router";

const routes = [
  {
    path: "/login",
    name: "login",
    component: () => import("@/views/LoginView.vue"),
    meta: { requiresAuth: false }
  },
  {
    path: "/",
    redirect: "/dashboard"
  },
  {
    path: "/dashboard",
    name: "dashboard",
    component: () => import("@/views/DashboardView.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/module/:moduleKey",
    name: "module",
    component: () => import("@/views/ResourceView.vue"),
    props: true,
    meta: { requiresAuth: true }
  },
  {
    path: "/batches/:batchId",
    name: "batch-detail",
    component: () => import("@/views/BatchDetailView.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/improvement",
    name: "improvement",
    component: () => import("@/views/ImprovementWorkflowView.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/files",
    name: "files",
    component: () => import("@/views/FilesView.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/spectrum-compare",
    name: "spectrum-compare",
    component: () => import("@/views/SpectrumCompareView.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/profile",
      name: "profile",
      component: () => import("@/views/ProfileView.vue"),
      meta: { requiresAuth: true }
    },
    {
      path: "/quality-metrics",
    name: "quality-metrics",
    component: () => import("@/views/QualityMetricView.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/profile/by-name/:name",
    name: "profile-by-name",
    component: () => import("@/views/UserPublicByName.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/profile/:userId",
    name: "user-profile",
    component: () => import("@/views/UserPublicView.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/mobile-devices",
    name: "mobile-devices",
    component: () => import("@/views/MobileDeviceView.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/multi-evaluations",
    name: "multi-evaluations",
    component: () => import("@/views/MultiEvaluationView.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/growth-analysis",
    name: "growth-analysis",
    component: () => import("@/views/GrowthAnalysisView.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/growth-analysis/:id",
    name: "growth-analysis-report",
    component: () => import("@/views/GrowthAnalysisReportView.vue"),
    meta: { requiresAuth: true }
  },
  {
    path: "/:pathMatch(.*)*",
    redirect: "/dashboard"
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to, from, next) => {
  const session = JSON.parse(sessionStorage.getItem("biomed-session") || "null");
  const isAuth = !!(session && session.token);

  if (to.meta.requiresAuth !== false && !isAuth) {
    next({ name: "login", query: { redirect: to.fullPath } });
  } else if (to.name === "login" && isAuth) {
    next({ path: "/dashboard" });
  } else {
    next();
  }
});

export default router;
