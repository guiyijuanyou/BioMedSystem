import { defineConfig } from "vite";
import uniModule from "@dcloudio/vite-plugin-uni";

const uni = uniModule.default || uniModule;

export default defineConfig({
  plugins: [uni()],
  server: {
    host: "0.0.0.0",
    port: 5174,
    proxy: {
      "/api": {
        target: "http://localhost:8088",
        changeOrigin: true
      }
    }
  }
});
