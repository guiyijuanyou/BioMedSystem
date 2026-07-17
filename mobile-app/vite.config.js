import { defineConfig } from "vite";
import uniModule from "@dcloudio/vite-plugin-uni";
import { fileURLToPath } from "url";
import path from "path";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const uni = uniModule.default || uniModule;

export default defineConfig({
  resolve: {
    alias: {
      vue: path.resolve(__dirname, "node_modules/@dcloudio/uni-h5-vue"),
    },
  },
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
