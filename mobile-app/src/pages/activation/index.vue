<script setup>
import { reactive } from "vue";
import { login } from "../../services/api";
import { loadSettings, saveSettings } from "../../services/settings";

const settings = reactive(loadSettings());
const account = reactive({
  username: "",
  password: ""
});

function saveDevice() {
  saveSettings({
    apiBase: settings.apiBase.trim(),
    deviceToken: settings.deviceToken.trim(),
    deviceCode: settings.deviceCode.trim()
  });
  uni.showToast({ title: "设备配置已保存", icon: "success" });
}

async function submitLogin() {
  if (!account.username || !account.password) {
    uni.showToast({ title: "请输入账号密码", icon: "none" });
    return;
  }
  try {
    const result = await login(account.username, account.password);
    Object.assign(settings, saveSettings({
      accountToken: result.token,
      accountName: result.name,
      accountRole: result.role
    }));
    account.password = "";
    uni.showToast({ title: "登录成功", icon: "success" });
  } catch (error) {
    uni.showToast({ title: error.message, icon: "none" });
  }
}

function logout() {
  Object.assign(settings, saveSettings({
    accountToken: "",
    accountName: "",
    accountRole: ""
  }));
  uni.showToast({ title: "已退出账号", icon: "none" });
}
</script>

<template>
  <view class="page">
    <view class="hero">
      <text class="eyebrow">设备与账号</text>
      <text class="title">移动端激活</text>
      <text class="subtitle">设备令牌用于离线采集同步，账号登录用于查看网页版业务数据。</text>
    </view>

    <view class="card">
      <text class="section-title">服务器</text>
      <view class="field">
        <text class="label">接口地址</text>
        <input v-model="settings.apiBase" class="input" placeholder="例如 http://172.20.10.2:8088；H5调试可留空" />
      </view>
      <view class="field">
        <text class="label">设备编号</text>
        <input v-model="settings.deviceCode" class="input" placeholder="例如 APP-001" />
      </view>
      <view class="field">
        <text class="label">设备令牌</text>
        <textarea v-model="settings.deviceToken" class="textarea" placeholder="管理员登记设备后生成的 token" />
      </view>
      <button class="primary" @tap="saveDevice">保存设备配置</button>
    </view>

    <view class="card">
      <text class="section-title">账号登录</text>
      <view v-if="settings.accountToken">
        <text class="list-title">{{ settings.accountName || "已登录账号" }}</text>
        <text class="list-meta">角色：{{ settings.accountRole || "未知" }}</text>
        <view class="button-row">
          <button class="secondary" @tap="uni.switchTab({ url: '/pages/dashboard/index' })">进入工作台</button>
          <button class="danger" @tap="logout">退出</button>
        </view>
      </view>
      <view v-else>
        <view class="field">
          <text class="label">账号</text>
          <input v-model="account.username" class="input" placeholder="admin / teacher / student" />
        </view>
        <view class="field">
          <text class="label">密码</text>
          <input v-model="account.password" class="input" password placeholder="请输入密码" />
        </view>
        <button class="primary" @tap="submitLogin">登录网页版账号</button>
      </view>
    </view>
  </view>
</template>
