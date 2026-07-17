<script setup>
import { onLaunch, onShow } from "@dcloudio/uni-app";
import { initOfflineStore } from "./services/offline-db";
import { syncPendingRecords } from "./services/sync";
import { loadSettings } from "./services/settings";

let wasOffline = false;

onLaunch(() => {
  initOfflineStore().catch(() => {});
  uni.onNetworkStatusChange((res) => {
    const isOnline = res.networkType !== "none";
    if (isOnline && wasOffline) {
      const settings = loadSettings();
      if (settings.deviceToken) {
        syncPendingRecords().catch(() => {});
      }
    }
    wasOffline = !isOnline;
  });
});

onShow(() => {
  uni.$emit("app-visible");
});
</script>

<style>
page {
  min-height: 100%;
  background: #f4f7f3;
  color: #14251c;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
}

view,
text,
button,
input,
textarea,
picker {
  box-sizing: border-box;
}

button {
  margin: 0;
}

.page {
  min-height: 100vh;
  padding: 28rpx;
  padding-bottom: calc(28rpx + env(safe-area-inset-bottom));
}

.hero {
  padding: 36rpx 32rpx;
  border-radius: 28rpx;
  color: #fff;
  background: linear-gradient(135deg, #0c6b4f, #143f33);
  box-shadow: 0 18rpx 42rpx rgba(10, 77, 56, 0.18);
}

.eyebrow {
  color: rgba(255, 255, 255, 0.74);
  font-size: 24rpx;
  line-height: 1.4;
}

.title {
  display: block;
  margin-top: 10rpx;
  font-size: 42rpx;
  font-weight: 800;
  line-height: 1.18;
}

.subtitle {
  display: block;
  margin-top: 14rpx;
  color: rgba(255, 255, 255, 0.8);
  font-size: 26rpx;
  line-height: 1.55;
}

.card {
  margin-top: 24rpx;
  padding: 26rpx;
  border: 1rpx solid rgba(19, 83, 59, 0.1);
  border-radius: 20rpx;
  background: #fff;
  box-shadow: 0 10rpx 28rpx rgba(35, 60, 47, 0.07);
}

.section-title {
  display: block;
  margin-bottom: 18rpx;
  color: #173d30;
  font-size: 30rpx;
  font-weight: 750;
}

.field {
  margin-top: 20rpx;
}

.label {
  display: block;
  margin-bottom: 10rpx;
  color: #526259;
  font-size: 24rpx;
}

.input,
.textarea,
.select {
  width: 100%;
  min-height: 88rpx;
  padding: 0 22rpx;
  border: 1rpx solid #d7e2dc;
  border-radius: 16rpx;
  background: #fbfdfb;
  color: #16231d;
  font-size: 28rpx;
}

.textarea {
  min-height: 140rpx;
  padding-top: 20rpx;
  line-height: 1.45;
}

.row {
  display: flex;
  gap: 18rpx;
}

.row > .field {
  flex: 1;
}

.primary,
.secondary,
.ghost,
.danger {
  min-height: 88rpx;
  border-radius: 18rpx;
  font-size: 28rpx;
  font-weight: 750;
  line-height: 88rpx;
}

.primary {
  color: #fff;
  background: #0f7b58;
}

.secondary {
  color: #0f6f51;
  border: 1rpx solid #b8d4c9;
  background: #edf8f3;
}

.ghost {
  color: #496158;
  border: 1rpx solid #dce7e1;
  background: #fff;
}

.danger {
  color: #9d2f2f;
  border: 1rpx solid #efc8c8;
  background: #fff7f7;
}

.button-row {
  display: flex;
  gap: 18rpx;
  margin-top: 24rpx;
}

.button-row button {
  flex: 1;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16rpx;
  margin-top: 20rpx;
}

.metric {
  padding: 18rpx;
  border-radius: 18rpx;
  background: #f1f6f3;
}

.metric-value {
  display: block;
  color: #143f33;
  font-size: 34rpx;
  font-weight: 800;
}

.metric-label {
  display: block;
  margin-top: 6rpx;
  color: #66766f;
  font-size: 22rpx;
}

.muted {
  color: #6a7b72;
  font-size: 24rpx;
  line-height: 1.5;
}

.pill {
  display: inline-flex;
  align-items: center;
  min-height: 48rpx;
  padding: 0 18rpx;
  border-radius: 999rpx;
  background: #edf8f3;
  color: #0c6b4f;
  font-size: 23rpx;
  font-weight: 700;
}

.empty {
  padding: 56rpx 24rpx;
  text-align: center;
  color: #718279;
  font-size: 26rpx;
}

.list-item {
  padding: 22rpx 0;
  border-top: 1rpx solid #edf2ef;
}

.list-title {
  display: block;
  color: #1d342a;
  font-size: 28rpx;
  font-weight: 750;
  line-height: 1.35;
}

.list-meta {
  display: block;
  margin-top: 8rpx;
  color: #677870;
  font-size: 24rpx;
  line-height: 1.4;
}
</style>
