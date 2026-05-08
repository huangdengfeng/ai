---
name: java-coding-standard
description: >-
  Java项目（Spring Boot + MyBatis + COLA 风格 DDD）的工程结构、代码分层与命名规范。
  当用户在该项目里新增/修改业务功能、新建包或类、做代码评审、或询问 "应该放在哪一层 / 类名怎么取"
  时使用此规范；包含 interfaces / application / domain / infrastructure 四层职责、
  Cmd / Qry / Exe / CO / VO / PO 等后缀约定，以及 Controller / Service / Mapper /
  Properties 等命名样板与示例代码。
---

# 编程规范

**新增代码必须遵循本规范。** 本文只列项目特定决策；通用代码样板与基础设施类按需读：

- [`assets/templates.md`](assets/templates.md) —— 各层完整代码（Controller / Cmd / Qry / Exe / Service / VO / PO / Mapper / XML / DDL / 枚举 / 事件），用 `Xxx` 占位符。
- [`assets/tests.md`](assets/tests.md) —— 集成测试与事件冒烟测试样板。
- [`references/infrastructure.md`](references/infrastructure.md) —— 基础设施基类（`ErrorCode` / `ExceptionFactory` / `Assertion` / `Page` / `PageQuery` / `Constants` / `RFC9457ExceptionAdvice`），新项目落地时整体拷贝。

---

## 1. 工程结构（COLA 风格 DDD 四层）

```
src/main/java/<your.base.package>/
├── MainApplication.java              # @SpringBootApplication 入口
├── interfaces/                       # 协议适配：HTTP/RPC ↔ application
│   └── XxxController.java
├── application/                      # 用例编排，无领域逻辑
│   └── <bounded-context>/
│       ├── dto/                      # CreateXxxCmd / UpdateXxxCmd / DeleteXxxCmd / XxxDetailQry / XxxPageQry
│       │   └── clientobject/         # XxxCO / XxxDetailCO
│       ├── executor/                 # 一个 Cmd/Qry 一个 Exe
│       ├── event/                    # 应用事件（POJO/record + 监听器）
│       └── scheduler/                # 定时任务 @Scheduled
├── domain/                           # 业务规则、事务边界、持久化抽象
│   ├── service/<context>/            # XxxService（事务在此） + vo/XxxVO
│   └── dao/                          # mapper/XxxMapper + po/XxxPO（含 Condition） + types/XxxStatus
└── infrastructure/                   # 与业务无关的基础能力
    ├── configuration/                # advice/ interceptor/ properties/ context/
    ├── constants/                    # 全局常量
    ├── dto/                          # 公共 DTO：Page / PageQuery
    ├── error/                        # ErrorCode 枚举
    ├── exception/                    # BaseException / BizException / SysException
    ├── rpc/                          # 调用外部服务
    └── utils/                        # 纯工具类
```

---

## 2. 分层职责与依赖方向

依赖**只能向内**：`interfaces → application → domain ← infrastructure`。`domain` 不依赖 `application` / `interfaces`。

| 层 | 职责 | 不该做 |
|---|---|---|
| `interfaces` | HTTP/RPC 适配，调用 executor | 写业务逻辑、直接调 mapper |
| `application` | 用例编排：Cmd → VO → Service；简单 Qry 直连 Mapper（见 §2.1）；DTO ↔ VO/PO 转换 | 写领域规则、自己开事务 |
| `domain` | 业务规则、不变量、事务边界 | 依赖 Spring MVC、感知 HTTP |
| `infrastructure` | 配置、异常、工具、常量等通用能力 | 含业务字段或业务方法 |

### 2.1 CQRS：写命令与读查询分离

| 路径 | 调用链 | 适用场景 |
|---|---|---|
| 写 Cmd | Controller → `XxxCmdExe` → `Service` → `Mapper` | 所有 Create / Update / Delete |
| 读 Qry（简单） | Controller → `XxxQryExe` → `Mapper` | 单表查询、无领域规则的详情/列表/分页 |
| 读 Qry（复杂） | Controller → `XxxQryExe` → `Service` → `Mapper` | 跨表聚合、复用领域规则、读后供领域决策 |

判断准则：

1. **写操作一律走 Service**（保留事务、领域校验、审计/事件切入点），无论多简单。
2. **简单 Qry Exe 直接注入 `Mapper`**：在 Exe 里完成 `Qry → Condition`、`PO → CO` 装配；**禁止**为单表查询硬抽 `findById` 这种薄壳 Service 方法。
3. **Qry 上提 Service 的触发条件**：多表聚合 / 跨 mapper / 与写路径共用领域规则 / 查询结果作为下一步领域决策输入。
4. **Qry Exe 不开事务**；如读后立即写，请上提为 Cmd 用例。

---

## 3. 类命名规范（强约束）

| 后缀 | 含义 | 所在层 / 包 |
|---|---|---|
| `Controller` | HTTP 入口 | `interfaces/` |
| `Cmd` / `Qry` | 写 / 读请求 | `application/<ctx>/dto/` |
| `CO` | 客户端对象（出参） | `application/<ctx>/dto/clientobject/` |
| `Exe` | 执行器（一个用例一个） | `application/<ctx>/executor/` |
| `Task` / `Event` | 定时任务 / 事件载荷 | `application/<ctx>/scheduler/` 或 `event/` |
| `Service` | 领域服务 | `domain/service/<ctx>/` |
| `VO` | 领域值对象 | `domain/service/<ctx>/vo/` |
| `Mapper` / `PO` | MyBatis 接口 / 表映射对象 | `domain/dao/mapper/` 或 `po/` |
| `Properties` | 配置类 | `infrastructure/configuration/properties/` |
| `Exception` | 自定义异常 | `infrastructure/exception/` |
| 枚举（无后缀） | 字段取值 | `domain/dao/types/` |

命名细节：

1. **Cmd/Qry 必须以动词开头**：`Create...Cmd` / `Update...Cmd` / `Delete...Cmd` / `...DetailQry` / `...PageQry`。
2. **Exe = 对应 Cmd/Qry 全名 + Exe**：`CreateStudentCmd` → `CreateStudentCmdExe`。**禁止**简写成 `CreateStudentExe`。
3. **PO / Mapper 严格跟随表名做 PascalCase + 后缀**（不要人为加/减 `Info`）：表 `student_info` → `StudentInfoPO` / `StudentInfoMapper`；表 `coupon` → `CouponPO` / `CouponMapper`；表 `payment_record` → `PaymentRecordPO` / `PaymentRecordMapper`。
4. **Service / VO / Controller 按业务概念命名，不跟着表走**：`StudentService` 即可，**不写** `StudentInfoService`。
5. **CO 与 PO/VO 字段允许同名**，但**类型走应用语义**（如 `LocalDateTime` 而非 `Date`）。

---

## 4. 注解使用约定（项目特定决策）

通用 Spring/Lombok 用法（`@RestController` / `@Service` / `@Slf4j` / `@Getter @Setter` 等）按业界惯例即可，本节只列项目自有规则。完整代码样板见 [`assets/templates.md`](assets/templates.md)。

- **构造注入**：所有 Bean 用 `@RequiredArgsConstructor` + `private final`，**禁用** `@Autowired` 字段注入。
- **DTO 字段用 `@Schema(description = "...")`**（Swagger 抓不到 Javadoc），`@NotNull` / `@NotEmpty` / `@Size(max = ...)` 校验放 DTO 字段上；VO/PO 不加 `@Schema`，用 Javadoc + `@see` 指向枚举。
- **Controller 全 POST + JSON Body**，方法只一行 `xxxExe.execute(cmd)`，不写 `if/for`。
- **Executor 用 `@Component`（不是 `@Service`）+ `@Validated`**：方法名固定 `execute`，签名 `(@Valid @NotNull XxxCmd/Qry)`；DTO ↔ VO 手写 setter（不引 MapStruct）。
- **事务边界只在 `@Service` 上**，Executor / Controller 不写 `@Transactional`；纯读不加。
- **业务校验抛 `ExceptionFactory.bizException(ErrorCode.XXX)`**，不直接 `throw new RuntimeException`。
- **Properties**：`@ConfigurationProperties + @Validated`，并在 `MainApplication` 上 `@EnableConfigurationProperties`。
- **PO 只 `@Getter @Setter`**，**不要** `@Builder` / `@AllArgsConstructor`（避免与 MyBatis 反射构造冲突）。
- **Mapper 接口禁止加 `@Mapper`**；统一由 `MainApplication` 上的 `@MapperScan(basePackages = "<your.base.package>.domain.dao.mapper")` 扫描。

---

## 5. DAO 层规范

DAO 只暴露**单表 CRUD + 单表条件查询**，不放业务、不返回 CO/VO。统一 MyBatis XML（不混用 `@Select` / `@Insert` 注解）。

### 5.1 Mapper 接口固定方法集（强约束）

每张表的 Mapper 必须提供以下方法，名字与签名严格对齐：

| 方法 | 用途 | 返回 |
|---|---|---|
| `int insert(XxxPO row)` | 插入，自增主键回填 `row.id` | 影响行数 |
| `int deleteByPrimaryKey(Integer id)` | 主键删除 | 影响行数 |
| `XxxPO selectByPrimaryKey(Integer id)` | 主键读 | PO 或 `null` |
| `XxxPO selectByPrimaryKeyForUpdate(Integer id)` | 主键读 + 行锁 | PO 或 `null` |
| `List<XxxPO> selectByCondition(XxxPO.Condition c)` | 条件查询 | 非 null List |
| `XxxPO selectByXxx(...)` | 业务唯一键查（如 `selectByNo`） | PO 或 `null` |
| `int updateByPrimaryKey(XxxPO row)` | 全量更新 | 影响行数 |
| `int updateByPrimaryKeySelective(XxxPO row)` | 非空字段更新 | 影响行数 |

约束：写操作返回 `int`（Service 用 `Assertion.affectedOne(...)` 校验）；单条无果返回 `null`、列表无果返回空 List，**禁止 `Optional`**；**不允许加业务感方法**（如 `findActiveXxx`、`countByDept`），需要业务条件就给 `Condition` 加字段，需要聚合就在 Service 里组合。

### 5.2 PO 规范

- 字段平铺所有列，与表 1:1；不允许嵌套对象、不允许 `transient` 业务字段。
- **类型映射特例**：主键 `bigint` → `Integer`（与 `selectByPrimaryKey(Integer id)` 一致，不用 `Long`）；状态/枚举字段 `tinyint` → `Byte` + `domain/dao/types/` 下枚举（提供 `code()` / `check(Byte)`）。其他类型按 JDBC 常规（`varchar`→`String`、`date`→`LocalDate`、`datetime`/`timestamp`→`LocalDateTime`、`decimal`→`BigDecimal`）。
- **`Condition` 内部静态类**：作为 `selectByCondition` 的查询条件容器，**只放可查字段**（不一定全部列），同样 `@Getter @Setter`。完整代码见 [`assets/templates.md` §2](assets/templates.md)。

### 5.3 Mapper XML 规范（项目踩坑点）

文件位置：`src/main/resources/mappings/<MapperName>.xml`。MyBatis 通用语法（`<where>` / `<set>` / `<if>` 双重判空等）按惯例即可，本项目额外要求：

1. `namespace` = mapper 接口全限定名；`<resultMap id="BaseResultMap">` **`jdbcType` 必填**（`TINYINT` / `DATE` / `LONGVARCHAR` 容易踩坑）。
2. `<sql id="Base_Column_List">` 列出所有列名，所有 `select` 必须 `<include refid="Base_Column_List" />`，**禁用 `select *`**。
3. `<insert>` 必填 `useGeneratedKeys="true" keyProperty="id" keyColumn="id"`。
4. `for update` 单独写一条 select，方法以 `ForUpdate` 结尾；**不要**给同一条 select 加可选 `for update` 开关。
5. **`updateByPrimaryKey`（全量）逐行检查逗号**：每行末尾必须有逗号、最后一行必须没有；这是历史踩坑点。可选字段更新走 `updateByPrimaryKeySelective` + `<set>`，由 MyBatis 处理逗号。

### 5.4 DDL（表结构）规范

完整 DDL 样板见 [`assets/templates.md` §1](assets/templates.md)。硬约束：

- **命名**：表 / 列 `snake_case`；按业务语义取名（**不强求 `_info` 后缀**）。常见模式：主信息表 `student_info`、关联表 `order_item`、流水表 `payment_record`、日志表 `audit_log`、独立实体 `coupon`。
- **必备字段**：`id bigint AUTO_INCREMENT` 主键；`create_time` / `update_time` 均 `datetime NOT NULL`。
- **状态字段**用 `tinyint`，COMMENT 列出取值（"1、有效；2、无效"），Java 侧建对应枚举。
- **每列必填 COMMENT**，表也必填 COMMENT。
- **索引命名**：唯一 `uidx_<col>`、普通 `idx_<col>`。
- **`AUTO_INCREMENT=10000`** 起始：避开测试小 id，方便人工识别真实数据。
- **字符集**：`utf8mb4` + `utf8mb4_0900_ai_ci`，统一不变。
- 所有 DDL 集中在 `build/sql/all.sql`，**不要散落在 migration 文件**（默认不引 Flyway/Liquibase）。

---

## 6. 异常与错误码

完整代码见 [`references/infrastructure.md`](references/infrastructure.md)。

- **`BizException`**（业务可预期错误） vs **`SysException`**（系统错误，可重试）。**不直接 `new`**，统一走 `ExceptionFactory.bizException(ErrorCode.XXX)`。
- **错误码集中**在 `infrastructure/error/ErrorCode.java`：公共 `1000-1999`（已内置）；各业务模块约定段位（如 student: 2000-2099）。
- **断言**用 `Assertion`：`Assertion.affectedOne(affected)` / `Assertion.notNull(id, msg)`。
- **全局异常处理** `RFC9457ExceptionAdvice` 统一转 `ProblemDetail`。**业务层不要 try-catch 后转 Response**，直接抛即可。

---

## 7. 通用约束

1. **日期格式**走 `Constants.DATE_PATTERN` / `Constants.DATETIME_PATTERN`，不要散落字符串；DTO 字段 `@JsonFormat(pattern = Constants.XXX)`。
2. **状态字段**用 `Byte` + 枚举（`code()` / `check(Byte)`）。
3. **分页**：请求继承 `infrastructure.dto.PageQuery`，返回 `infrastructure.dto.Page<T>`，配 PageHelper 在 Executor 里 `PageHelper.startPage(...)`。
4. **包结构平铺**：每个业务上下文（如 `student`）在 application 下平铺 `dto/executor/event/scheduler`，**不要**为单个类再分子包。

---

## 8. 测试规范

完整样板见 [`assets/tests.md`](assets/tests.md)。

- **默认走真实集成测试**：真 Spring 上下文 + 真 DB，**复用主配置**（不另建 test profile，不维护 `application-test.properties`）。
- **Controller / Executor 测试**类注解三件套：`@SpringBootTest` + `@AutoConfigureMockMvc` + `@Transactional`（结束自动回滚 → "插入真实数据，测试完删除"）。预置数据用 `mapper.insert`，**不要** `@Sql`；用 `UUID` 拼唯一 `no` 避开唯一索引冲突；JSON 序列化用 Jackson 3 `tools.jackson.databind.json.JsonMapper`。
- **应用事件**默认写**最小冒烟测试**：注入真实 bean → `eventDemo.publish("test")`，跑一次看控制台日志即可；**不引 mock / `Awaitility` / `ListAppender`**。仅当监听器只打日志又必须严格断言不漏触发时，才升级到 `ListAppender` + `Awaitility`（[`assets/tests.md` §3.1](assets/tests.md)）。监听器有可观察副作用（写库、调 RPC）时直接断言副作用。
- **何时用 mock**：仅当**没有 DB / 起不了 Spring 上下文**时才用 `@MockitoSpyBean` / `@Mock`。
- **Spring Boot 4 注意**：`pom.xml` 显式加 `spring-boot-starter-webmvc-test`（MockMvc 自动装配在独立模块）。

---

## 9. 新增业务用例的步骤模板

以"新增 Teacher 模块的 createTeacher"为例（按业务语义命名表，命名取舍见 [`assets/templates.md`](assets/templates.md) 顶部）：

1. **DB**：`build/sql/all.sql` 加表（如主信息表 `teacher_info`）。
2. **PO + Mapper + XML**：`domain/dao/po/TeacherInfoPO.java`（含 `Condition`） + `domain/dao/mapper/TeacherInfoMapper.java` + `resources/mappings/TeacherInfoMapper.xml`。
3. **枚举**（如需要）：`domain/dao/types/TeacherTitle.java`。
4. **Domain Service**：`domain/service/teacher/TeacherService.java` + `vo/TeacherVO.java`，写操作 `@Transactional`。
5. **Application 层**：`dto/CreateTeacherCmd.java` + `executor/CreateTeacherCmdExe.java`。
6. **Interfaces 层**：`interfaces/TeacherController.java`，`@PostMapping("/teacher/create")` 委托给 Exe。
7. **错误码**：`ErrorCode` 加新段位（如 2100-2199）。
8. **测试**：参照 [`assets/tests.md` §2](assets/tests.md) 骨架，跑一遍真实创建/查询/删除链路。

---

## 10. 反模式（看到立即改）

- ❌ Controller 写 `if/else` 业务判断 → ✅ 移到 Executor 或 Service。
- ❌ Controller 用 `GET ?id=1` → ✅ 全部 POST + JSON Body。
- ❌ `@Autowired` 字段注入 → ✅ `@RequiredArgsConstructor` + `private final`。
- ❌ Cmd Executor 直接调 `Mapper` → ✅ 写操作必须经过 Service（CQRS §2.1）。
- ❌ 为简单单表查询硬抽 `findById` 薄壳 Service 方法 → ✅ Qry Exe 直接注入 Mapper。
- ❌ Service 返回 `XxxCO` → ✅ Service 返回 `VO` / `PO` / 基本类型，Exe 装配 CO。
- ❌ DTO 字段用 `Date` → ✅ `LocalDate` / `LocalDateTime` + `@JsonFormat(pattern = Constants.XXX)`。
- ❌ 错误码硬编码字符串 → ✅ 入 `ErrorCode` 枚举。
- ❌ `domain` 层 `import org.springframework.web.*` → ✅ 上提到 `application` / `interfaces`。
- ❌ Mapper 返回 `XxxCO` / `XxxVO` 或加 `findActiveXxx` 业务方法 → ✅ 只返 PO / 基本类型；业务过滤放 Service，给 `Condition` 加字段。
- ❌ Mapper 接口加 `@Mapper`、用 `@Select` / `@Insert` 写 SQL、XML 里 `select *` → ✅ `@MapperScan` 扫描 + 全 XML + `<include refid="Base_Column_List" />`。
- ❌ PO 加 `@Builder` / 自定义全参构造 → ✅ 只 `@Getter @Setter`。
