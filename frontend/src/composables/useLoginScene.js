const clamp = value => Math.max(0, Math.min(1, value));
const lerp = (start, end, amount) => start + (end - start) * amount;

export const sceneStages = [
  { id: "cloud", cameraZ: 9.4, rotationY: 0, rotationZ: -0.18, spread: 0, opacity: 1, accentMix: 0 },
  { id: "collection", cameraZ: 8.2, rotationY: 0.75, rotationZ: 0.12, spread: 0.45, opacity: 0.95, accentMix: 0.25 },
  { id: "distribution", cameraZ: 10.2, rotationY: 1.55, rotationZ: 0.6, spread: 1.15, opacity: 0.78, accentMix: 0.55 },
  { id: "trace", cameraZ: 7.8, rotationY: 2.35, rotationZ: 1.15, spread: 0.72, opacity: 0.9, accentMix: 0.82 },
  { id: "roles", cameraZ: 9, rotationY: 3.2, rotationZ: 0, spread: 0.12, opacity: 0.72, accentMix: 1 }
];

export function resolveSceneState(progress) {
  const normalized = clamp(Number(progress) || 0);
  const scaled = normalized * (sceneStages.length - 1);
  const index = Math.min(sceneStages.length - 2, Math.floor(scaled));
  const amount = scaled - index;
  const from = sceneStages[index];
  const to = sceneStages[index + 1];
  return {
    cameraZ: lerp(from.cameraZ, to.cameraZ, amount),
    rotationY: lerp(from.rotationY, to.rotationY, amount),
    rotationZ: lerp(from.rotationZ, to.rotationZ, amount),
    spread: lerp(from.spread, to.spread, amount),
    opacity: lerp(from.opacity, to.opacity, amount),
    accentMix: lerp(from.accentMix, to.accentMix, amount)
  };
}
