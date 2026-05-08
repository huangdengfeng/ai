# 基础设施基类样板（reference）

> 这些类是 SKILL.md 中所有规则的**前置条件**。落地新项目时，把它们整体拷贝到 `infrastructure/` 下并把包名 `com.example.app` 改成你的包名即可，**不要逐个手抄**。
>
> 所有规则（异常工厂、断言、错误码段位、分页、日期格式常量等）都假设这些基类存在。

---

## 1. `infrastructure/constants/Constants.java`

```java
package com.example.app.infrastructure.constants;

import java.time.format.DateTimeFormatter;

public class Constants {

    public static final String COMMA = ",";
    public static final String UNDERLINE = "_";
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern(Constants.DATETIME_PATTERN);
}
```

---

## 2. `infrastructure/dto/Page.java`

```java
package com.example.app.infrastructure.dto;

import java.util.List;

/**
 * 分页响应。仅出现在 application / interfaces 层。
 */
public class Page<T> {

    private long total;
    private List<T> data;

    public Page() {
    }

    public Page(List<T> data) {
        this.data = data;
    }

    public Page(long total, List<T> data) {
        this.total = total;
        this.data = data;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
```

---

## 3. `infrastructure/dto/PageQuery.java`

```java
package com.example.app.infrastructure.dto;

/**
 * 所有分页查询 Qry 的父类。Qry 类继承本类后即可在请求体里直接传 page / pageSize。
 */
public abstract class PageQuery {

    public static final int DEFAULT_PAGE_SIZE_LIMIT = 1000;

    private int page = 1;
    private int pageSize = 10;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        if (pageSize > this.pageSizeLimit()) {
            throw new IllegalArgumentException(
                    "page size limit:" + this.pageSizeLimit() + ",current is " + pageSize);
        }
        this.pageSize = pageSize;
    }

    /** 子类可覆写以提供更小的 limit。 */
    public int pageSizeLimit() {
        return DEFAULT_PAGE_SIZE_LIMIT;
    }
}
```

---

## 4. `infrastructure/exception/ErrorDefinition.java`

```java
package com.example.app.infrastructure.exception;

/**
 * 业务方实现此接口扩展自己的错误码枚举。
 */
public interface ErrorDefinition {

    int code();

    String msg();

    int type();
}
```

---

## 5. `infrastructure/exception/BaseException.java`

```java
package com.example.app.infrastructure.exception;

/**
 * 所有自定义异常的根。运行时异常，避免污染方法签名。
 */
public abstract class BaseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private int code;

    public BaseException(String msg) {
        super(msg);
    }

    public BaseException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public BaseException(String msg, Throwable e) {
        super(msg, e);
    }

    public BaseException(int code, String msg, Throwable e) {
        super(msg, e);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
```

---

## 6. `infrastructure/exception/BizException.java`

```java
package com.example.app.infrastructure.exception;

/**
 * 业务可预期错误（参数非法、记录不存在等）。前端友好提示，**不需要重试**。
 */
public class BizException extends BaseException {

    public static final int DEFAULT_ERR_CODE = -1;
    private static final long serialVersionUID = 1L;

    public BizException(String msg) {
        super(DEFAULT_ERR_CODE, msg);
    }

    public BizException(int code, String msg) {
        super(code, msg);
    }

    public BizException(String msg, Throwable e) {
        super(DEFAULT_ERR_CODE, msg, e);
    }

    public BizException(int code, String msg, Throwable e) {
        super(code, msg, e);
    }
}
```

---

## 7. `infrastructure/exception/SysException.java`

```java
package com.example.app.infrastructure.exception;

/**
 * 系统级异常（DB 抽风、外部依赖挂了），调用方**重试可能成功**。
 */
public class SysException extends BaseException {

    public static final int DEFAULT_ERR_CODE = -2;
    private static final long serialVersionUID = 1L;

    public SysException(String msg) {
        super(DEFAULT_ERR_CODE, msg);
    }

    public SysException(int code, String msg) {
        super(code, msg);
    }

    public SysException(String msg, Throwable e) {
        super(DEFAULT_ERR_CODE, msg, e);
    }

    public SysException(int code, String msg, Throwable e) {
        super(code, msg, e);
    }
}
```

---

## 8. `infrastructure/error/ErrorCode.java`

```java
package com.example.app.infrastructure.error;

import com.example.app.infrastructure.exception.ErrorDefinition;

/**
 * 错误码集中处。
 *
 * <ul>
 *   <li>1000-1999：公共错误码（本枚举内置）</li>
 *   <li>2000+：各业务模块自行约定段位（如 student: 2000-2099）</li>
 * </ul>
 */
public enum ErrorCode implements ErrorDefinition {

    UNKNOWN(1000, "系统错误，请稍后重试"),
    PARAM_INVALID(1001, "参数错误"),
    PARAM_ILLEGAL(1002, "参数不合法"),
    SQL_ERROR(1003, "数据库操作错误"),
    ASSERTION_ERROR(1004, "断言错误"),
    RECORD_NOT_EXISTS(1005, "记录不存在"),
    ;

    public static final int ERROR_TYPE_BUSINESS = 0;
    public static final int ERROR_TYPE_SYSTEM = 1;

    private final int code;
    private final String msg;
    private final int type;

    ErrorCode(int code, String msg) {
        this(code, msg, false);
    }

    ErrorCode(int code, String msg, boolean sysError) {
        this.code = code;
        this.msg = msg;
        this.type = sysError ? ERROR_TYPE_SYSTEM : ERROR_TYPE_BUSINESS;
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public String msg() {
        return msg;
    }

    @Override
    public int type() {
        return type;
    }

    public boolean isSystemError() {
        return type == ERROR_TYPE_SYSTEM;
    }
}
```

---

## 9. `infrastructure/exception/ExceptionFactory.java`

```java
package com.example.app.infrastructure.exception;

import com.example.app.infrastructure.error.ErrorCode;

/**
 * 业务代码统一通过本工厂构造异常，**禁止 new BizException / new SysException**。
 */
public class ExceptionFactory {

    public static BizException bizException(String msg) {
        return new BizException(msg);
    }

    public static BizException bizException(ErrorCode errorCode) {
        return new BizException(errorCode.code(), errorCode.msg());
    }

    public static BizException bizException(int code, String msg) {
        return new BizException(code, msg);
    }

    public static SysException sysException(String msg) {
        return new SysException(msg);
    }

    public static SysException sysException(int code, String msg) {
        return new SysException(code, msg);
    }

    public static SysException sysException(String msg, Throwable e) {
        return new SysException(msg, e);
    }

    public static SysException sysException(int code, String msg, Throwable e) {
        return new SysException(code, msg, e);
    }
}
```

---

## 10. `infrastructure/exception/Assertion.java`

```java
package com.example.app.infrastructure.exception;

import com.example.app.infrastructure.error.ErrorCode;
import org.apache.commons.lang3.StringUtils;

/**
 * 业务断言工具。失败一律抛 BizException。
 *
 * <p>典型用法：
 * <pre>
 *     int affected = mapper.updateByPrimaryKey(po);
 *     Assertion.affectedOne(affected);          // 期望影响 1 行，否则中断
 *     Assertion.notNull(id, "id is null");
 * </pre>
 */
public abstract class Assertion {

    private static final int DEFAULT_CODE = ErrorCode.ASSERTION_ERROR.code();

    public static void isTrue(boolean expression, String msg) {
        if (!expression) {
            throw new BizException(DEFAULT_CODE, msg);
        }
    }

    public static void affectedOne(int affectedRows) {
        isTrue(affectedRows == 1, "expect affected one,actual " + affectedRows);
    }

    public static void leOneRow(int actual) {
        isTrue(actual <= 1, "expect: <= 1 ,actual:" + actual);
    }

    public static void affectedRow(int expect, int actual) {
        isTrue(expect == actual, "expect: " + expect + " ,actual:" + actual);
    }

    public static void notNull(Object object, String msg) {
        if (object == null) {
            throw new BizException(DEFAULT_CODE, msg);
        }
    }

    public static void notEmpty(String object, String msg) {
        if (StringUtils.isEmpty(object)) {
            throw new BizException(DEFAULT_CODE, msg);
        }
    }
}
```

---

## 11. `infrastructure/configuration/advice/RFC9457ExceptionAdvice.java`

> 全局异常处理 → 统一返回 `ProblemDetail`（[RFC 9457](https://datatracker.ietf.org/doc/html/rfc9457)）。
> 业务层只管抛 `BizException` / `SysException`，**不要 try-catch 后再转 Response**。

```java
package com.example.app.infrastructure.configuration.advice;

import com.example.app.infrastructure.error.ErrorCode;
import com.example.app.infrastructure.exception.BizException;
import com.example.app.infrastructure.exception.SysException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Slf4j
public class RFC9457ExceptionAdvice {

    private static final String ERROR_CODE = "errorCode";

    /** 参数 / 校验类异常：HTTP 400。 */
    @ExceptionHandler({MissingServletRequestParameterException.class, ValidationException.class,
            MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class,
            BindException.class, IllegalArgumentException.class})
    public ProblemDetail parameterInvalidException(Exception e) {
        log.error("parameter invalid exception", e);
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle(ErrorCode.PARAM_INVALID.msg());
        if (log.isDebugEnabled()) {
            pd.setDetail(e.getMessage());
        }
        pd.setProperty(ERROR_CODE, ErrorCode.PARAM_INVALID.code());
        return pd;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail noResourceFoundException(NoResourceFoundException e) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setTitle(HttpStatus.NOT_FOUND.getReasonPhrase());
        if (log.isDebugEnabled()) {
            pd.setDetail(e.getMessage());
        }
        return pd;
    }

    @ExceptionHandler(BizException.class)
    public ProblemDetail bizException(BizException e) {
        log.error("biz exception code:{},msg:{}", e.getCode(), e.getMessage());
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle(e.getMessage());
        pd.setProperty(ERROR_CODE, e.getCode());
        return pd;
    }

    @ExceptionHandler(SysException.class)
    public ProblemDetail sysException(SysException e) {
        log.error("system exception code:{},msg:{}", e.getCode(), e.getMessage());
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle(e.getMessage());
        pd.setProperty(ERROR_CODE, e.getCode());
        return pd;
    }

    @ExceptionHandler({Exception.class, RuntimeException.class})
    public ProblemDetail exception(Exception e) {
        log.error("internal server error", e);
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        if (log.isDebugEnabled()) {
            pd.setDetail(e.getMessage());
        }
        return pd;
    }
}
```

---

## 12. `MainApplication.java`

> 入口类，集中配置 `@MapperScan` / `@EnableAsync` / `@EnableConfigurationProperties`。

```java
package com.example.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync                    // 让 @Async / 异步事件监听器生效
@EnableScheduling               // 让 @Scheduled 生效
@MapperScan(basePackages = "com.example.app.domain.dao.mapper")  // Mapper 接口禁止再加 @Mapper
public class MainApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
}
```
