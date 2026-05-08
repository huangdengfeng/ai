package com.seezoon.interfaces;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.seezoon.application.student.dto.CreateStudentCmd;
import com.seezoon.application.student.dto.DeleteStudentCmd;
import com.seezoon.application.student.dto.StudentDetailQry;
import com.seezoon.application.student.dto.StudentPageQry;
import com.seezoon.application.student.dto.UpdateStudentCmd;
import com.seezoon.domain.dao.mapper.StudentInfoMapper;
import com.seezoon.domain.dao.po.StudentInfoPO;
import com.seezoon.domain.dao.types.DbRecordStatus;
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
 * {@link StudentController} 集成测试（真实 DB）。
 *
 * <p>策略：
 * <ul>
 *     <li>{@link SpringBootTest} 启动完整应用上下文（含 MyBatis、HikariCP、真实 MySQL 连接）。</li>
 *     <li>{@link AutoConfigureMockMvc} 提供同进程内的 MockMvc，无需启动 Tomcat。</li>
 *     <li>{@link Transactional} 注解使每个测试方法运行在独立事务中，
 *         测试结束后由 Spring TestContext 自动回滚 —— "插入真实数据，测试完删除"。</li>
 *     <li>因 service 层的 {@code @Transactional} 默认 REQUIRED 传播，
 *         它会加入测试事务，故 controller→executor→service→mapper 路径的所有写入都会随测试一起回滚。</li>
 * </ul>
 *
 * <p>前置条件：本机 MySQL 运行在 127.0.0.1:3306，root/12345678，库 {@code java_demo_db} 已建表
 * （见 {@code build/sql/all.sql}）。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StudentControllerTest {

    private static final String BASE = "/student";
    private final JsonMapper objectMapper = JsonMapper.builder().build();
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private StudentInfoMapper studentInfoMapper;

    /**
     * 生成一个全局唯一的学号，避开 uidx_no 唯一索引冲突（即便事务回滚也使用唯一值）。
     */
    private static String uniqueNo(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    /**
     * 在 DB 中预置一条学生记录，返回其主键。
     */
    private Integer insertFixture(String namePrefix) {
        StudentInfoPO po = new StudentInfoPO();
        po.setNo(uniqueNo("FX"));
        po.setName(namePrefix + "_" + System.nanoTime());
        po.setSex((byte) 1);
        po.setIntroduce("fixture");
        po.setBirthday(LocalDate.of(2000, 1, 1));
        po.setMobile("13800000000");
        po.setStatus(DbRecordStatus.VALID.code());
        po.setCreateTime(LocalDateTime.now());
        po.setUpdateTime(LocalDateTime.now());
        int affected = studentInfoMapper.insert(po);
        assertThat(affected).isEqualTo(1);
        assertThat(po.getId()).isNotNull();
        return po.getId();
    }

    @Test
    void createStudent_shouldPersistRecord() throws Exception {
        CreateStudentCmd cmd = new CreateStudentCmd();
        String no = uniqueNo("CR");
        cmd.setNo(no);
        cmd.setName("张三");
        cmd.setSex((byte) 1);
        cmd.setIntroduce("一名学生");
        cmd.setBirthday(LocalDate.of(2000, 1, 1));
        cmd.setMobile("13800000000");

        mockMvc.perform(post(BASE + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isOk());

        StudentInfoPO persisted = studentInfoMapper.selectByNo(no);
        assertThat(persisted).isNotNull();
        assertThat(persisted.getName()).isEqualTo("张三");
        assertThat(persisted.getSex()).isEqualTo((byte) 1);
        assertThat(persisted.getIntroduce()).isEqualTo("一名学生");
        assertThat(persisted.getBirthday()).isEqualTo(LocalDate.of(2000, 1, 1));
        assertThat(persisted.getMobile()).isEqualTo("13800000000");
        assertThat(persisted.getStatus()).isEqualTo(DbRecordStatus.VALID.code());
        assertThat(persisted.getCreateTime()).isNotNull();
    }

    @Test
    void createStudent_duplicateNo_shouldFail() throws Exception {
        Integer fixtureId = insertFixture("dup");
        StudentInfoPO fixture = studentInfoMapper.selectByPrimaryKey(fixtureId);

        CreateStudentCmd cmd = new CreateStudentCmd();
        cmd.setNo(fixture.getNo());
        cmd.setName("重复学号");
        cmd.setSex((byte) 1);

        mockMvc.perform(post(BASE + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void updateStudent_shouldChangeFields() throws Exception {
        Integer id = insertFixture("upd");

        UpdateStudentCmd cmd = new UpdateStudentCmd();
        cmd.setId(id);
        cmd.setNo(uniqueNo("UP"));
        cmd.setName("李四改名");
        cmd.setSex((byte) 2);
        cmd.setStatus(DbRecordStatus.VALID.code());
        cmd.setBirthday(LocalDate.of(1999, 6, 15));
        cmd.setMobile("13911111111");
        cmd.setIntroduce("已更新");

        mockMvc.perform(post(BASE + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isOk());

        StudentInfoPO updated = studentInfoMapper.selectByPrimaryKey(id);
        assertThat(updated).isNotNull();
        assertThat(updated.getNo()).isEqualTo(cmd.getNo());
        assertThat(updated.getName()).isEqualTo("李四改名");
        assertThat(updated.getSex()).isEqualTo((byte) 2);
        assertThat(updated.getMobile()).isEqualTo("13911111111");
        assertThat(updated.getIntroduce()).isEqualTo("已更新");
        assertThat(updated.getBirthday()).isEqualTo(LocalDate.of(1999, 6, 15));
    }

    @Test
    void deleteStudent_shouldRemoveRecord() throws Exception {
        Integer id = insertFixture("del");
        assertThat(studentInfoMapper.selectByPrimaryKey(id)).isNotNull();

        DeleteStudentCmd cmd = new DeleteStudentCmd();
        cmd.setId(id);

        mockMvc.perform(post(BASE + "/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().isOk());

        assertThat(studentInfoMapper.selectByPrimaryKey(id)).isNull();
    }

    @Test
    void deleteStudent_notExists_shouldFail() throws Exception {
        DeleteStudentCmd cmd = new DeleteStudentCmd();
        cmd.setId(Integer.MAX_VALUE);

        mockMvc.perform(post(BASE + "/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void studentPage_shouldFindInsertedRecord() throws Exception {
        Integer id = insertFixture("page");
        StudentInfoPO fixture = studentInfoMapper.selectByPrimaryKey(id);

        StudentPageQry qry = new StudentPageQry();
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
                .andExpect(jsonPath("$.data[0].id").value(id))
                .andExpect(jsonPath("$.data[0].no").value(fixture.getNo()))
                .andExpect(jsonPath("$.data[0].name").value(fixture.getName()))
                .andExpect(jsonPath("$.data[0].birthday").value("2000-01-01"));
    }

    @Test
    void getStudentDetail_shouldReturnRecord() throws Exception {
        Integer id = insertFixture("detail");
        StudentInfoPO fixture = studentInfoMapper.selectByPrimaryKey(id);

        StudentDetailQry qry = new StudentDetailQry();
        qry.setId(id);

        mockMvc.perform(post(BASE + "/detail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(qry)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.no").value(fixture.getNo()))
                .andExpect(jsonPath("$.name").value(fixture.getName()))
                .andExpect(jsonPath("$.sex").value(1))
                .andExpect(jsonPath("$.mobile").value("13800000000"))
                .andExpect(jsonPath("$.birthday").value("2000-01-01"))
                .andExpect(jsonPath("$.status").value((int) DbRecordStatus.VALID.code()));
    }

    @Test
    void getStudentDetail_notExists_shouldFail() throws Exception {
        StudentDetailQry qry = new StudentDetailQry();
        qry.setId(Integer.MAX_VALUE);

        mockMvc.perform(post(BASE + "/detail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(qry)))
                .andExpect(status().is4xxClientError());
    }
}
