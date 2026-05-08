# 测试样板（tests）

> 本工程**默认走真实集成测试**：真 Spring 上下文 + 真 DB，靠 `@Transactional` 自动回滚清理数据。
> 不引 mock；使用主配置（`application*.yml`）连本地 DB，**不另建 test profile**。
> 仅在没有 DB / 起不了 Spring 时才考虑用 mock。

---

## 1. Maven 依赖（`pom.xml`，Spring Boot 4 需要）

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<!-- Spring Boot 4: MockMvc 自动装配位于独立模块 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc-test</artifactId>
    <scope>test</scope>
</dependency>
```

---

## 2. Controller / Executor 集成测试样板

> 路径：`src/test/java/<和被测类同源 package>/XxxControllerTest.java`

```java
package com.example.app.interfaces;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.app.application.xxx.dto.CreateXxxCmd;
import com.example.app.application.xxx.dto.DeleteXxxCmd;
import com.example.app.application.xxx.dto.UpdateXxxCmd;
import com.example.app.application.xxx.dto.XxxDetailQry;
import com.example.app.application.xxx.dto.XxxPageQry;
import com.example.app.domain.dao.mapper.XxxInfoMapper;
import com.example.app.domain.dao.po.XxxInfoPO;
import com.example.app.domain.dao.types.DbRecordStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

/**
 * Xxx Controller 集成测试（真实 DB）。
 *
 * <p>策略：
 * <ul>
 *     <li>{@link SpringBootTest} 启动完整应用上下文（含 MyBatis、HikariCP、真实 MySQL 连接）。</li>
 *     <li>{@link AutoConfigureMockMvc} 提供同进程内的 MockMvc，无需启动 Tomcat。</li>
 *     <li>{@link Transactional} 注解使每个测试方法运行在独立事务中，结束后自动回滚 ——
 *         "插入真实数据，测试完删除"。Service 层默认 REQUIRED 传播，会加入测试事务。</li>
 * </ul>
 *
 * <p>前置：本机 MySQL 已起，库已建表（参考 templates.md 的 DDL）。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class XxxControllerTest {

    private static final String BASE = "/xxx";
    private final JsonMapper objectMapper = JsonMapper.builder().build();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private XxxInfoMapper xxxInfoMapper;

    /** 唯一编号生成，避开 uidx_no 冲突。 */
    private static String uniqueNo(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    /** 通过 mapper 直接预置一条记录，返回主键。 */
    private Integer insertFixture(String namePrefix) {
        XxxInfoPO po = new XxxInfoPO();
        po.setNo(uniqueNo("FX"));
        po.setName(namePrefix + "_" + System.nanoTime());
        po.setSex((byte) 1);
        po.setIntroduce("fixture");
        po.setBirthday(LocalDate.of(2000, 1, 1));
        po.setMobile("13800000000");
        po.setStatus(DbRecordStatus.VALID.code());
        po.setCreateTime(LocalDateTime.now());
        po.setUpdateTime(LocalDateTime.now());
        int affected = xxxInfoMapper.insert(po);
        assertThat(affected).isEqualTo(1);
        assertThat(po.getId()).isNotNull();
        return po.getId();
    }

    @Test
    void create_shouldPersistRecord() throws Exception {
        CreateXxxCmd cmd = new CreateXxxCmd();
        String no = uniqueNo("CR");
        cmd.setNo(no);
        cmd.setName("张三");
        cmd.setSex((byte) 1);
        cmd.setBirthday(LocalDate.of(2000, 1, 1));
        cmd.setMobile("13800000000");

        mockMvc.perform(post(BASE + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isOk());

        XxxInfoPO persisted = xxxInfoMapper.selectByNo(no);
        assertThat(persisted).isNotNull();
        assertThat(persisted.getName()).isEqualTo("张三");
        assertThat(persisted.getStatus()).isEqualTo(DbRecordStatus.VALID.code());
    }

    @Test
    void create_duplicateNo_shouldFail() throws Exception {
        Integer id = insertFixture("dup");
        XxxInfoPO fixture = xxxInfoMapper.selectByPrimaryKey(id);

        CreateXxxCmd cmd = new CreateXxxCmd();
        cmd.setNo(fixture.getNo());
        cmd.setName("重复编号");
        cmd.setSex((byte) 1);

        mockMvc.perform(post(BASE + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void update_shouldChangeFields() throws Exception {
        Integer id = insertFixture("upd");

        UpdateXxxCmd cmd = new UpdateXxxCmd();
        cmd.setId(id);
        cmd.setNo(uniqueNo("UP"));
        cmd.setName("李四改名");
        cmd.setSex((byte) 2);
        cmd.setStatus(DbRecordStatus.VALID.code());

        mockMvc.perform(post(BASE + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isOk());

        XxxInfoPO updated = xxxInfoMapper.selectByPrimaryKey(id);
        assertThat(updated.getNo()).isEqualTo(cmd.getNo());
        assertThat(updated.getName()).isEqualTo("李四改名");
    }

    @Test
    void delete_shouldRemoveRecord() throws Exception {
        Integer id = insertFixture("del");
        DeleteXxxCmd cmd = new DeleteXxxCmd();
        cmd.setId(id);

        mockMvc.perform(post(BASE + "/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isOk());

        assertThat(xxxInfoMapper.selectByPrimaryKey(id)).isNull();
    }

    @Test
    void detail_shouldReturnRecord() throws Exception {
        Integer id = insertFixture("detail");
        XxxInfoPO fixture = xxxInfoMapper.selectByPrimaryKey(id);

        XxxDetailQry qry = new XxxDetailQry();
        qry.setId(id);

        mockMvc.perform(post(BASE + "/detail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(qry)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.no").value(fixture.getNo()));
    }

    @Test
    void detail_notExists_shouldFail() throws Exception {
        XxxDetailQry qry = new XxxDetailQry();
        qry.setId(Integer.MAX_VALUE);

        mockMvc.perform(post(BASE + "/detail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(qry)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void page_shouldFindInsertedRecord() throws Exception {
        Integer id = insertFixture("page");
        XxxInfoPO fixture = xxxInfoMapper.selectByPrimaryKey(id);

        XxxPageQry qry = new XxxPageQry();
        qry.setNo(fixture.getNo());
        qry.setPage(1);
        qry.setPageSize(10);

        mockMvc.perform(post(BASE + "/page")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(qry)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(id));
    }
}
```

要点：
- 类上注解三件套：`@SpringBootTest` + `@AutoConfigureMockMvc` + `@Transactional`，**不加** `@ActiveProfiles`。
- 用 `UUID` 拼唯一 `no`，避开唯一索引冲突——事务即便回滚也不会复用同一个唯一键。
- 预置数据用 `mapper.insert`，**不要**用 `@Sql` 脚本（保持纯 Java 可读）。
- `JsonMapper` 用 Jackson 3（`tools.jackson.databind.json.JsonMapper`），不要错用旧 `ObjectMapper`。

---

## 3. 应用事件冒烟测试样板

> 默认采用最小冒烟测试：注入真实 bean 触发 publish，**通过控制台日志肉眼确认**所有监听器（同步 / 异步 / 事务后）都打印了预期日志。
> 这种写法不用 mock、不用 `Awaitility`，跑一次能立刻看见效果，对 demo 与轻量级监听器够用。

```java
package com.example.app.application.xxx.event;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Xxx 事件冒烟测试。
 *
 * <p>跑完观察输出里至少出现：
 * <ul>
 *     <li>{@code [sync]} —— 同步监听</li>
 *     <li>{@code [async]} —— 异步监听，必然在非主线程</li>
 * </ul>
 */
@SpringBootTest
class XxxEventDemoTest {

    @Autowired
    private XxxEventDemo eventDemo;

    @Test
    public void send() {
        eventDemo.publish("test");
    }
}
```
