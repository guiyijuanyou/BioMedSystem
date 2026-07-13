import { nextTick, onBeforeUnmount, onMounted, ref } from "vue";
import Lenis from "lenis";
import gsap from "gsap";
import ScrollTrigger from "gsap/ScrollTrigger";
import {
  createSectionSnapPoints,
  directionalSnapPoint,
} from "@/composables/loginScrollSnap";

gsap.registerPlugin(ScrollTrigger);

export function useImmersiveScroll(sectionIds) {
  const contentSnapOffset = 40;
  const progress = ref(0);
  const activeSection = ref(sectionIds[0]);
  let lenis;
  let ticker;
  let snapPoints = [];
  let snapEnabled = true;
  let autoSnapping = false;
  let navigationTimer;
  let wheelSnapQuery;
  let reducedMotionQuery;
  const contexts = [];

  function refreshSnapPoints() {
    const maxScroll = Math.max(
      1,
      document.documentElement.scrollHeight - window.innerHeight,
    );
    const offsets = sectionIds.map(
      (id) => document.getElementById(id)?.offsetTop || 0,
    );
    snapPoints = createSectionSnapPoints(
      offsets,
      maxScroll,
      -contentSnapOffset,
    );
  }

  function updatePosition() {
    const max = Math.max(
      1,
      document.documentElement.scrollHeight - window.innerHeight,
    );
    progress.value = Math.max(0, Math.min(1, window.scrollY / max));
    const marker = window.innerHeight * 0.46;
    let current = sectionIds[0];
    for (const id of sectionIds) {
      const element = document.getElementById(id);
      if (element && element.getBoundingClientRect().top <= marker)
        current = id;
    }
    activeSection.value = current;
  }

  function scrollToSection(id) {
    const target = document.getElementById(id);
    if (!target) return;
    snapEnabled = false;
    autoSnapping = true;
    clearTimeout(navigationTimer);
    lenis?.scrollTo(target, {
      offset: id === sectionIds[0] ? 0 : contentSnapOffset,
      duration: 1.15,
      onComplete: () => {
        navigationTimer = setTimeout(() => {
          autoSnapping = false;
          snapEnabled = true;
        }, 260);
      },
    });
  }

  function handleWheel(event) {
    if (
      !wheelSnapQuery?.matches
      || !snapEnabled
      || !snapPoints.length
      || event.ctrlKey
      || event.deltaY === 0
    ) return;

    event.preventDefault();
    event.stopPropagation();
    if (autoSnapping) return;

    const maxScroll = Math.max(
      1,
      document.documentElement.scrollHeight - window.innerHeight,
    );
    const targetProgress = directionalSnapPoint(
      window.scrollY,
      event.deltaY,
      maxScroll,
      snapPoints,
    );
    const targetScroll = targetProgress * maxScroll;
    if (Math.abs(targetScroll - window.scrollY) < 1) return;

    autoSnapping = true;
    lenis?.scrollTo(targetScroll, {
      duration: reducedMotionQuery?.matches ? 0.01 : 0.65,
      easing: (value) => 1 - Math.pow(1 - value, 4),
      onComplete: () => {
        autoSnapping = false;
      },
    });
  }

  function pause() {
    snapEnabled = false;
    autoSnapping = false;
    lenis?.stop();
  }

  function resume() {
    lenis?.start();
    requestAnimationFrame(() => {
      autoSnapping = false;
      snapEnabled = true;
    });
  }

  onMounted(async () => {
    await nextTick();
    lenis = new Lenis({
      duration: 1.15,
      smoothWheel: true,
      wheelMultiplier: 0.86,
      touchMultiplier: 1.05,
    });
    document.documentElement.classList.add("immersive-scroll");
    wheelSnapQuery = window.matchMedia("(hover: hover) and (pointer: fine)");
    reducedMotionQuery = window.matchMedia("(prefers-reduced-motion: reduce)");
    window.addEventListener("wheel", handleWheel, { passive: false, capture: true });
    lenis.on("scroll", ScrollTrigger.update);
    lenis.on("scroll", updatePosition);
    ticker = (time) => lenis?.raf(time * 1000);
    gsap.ticker.add(ticker);
    gsap.ticker.lagSmoothing(0);

    document.querySelectorAll(".login-section").forEach((section) => {
      const context = gsap.context(() => {
        gsap.fromTo(
          section.querySelectorAll(
            ".section-copy > *, .collection-console, .distribution-map, .spectrum-panel, .role-layout",
          ),
          { y: 48, opacity: 0 },
          {
            y: 0,
            opacity: 1,
            duration: 1,
            stagger: 0.08,
            ease: "power3.out",
            scrollTrigger: { trigger: section, start: "top 72%", once: true },
          },
        );
      }, section);
      contexts.push(context);
    });
    refreshSnapPoints();
    ScrollTrigger.addEventListener("refresh", refreshSnapPoints);
    updatePosition();
    ScrollTrigger.refresh();
  });

  onBeforeUnmount(() => {
    contexts.forEach((context) => context.revert());
    clearTimeout(navigationTimer);
    window.removeEventListener("wheel", handleWheel, true);
    document.documentElement.classList.remove("immersive-scroll");
    ScrollTrigger.removeEventListener("refresh", refreshSnapPoints);
    if (ticker) gsap.ticker.remove(ticker);
    lenis?.destroy();
    ScrollTrigger.getAll().forEach((trigger) => trigger.kill());
  });

  return { progress, activeSection, scrollToSection, pause, resume };
}
