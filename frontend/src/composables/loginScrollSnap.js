const clamp = value => Math.max(0, Math.min(1, value));
const round = value => Number(value.toFixed(4));

export function createSectionSnapPoints(offsets, maxScroll, topOffset = 0) {
  const safeMax = Math.max(1, Number(maxScroll) || 1);
  return offsets.map(offset => round(clamp((Math.max(0, Number(offset) || 0) - topOffset) / safeMax)));
}

export function nearestSnapPoint(progress, snapPoints) {
  const current = clamp(Number(progress) || 0);
  return snapPoints.reduce((nearest, point) =>
    Math.abs(point - current) < Math.abs(nearest - current) ? point : nearest,
  snapPoints[0] ?? 0);
}

export function projectedSnapPoint(scroll, velocity, maxScroll, snapPoints, projectionFrames = 18) {
  const safeMax = Math.max(1, Number(maxScroll) || 1);
  const currentProgress = clamp((Number(scroll) || 0) / safeMax);
  const currentPoint = nearestSnapPoint(currentProgress, snapPoints);
  const currentIndex = Math.max(0, snapPoints.indexOf(currentPoint));
  const rawVelocity = Number(velocity) || 0;
  const direction = Math.sign(rawVelocity);
  const adjacentIndex = Math.max(0, Math.min(snapPoints.length - 1, currentIndex + direction));
  const candidates = direction === 0 ? [currentPoint] : [currentPoint, snapPoints[adjacentIndex]];
  const boundedVelocity = Math.max(-12, Math.min(12, rawVelocity));
  const projectedProgress = clamp(((Number(scroll) || 0) + boundedVelocity * projectionFrames) / safeMax);
  return nearestSnapPoint(projectedProgress, candidates);
}
