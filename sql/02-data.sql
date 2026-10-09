-- ============================================================================
--  个人博客系统 —— 初始数据脚本
--
--  本文件由 scripts/gen-seed-sql.py 自动生成，请勿手工编辑！
--  要改初始数据，请修改生成脚本或 mock/articles/*.md 后重新生成：
--      python3 scripts/gen-seed-sql.py
--
--  前置条件：已执行 01-schema.sql 建好库表
--
--  【重要】请用「执行 SQL 脚本 / 运行 SQL 文件」的方式执行本文件：
--      Navicat ：右键数据库 -> 运行 SQL 文件
--      DBeaver ：Alt+X（Execute SQL Script）
--      IDEA    ：选中文件右键 -> Run
--      千万不要用「执行当前语句」(Ctrl+Enter) 并全选，
--      那会把整个文件当一条语句发送，导致 1064 语法错误。
--
--  数据规模：5 个用户 / 6 个分类 / 12 个标签 / 12 篇文章（10 已发布 + 2 草稿）/ 16 条评论
-- ============================================================================

USE `blog_db`;

-- 清空业务数据（保留表结构），保证脚本可重复执行
DELETE FROM `t_user_token`;
DELETE FROM `t_article_like`;
DELETE FROM `t_article_tag`;
DELETE FROM `t_comment`;
DELETE FROM `t_article`;
DELETE FROM `t_tag`;
DELETE FROM `t_category`;
DELETE FROM `t_user`;

-- ---------------------------------------------------------------------------
-- 1. 用户（密码统一为 123456，库里存的是 MD5 摘要：e10adc3949ba59abbe56e057f20f883e）
-- ---------------------------------------------------------------------------
INSERT INTO `t_user` (`id`, `username`, `password`, `nickname`, `avatar`, `email`, `signature`, `role`, `status`, `create_time`, `last_login_time`) VALUES
(1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '超级管理员', 'https://api.dicebear.com/7.x/identicon/svg?seed=admin', 'admin@blog.com', '全栈搬砖工，热爱折腾各种中间件。', 'ADMIN', 1, DATE_SUB(NOW(), INTERVAL 400 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 'zhangsan', 'e10adc3949ba59abbe56e057f20f883e', '张三', 'https://api.dicebear.com/7.x/identicon/svg?seed=zhangsan', 'zhangsan@blog.com', '三年经验前端，Vue 爱好者。', 'USER', 1, DATE_SUB(NOW(), INTERVAL 300 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 'lisi', 'e10adc3949ba59abbe56e057f20f883e', '李四', 'https://api.dicebear.com/7.x/identicon/svg?seed=lisi', 'lisi@blog.com', '后端工程师，专注高并发与性能优化。', 'USER', 1, DATE_SUB(NOW(), INTERVAL 200 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 'wangwu', 'e10adc3949ba59abbe56e057f20f883e', '王五', 'https://api.dicebear.com/7.x/identicon/svg?seed=wangwu', 'wangwu@blog.com', '被禁用的演示账号，用于验证用户管理功能。', 'USER', 0, DATE_SUB(NOW(), INTERVAL 150 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(5, 'xiaomei', 'e10adc3949ba59abbe56e057f20f883e', '小美', 'https://api.dicebear.com/7.x/identicon/svg?seed=xiaomei', 'xiaomei@blog.com', '测试工程师，偶尔写点自动化脚本。', 'USER', 1, DATE_SUB(NOW(), INTERVAL 60 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

-- ---------------------------------------------------------------------------
-- 2. 分类
-- ---------------------------------------------------------------------------
INSERT INTO `t_category` (`id`, `name`, `description`, `sort`, `create_time`) VALUES
(1, '前端开发', 'Vue / React / 工程化 / 性能优化', 1, DATE_SUB(NOW(), INTERVAL 500 DAY)),
(2, '后端开发', 'Java / Spring 全家桶 / 微服务', 2, DATE_SUB(NOW(), INTERVAL 480 DAY)),
(3, '数据库', 'MySQL / Redis / 索引与调优', 3, DATE_SUB(NOW(), INTERVAL 400 DAY)),
(4, '算法与数据结构', '刷题笔记与解题思路', 4, DATE_SUB(NOW(), INTERVAL 350 DAY)),
(5, '运维部署', 'Linux / Docker / Nginx / CI-CD', 5, DATE_SUB(NOW(), INTERVAL 300 DAY)),
(6, '生活随笔', '工作之外的碎碎念', 6, DATE_SUB(NOW(), INTERVAL 200 DAY));

-- ---------------------------------------------------------------------------
-- 3. 标签
-- ---------------------------------------------------------------------------
INSERT INTO `t_tag` (`id`, `name`, `color`, `create_time`) VALUES
(1, 'Vue3', '#42B983', DATE_SUB(NOW(), INTERVAL 520 DAY)),
(2, 'SpringBoot', '#6DB33F', DATE_SUB(NOW(), INTERVAL 520 DAY)),
(3, 'Java', '#E76F00', DATE_SUB(NOW(), INTERVAL 520 DAY)),
(4, 'JavaScript', '#F7DF1E', DATE_SUB(NOW(), INTERVAL 520 DAY)),
(5, 'CSS', '#264DE4', DATE_SUB(NOW(), INTERVAL 520 DAY)),
(6, 'MySQL', '#00758F', DATE_SUB(NOW(), INTERVAL 520 DAY)),
(7, 'Redis', '#DC382D', DATE_SUB(NOW(), INTERVAL 520 DAY)),
(8, 'Docker', '#2496ED', DATE_SUB(NOW(), INTERVAL 520 DAY)),
(9, 'Nginx', '#009639', DATE_SUB(NOW(), INTERVAL 520 DAY)),
(10, '算法', '#8E44AD', DATE_SUB(NOW(), INTERVAL 520 DAY)),
(11, '性能优化', '#E67E22', DATE_SUB(NOW(), INTERVAL 520 DAY)),
(12, '效率工具', '#16A085', DATE_SUB(NOW(), INTERVAL 520 DAY));

-- ---------------------------------------------------------------------------
-- 4. 文章（正文来自 src/main/resources/mock/articles/*.md，共 12 篇）
--    作者规则：id % 3 == 0 的文章作者是用户 2（张三），其余是用户 1（超级管理员）
-- ---------------------------------------------------------------------------
INSERT INTO `t_article` (`id`, `title`, `summary`, `cover`, `content`, `category_id`, `author_id`, `views`, `likes`, `comment_count`, `status`, `is_top`, `create_time`, `update_time`) VALUES
(1, 'Vue3 组合式 API 实战：从 Options API 平滑迁移', '用真实项目中的例子，讲清楚 setup 语法糖、ref 与 reactive 的取舍、组合式函数的抽取方式，以及迁移过程中最容易踩的 5 个坑。', 'https://picsum.photos/seed/blog1/800/450', '## 为什么一定要迁移到组合式 API

Options API 最大的问题是**逻辑按选项切分，而不是按功能切分**。一个搜索功能往往要在
`data`、`computed`、`methods`、`watch` 之间来回跳，代码超过 300 行后维护成本陡增。

组合式 API 把同一个功能的代码聚在一起，并且可以抽成独立的函数复用。

### 一、ref 还是 reactive

| 场景 | 推荐 | 原因 |
| --- | --- | --- |
| 基本类型 | `ref` | `reactive` 无法代理原始值 |
| 表单对象 | `reactive` | 免去 `.value` 的书写负担 |
| 需要整体替换 | `ref` | `reactive` 丢失响应式引用 |
| 需要解构 | `ref` + `toRefs` | 直接解构 `reactive` 会断链 |

```js
import { ref, reactive, toRefs } from ''vue''

// ✅ 基本类型用 ref
const keyword = ref('''')

// ✅ 对象用 reactive，但导出时要 toRefs
const query = reactive({ page: 1, size: 10 })
const { page, size } = toRefs(query)
```

### 二、把逻辑抽成组合式函数

```js
// composables/useArticleList.js
import { ref } from ''vue''
import { getArticlePage } from ''@/api/article''

export function useArticleList(initialSize = 6) {
  const loading = ref(false)
  const list = ref([])
  const total = ref(0)

  async function load(params) {
    loading.value = true
    try {
      const { data } = await getArticlePage({ page: 1, size: initialSize, ...params })
      list.value = data.records
      total.value = data.total
    } finally {
      loading.value = false
    }
  }

  return { loading, list, total, load }
}
```

### 三、迁移时最容易踩的 5 个坑

1. `reactive` 对象直接解构会丢失响应式，必须用 `toRefs`。
2. `ref` 在模板里自动解包，但在 JS 里必须写 `.value`，混用容易出错。
3. `watch` 监听 `reactive` 对象默认是**深度**的，监听 `ref` 默认是浅的。
4. `setup` 中不能用 `this`，需要 `getCurrentInstance()` 时优先考虑替代方案。
5. `v-model` 绑定到 `props` 上的对象属性属于反模式，改用 `emit`。

> 迁移不必一次性完成。Options API 和组合式 API 可以在同一个组件中共存，
> 建议新页面直接用 `<script setup>`，老页面按迭代节奏逐步替换。
', 1, 1, 1286, 96, 3, 1, 1, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO `t_article` (`id`, `title`, `summary`, `cover`, `content`, `category_id`, `author_id`, `views`, `likes`, `comment_count`, `status`, `is_top`, `create_time`, `update_time`) VALUES
(2, 'Spring Boot 参数校验最佳实践：别再写 if-else 了', '从 @Valid 到分组校验、自定义校验注解、全局异常处理，一套可复用的参数校验方案，附带完整代码。', 'https://picsum.photos/seed/blog2/800/450', '## 一、引入依赖

Spring Boot 2.7 的校验实现已经独立成 starter，必须显式引入：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

## 二、在 DTO 上声明规则

```java
@Data
public class ArticleDTO {
    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题最长 100 字")
    private String title;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @Min(value = 0, message = "状态值只能是 0 或 1")
    @Max(value = 1, message = "状态值只能是 0 或 1")
    private Integer status = 1;
}
```

> Spring Boot 2.7 用 `javax.validation.constraints.*`；如果你的项目升级到 Spring Boot 3，
> 需要整体替换成 `jakarta.validation.constraints.*`。

## 三、Controller 上开启校验

只有加了 `@Valid`（或 `@Validated`）才会触发校验：

```java
@PostMapping
public Result<ArticleVO> create(@RequestBody @Valid ArticleDTO dto) {
    return Result.success(articleService.create(dto));
}
```

## 四、全局异常兜底

校验失败会抛出 `MethodArgumentNotValidException`，
用 `@RestControllerAdvice` 统一转换成友好提示：

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public Result<Void> handleValid(MethodArgumentNotValidException e) {
    String msg = e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining("；"));
    return Result.error(ResultCode.PARAM_ERROR, msg);
}
```

## 五、分组校验

新增时 ID 必须为空，修改时 ID 必须非空 —— 用分组接口解决：

```java
public interface Create {}
public interface Update {}

@Null(groups = Create.class, message = "新增时不允许传 ID")
@NotNull(groups = Update.class, message = "修改时必须传 ID")
private Long id;
```

> 小结：校验规则写在 DTO 上，错误信息交给全局异常处理，
> Service 里只保留「业务规则」校验（如用户名已存在）。
', 2, 1, 942, 68, 2, 1, 1, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY));

INSERT INTO `t_article` (`id`, `title`, `summary`, `cover`, `content`, `category_id`, `author_id`, `views`, `likes`, `comment_count`, `status`, `is_top`, `create_time`, `update_time`) VALUES
(3, 'JWT 在前后端分离项目中的落地细节', 'JWT 的结构、签名过程、过期与刷新策略，以及在 Vue + Axios 项目中如何优雅地携带与处理 token。', 'https://picsum.photos/seed/blog3/800/450', '## 一、JWT 的三个部分

```
header.payload.signature
```

- **header**：声明签名算法，如 `{"alg":"HS256","typ":"JWT"}`
- **payload**：业务数据 + 标准声明（`sub` / `exp` / `iat`）
- **signature**：`HMACSHA256(base64Url(header) + "." + base64Url(payload), secret)`

关键结论：**payload 只是 Base64 编码，不是加密**。任何人都能解出里面的内容，
所以绝对不要往 JWT 里放密码、手机号等敏感信息。

## 二、服务端签发

```java
String token = Jwts.builder()
        .subject(username)
        .claim("userId", userId)
        .claim("role", role)
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + expireMillis))
        .signWith(key)          // HS256，密钥至少 32 字节
        .compact();
```

## 三、前端如何携带

方式一：请求头 `Authorization: Bearer <token>`（推荐，天然免疫 CSRF）。

```js
service.interceptors.request.use(config => {
  const token = localStorage.getItem(''blog_token'')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})
```

## 四、过期了怎么办

| 方案 | 优点 | 缺点 |
| --- | --- | --- |
| 过期直接重新登录 | 实现最简单 | 体验差 |
| 双 token（access + refresh） | 体验好 | 实现复杂 |
| 滑动续期 | 折中 | 需要服务端状态 |

本项目采用**内存白名单**：token 有效期内每次请求都放行，
登出时把 token 从白名单移除，立刻失效。

## 五、常见误区

1. 把密钥硬编码提交到 Git —— 应该走环境变量。
2. 密钥太短导致 `WeakKeyException`，HS256 要求 ≥ 256 bit。
3. 用 JWT 当 Session 存大量数据，导致请求头过大。
4. 不做服务端黑名单，导致登出后旧 token 依然可用。
', 2, 2, 1533, 121, 3, 1, 0, DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY));

INSERT INTO `t_article` (`id`, `title`, `summary`, `cover`, `content`, `category_id`, `author_id`, `views`, `likes`, `comment_count`, `status`, `is_top`, `create_time`, `update_time`) VALUES
(4, 'Vite 冷启动从 30 秒优化到 6 秒的完整记录', '依赖预构建、按需引入、插件瘦身、sourcemap 关闭，一步步定位并解决大型项目的启动性能问题。', 'https://picsum.photos/seed/blog4/800/450', '## 优化前的体检

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
    include: [''vue'', ''vue-router'', ''element-plus'', ''axios'', ''echarts'']
  }
})
```

## 二、Element Plus 按需引入

全量引入会把 200+ 组件都拉进来，改成按需后体积下降明显：

```js
import AutoImport from ''unplugin-auto-import/vite''
import Components from ''unplugin-vue-components/vite''
import { ElementPlusResolver } from ''unplugin-vue-components/resolvers''

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
  minify: ''esbuild''
}
```

## 四、优化效果对比

| 指标 | 优化前 | 优化后 |
| --- | --- | --- |
| 冷启动 | 30.2s | 6.1s |
| HMR | 1.8s | 0.3s |
| 生产构建 | 96s | 31s |

> 经验：先用数据定位瓶颈，再动手优化，不要凭感觉删插件。
', 1, 1, 764, 57, 0, 1, 0, DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 11 DAY));

INSERT INTO `t_article` (`id`, `title`, `summary`, `cover`, `content`, `category_id`, `author_id`, `views`, `likes`, `comment_count`, `status`, `is_top`, `create_time`, `update_time`) VALUES
(5, 'Redis 缓存三大经典问题：穿透、击穿、雪崩', '从原理到代码，讲清楚三种缓存异常的区别、危害以及各自的工业级解决方案。', 'https://picsum.photos/seed/blog5/800/450', '## 一、缓存穿透

**现象**：查询一个数据库里根本不存在的数据，缓存永远不命中，请求全部打到 DB。

**危害**：恶意攻击可用不存在的 ID 压垮数据库。

**解决方案**：

1. **缓存空值**：即使查不到也写入 `null`，并设置较短过期时间（如 60s）。
2. **布隆过滤器**：在缓存之前拦截绝对不存在的 key。

```java
public ArticleVO getById(Long id) {
    String key = "article:" + id;
    Object cached = redis.get(key);
    if (cached != null) {
        // 缓存了空值标记，直接返回，保护数据库
        return EMPTY.equals(cached) ? null : (ArticleVO) cached;
    }
    ArticleVO vo = loadFromDb(id);
    redis.set(key, vo == null ? EMPTY : vo, vo == null ? 60 : 1800, SECONDS);
    return vo;
}
```

## 二、缓存击穿

**现象**：某个**热点 key** 过期瞬间，大量并发请求同时打到 DB。

**解决方案**：

- 互斥锁（只放一个线程去加载）
- 逻辑过期（不设置 TTL，异步刷新）

```java
// 简易互斥锁：拿不到锁就重试，避免全部压到 DB
if (redis.setnx(lockKey, "1", 10, SECONDS)) {
    try {
        vo = loadFromDb(id);
        redis.set(key, vo, 1800, SECONDS);
    } finally {
        redis.del(lockKey);
    }
}
```

## 三、缓存雪崩

**现象**：大批 key 在同一时刻集体过期，或 Redis 集群宕机，流量瞬间压垮 DB。

**解决方案**：

1. 过期时间加**随机扰动**：`1800 + random(0, 300)` 秒。
2. 热点数据**永不过期** + 后台定时刷新。
3. 多级缓存（本地 Caffeine + Redis）。
4. Redis 高可用（哨兵 / Cluster）。

## 四、一张表记住区别

| 问题 | 数据是否存在 | 范围 | 核心解法 |
| --- | --- | --- | --- |
| 穿透 | 不存在 | 单个 key | 缓存空值 / 布隆过滤器 |
| 击穿 | 存在 | 热点 key | 互斥锁 / 逻辑过期 |
| 雪崩 | 存在 | 大批 key | 随机 TTL / 多级缓存 |
', 3, 1, 1105, 88, 2, 1, 0, DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY));

INSERT INTO `t_article` (`id`, `title`, `summary`, `cover`, `content`, `category_id`, `author_id`, `views`, `likes`, `comment_count`, `status`, `is_top`, `create_time`, `update_time`) VALUES
(6, '手写一个线程安全的 LRU 缓存', '用 LinkedHashMap 和「哈希表 + 双向链表」两种方式实现 LRU，并分析时间复杂度与并发安全。', 'https://picsum.photos/seed/blog6/800/450', '## 一、LRU 的核心要求

- `get(key)`：命中则返回并把该节点移到「最近使用」端，O(1)
- `put(key, value)`：写入并移到最近使用端；容量超限时淘汰「最久未使用」端，O(1)

要同时满足 O(1)，必须 **哈希表 + 双向链表** 组合。

## 二、用 LinkedHashMap 快速实现

`LinkedHashMap` 的 `accessOrder=true` 天然满足 LRU 顺序：

```java
public class LruCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;

    public LruCache(int capacity) {
        // accessOrder = true：每次 get 都把节点移到链表尾部
        super(capacity, 0.75f, true);
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        // 超过容量就淘汰链表头部（最久未使用）
        return size() > capacity;
    }
}
```

注意：`LinkedHashMap` **不是线程安全**的。多线程环境需要：

```java
Map<K, V> cache = Collections.synchronizedMap(new LruCache<>(128));
```

## 三、手写双向链表版本

```java
class Node {
    int key, value;
    Node prev, next;
    Node(int k, int v) { key = k; value = v; }
}
```

用虚拟头尾节点（哨兵）可以省掉大量边界判断：

```java
private void removeNode(Node node) {
    node.prev.next = node.next;
    node.next.prev = node.prev;
}

private void addToHead(Node node) {
    node.next = head.next;
    node.prev = head;
    head.next.prev = node;
    head.next = node;
}
```

## 四、复杂度与对比

| 实现方式 | get | put | 线程安全 |
| --- | --- | --- | --- |
| LinkedHashMap | O(1) | O(1) | 否 |
| 哈希表+双向链表 | O(1) | O(1) | 否 |
| ConcurrentHashMap + 分段 LRU | O(1) | O(1) | 是 |

> 面试延伸：LFU、FIFO、W-TinyLFU（Caffeine 的实现）以及它们各自适用的访问分布。
', 4, 2, 623, 45, 1, 1, 0, DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 19 DAY));

INSERT INTO `t_article` (`id`, `title`, `summary`, `cover`, `content`, `category_id`, `author_id`, `views`, `likes`, `comment_count`, `status`, `is_top`, `create_time`, `update_time`) VALUES
(7, 'Docker + Nginx 部署 Vue 单页应用完整流程', '多阶段构建、history 路由回退、gzip 压缩、接口反向代理，一份可直接复用的 Dockerfile 与 nginx.conf。', 'https://picsum.photos/seed/blog7/800/450', '## 一、多阶段构建 Dockerfile

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
', 5, 1, 878, 64, 1, 1, 0, DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 24 DAY));

INSERT INTO `t_article` (`id`, `title`, `summary`, `cover`, `content`, `category_id`, `author_id`, `views`, `likes`, `comment_count`, `status`, `is_top`, `create_time`, `update_time`) VALUES
(8, 'MySQL 索引失效的 8 种常见场景（附执行计划分析）', '结合 EXPLAIN 的 type / key / rows 字段，逐个复现索引失效的场景，避免线上慢查询。', 'https://picsum.photos/seed/blog8/800/450', '## 一、先用 EXPLAIN 建立判断标准

| 字段 | 关注点 |
| --- | --- |
| `type` | 至少要到 `range`，`ALL` 表示全表扫描 |
| `key` | 实际使用的索引，为 `NULL` 就是没走索引 |
| `rows` | 预估扫描行数，越小越好 |
| `Extra` | 出现 `Using filesort` / `Using temporary` 要警惕 |

## 二、8 种典型失效场景

1. **对索引列做运算或函数**：`WHERE YEAR(create_time) = 2024` ❌ → `WHERE create_time >= ''2024-01-01'' AND create_time < ''2025-01-01''` ✅
2. **隐式类型转换**：`phone` 是 varchar，写成 `WHERE phone = 13800000000` 会导致全表扫描。
3. **前导模糊查询**：`LIKE ''%关键词''` 必然失效；`LIKE ''关键词%''` 可以走索引。
4. **违反最左前缀原则**：联合索引 `(a, b, c)`，直接查 `b` 或 `c` 用不上。
5. **OR 连接非索引列**：`WHERE a = 1 OR d = 2`，`d` 无索引则整体失效。
6. **范围条件后的列失效**：`(a, b)` 索引中 `a > 1 AND b = 2`，`b` 用不上索引排序。
7. **使用 `!=` / `NOT IN` / `IS NOT NULL`**：优化器可能判断全表扫描更快。
8. **数据区分度太低**：性别字段只有两个值，优化器宁愿全表扫描。

## 三、几个容易被忽略的细节

```sql
-- 覆盖索引：查询列都在索引里，避免回表
EXPLAIN SELECT id, title FROM article WHERE category_id = 3;
-- Extra: Using index  ✅

-- 索引下推（ICP）：MySQL 5.6+ 在存储引擎层就过滤掉不匹配的行
EXPLAIN SELECT * FROM article WHERE title LIKE ''Vue%'' AND author_id = 5;
```

> 结论：索引不是越多越好，写多读少的表要谨慎；
> 上线前务必用真实数据量做 EXPLAIN 验证。
', 3, 1, 1420, 103, 2, 1, 0, DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 29 DAY));

INSERT INTO `t_article` (`id`, `title`, `summary`, `cover`, `content`, `category_id`, `author_id`, `views`, `likes`, `comment_count`, `status`, `is_top`, `create_time`, `update_time`) VALUES
(9, '前端性能优化清单：首屏从 3.2s 降到 1.4s', '从资源体积、网络请求、渲染阻塞三个维度，整理一份可落地的性能优化检查表。', 'https://picsum.photos/seed/blog9/800/450', '## 一、先量化，再优化

```js
// 用 PerformanceObserver 采集真实用户指标
new PerformanceObserver((list) => {
  for (const entry of list.getEntries()) {
    console.log(entry.name, entry.startTime)
  }
}).observe({ entryTypes: [''largest-contentful-paint'', ''paint''] })
```

关注三个核心指标：

- **LCP**（最大内容绘制）< 2.5s
- **FID / INP**（交互延迟）< 200ms
- **CLS**（布局偏移）< 0.1

## 二、减少资源体积

1. 路由懒加载：`const Home = () => import(''@/views/front/Home.vue'')`
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
', 1, 2, 951, 72, 0, 1, 0, DATE_SUB(NOW(), INTERVAL 35 DAY), DATE_SUB(NOW(), INTERVAL 34 DAY));

INSERT INTO `t_article` (`id`, `title`, `summary`, `cover`, `content`, `category_id`, `author_id`, `views`, `likes`, `comment_count`, `status`, `is_top`, `create_time`, `update_time`) VALUES
(10, '我的 2024 年度总结：写了 45 篇博客之后', '关于坚持输出的方法、时间管理，以及这一年踩过的坑和明年的计划。', 'https://picsum.photos/seed/blog10/800/450', '## 一、为什么要开始写博客

三年前我还是个「收藏夹吃灰型选手」，存了 800 多个链接，真到用时一个都想不起来。
写作是唯一能逼自己把知识嚼碎的方式 —— **你以为你懂了，直到你试图讲给别人听**。

## 二、我是怎么坚持下来的

1. **降低启动成本**：先写 200 字的大纲，不追求一次成文。
2. **绑定已有习惯**：每周日晚 8 点，雷打不动写一小时。
3. **建立素材库**：工作中遇到的每个坑，随手记进 Notion，周末再整理成文。
4. **允许写得烂**：先完成，再完美。发布比完美重要得多。

## 三、数据

| 项目 | 数量 |
| --- | --- |
| 发布文章 | 45 篇 |
| 总字数 | 约 12 万字 |
| 最高阅读 | 1.5 万 |
| 收到评论 | 300+ |

## 四、踩过的坑

- 一开始追求排版花哨，结果内容反而敷衍了。
- 写过几篇「翻译式」文章，没有自己的思考，阅读量惨淡。
- 有段时间为了凑更新而写，质量下滑明显，后来果断降低频率。

## 五、明年的计划

- [ ] 完成「手写系列」10 篇：Promise、Vue 响应式、简易 ORM
- [ ] 尝试做视频，把文章转成 5 分钟讲解
- [ ] 读 20 本非技术书

> 写博客最大的收益不是流量，而是三年后回头看，能清楚看到自己成长的轨迹。
', 6, 1, 512, 39, 2, 1, 0, DATE_SUB(NOW(), INTERVAL 40 DAY), DATE_SUB(NOW(), INTERVAL 39 DAY));

INSERT INTO `t_article` (`id`, `title`, `summary`, `cover`, `content`, `category_id`, `author_id`, `views`, `likes`, `comment_count`, `status`, `is_top`, `create_time`, `update_time`) VALUES
(11, 'CSS Grid 布局速查笔记（草稿）', '整理 grid-template-areas、auto-fit 与 minmax 的常见组合写法。', 'https://picsum.photos/seed/blog11/800/450', '## 常用属性速查

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
', 1, 1, 12, 0, 0, 0, 0, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 0 DAY));

INSERT INTO `t_article` (`id`, `title`, `summary`, `cover`, `content`, `category_id`, `author_id`, `views`, `likes`, `comment_count`, `status`, `is_top`, `create_time`, `update_time`) VALUES
(12, '微服务拆分的时机与代价（草稿）', '单体不是原罪，过早拆分才是。记录一次拆分评审的思考过程。', 'https://picsum.photos/seed/blog12/800/450', '## 什么时候该拆

- 团队规模超过 2 个披萨能喂饱的人数
- 不同模块的伸缩需求差异巨大
- 发布节奏互相阻塞，改 A 模块要回归 B 模块

## 拆分的代价

1. 分布式事务
2. 链路追踪与日志聚合
3. 本地开发环境复杂度陡增
4. 运维成本与人力成本

## 一个判断清单

| 维度 | 适合单体 | 适合微服务 |
| --- | --- | --- |
| 团队规模 | < 8 人 | > 15 人 |
| 发布频率 | 每周一次 | 每天多次 |
| 模块耦合 | 强 | 弱 |
| 运维能力 | 无专职运维 | 有 SRE |

> 待补充：实际拆分案例与监控指标。
', 2, 2, 8, 0, 0, 0, 0, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY));

-- ---------------------------------------------------------------------------
-- 5. 文章-标签关联（多对多中间表）
-- ---------------------------------------------------------------------------
INSERT INTO `t_article_tag` (`article_id`, `tag_id`) VALUES
(1, 1),
(1, 4),
(1, 11),
(2, 2),
(2, 3),
(3, 3),
(3, 2),
(3, 4),
(4, 1),
(4, 11),
(4, 4),
(5, 7),
(5, 3),
(5, 11),
(6, 10),
(6, 3),
(7, 8),
(7, 9),
(7, 1),
(8, 6),
(8, 11),
(9, 11),
(9, 4),
(9, 5),
(10, 12),
(11, 5),
(12, 3),
(12, 2);

-- ---------------------------------------------------------------------------
-- 6. 评论（两级：parent_id 为 NULL 是顶级评论）
-- ---------------------------------------------------------------------------
INSERT INTO `t_comment` (`id`, `article_id`, `user_id`, `content`, `parent_id`, `reply_to_nickname`, `status`, `create_time`) VALUES
(1, 1, 2, 'ref 和 reactive 的取舍这块讲得太清楚了，之前一直分不清什么时候该用哪个，收藏了！', NULL, NULL, 1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 1, 3, '补充一点：如果对象层级很深，reactive 的性能反而更好，因为不需要层层 .value。', 1, '张三', 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 1, 5, '迁移那 5 个坑我全踩过，尤其是第 4 条，setup 里用 this 直接报 undefined。', NULL, NULL, 1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 2, 3, '分组校验这一段解决了我的大问题，之前新增和修改写了两个 DTO，现在合并成一个了。', NULL, NULL, 1, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(5, 2, 2, '建议再补充一下嵌套对象的校验，需要加 @Valid 才生效，很多人不知道这个坑。', NULL, NULL, 1, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(6, 3, 5, '双 token 方案能展开讲讲吗？refresh token 存在哪里比较安全？', NULL, NULL, 1, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(7, 3, 1, '推荐存在 httpOnly Cookie 里，前端 JS 读不到，能有效防止 XSS 窃取。', 6, '小美', 1, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(8, 3, 3, '密钥长度这个问题我踩过，jjwt 0.12 密钥不足 32 字节会直接抛 WeakKeyException。', NULL, NULL, 1, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(9, 5, 2, '缓存空值那段代码里的 EMPTY 常量是怎么定义的？用空对象还是特殊字符串？', NULL, NULL, 1, DATE_SUB(NOW(), INTERVAL 13 DAY)),
(10, 5, 1, '布隆过滤器的误判率怎么估算？有没有推荐的开源实现？', NULL, NULL, 1, DATE_SUB(NOW(), INTERVAL 14 DAY)),
(11, 6, 3, '手写双向链表那段如果加上泛型会更通用，不过用来讲原理足够清晰了。', NULL, NULL, 1, DATE_SUB(NOW(), INTERVAL 18 DAY)),
(12, 7, 5, 'try_files 这行太关键了，我第一次部署 history 模式刷新 404 排查了一下午。', NULL, NULL, 1, DATE_SUB(NOW(), INTERVAL 23 DAY)),
(13, 8, 2, '隐式类型转换这个坑太隐蔽了，线上一个手机号查询慢查询就是这么来的。', NULL, NULL, 1, DATE_SUB(NOW(), INTERVAL 28 DAY)),
(14, 8, 3, '对，而且 EXPLAIN 的 type 还是 ALL，看一眼就能定位。', 13, '张三', 1, DATE_SUB(NOW(), INTERVAL 27 DAY)),
(15, 10, 5, '「先完成，再完美」这句话戳中我了，我的草稿箱里躺着 30 篇没写完的。', NULL, NULL, 1, DATE_SUB(NOW(), INTERVAL 38 DAY)),
(16, 10, 2, '一起加油，坚持输出真的会上瘾。', NULL, NULL, 1, DATE_SUB(NOW(), INTERVAL 39 DAY));

-- 初始数据写入完成（点赞表 t_article_like 故意留空，用于演示点赞功能）
SELECT
  (SELECT COUNT(*) FROM `t_user`)         AS 用户数,
  (SELECT COUNT(*) FROM `t_category`)     AS 分类数,
  (SELECT COUNT(*) FROM `t_tag`)          AS 标签数,
  (SELECT COUNT(*) FROM `t_article`)      AS 文章数,
  (SELECT COUNT(*) FROM `t_article_tag`)  AS 标签关联数,
  (SELECT COUNT(*) FROM `t_comment`)      AS 评论数;
