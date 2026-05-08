# 各层样板代码（templates）

> 一个新业务模块从 0 到 1 的所有文件样板。**落地时按用户描述的业务概念合理命名，不要机械替换。**
>
> **命名取舍**（重要 —— 不强求 `_info` 后缀）：
>
> - **业务实体名** `Xxx`：按用户输入的业务概念取，如 `Order` / `Payment` / `Teacher` / `AuditLog` / `Coupon`。
> - **数据表名**：按业务语义灵活定 —— 主信息表惯用 `_info` 后缀（`student_info`、`product_info`），但关联表 / 记录表 / 流水表 / 日志表通常不带（`order_item`、`payment_record`、`audit_log`、`coupon`）。本样板用 `xxx_info` 演示，**不代表所有表都必须 `_info` 结尾**。
> - **PO / Mapper 严格跟随表名做 PascalCase + 后缀**，不要人为加/减 `Info`：
>   - 表 `student_info` → `StudentInfoPO` / `StudentInfoMapper`
>   - 表 `order_item` → `OrderItemPO` / `OrderItemMapper`
>   - 表 `payment_record` → `PaymentRecordPO` / `PaymentRecordMapper`
>   - 表 `coupon` → `CouponPO` / `CouponMapper`
> - **Service / VO / Controller 按业务概念命名，不跟着表走**：表 `student_info` 对应的服务是 `StudentService`、值对象是 `StudentVO`、控制器是 `StudentController`，**不写** `StudentInfoService`。
> - **根包名** `com.example.app`：替换为你的根包。
>
> 依赖 [`../references/infrastructure.md`](../references/infrastructure.md) 中的基础设施类（`Constants` / `ErrorCode` / `ExceptionFactory` / `Assertion` / `Page` / `PageQuery`）。

---

## 1. DDL：`build/sql/<module>.sql`

```sql
DROP TABLE IF EXISTS `xxx_info`;
CREATE TABLE `xxx_info` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `no` varchar(64) NOT NULL COMMENT '业务编号',
    `name` varchar(255) NOT NULL COMMENT '名称',
    `sex` tinyint NOT NULL COMMENT '性别：1、男；2、女',
    `introduce` text DEFAULT NULL COMMENT '介绍',
    `birthday` date DEFAULT NULL COMMENT '生日',
    `mobile` varchar(45) DEFAULT NULL COMMENT '手机号',
    `status` tinyint NOT NULL COMMENT '状态：1、有效；2、无效',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_time` datetime NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uidx_no` (`no`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=10000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Xxx 信息表';
```

要点：`bigint AUTO_INCREMENT` 主键、`create_time` / `update_time` 必备、状态用 `tinyint` + 注释列出取值、唯一索引 `uidx_*` / 普通索引 `idx_*`、起始 ID 10000。

---

## 2. PO：`domain/dao/po/XxxInfoPO.java`

```java
package com.example.app.domain.dao.po;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class XxxInfoPO {

    /** 主键 ID  NOT NULL */
    private Integer id;

    /** 业务编号  NOT NULL */
    private String no;

    /** 名称  NOT NULL */
    private String name;

    /**
     * 性别：1、男；2、女  NOT NULL
     *
     * @see com.example.app.domain.dao.types.XxxInfoSex
     */
    private Byte sex;

    /** 介绍 */
    private String introduce;

    /** 生日 */
    private LocalDate birthday;

    /** 手机号 */
    private String mobile;

    /**
     * 状态：1、有效；2、无效  NOT NULL
     *
     * @see com.example.app.domain.dao.types.DbRecordStatus
     */
    private Byte status;

    /** 创建时间  NOT NULL */
    private LocalDateTime createTime;

    /** 更新时间  NOT NULL */
    private LocalDateTime updateTime;

    /**
     * `selectByCondition` 的查询条件容器。**只放可查字段**，不一定全部列。
     */
    @Getter
    @Setter
    public static class Condition {

        private Integer id;
        private String no;
        private String name;
        private String mobile;
        /** @see com.example.app.domain.dao.types.DbRecordStatus */
        private Byte status;
    }
}
```

约束：`@Getter @Setter`，**不加** `@Builder` / `@AllArgsConstructor`；字段平铺所有列，类型按 SKILL.md §5.2 类型映射表。

---

## 3. 枚举：`domain/dao/types/DbRecordStatus.java` & `XxxInfoSex.java`

公共状态枚举（每个项目放一份即可）：

```java
package com.example.app.domain.dao.types;

import java.util.Arrays;
import java.util.Objects;

public enum DbRecordStatus {

    VALID((byte) 1, "有效"),
    INVALID((byte) 2, "无效");

    private final byte code;
    private final String name;

    DbRecordStatus(byte code, String name) {
        this.code = code;
        this.name = name;
    }

    public byte code() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static boolean isValid(Byte code) {
        return Objects.equals(VALID.code, code);
    }

    public static boolean isInvalid(Byte code) {
        return Objects.equals(INVALID.code, code);
    }

    /** 不是合法取值则抛 IllegalArgumentException。 */
    public static void check(Byte code) {
        boolean match = Arrays.stream(values()).anyMatch(item -> Objects.equals(item.code, code));
        if (!match) {
            throw new IllegalArgumentException("db record status code invalid:" + code);
        }
    }
}
```

业务字段枚举样板（如 `sex`）：

```java
package com.example.app.domain.dao.types;

import java.util.Arrays;
import java.util.Objects;

public enum XxxInfoSex {

    MALE((byte) 1, "男"),
    FEMALE((byte) 2, "女");

    private final byte code;
    private final String name;

    XxxInfoSex(byte code, String name) {
        this.code = code;
        this.name = name;
    }

    public byte code() {
        return code;
    }

    public static void check(Byte code) {
        boolean match = Arrays.stream(values()).anyMatch(item -> Objects.equals(item.code, code));
        if (!match) {
            throw new IllegalArgumentException("xxx info sex invalid:" + code);
        }
    }
}
```

---

## 4. Mapper 接口：`domain/dao/mapper/XxxInfoMapper.java`

```java
package com.example.app.domain.dao.mapper;

import com.example.app.domain.dao.po.XxxInfoPO;
import com.example.app.domain.dao.po.XxxInfoPO.Condition;
import java.util.List;

/**
 * 不加 {@code @Mapper}，由 MainApplication 上的 {@code @MapperScan} 统一扫描。
 */
public interface XxxInfoMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(XxxInfoPO row);

    XxxInfoPO selectByPrimaryKey(Integer id);

    /** 写前置读，加行锁。 */
    XxxInfoPO selectByPrimaryKeyForUpdate(Integer id);

    List<XxxInfoPO> selectByCondition(Condition condition);

    /** 业务唯一键查询。 */
    XxxInfoPO selectByNo(String no);

    int updateByPrimaryKeySelective(XxxInfoPO row);

    int updateByPrimaryKey(XxxInfoPO row);
}
```

---

## 5. Mapper XML：`src/main/resources/mappings/XxxInfoMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.app.domain.dao.mapper.XxxInfoMapper">

  <resultMap id="BaseResultMap" type="com.example.app.domain.dao.po.XxxInfoPO">
    <id column="id" jdbcType="INTEGER" property="id" />
    <result column="no" jdbcType="VARCHAR" property="no" />
    <result column="name" jdbcType="VARCHAR" property="name" />
    <result column="sex" jdbcType="TINYINT" property="sex" />
    <result column="introduce" jdbcType="LONGVARCHAR" property="introduce" />
    <result column="birthday" jdbcType="DATE" property="birthday" />
    <result column="mobile" jdbcType="VARCHAR" property="mobile" />
    <result column="status" jdbcType="TINYINT" property="status" />
    <result column="create_time" jdbcType="TIMESTAMP" property="createTime" />
    <result column="update_time" jdbcType="TIMESTAMP" property="updateTime" />
  </resultMap>

  <sql id="Base_Column_List">
    id, no, name, sex, introduce, birthday, mobile, status, create_time, update_time
  </sql>

  <select id="selectByPrimaryKey" parameterType="java.lang.Integer" resultMap="BaseResultMap">
    select <include refid="Base_Column_List" />
    from xxx_info
    where id = #{id,jdbcType=INTEGER}
  </select>

  <select id="selectByPrimaryKeyForUpdate" parameterType="java.lang.Integer" resultMap="BaseResultMap">
    select <include refid="Base_Column_List" />
    from xxx_info
    where id = #{id,jdbcType=INTEGER} for update
  </select>

  <select id="selectByNo" parameterType="java.lang.String" resultMap="BaseResultMap">
    select <include refid="Base_Column_List" />
    from xxx_info
    where no = #{no,jdbcType=VARCHAR}
  </select>

  <select id="selectByCondition" resultMap="BaseResultMap"
          parameterType="com.example.app.domain.dao.po.XxxInfoPO$Condition">
    select <include refid="Base_Column_List" />
    from xxx_info
    <where>
      <if test="id != null">
        id = #{id,jdbcType=INTEGER}
      </if>
      <if test="no != null and no != ''">
        and no = #{no,jdbcType=VARCHAR}
      </if>
      <if test="name != null and name != ''">
        and name = #{name,jdbcType=VARCHAR}
      </if>
      <if test="mobile != null and mobile != ''">
        and mobile = #{mobile,jdbcType=VARCHAR}
      </if>
      <if test="status != null">
        and status = #{status,jdbcType=TINYINT}
      </if>
    </where>
  </select>

  <delete id="deleteByPrimaryKey" parameterType="java.lang.Integer">
    delete from xxx_info
    where id = #{id,jdbcType=INTEGER}
  </delete>

  <insert id="insert" parameterType="com.example.app.domain.dao.po.XxxInfoPO"
          useGeneratedKeys="true" keyProperty="id" keyColumn="id">
    insert into xxx_info (id, no, name,
      sex, birthday, mobile,
      status, create_time, update_time,
      introduce)
    values (#{id,jdbcType=INTEGER}, #{no,jdbcType=VARCHAR}, #{name,jdbcType=VARCHAR},
      #{sex,jdbcType=TINYINT}, #{birthday,jdbcType=DATE}, #{mobile,jdbcType=VARCHAR},
      #{status,jdbcType=TINYINT}, #{createTime,jdbcType=TIMESTAMP}, #{updateTime,jdbcType=TIMESTAMP},
      #{introduce,jdbcType=LONGVARCHAR})
  </insert>

  <update id="updateByPrimaryKeySelective" parameterType="com.example.app.domain.dao.po.XxxInfoPO">
    update xxx_info
    <set>
      <if test="no != null">no = #{no,jdbcType=VARCHAR},</if>
      <if test="name != null">name = #{name,jdbcType=VARCHAR},</if>
      <if test="sex != null">sex = #{sex,jdbcType=TINYINT},</if>
      <if test="introduce != null">introduce = #{introduce,jdbcType=LONGVARCHAR},</if>
      <if test="birthday != null">birthday = #{birthday,jdbcType=DATE},</if>
      <if test="mobile != null">mobile = #{mobile,jdbcType=VARCHAR},</if>
      <if test="status != null">status = #{status,jdbcType=TINYINT},</if>
      <if test="createTime != null">create_time = #{createTime,jdbcType=TIMESTAMP},</if>
      <if test="updateTime != null">update_time = #{updateTime,jdbcType=TIMESTAMP},</if>
    </set>
    where id = #{id,jdbcType=INTEGER}
  </update>

  <update id="updateByPrimaryKey" parameterType="com.example.app.domain.dao.po.XxxInfoPO">
    update xxx_info
    set no = #{no,jdbcType=VARCHAR},
      name = #{name,jdbcType=VARCHAR},
      sex = #{sex,jdbcType=TINYINT},
      introduce = #{introduce,jdbcType=LONGVARCHAR},
      birthday = #{birthday,jdbcType=DATE},
      mobile = #{mobile,jdbcType=VARCHAR},
      status = #{status,jdbcType=TINYINT},
      create_time = #{createTime,jdbcType=TIMESTAMP},
      update_time = #{updateTime,jdbcType=TIMESTAMP}
    where id = #{id,jdbcType=INTEGER}
  </update>
</mapper>
```

> ⚠️ 全量更新（`updateByPrimaryKey`）逐行检查逗号：每行末尾必须有，最后一行必须没有。这是历史踩坑点。

---

## 6. 领域 VO：`domain/service/xxx/vo/XxxVO.java`

```java
package com.example.app.domain.service.xxx.vo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/**
 * 领域内部值对象。Application 层 Cmd → VO 后传给 Service。
 */
@Getter
@Setter
public class XxxVO {

    private Integer id;

    @NotEmpty
    private String no;

    @NotEmpty
    private String name;

    /** @see com.example.app.domain.dao.types.XxxInfoSex */
    @NotNull
    private Byte sex;

    private String introduce;

    private LocalDate birthday;

    private String mobile;

    /** @see com.example.app.domain.dao.types.DbRecordStatus */
    @NotNull
    private Byte status;
}
```

---

## 7. 领域 Service：`domain/service/xxx/XxxService.java`

```java
package com.example.app.domain.service.xxx;

import com.example.app.domain.dao.mapper.XxxInfoMapper;
import com.example.app.domain.dao.po.XxxInfoPO;
import com.example.app.domain.dao.types.XxxInfoSex;
import com.example.app.domain.service.xxx.vo.XxxVO;
import com.example.app.infrastructure.error.ErrorCode;
import com.example.app.infrastructure.exception.Assertion;
import com.example.app.infrastructure.exception.ExceptionFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * Xxx 领域服务。事务边界**只在这里**。
 */
@RequiredArgsConstructor
@Slf4j
@Service
@Validated
public class XxxService {

    private final XxxInfoMapper xxxInfoMapper;

    @Transactional
    public Integer createXxx(@Valid @NotNull XxxVO vo) {
        XxxInfoSex.check(vo.getSex());
        XxxInfoPO existing = xxxInfoMapper.selectByNo(vo.getNo());
        if (existing != null) {
            log.error("xxx no:{} exists,id:{}", vo.getNo(), existing.getId());
            throw ExceptionFactory.bizException(ErrorCode.PARAM_ILLEGAL);
        }

        XxxInfoPO po = new XxxInfoPO();
        po.setNo(vo.getNo());
        po.setName(vo.getName());
        po.setSex(vo.getSex());
        po.setIntroduce(vo.getIntroduce());
        po.setBirthday(vo.getBirthday());
        po.setMobile(vo.getMobile());
        po.setStatus(vo.getStatus());
        po.setCreateTime(LocalDateTime.now());
        po.setUpdateTime(LocalDateTime.now());

        int affected = xxxInfoMapper.insert(po);
        Assertion.affectedOne(affected);
        return po.getId();
    }

    @Transactional
    public void updateXxx(@Valid @NotNull XxxVO vo) {
        XxxInfoSex.check(vo.getSex());
        Assertion.notNull(vo.getId(), "xxx id is null");

        XxxInfoPO po = xxxInfoMapper.selectByPrimaryKeyForUpdate(vo.getId());
        if (po == null) {
            log.error("xxx not exists id:{}", vo.getId());
            throw ExceptionFactory.bizException(ErrorCode.RECORD_NOT_EXISTS);
        }
        XxxInfoPO duplicate = xxxInfoMapper.selectByNo(vo.getNo());
        if (duplicate != null && !duplicate.getId().equals(vo.getId())) {
            log.error("xxx no already used no:{}", vo.getNo());
            throw ExceptionFactory.bizException(ErrorCode.PARAM_ILLEGAL);
        }

        po.setNo(vo.getNo());
        po.setName(vo.getName());
        po.setSex(vo.getSex());
        po.setIntroduce(vo.getIntroduce());
        po.setBirthday(vo.getBirthday());
        po.setMobile(vo.getMobile());
        po.setUpdateTime(LocalDateTime.now());

        int affected = xxxInfoMapper.updateByPrimaryKey(po);
        Assertion.affectedOne(affected);
    }

    @Transactional
    public void deleteXxx(@NotNull Integer id) {
        XxxInfoPO po = xxxInfoMapper.selectByPrimaryKeyForUpdate(id);
        if (po == null) {
            log.error("xxx not exists id:{}", id);
            throw ExceptionFactory.bizException(ErrorCode.RECORD_NOT_EXISTS);
        }
        int affected = xxxInfoMapper.deleteByPrimaryKey(id);
        Assertion.affectedOne(affected);
    }
}
```

---

## 8. Application DTO

### 8.1 `application/xxx/dto/CreateXxxCmd.java`

```java
package com.example.app.application.xxx.dto;

import com.example.app.infrastructure.constants.Constants;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CreateXxxCmd {

    @Schema(description = "业务编号")
    @NotEmpty
    @Size(max = 64)
    private String no;

    @Schema(description = "名称")
    @NotEmpty
    @Size(max = 255)
    private String name;

    @Schema(description = "性别：1、男；2、女")
    @NotNull
    private Byte sex;

    @Schema(description = "介绍")
    private String introduce;

    @Schema(description = "生日")
    @JsonFormat(pattern = Constants.DATE_PATTERN)
    private LocalDate birthday;

    @Schema(description = "手机号")
    @Size(max = 45)
    private String mobile;
}
```

### 8.2 `application/xxx/dto/UpdateXxxCmd.java`

```java
package com.example.app.application.xxx.dto;

import com.example.app.infrastructure.constants.Constants;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UpdateXxxCmd {

    @Schema(description = "ID")
    @NotNull
    private Integer id;

    @Schema(description = "业务编号")
    @Size(max = 64)
    private String no;

    @Schema(description = "名称")
    @Size(max = 255)
    private String name;

    @Schema(description = "性别：1、男；2、女")
    private Byte sex;

    @Schema(description = "介绍")
    private String introduce;

    @Schema(description = "生日")
    @JsonFormat(pattern = Constants.DATE_PATTERN)
    private LocalDate birthday;

    @Schema(description = "手机号")
    @Size(max = 45)
    private String mobile;

    @Schema(description = "状态：1、有效；2、无效")
    private Byte status;
}
```

### 8.3 `application/xxx/dto/DeleteXxxCmd.java`

```java
package com.example.app.application.xxx.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DeleteXxxCmd {

    @Schema(description = "ID")
    @NotNull
    private Integer id;
}
```

### 8.4 `application/xxx/dto/XxxDetailQry.java`

```java
package com.example.app.application.xxx.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class XxxDetailQry {

    @Schema(description = "ID")
    @NotNull
    private Integer id;
}
```

### 8.5 `application/xxx/dto/XxxPageQry.java`

```java
package com.example.app.application.xxx.dto;

import com.example.app.infrastructure.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class XxxPageQry extends PageQuery {

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "业务编号")
    private String no;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "状态：1、有效；2、无效")
    private Byte status;
}
```

### 8.6 `application/xxx/dto/clientobject/XxxCO.java`

```java
package com.example.app.application.xxx.dto.clientobject;

import com.example.app.infrastructure.constants.Constants;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class XxxCO {

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "业务编号")
    private String no;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "性别：1、男；2、女")
    private Byte sex;

    @Schema(description = "介绍")
    private String introduce;

    @Schema(description = "生日")
    @JsonFormat(pattern = Constants.DATE_PATTERN)
    private LocalDate birthday;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "状态：1、有效；2、无效")
    private Byte status;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = Constants.DATETIME_PATTERN)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = Constants.DATETIME_PATTERN)
    private LocalDateTime updateTime;
}
```

`XxxDetailCO.java` 通常与 `XxxCO` 字段一致，单独建类是为日后扩展（如关联子表）。结构同上，不再重复。

---

## 9. Application Executor

### 9.1 写命令：`CreateXxxCmdExe.java` —— **必须经过 Service**

```java
package com.example.app.application.xxx.executor;

import com.example.app.application.xxx.dto.CreateXxxCmd;
import com.example.app.domain.dao.types.DbRecordStatus;
import com.example.app.domain.service.xxx.XxxService;
import com.example.app.domain.service.xxx.vo.XxxVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@RequiredArgsConstructor
@Slf4j
@Component                          // 不是 @Service，executor 不是领域概念
@Validated
public class CreateXxxCmdExe {

    private final XxxService xxxService;

    public void execute(@Valid @NotNull CreateXxxCmd cmd) {
        log.info("create cmd:{}", cmd);

        XxxVO vo = new XxxVO();
        vo.setNo(cmd.getNo());
        vo.setName(cmd.getName());
        vo.setSex(cmd.getSex());
        vo.setIntroduce(cmd.getIntroduce());
        vo.setBirthday(cmd.getBirthday());
        vo.setMobile(cmd.getMobile());
        vo.setStatus(DbRecordStatus.VALID.code());

        Integer id = xxxService.createXxx(vo);
        log.info("create xxx success id:{}", id);
    }
}
```

### 9.2 简单读：`XxxDetailQryExe.java` —— **直连 Mapper（CQRS）**

```java
package com.example.app.application.xxx.executor;

import com.example.app.application.xxx.dto.XxxDetailQry;
import com.example.app.application.xxx.dto.clientobject.XxxCO;
import com.example.app.domain.dao.mapper.XxxInfoMapper;
import com.example.app.domain.dao.po.XxxInfoPO;
import com.example.app.infrastructure.error.ErrorCode;
import com.example.app.infrastructure.exception.ExceptionFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * 单表查询，无领域规则 → 直接注入 Mapper，**不要**为它硬抽 Service 方法。
 */
@RequiredArgsConstructor
@Slf4j
@Component
@Validated
public class XxxDetailQryExe {

    private final XxxInfoMapper xxxInfoMapper;

    public XxxCO execute(@Valid @NotNull XxxDetailQry qry) {
        log.info("detail qry:{}", qry);

        XxxInfoPO po = xxxInfoMapper.selectByPrimaryKey(qry.getId());
        if (po == null) {
            log.error("xxx not exists id:{}", qry.getId());
            throw ExceptionFactory.bizException(ErrorCode.RECORD_NOT_EXISTS);
        }

        XxxCO co = new XxxCO();
        co.setId(po.getId());
        co.setNo(po.getNo());
        co.setName(po.getName());
        co.setSex(po.getSex());
        co.setIntroduce(po.getIntroduce());
        co.setBirthday(po.getBirthday());
        co.setMobile(po.getMobile());
        co.setStatus(po.getStatus());
        co.setCreateTime(po.getCreateTime());
        co.setUpdateTime(po.getUpdateTime());
        return co;
    }
}
```

### 9.3 分页读：`XxxPageQryExe.java` —— PageHelper 模式

```java
package com.example.app.application.xxx.executor;

import com.example.app.application.xxx.dto.XxxPageQry;
import com.example.app.application.xxx.dto.clientobject.XxxCO;
import com.example.app.domain.dao.mapper.XxxInfoMapper;
import com.example.app.domain.dao.po.XxxInfoPO;
import com.example.app.infrastructure.dto.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageSerializable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@RequiredArgsConstructor
@Slf4j
@Component
@Validated
public class XxxPageQryExe {

    private final XxxInfoMapper xxxInfoMapper;

    public Page<XxxCO> execute(@Valid @NotNull XxxPageQry qry) {
        log.info("page qry:{}", qry);

        XxxInfoPO.Condition condition = new XxxInfoPO.Condition();
        condition.setId(qry.getId());
        condition.setNo(qry.getNo());
        condition.setName(qry.getName());
        condition.setMobile(qry.getMobile());
        condition.setStatus(qry.getStatus());

        PageHelper.startPage(qry.getPage(), qry.getPageSize());
        PageSerializable<XxxInfoPO> page =
                new PageSerializable<>(xxxInfoMapper.selectByCondition(condition));

        List<XxxCO> data = new ArrayList<>();
        page.getList().forEach(item -> {
            XxxCO co = new XxxCO();
            co.setId(item.getId());
            co.setNo(item.getNo());
            co.setName(item.getName());
            co.setSex(item.getSex());
            co.setIntroduce(item.getIntroduce());
            co.setBirthday(item.getBirthday());
            co.setMobile(item.getMobile());
            co.setStatus(item.getStatus());
            co.setCreateTime(item.getCreateTime());
            co.setUpdateTime(item.getUpdateTime());
            data.add(co);
        });
        return new Page<>(page.getTotal(), data);
    }
}
```

---

## 10. Controller：`interfaces/XxxController.java`

```java
package com.example.app.interfaces;

import com.example.app.application.xxx.dto.CreateXxxCmd;
import com.example.app.application.xxx.dto.DeleteXxxCmd;
import com.example.app.application.xxx.dto.UpdateXxxCmd;
import com.example.app.application.xxx.dto.XxxDetailQry;
import com.example.app.application.xxx.dto.XxxPageQry;
import com.example.app.application.xxx.dto.clientobject.XxxCO;
import com.example.app.application.xxx.executor.CreateXxxCmdExe;
import com.example.app.application.xxx.executor.DeleteXxxCmdExe;
import com.example.app.application.xxx.executor.UpdateXxxCmdExe;
import com.example.app.application.xxx.executor.XxxDetailQryExe;
import com.example.app.application.xxx.executor.XxxPageQryExe;
import com.example.app.infrastructure.dto.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/xxx")
@Tag(name = "Xxx 管理")
public class XxxController {

    private final CreateXxxCmdExe createXxxCmdExe;
    private final UpdateXxxCmdExe updateXxxCmdExe;
    private final DeleteXxxCmdExe deleteXxxCmdExe;
    private final XxxDetailQryExe xxxDetailQryExe;
    private final XxxPageQryExe xxxPageQryExe;

    @PostMapping("/create")
    @Operation(summary = "创建 Xxx")
    public void create(@RequestBody CreateXxxCmd cmd) {
        createXxxCmdExe.execute(cmd);
    }

    @PostMapping("/update")
    @Operation(summary = "更新 Xxx")
    public void update(@RequestBody UpdateXxxCmd cmd) {
        updateXxxCmdExe.execute(cmd);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除 Xxx")
    public void delete(@RequestBody DeleteXxxCmd cmd) {
        deleteXxxCmdExe.execute(cmd);
    }

    @PostMapping("/detail")
    @Operation(summary = "查询 Xxx 详情")
    public XxxCO detail(@RequestBody XxxDetailQry qry) {
        return xxxDetailQryExe.execute(qry);
    }

    @PostMapping("/page")
    @Operation(summary = "Xxx 分页列表")
    public Page<XxxCO> page(@RequestBody XxxPageQry qry) {
        return xxxPageQryExe.execute(qry);
    }
}
```

---

## 11. 应用事件（可选）：`application/xxx/event/XxxCreatedEvent.java`

```java
package com.example.app.application.xxx.event;

/**
 * 普通 POJO/record 即可作事件，无需继承 ApplicationEvent。
 */
public record XxxCreatedEvent(Integer id, String no, long timestamp) {

    public static XxxCreatedEvent of(Integer id, String no) {
        return new XxxCreatedEvent(id, no, System.currentTimeMillis());
    }
}
```

发布与监听样板：

```java
package com.example.app.application.xxx.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class XxxEventListeners {

    /** 同步监听：与 publish 同线程，发布方等待。 */
    @EventListener
    public void onSync(XxxCreatedEvent e) {
        log.info("[sync] {}", e);
    }

    /** 异步监听：需 MainApplication 上 {@code @EnableAsync}。 */
    @Async
    @EventListener
    public void onAsync(XxxCreatedEvent e) {
        log.info("[async] {}", e);
    }

    /** 事务提交后才触发；事务回滚则不执行。最适合 "数据落库后再发外部消息"。 */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAfterCommit(XxxCreatedEvent e) {
        log.info("[afterCommit] {}", e);
    }
}
```

---

## 12. 配置类（可选）：`infrastructure/configuration/properties/AppProperties.java`

```java
package com.example.app.infrastructure.configuration.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
@Validated
public class AppProperties {

    @Valid
    @NotNull
    @NestedConfigurationProperty
    private CorsProperties cors = new CorsProperties();
}
```

`MainApplication` 上需 `@EnableConfigurationProperties(AppProperties.class)` 才会生效。
