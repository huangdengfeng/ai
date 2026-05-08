package com.seezoon.application.student.executor;

import com.seezoon.application.student.dto.CreateStudentCmd;
import com.seezoon.domain.dao.types.DbRecordStatus;
import com.seezoon.domain.service.student.StudentService;
import com.seezoon.domain.service.student.vo.StudentVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * 创建学生信息
 */
@RequiredArgsConstructor
@Slf4j
@Component
@Validated
public class CreateStudentCmdExe {

    private final StudentService studentService;

    public void execute(@Valid @NotNull CreateStudentCmd cmd) {
        log.info("create cmd:{}",cmd);
        StudentVO vo = new StudentVO();
        vo.setNo(cmd.getNo());
        vo.setName(cmd.getName());
        vo.setSex(cmd.getSex());
        vo.setIntroduce(cmd.getIntroduce());
        vo.setBirthday(cmd.getBirthday());
        vo.setMobile(cmd.getMobile());
        vo.setStatus(DbRecordStatus.VALID.code());

        Integer id = studentService.createStudent(vo);
        log.info("create student success id:{}", id);
    }
} 