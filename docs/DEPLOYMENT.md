# 部署要求与部署指南

> 项目：个人博客系统（前后端分离） · 适用版本：v1.0.0 · 更新日期：2025

本文档描述从零把本项目跑起来所需的**全部环境要求**，以及**开发环境 / 生产环境**的完整部署步骤。

---

## 目录

- [一、环境要求](#一环境要求)
- [二、依赖清单](#二依赖清单)
- [三、数据库准备](#三数据库准备)
- [四、配置说明](#四配置说明)
- [五、开发环境部署](#五开发环境部署)
- [六、生产环境部署](#六生产环境部署)
- [七、上线前安全检查清单](#七上线前安全检查清单)
- [八、数据备份与恢复](#八数据备份与恢复)
- [九、升级与回滚](#九升级与回滚)
- [十、故障排查](#十故障排查)
- [十一、部署验收清单](#十一部署验收清单)

---

## 一、环境要求

### 1.1 软件环境（必需）

| 组件 | 版本要求 | 本项目验证版本 | 检查命令 | 说明 |
|---|---|---|---|---|
| **JDK** | **11**（必需） | OpenJDK 11.0.30 | `java -version` | 代码使用了 `List.of` 等 Java 9+ API，**JDK 8 无法编译**；Spring Boot 2.7 支持 8~19 |
| **Maven** | 3.6+ | 3.9.12 | `mvn -v` | 需确认输出的 Java version 是 11 |
| **MySQL** | 5.7+ / 8.0+ | 8.0.46 | `SELECT VERSION();` | 字符集必须 utf8mb4 |
| **Node.js** | 16+ | v24.13.1 | `node -v` | 推荐 18 / 20 / 22 LTS |
| **npm** | 8+ | 11.8.0 | `npm -v` | 项目未使用 pnpm/yarn，但用它们也能装 |
| 浏览器 | Chrome / Edge / Firefox 最新版 | — | — | 需支持 ES2020、CSS Grid |

> ⚠️ **JDK 版本是最容易踩的坑**。如果机器上有多个 JDK，运行 Maven 前务必确认：
>
> ```bash
> mvn -v        # 看 "Java version:" 一行
> ```
>
> 不是 11 时先切换（以 Homebrew 安装的 JDK 11 为例）：
>
> ```bash
> # macOS
> export JAVA_HOME=/opt/homebrew/opt/openjdk@11/libexec/openjdk.jdk/Contents/Home
> # Linux
> export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
> # 只对单条命令生效
> JAVA_HOME=/path/to/jdk-11 mvn clean spring-boot:run
> ```
>
> 若报 `Fatal error compiling: invalid target release: 11`，就是这个原因。

### 1.2 硬件要求

| 场景 | CPU | 内存 | 磁盘 | 说明 |
|---|---|---|---|---|
| 开发（本机） | 2 核 | 4 GB | 2 GB 可用 | 同时跑 IDEA + MySQL + Node 会比较吃内存 |
| 生产（最低） | 1 核 | 2 GB | 20 GB | 单机跑 jar + Nginx + MySQL |
| 生产（推荐） | 2 核 | 4 GB | 40 GB+ | JVM 建议 `-Xmx512m`，其余留给 MySQL 与系统 |

> 项目本身很轻：后端 jar 约 25 MB，前端构建产物约 3.9 MB，空载内存占用约 300 MB。

### 1.3 端口要求

| 端口 | 用途 | 是否可改 | 修改位置 |
|---|---|---|---|
| **8082** | 后端 API | ✅ | `blog-backend/src/main/resources/application.yml` → `server.port` |
| **5173** | 前端开发服务器 | ✅ | `blog-frontend/vite.config.js` → `server.port` |
| **3306** | MySQL | ✅ | `application.yml` → `spring.datasource.url` |
| 80 / 443 | 生产环境 Nginx | ✅ | Nginx 配置 |

> **改后端端口时，必须同步修改前端代理**，否则前端所有请求 404：
> `blog-frontend/vite.config.js` → `server.proxy['/api'].target`

### 1.4 网络要求

| 方向 | 目标 | 是否必需 | 说明 |
|---|---|---|---|
| 出站 | Maven Central / 阿里云镜像 | 首次构建必需 | 下载后端依赖（约 60 MB） |
| 出站 | npm registry | 首次构建必需 | 下载前端依赖（约 200 MB） |
| 出站 | `picsum.photos` | 可选 | 文章封面占位图 |
| 出站 | `api.dicebear.com` | 可选 | 用户头像 |

> 后两项是**演示用的外链图片**，内网/离线环境会加载失败（前端已做失败兜底，不会出现破图）。
> 生产环境建议替换为本地图片或自有 CDN。

---

## 二、依赖清单

### 2.1 后端依赖（Maven）

| 依赖 | 版本 | 用途 |
|---|---|---|
| `spring-boot-starter-parent` | 2.7.18 | 版本管理父工程 |
| `spring-boot-starter-web` | 2.7.18 | Spring MVC + 内嵌 Tomcat + Jackson |
| `spring-boot-starter-validation` | 2.7.18 | `@Valid` 参数校验（Hibernate Validator） |
| `mybatis-plus-boot-starter` | 3.5.5 | ORM + 分页插件（**不是** mybatis-plus-spring-boot3-starter） |
| `com.mysql:mysql-connector-j` | 8.0.33 | MySQL 驱动（Boot 2.7.9+ 的新坐标） |
| `io.jsonwebtoken:jjwt-api / impl / jackson` | 0.12.5 | JWT 签发与解析 |
| `org.projectlombok:lombok` | 1.18.30 | `@Data` / `@Slf4j`（编译期，不打包） |
| `spring-boot-starter-test` | 2.7.18 | 测试（test scope） |

> 没有任何连接池以外的中间件依赖：**不需要 Redis、不需要 MQ、不需要 Nacos**。

### 2.2 前端依赖（npm）

| 依赖 | 版本 | 用途 |
|---|---|---|
| `vue` | ^3.5.13 | 全部使用 `<script setup>` 组合式 API |
| `vue-router` | ^4.4.5 | history 模式 + 全局前置守卫 |
| `element-plus` | ^2.8.8 | UI 组件库（全量引入） |
| `@element-plus/icons-vue` | ^2.3.1 | 图标 |
| `axios` | ^1.7.7 | HTTP 请求（统一封装拦截器） |
| `md-editor-v3` | ^5.1.1 | Markdown 编辑器 + 预览渲染（自带 markdown-it 与代码高亮） |
| `vite` (dev) | ^5.4.11 | 构建工具 |
| `@vitejs/plugin-vue` (dev) | ^5.2.1 | Vue 单文件组件支持 |

---

## 三、数据库准备

### 3.1 执行建库建表脚本

数据库名为 **`blog_db`**，由脚本自动创建，**应用启动时不会自动建库建表**。

按顺序执行 `blog-backend/sql/` 下的两个文件：

1. `01-schema.sql` —— 创建数据库 `blog_db` + 8 张表
2. `02-data.sql` —— 灌入初始数据（5 用户 / 6 分类 / 12 标签 / 12 文章 / 16 评论）

#### 方式 A：图形化工具

用 Navicat / DataGrip / IDEA Database 连接 `127.0.0.1:3306`，依次打开并执行这两个文件。

> ### ⚠️ 必须用「执行 SQL 脚本 / 运行 SQL 文件」
>
> | 工具 | ✅ 正确操作 | ❌ 错误操作 |
> |---|---|---|
> | Navicat | 右键数据库 → **运行 SQL 文件** | 全选后点「运行」 |
> | DBeaver | **Alt+X**（Execute SQL Script） | 全选后 **Ctrl+Enter**（Execute Statement） |
> | IDEA | 选中文件右键 → **Run** | 编辑器里全选后 Ctrl+Enter |
>
> 用错会报 **1064 语法错误**，且报错位置莫名其妙地停在文件开头（例如
> `near 'CREATE DATABASE ...'`）。原因是**整个文件被当成一条语句**发给了 MySQL，
> MySQL 解析完第一条就撞上了第二条。**这不是脚本的问题，是执行方式的问题。**

#### 方式 B：命令行

```bash
cd blog-backend/sql
mysql -h127.0.0.1 -P3306 -uroot -p < 01-schema.sql
mysql -h127.0.0.1 -P3306 -uroot -p < 02-data.sql
```

#### 方式 C：JDBC 一次性执行（无 mysql 客户端时）

任意能跑 Java 的环境都可以：

```bash
java -cp "mysql-connector-j-8.0.33.jar:." YourRunner \
  "jdbc:mysql://127.0.0.1:3306/?allowMultiQueries=true&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true" \
  root <password> 01-schema.sql 02-data.sql
```

### 3.2 表结构清单

| 表名 | 说明 | 关键索引 / 约束 |
|---|---|---|
| `t_user` | 用户 | `uk_username` 用户名唯一 |
| `t_category` | 文章分类 | `uk_name` 分类名唯一，`idx_sort` |
| `t_tag` | 文章标签 | `uk_name` 标签名唯一 |
| `t_article` | 文章（含 Markdown 正文 `LONGTEXT`） | `idx_status_top_create` 覆盖前台列表查询 |
| `t_article_tag` | 文章-标签关联（多对多） | `uk_article_tag(article_id, tag_id)` 防重复打标 |
| `t_article_like` | 点赞记录 | `uk_article_user(article_id, user_id)` 防重复点赞 |
| `t_comment` | 评论（两级，`parent_id` 自关联） | `idx_article_id(article_id, status, create_time)` |
| `t_user_token` | JWT 令牌白名单 | `uk_token` 支持登出即失效 |

> **刻意没有建物理外键**（互联网项目常见做法：约束放在应用层，
> 避免分库分表、数据迁移、批量导入时被外键卡住）。
> 级联删除与"分类下有文章不允许删除"等规则由 Service 层显式实现。

### 3.3 验证是否执行成功

```sql
USE blog_db;

SELECT
  (SELECT COUNT(*) FROM t_user)         AS 用户数,     -- 期望 5
  (SELECT COUNT(*) FROM t_category)     AS 分类数,     -- 期望 6
  (SELECT COUNT(*) FROM t_tag)          AS 标签数,     -- 期望 12
  (SELECT COUNT(*) FROM t_article)      AS 文章数,     -- 期望 12
  (SELECT COUNT(*) FROM t_article WHERE status = 1) AS 已发布,  -- 期望 10
  (SELECT COUNT(*) FROM t_article_tag)  AS 标签关联数, -- 期望 28
  (SELECT COUNT(*) FROM t_comment)      AS 评论数;     -- 期望 16
```

再抽查一条中文，确认没有乱码：

```sql
SELECT id, username, nickname FROM t_user WHERE id = 1;
-- 期望：1 | admin | 超级管理员
```

若中文显示为 `????` 或乱码，说明客户端的**连接字符集**不是 utf8mb4，请在连接设置里改为 `utf8mb4`。

---

## 四、配置说明

### 4.1 后端 `application.yml`

```yaml
server:
  port: 8082                    # ① 后端端口

spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:127.0.0.1}:${DB_PORT:3306}/${DB_NAME:blog_db}?useUnicode=true&characterEncoding=utf8
         &useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
         &rewriteBatchedStatements=true
    username: ${DB_USERNAME:root}      # ② 数据库账号（环境变量优先）
    password: ${DB_PASSWORD:abc123}    # ③ 数据库密码（环境变量优先）
    hikari:
      maximum-pool-size: 10     # 连接池上限，按数据库承载能力调整

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl   # ④ 生产请关掉

blog:
  jwt:
    secret: ${JWT_SECRET:blog-mock-jwt-secret-key-please-change-in-production-2024}   # ⑤ 生产必须改
    expire-hours: 24            # token 有效期（小时）
```

> **`${环境变量:默认值}` 语法**：本地开发直接用默认值（开箱即用），
> 生产环境通过环境变量覆盖，**不需要修改任何代码或配置文件**。
> 这也是本项目能安全开源的原因 —— 仓库里不含任何真实密钥。

**各配置项说明：**

| 编号 | 配置项 | 环境变量 | 生产环境要求 |
|---|---|---|---|
| ① | `server.port` | — | 按需修改，改了记得同步前端代理 |
| ② | `DB_USERNAME` | ✅ | **必须**改用最小权限账号 |
| ③ | `DB_PASSWORD` | ✅ | **必须**用环境变量注入，不要写进文件 |
| ④ | `log-impl` | — | **必须**改为 `org.apache.ibatis.logging.nologging.NoLoggingImpl`，否则每条 SQL 都打日志，性能与磁盘都吃不消 |
| ⑤ | `JWT_SECRET` | ✅ | **必须**改成随机串，长度 ≥ 32 字节（HS256 要求） |

**可用的环境变量一览：**

| 变量 | 默认值 | 说明 |
|---|---|---|
| `DB_HOST` | `127.0.0.1` | 数据库地址 |
| `DB_PORT` | `3306` | 数据库端口 |
| `DB_NAME` | `blog_db` | 数据库名 |
| `DB_USERNAME` | `root` | 数据库账号 |
| `DB_PASSWORD` | `abc123` | 数据库密码 |
| `JWT_SECRET` | 演示用固定串 | JWT 签名密钥（≥32 字节） |
| `SPRING_PROFILES_ACTIVE` | 无 | 激活的 profile |

**JDBC URL 参数说明：**

| 参数 | 作用 |
|---|---|
| `useUnicode=true&characterEncoding=utf8` | 解决中文乱码 |
| `useSSL=false` | 本地/内网关闭 SSL，避免握手告警；公网数据库请开启 |
| `serverTimezone=Asia/Shanghai` | 不设置时 MySQL 8 驱动可能报时区错误 |
| `allowPublicKeyRetrieval=true` | MySQL 8 默认 `caching_sha2_password` 认证需要 |
| `rewriteBatchedStatements=true` | 批量插入性能优化 |

**环境变量覆盖示例（推荐生产使用）：**

```bash
export SPRING_DATASOURCE_USERNAME=blog_user
export SPRING_DATASOURCE_PASSWORD='强密码'
export BLOG_JWT_SECRET='随机生成的32字节以上字符串'
java -jar blog-backend-1.0.0.jar
```

Spring Boot 的松散绑定会自动把环境变量映射到配置项，无需改代码。

### 4.2 前端配置

**`blog-frontend/.env.development`**

```ini
VITE_API_BASE_URL=/api          # 走 Vite 代理，保持相对路径即可
VITE_API_TIMEOUT=15000          # 请求超时（毫秒）
VITE_APP_TITLE=个人博客系统      # 页面标题
```

**`blog-frontend/vite.config.js`（开发代理）**

```js
server: {
  port: 5173,
  proxy: {
    '/api': {
      target: 'http://localhost:8082',   // ← 后端地址，改后端端口时同步改这里
      changeOrigin: true
    }
  }
}
```

> 生产环境不需要 Vite 代理，改由 Nginx 把 `/api` 转发到后端（见 6.3）。

---

## 五、开发环境部署

### 步骤 1：准备数据库

见[第三章](#三数据库准备)，执行两个 SQL 脚本。

### 步骤 2：启动后端（端口 8082）

```bash
cd blog-backend
mvn clean spring-boot:run
```

**成功标志：**

```
==========================================================
  博客后端启动成功！（JDK 11 + Spring Boot 2.7.18）
  接口地址  : http://localhost:8082/api
  数据库    : mysql://127.0.0.1:3306/blog_db
  管理员账号: admin    / 123456
  普通用户  : zhangsan / 123456
  数据已持久化到 MySQL，重启不再丢失
==========================================================
```

**验证：**

```bash
curl "http://localhost:8082/api/articles?page=1&size=3"
# 期望返回 {"code":200,...,"total":10}
```

同时控制台会打印 MyBatis 执行的 SQL（`==>  Preparing: SELECT ...`），
这是确认"操作确实落到了数据库"最直观的方式。

### 步骤 3：启动前端（端口 5173）

**另开一个终端：**

```bash
cd blog-frontend
npm install          # 首次执行，约 1~2 分钟
npm run dev
```

**成功标志：**

```
  VITE v5.4.21  ready in 197 ms
  ➜  Local:   http://localhost:5173/
```

浏览器打开 <http://localhost:5173> 即可。

### 步骤 4：跑一遍回归测试（可选但推荐）

```bash
python3 scripts/e2e-smoke-test.py
# 覆盖 99 项断言：分页/筛选/权限/点赞/评论/CRUD/异常处理
```

> 注意：脚本会**真实写入数据库**（注册用户、发评论等）。
> 跑完想恢复初始数据，重跑一次 `02-data.sql` 即可。

---

## 六、生产环境部署

### 6.1 后端打包与运行

**打包：**

```bash
cd blog-backend
mvn clean package -DskipTests
# 产物：target/blog-backend-1.0.0.jar（可执行 jar，内嵌 Tomcat）
```

**直接运行：**

```bash
java -jar target/blog-backend-1.0.0.jar \
  --spring.profiles.active=prod \
  --server.port=8082
```

**生产环境 JVM 参数建议：**

```bash
java -Xms256m -Xmx512m -XX:+UseG1GC \
     -Dfile.encoding=UTF-8 \
     -Duser.timezone=Asia/Shanghai \
     -jar blog-backend-1.0.0.jar
```

**注册为 systemd 服务（推荐）：**

`/etc/systemd/system/blog-backend.service`

```ini
[Unit]
Description=Blog Backend (Spring Boot)
After=network.target mysql.service
Wants=mysql.service

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/blog
Environment="JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64"
Environment="SPRING_DATASOURCE_USERNAME=blog_user"
Environment="SPRING_DATASOURCE_PASSWORD=改成你的强密码"
Environment="BLOG_JWT_SECRET=改成你的32字节以上随机串"
ExecStart=/usr/lib/jvm/java-11-openjdk-amd64/bin/java \
          -Xms256m -Xmx512m -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai \
          -jar /opt/blog/blog-backend-1.0.0.jar
SuccessExitStatus=143
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now blog-backend
sudo systemctl status blog-backend
sudo journalctl -u blog-backend -f          # 看日志
```

### 6.2 前端构建

```bash
cd blog-frontend
npm install
npm run build
# 产物：dist/（约 3.9 MB，含 index.html 与 assets/）
```

把 `dist/` 上传到服务器，例如 `/opt/blog/web/`：

```bash
scp -r dist/* user@server:/opt/blog/web/
```

### 6.3 Nginx 完整配置

`/etc/nginx/conf.d/blog.conf`

```nginx
server {
    listen       80;
    server_name  blog.example.com;

    # 开启 gzip，静态资源体积可再降 60% 以上
    gzip on;
    gzip_min_length 1k;
    gzip_comp_level 6;
    gzip_types text/css application/javascript application/json image/svg+xml text/plain;
    gzip_vary on;

    root /opt/blog/web;
    index index.html;

    # ---------- ① 前端静态资源 ----------
    location / {
        # 关键：history 路由回退。少了这行，刷新 /article/1 会 404
        try_files $uri $uri/ /index.html;
    }

    # ---------- ② 带内容哈希的静态资源，可长期缓存 ----------
    location /assets/ {
        expires 1y;
        add_header Cache-Control "public, immutable";
        access_log off;
    }

    # ---------- ③ index.html 绝不能长缓存，否则发版后用户拿到旧版本 ----------
    location = /index.html {
        add_header Cache-Control "no-cache, no-store, must-revalidate";
        add_header Pragma "no-cache";
        expires 0;
    }

    # ---------- ④ 接口反向代理 ----------
    location /api/ {
        proxy_pass http://127.0.0.1:8082/api/;
        proxy_http_version 1.1;
        proxy_set_header Host              $host;
        proxy_set_header X-Real-IP         $remote_addr;
        proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 10s;
        proxy_read_timeout    30s;
    }

    # ---------- ⑤ 安全响应头 ----------
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-Frame-Options        "SAMEORIGIN" always;
    add_header Referrer-Policy        "strict-origin-when-cross-origin" always;
}
```

```bash
sudo nginx -t && sudo systemctl reload nginx
```

**HTTPS（可选，用 certbot 一条命令）：**

```bash
sudo certbot --nginx -d blog.example.com
```

> `proxy_pass` 结尾的 `/api/` 不能省，且**不能**把前缀 rewrite 掉 ——
> 后端 Controller 路径本身就带 `/api`。

### 6.4 数据库最小权限账号

**不要用 root 跑应用**。创建专用账号：

```sql
CREATE USER 'blog_user'@'127.0.0.1' IDENTIFIED BY '你的强密码';
GRANT SELECT, INSERT, UPDATE, DELETE ON blog_db.* TO 'blog_user'@'127.0.0.1';
FLUSH PRIVILEGES;
```

> 应用只需要 CRUD 权限，**不需要** `DROP` / `ALTER` / `CREATE`，
> 建表由 DBA 执行 `01-schema.sql` 完成。

---

## 七、上线前安全检查清单

| # | 检查项 | 默认值（开发） | 生产要求 |
|---|---|---|---|
| 1 | JWT 密钥 | `${JWT_SECRET:演示串}` | 用 `openssl rand -base64 48` 生成随机串，通过环境变量注入 |
| 2 | 数据库账号 | `root` | 专用最小权限账号 |
| 3 | 数据库密码 | `${DB_PASSWORD:abc123}` 默认值 | 通过环境变量注入，**默认值仅用于本地演示** |
| 4 | 密码摘要算法 | MD5（无盐） | 换成 BCrypt（引入 `spring-security-crypto`） |
| 5 | CORS 配置 | `allowedOriginPatterns("*")` | 收紧为具体域名 |
| 6 | SQL 日志 | `StdOutImpl` 全打印 | `NoLoggingImpl` |
| 7 | 前端 sourcemap | 已关闭 | 保持关闭 |
| 8 | 错误信息 | 兜底异常返回模糊提示 | 保持（不要暴露堆栈） |
| 9 | 数据库备份 | 无 | 配置定时备份（见第八章） |
| 10 | HTTPS | 无 | 必须开启 |
| 11 | 管理员密码 | `admin / 123456` | **首次登录后立即修改** |
| 12 | `t_user_token` 清理 | 登录时顺带清理过期记录 | 建议加定时任务或改 Redis |

---

## 八、数据备份与恢复

### 8.1 备份

```bash
# 全量备份（结构 + 数据）
mysqldump -h127.0.0.1 -uroot -p \
  --single-transaction --default-character-set=utf8mb4 \
  blog_db > /backup/blog_db_$(date +%F_%H%M).sql

# 只备份数据（结构由 01-schema.sql 管理）
mysqldump -h127.0.0.1 -uroot -p --no-create-info blog_db > /backup/blog_data_$(date +%F).sql
```

### 8.2 定时任务

```bash
# crontab -e，每天凌晨 3 点备份并保留 30 天
0 3 * * * mysqldump -uroot -p'密码' --single-transaction --default-character-set=utf8mb4 blog_db | gzip > /backup/blog_db_$(date +\%F).sql.gz && find /backup -name "blog_db_*.sql.gz" -mtime +30 -delete
```

### 8.3 恢复

```bash
mysql -h127.0.0.1 -uroot -p blog_db < /backup/blog_db_2025-01-01_0300.sql
```

### 8.4 恢复出厂设置

想清空所有业务数据、回到项目初始状态，只需重跑初始化脚本：

```sql
-- 图形化工具里「运行 SQL 文件」执行
blog-backend/sql/02-data.sql
```

> `02-data.sql` 会先 `DELETE` 全部业务数据再重新插入，**幂等**，可反复执行。
> `01-schema.sql` 会 `DROP TABLE` 后重建，**会丢数据**，谨慎使用。

---

## 九、升级与回滚

### 9.1 升级步骤

```bash
# 1. 备份数据库（必须）
mysqldump -uroot -p --single-transaction blog_db > /backup/before_upgrade.sql

# 2. 拉取新代码
cd /opt/blog/src && git pull

# 3. 如有表结构变更，执行新的迁移脚本（本项目结构由 01-schema.sql 管理）

# 4. 重新打包
cd blog-backend  && mvn clean package -DskipTests
cd blog-frontend && npm install && npm run build

# 5. 停服 → 替换产物 → 启服
sudo systemctl stop blog-backend
cp blog-backend/target/blog-backend-*.jar /opt/blog/
sudo cp -r blog-frontend/dist/* /opt/blog/web/
sudo systemctl start blog-backend

# 6. 验证
curl http://127.0.0.1:8082/api/articles?page=1&size=1
```

### 9.2 回滚

```bash
# 1. 换回旧 jar
sudo systemctl stop blog-backend
cp /opt/blog/backup/blog-backend-1.0.0.jar /opt/blog/
sudo systemctl start blog-backend

# 2. 前端回滚
sudo rm -rf /opt/blog/web/* && sudo cp -r /opt/blog/backup/web/* /opt/blog/web/

# 3. 如果本次升级有表结构变更，恢复数据库
mysql -uroot -p blog_db < /backup/before_upgrade.sql
```

> 建议每次发布前把旧 jar 与前端产物打上版本号归档，回滚时直接替换。

---

## 十、故障排查

| 现象 | 原因 | 解决 |
|---|---|---|
| `Fatal error compiling: invalid target release: 11` | Maven 用的不是 JDK 11 | 设置 `JAVA_HOME` 指向 JDK 11 |
| `'dependencies.dependency.version' for mysql:mysql-connector-java is missing` | MySQL 驱动坐标写错 | Boot 2.7.9+ 必须用 `com.mysql:mysql-connector-j` |
| `Unknown database 'blog_db'` | 没执行 `01-schema.sql` | 先建库建表 |
| `Access denied for user 'root'@'localhost'` | 账号或密码不对 | 检查 `application.yml`；注意 `localhost` 与 `127.0.0.1` 可能是不同授权 |
| `Public Key Retrieval is not allowed` | JDBC URL 缺少参数 | 加 `allowPublicKeyRetrieval=true` |
| `The server time zone value ... is unrecognized` | 时区未指定 | JDBC URL 加 `serverTimezone=Asia/Shanghai` |
| **1064 语法错误**，报错停在脚本开头 | 用「执行当前语句」跑了整个文件 | 改用「执行 SQL 脚本 / 运行 SQL 文件」 |
| 中文乱码 `???` | 客户端连接字符集不对 | 库/表/连接三处统一 utf8mb4 |
| 前端所有请求 404 | 后端没启动，或代理端口不一致 | 检查 `vite.config.js` 的 `target` 与后端端口 |
| 前端刷新子页面 404 | 生产环境缺 history 回退 | Nginx 加 `try_files $uri $uri/ /index.html` |
| 接口全部返回 401 | token 过期（24h）或被清理 | 重新登录 |
| 登录后立刻掉线 | `t_user_token` 被清空 / 账号被禁用 | 查 `t_user.status`；重新登录 |
| 点赞数不增加 | 已点过赞，再点即取消 | 属正常行为（切换语义） |
| 新发的文章前台看不到 | 新文章默认是**草稿** | 后台编辑页点「发布文章」 |
| `Table 'blog_db.t_xxx' doesn't exist` | 表没建全 | 重新执行 `01-schema.sql` |
| 端口被占用 | 已有实例在跑 | `lsof -ti:8082 \| xargs kill -9` |

---

## 十一、部署验收清单

部署完成后逐项确认：

- [ ] `java -version` 为 11，`mvn -v` 的 Java version 为 11
- [ ] `blog_db` 已创建，8 张表齐全
- [ ] 初始数据核对通过（5 用户 / 6 分类 / 12 标签 / 12 文章 / 28 关联 / 16 评论）
- [ ] `SELECT nickname FROM t_user WHERE id=1` 中文无乱码
- [ ] 后端启动日志出现「博客后端启动成功」
- [ ] `curl http://localhost:8082/api/articles?page=1&size=3` 返回 `code: 200`
- [ ] 前端 `npm run dev` 或 Nginx 访问正常，首页显示 10 篇文章
- [ ] 用 `admin / 123456` 能登录并进入 `/admin`
- [ ] `wangwu / 123456` 登录被拒绝（禁用账号）
- [ ] 后台能新建文章并发布，前台立即可见
- [ ] 点赞、评论、删除评论功能正常
- [ ] 刷新 `/article/1` 不出现 404（history 回退已配置）
- [ ] **重启后端后数据仍在**（持久化生效）
- [ ] `python3 scripts/e2e-smoke-test.py` 99 项全部通过
- [ ] 生产安全检查清单（第七章）已逐项落实
