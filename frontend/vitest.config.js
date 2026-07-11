import { fileURLToPath, URL } from "node:url";
import { defineConfig } from "vitest/config";
import vue from "@vitejs/plugin-vue";
import { templateCompilerOptions } from "@tresjs/core";

export default defineConfig({
  plugins: [vue({ ...templateCompilerOptions })],
  test: {
    environment: "jsdom",
    globals: true
  },
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url))
    }
  }
});
