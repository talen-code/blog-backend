## 一、缓存穿透

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
