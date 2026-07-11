<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from "vue";
import L from "leaflet";
import markerIcon2x from "leaflet/dist/images/marker-icon-2x.png";
import markerIconSrc from "leaflet/dist/images/marker-icon.png";
import markerShadow from "leaflet/dist/images/marker-shadow.png";

// 修复 Vite 打包后 Leaflet 默认图标路径丢失的问题
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: markerIcon2x,
  iconUrl: markerIconSrc,
  shadowUrl: markerShadow
});

const props = defineProps({
  latitude: [String, Number],
  longitude: [String, Number]
});

const emit = defineEmits(["update:latitude", "update:longitude"]);
let map;
let marker;

const locating = ref(false);
const locateStatus = ref("");

const center = [29.563, 106.5516]; // 重庆市默认坐标

function validPoint() {
  const latRaw = props.latitude;
  const lngRaw = props.longitude;
  if (latRaw === "" || latRaw === null || latRaw === undefined) return false;
  if (lngRaw === "" || lngRaw === null || lngRaw === undefined) return false;
  const lat = Number(latRaw);
  const lng = Number(lngRaw);
  return Number.isFinite(lat) && Number.isFinite(lng) && lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180;
}

function setPoint(lat, lng, pan = false) {
  if (!map) return;
  if (marker) marker.setLatLng([lat, lng]);
  else {
    marker = L.marker([lat, lng], { draggable: true }).addTo(map);
    marker.on("dragend", event => {
      const point = event.target.getLatLng();
      setPoint(point.lat, point.lng);
    });
  }
  emit("update:latitude", Number(lat).toFixed(6));
  emit("update:longitude", Number(lng).toFixed(6));
  if (pan) map.panTo([lat, lng]);
}

function locateMe() {
  if (!("geolocation" in navigator)) {
    locateStatus.value = "浏览器不支持定位功能";
    return;
  }
  locating.value = true;
  locateStatus.value = "";
  navigator.geolocation.getCurrentPosition(
    position => {
      const { latitude: lat, longitude: lng } = position.coords;
      setPoint(lat, lng);
      map.setView([lat, lng], 14);
      locating.value = false;
    },
    error => {
      locating.value = false;
      switch (error.code) {
        case error.PERMISSION_DENIED:
          locateStatus.value = "定位权限被拒绝，请在浏览器设置中允许";
          break;
        case error.POSITION_UNAVAILABLE:
          locateStatus.value = "无法获取位置信息";
          break;
        case error.TIMEOUT:
          locateStatus.value = "定位超时，请检查网络或 GPS 信号";
          break;
        default:
          locateStatus.value = "定位失败，请手动点击地图选点";
      }
    },
    { enableHighAccuracy: true, timeout: 10000, maximumAge: 60000 }
  );
}

onMounted(async () => {
  await nextTick();
  const point = validPoint() ? [Number(props.latitude), Number(props.longitude)] : center;
  map = L.map("record-location-picker", { center: point, zoom: validPoint() ? 11 : 7 });
  const primary = L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19,
    attribution: "&copy; OpenStreetMap"
  });
  let failed = 0;
  primary.on("tileerror", () => {
    failed += 1;
    if (failed === 3) {
      map.removeLayer(primary);
      L.tileLayer("/api/map/tiles/esri/{z}/{x}/{y}", {
        maxZoom: 18,
        attribution: "Tiles &copy; Esri"
      }).addTo(map);
    }
  });
  primary.addTo(map);
  if (validPoint()) setPoint(point[0], point[1]);
  map.on("click", event => setPoint(event.latlng.lat, event.latlng.lng, true));
  setTimeout(() => map.invalidateSize(), 0);
});

watch(() => [props.latitude, props.longitude], () => {
  if (map && validPoint()) setPoint(Number(props.latitude), Number(props.longitude));
});

onBeforeUnmount(() => map?.remove());
</script>

<template>
  <section class="location-picker">
    <div class="location-picker-head">
      <div>
        <strong>地图选点</strong>
        <small>点击或拖动标记设置真实位置</small>
      </div>
      <div class="location-picker-actions">
        <output>{{ validPoint() ? `${longitude}, ${latitude}` : "尚未选择位置" }}</output>
        <button class="locate-button" type="button" :disabled="locating" @click="locateMe" title="获取当前定位">
          {{ locating ? "定位中..." : "📍 定位" }}
        </button>
      </div>
    </div>
    <div id="record-location-picker" class="location-picker-map"></div>
    <p v-if="locateStatus" class="locate-message">{{ locateStatus }}</p>
  </section>
</template>
