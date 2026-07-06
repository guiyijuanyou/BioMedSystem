<script setup>
import { nextTick, onBeforeUnmount, onMounted, watch } from "vue";
import L from "leaflet";

const props = defineProps({
  latitude: [String, Number],
  longitude: [String, Number]
});

const emit = defineEmits(["update:latitude", "update:longitude"]);
let map;
let marker;

const center = [29.563, 106.5516];

function validPoint() {
  const lat = Number(props.latitude);
  const lng = Number(props.longitude);
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
      <output>{{ validPoint() ? `${longitude}, ${latitude}` : "尚未选择位置" }}</output>
    </div>
    <div id="record-location-picker" class="location-picker-map"></div>
  </section>
</template>
