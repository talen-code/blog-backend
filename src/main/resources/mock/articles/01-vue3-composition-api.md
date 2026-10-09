## 为什么一定要迁移到组合式 API

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
import { ref, reactive, toRefs } from 'vue'

// ✅ 基本类型用 ref
const keyword = ref('')

// ✅ 对象用 reactive，但导出时要 toRefs
const query = reactive({ page: 1, size: 10 })
const { page, size } = toRefs(query)
```

### 二、把逻辑抽成组合式函数

```js
// composables/useArticleList.js
import { ref } from 'vue'
import { getArticlePage } from '@/api/article'

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
