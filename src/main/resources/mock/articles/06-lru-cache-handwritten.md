## 一、LRU 的核心要求

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
