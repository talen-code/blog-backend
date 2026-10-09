## 一、多阶段构建 Dockerfile

```dockerfile
# ---------- 阶段一：构建 ----------
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci --registry=https://registry.npmmirror.com
COPY . .
RUN npm run build

# ---------- 阶段二：运行 ----------
FROM nginx:1.25-alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

最终镜像只有 20MB 左右，因为不包含 node_modules 和源码。

## 二、history 路由必须配置回退

Vue Router 的 history 模式刷新 `/article/1` 时，Nginx 会去找这个物理文件而 404：

```nginx
location / {
    root /usr/share/nginx/html;
    index index.html;
    # 关键：找不到文件就回退到 index.html
    try_files $uri $uri/ /index.html;
}
```

## 三、接口反向代理

```nginx
location /api/ {
    proxy_pass http://backend:8080/api/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}
```

## 四、开启 gzip

```nginx
gzip on;
gzip_min_length 1k;
gzip_comp_level 6;
gzip_types text/css application/javascript application/json image/svg+xml;
```

## 五、静态资源缓存策略

Vite 打包出的文件名带内容哈希，可以放心设置长期缓存：

```nginx
location /assets/ {
    expires 1y;
    add_header Cache-Control "public, immutable";
}
```

> `index.html` 千万不要设长缓存，否则发版后用户拿不到新版本。
