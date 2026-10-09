## 常用属性速查

```css
.container {
  display: grid;
  /* 自适应列数：最小 240px，能放几列放几列 */
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
}
```

## 用 grid-template-areas 做页面骨架

```css
.layout {
  display: grid;
  grid-template-areas:
    "header header"
    "sidebar main"
    "footer footer";
  grid-template-columns: 240px 1fr;
  grid-template-rows: 64px 1fr 60px;
}
```

> 本文尚未完成，仅用于演示后台「草稿」状态。
