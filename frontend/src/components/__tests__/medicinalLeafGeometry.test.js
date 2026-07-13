import { describe, expect, it, vi } from "vitest";
import {
  LEAF_RENDER_BUDGET,
  createMedicinalLeafModel,
  disposeMedicinalLeafModel,
  updateMedicinalLeafModel,
} from "@/components/background/medicinalLeafGeometry";

describe("medicinal leaf geometry", () => {
  it("stays within the accepted batched render budget", () => {
    const model = createMedicinalLeafModel({ particleCount: 32 });

    expect(LEAF_RENDER_BUDGET).toEqual({
      longitudinalSegments: 28,
      widthSegments: 10,
      flowNodeCount: 18,
      maxDrawObjects: 5,
    });
    expect(model.group.children.length).toBeLessThanOrEqual(LEAF_RENDER_BUDGET.maxDrawObjects);
    expect(model.surface.isMesh).toBe(true);
    expect(model.surface.geometry.getAttribute("position").count).toBeGreaterThan(100);
    expect(model.surface.geometry.getAttribute("color").count)
      .toBe(model.surface.geometry.getAttribute("position").count);
    expect(model.veins.isLineSegments).toBe(true);
    expect(model.flowNodes.isInstancedMesh).toBe(true);
    expect(model.flowNodes.count).toBe(LEAF_RENDER_BUDGET.flowNodeCount);
    expect(model.flowNodes.instanceColor).toBeTruthy();
    expect(model.particles.isPoints).toBe(true);
    expect(model.particles.geometry.getAttribute("position").count).toBe(32);

    disposeMedicinalLeafModel(model);
  });

  it("builds deterministic ambient particle positions", () => {
    const first = createMedicinalLeafModel({ particleCount: 8 });
    const second = createMedicinalLeafModel({ particleCount: 8 });

    expect(Array.from(first.particles.geometry.getAttribute("position").array))
      .toEqual(Array.from(second.particles.geometry.getAttribute("position").array));

    disposeMedicinalLeafModel(first);
    disposeMedicinalLeafModel(second);
  });

  it("keeps rotations and instance matrices finite during animation", () => {
    const model = createMedicinalLeafModel({ particleCount: 32 });

    updateMedicinalLeafModel(model, 12.5);

    expect(model.group.rotation.toArray().slice(0, 3).every(Number.isFinite)).toBe(true);
    expect(Array.from(model.flowNodes.instanceMatrix.array).every(Number.isFinite)).toBe(true);

    disposeMedicinalLeafModel(model);
  });

  it("disposes owned geometry and materials", () => {
    const model = createMedicinalLeafModel({ particleCount: 32 });
    const surfaceDispose = vi.spyOn(model.surface.geometry, "dispose");
    const surfaceMaterialDispose = vi.spyOn(model.surface.material, "dispose");

    disposeMedicinalLeafModel(model);

    expect(surfaceDispose).toHaveBeenCalledOnce();
    expect(surfaceMaterialDispose).toHaveBeenCalledOnce();
  });
});
