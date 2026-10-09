## 一、先用 EXPLAIN 建立判断标准

| 字段 | 关注点 |
| --- | --- |
| `type` | 至少要到 `range`，`ALL` 表示全表扫描 |
| `key` | 实际使用的索引，为 `NULL` 就是没走索引 |
| `rows` | 预估扫描行数，越小越好 |
| `Extra` | 出现 `Using filesort` / `Using temporary` 要警惕 |

## 二、8 种典型失效场景

1. **对索引列做运算或函数**：`WHERE YEAR(create_time) = 2024` ❌ → `WHERE create_time >= '2024-01-01' AND create_time < '2025-01-01'` ✅
2. **隐式类型转换**：`phone` 是 varchar，写成 `WHERE phone = 13800000000` 会导致全表扫描。
3. **前导模糊查询**：`LIKE '%关键词'` 必然失效；`LIKE '关键词%'` 可以走索引。
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
EXPLAIN SELECT * FROM article WHERE title LIKE 'Vue%' AND author_id = 5;
```

> 结论：索引不是越多越好，写多读少的表要谨慎；
> 上线前务必用真实数据量做 EXPLAIN 验证。
