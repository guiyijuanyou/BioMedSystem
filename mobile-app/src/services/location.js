export function getCurrentLocation() {
  return new Promise((resolve, reject) => {
    uni.getLocation({
      type: "gcj02",
      isHighAccuracy: true,
      success(result) {
        resolve({
          longitude: normalizeNumber(result.longitude, 6),
          latitude: normalizeNumber(result.latitude, 6),
          locationAccuracy: normalizeNumber(result.accuracy || result.horizontalAccuracy, 1)
        });
      },
      fail(error) {
        reject(new Error(error.errMsg || "定位失败"));
      }
    });
  });
}

function normalizeNumber(value, digits) {
  if (value === undefined || value === null || value === "") return "";
  const number = Number(value);
  return Number.isFinite(number) ? Number(number.toFixed(digits)) : "";
}
