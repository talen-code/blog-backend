## 一、JWT 的三个部分

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
  const token = localStorage.getItem('blog_token')
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
