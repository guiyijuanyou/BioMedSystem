import { nextTick, onBeforeUnmount, watch } from "vue";
import gsap from "gsap";

/**
 * Orchestrates all analysis animations — A1 (button + step indicator),
 * A3 (gauge bars), A4 (card stagger entrance), A5 (stage compliance bars).
 *
 * A2 (canvas line drawing) is provided by the view as `drawTrendAnimated`
 * and called per-card after that card enters.
 */
export function useAnalysisAnimation({
  computing,
  result,
  analyzeButtonRef,
  stepsContainerRef,
  conclusionPanelRef,
  drawTrendAnimated,
  stepIndex,
}) {
  let buttonTween = null;
  let stepTimeline = null;
  let resultTimeline = null;

  // ── A1: Button + step indicator (called from runAnalysis) ──

  function animateButtonStart() {
    const btn = analyzeButtonRef.value;
    if (btn) {
      buttonTween = gsap.to(btn, {
        minWidth: 190,
        duration: 0.3,
        ease: "power2.out",
      });
    }

    const steps = stepsContainerRef.value;
    if (!steps) return;

    const items = steps.querySelectorAll(".analysis-step-item");
    gsap.set(steps, { height: 0, opacity: 0 });
    gsap.set(items, { opacity: 0, y: 8 });

    stepTimeline = gsap.timeline();
    let stepCount = 0;
    stepTimeline
      .to(steps, { height: "auto", opacity: 1, duration: 0.25, ease: "power2.out" })
      .to(items, {
        opacity: 1,
        y: 0,
        duration: 0.25,
        stagger: 0.4,
        ease: "power2.out",
        onStart: () => {
          stepIndex.value = ++stepCount;
        },
      });
  }

  function animateButtonComplete(success = true) {
    // Kill step timeline and collapse
    const steps = stepsContainerRef.value;
    if (steps) {
      stepTimeline?.kill();
      gsap.to(steps, {
        opacity: 0,
        height: 0,
        duration: 0.2,
        ease: "power2.in",
        onComplete: () => gsap.set(steps, { display: "none" }),
      });
    }

    // Button: restore width + optional success flash
    const btn = analyzeButtonRef.value;
    if (btn) {
      buttonTween?.kill();
      gsap.set(btn, { minWidth: 140 });
      if (success) {
        btn.classList.add("analysis-success-flash");
        setTimeout(() => btn.classList.remove("analysis-success-flash"), 600);
      }
    }

    stepTimeline = null;
    buttonTween = null;
  }

  // ── A3: Gauge stacked-bar growth ──

  function animateGaugesForCard(cardEl) {
    const container = cardEl.querySelector(".gauge-stacked-bar");
    if (!container) return;
    const segments = container.querySelectorAll(".gauge-segment");
    if (!segments.length) return;

    const targets = Array.from(segments).map((el) => el.style.width);
    gsap.set(segments, { width: "0%" });
    gsap.to(segments, {
      width: (i) => targets[i],
      duration: 0.35,
      stagger: 0.08,
      ease: "power2.out",
    });
  }

  // ── A5: Stage compliance bar growth ──

  function animateStageBarsForCard(cardEl) {
    const bars = cardEl.querySelectorAll(".stage-compliance-bar-fill");
    if (!bars.length) return;

    const targets = Array.from(bars).map((el) => el.style.width);
    gsap.set(bars, { width: "0%" });
    gsap.to(bars, {
      width: (i) => targets[i],
      duration: 0.3,
      stagger: 0.06,
      ease: "power2.out",
    });
  }

  // ── A2 helper: find canvas, extract data, call drawTrendAnimated ──

  function animateChartForCard(cardEl, trendData, suitability) {
    const t = trendData.find(
      (td) => "chart-" + td.indicator === cardEl.querySelector("canvas")?.id
    );
    // Match by canvas id inside the card
    const canvas = cardEl.querySelector("canvas");
    if (!canvas || !t) return;

    const opt = suitability.find((s) => s.indicator === t.indicator);
    if (!opt || !t.values || t.values.length < 2) return;

    const colorMap = { temperature: "#e67e22", humidity: "#3498db", soil_ph: "#2c7a4a" };
    drawTrendAnimated(canvas, t.values, opt.optMin, opt.optMax, t.unit, colorMap[t.indicator] || "#635bff");
  }

  // ── A4: Card stagger entrance → then per-card A2/A3/A5 ──

  function animateResults() {
    const cards = document.querySelectorAll(".analysis-result-card");
    const conclusion = conclusionPanelRef.value;
    if (!cards.length) return;

    const data = result.value;
    const trendData = data?.trendData || [];
    const suitability = data?.suitability || [];

    // Set initial hidden state
    gsap.set(cards, { opacity: 0, y: 40 });
    if (conclusion) gsap.set(conclusion, { opacity: 0, y: 40 });

    resultTimeline = gsap.timeline();

    // Cards stagger in
    resultTimeline.to(cards, {
      opacity: 1,
      y: 0,
      duration: 0.35,
      stagger: 0.12,
      ease: "power2.out",
    });

    // Fire internal animations per card — slightly before entrance completes
    cards.forEach((cardEl, i) => {
      resultTimeline.call(
        () => {
          animateChartForCard(cardEl, trendData, suitability);
          animateGaugesForCard(cardEl);
          animateStageBarsForCard(cardEl);
        },
        null,
        i * 0.12 + 0.2 // stagger offset + partial overlap with entrance
      );
    });

    // Conclusion panel enters after cards
    const lastCardStart = (cards.length - 1) * 0.12;
    resultTimeline.to(
      conclusion,
      { opacity: 1, y: 0, duration: 0.35, ease: "power2.out" },
      lastCardStart + 0.4
    );
  }

  // ── Watch result → trigger A4+A2+A3+A5 ──

  watch(
    () => result.value,
    async (val) => {
      if (!val) {
        // Clear any running result animations when switching batches
        resultTimeline?.kill();
        resultTimeline = null;
        return;
      }
      await nextTick();
      // Small delay to let Vue finish rendering
      await new Promise((r) => setTimeout(r, 150));

      // Auto-scroll to results so animations are visible
      const firstCard = document.querySelector(".analysis-result-card");
      if (firstCard) {
        firstCard.scrollIntoView({ behavior: "smooth", block: "start" });
      }

      animateResults();
    }
  );

  // ── Watch computing → reset step index ──

  watch(
    () => computing.value,
    (val) => {
      if (val) stepIndex.value = 0;
    }
  );

  // ── Cleanup ──

  onBeforeUnmount(() => {
    buttonTween?.kill();
    stepTimeline?.kill();
    resultTimeline?.kill();
  });

  return { animateButtonStart, animateButtonComplete };
}
