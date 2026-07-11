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
