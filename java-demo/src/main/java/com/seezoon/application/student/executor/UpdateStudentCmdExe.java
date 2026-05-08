package com.seezoon.application.student.executor;

import com.seezoon.application.student.dto.UpdateStudentCmd;
import com.seezoon.domain.service.student.StudentService;
import com.seezoon.domain.service.student.vo.StudentVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * 更新学生信息
 */
@RequiredArgsConstructor
@Slf4j
@Component
@Validated
public class UpdateStudentCmdExe {

    private final StudentService studentService;

    public void execute(@Valid @NotNull UpdateStudentCmd cmd) {
        log.info("update cmd:{}",cmd);
        StudentVO vo = new StudentVO();
        vo.setId(cmd.getId());
        vo.setNo(cmd.getNo());
        vo.setName(cmd.getName());
        vo.setSex(cmd.getSex());
        vo.setIntroduce(cmd.getIntroduce());
        vo.setBirthday(cmd.getBirthday());
        vo.setMobile(cmd.getMobile());
        vo.setStatus(cmd.getStatus());
        studentService.updateStudent(vo);
    }
} 