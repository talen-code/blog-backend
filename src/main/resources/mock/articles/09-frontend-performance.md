## 一、先量化，再优化

```js
// 用 PerformanceObserver 采集真实用户指标
new PerformanceObserver((list) => {
  for (const entry of list.getEntries()) {
    console.log(entry.name, entry.startTime)
  }
}).observe({ entryTypes: ['largest-contentful-paint', 'paint'] })
```

关注三个核心指标：

- **LCP**（最大内容绘制）< 2.5s
- **FID / INP**（交互延迟）< 200ms
- **CLS**（布局偏移）< 0.1

## 二、减少资源体积

1. 路由懒加载：`const Home = () => import('@/views/front/Home.vue')`
2. 组件库按需引入（Element Plus 用 unplugin 方案）
3. 图片转 WebP，使用 `loading="lazy"`
4. 大依赖换轻量替代（moment → dayjs，体积 232KB → 7KB）

## 三、减少请求数

```html
<!-- 关键：preconnect 提前建立连接 -->
<link rel="preconnect" href="https://api.example.com">
<!-- 首屏不需要的模块用 prefetch，空闲时加载 -->
<link rel="prefetch" href="/js/detail.chunk.js">
```

## 四、减少渲染阻塞

- CSS 放 `<head>`，JS 加 `defer`
- 长列表用虚拟滚动
- 骨架屏占位，避免白屏焦虑
- 高频事件用防抖节流（搜索框 300ms 防抖）

## 五、优化前后对比

| 指标 | 优化前 | 优化后 |
| --- | --- | --- |
| 首屏 LCP | 3.2s | 1.4s |
| 首包体积 | 1.8MB | 620KB |
| 请求数 | 42 | 17 |

> 原则：优化前一定要有基线数据，否则无法判断收益。
