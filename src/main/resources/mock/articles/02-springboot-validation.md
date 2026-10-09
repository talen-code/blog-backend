## 一、引入依赖

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
