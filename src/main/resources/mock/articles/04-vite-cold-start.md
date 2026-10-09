## 优化前的体检

```bash
# 查看各阶段耗时
npx vite --debug transform
```

结论：80% 的时间花在了 **依赖预构建** 和 **插件 transform** 上。

## 一、锁定依赖预构建范围

```js
// vite.config.js
export default defineConfig({
  optimizeDeps: {
    // 明确列出需要预构建的重型依赖，避免 Vite 反复扫描
    include: ['vue', 'vue-router', 'element-plus', 'axios', 'echarts']
  }
})
```

## 二、Element Plus 按需引入

全量引入会把 200+ 组件都拉进来，改成按需后体积下降明显：

```js
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

plugins: [
  AutoImport({ resolvers: [ElementPlusResolver()] }),
  Components({ resolvers: [ElementPlusResolver()] })
]
```

## 三、关掉不必要的 sourcemap 与压缩

```js
build: {
  sourcemap: false,
  // 生产环境用 esbuild 压缩，比 terser 快一个数量级
  minify: 'esbuild'
}
```

## 四、优化效果对比

| 指标 | 优化前 | 优化后 |
| --- | --- | --- |
| 冷启动 | 30.2s | 6.1s |
| HMR | 1.8s | 0.3s |
| 生产构建 | 96s | 31s |

> 经验：先用数据定位瓶颈，再动手优化，不要凭感觉删插件。
