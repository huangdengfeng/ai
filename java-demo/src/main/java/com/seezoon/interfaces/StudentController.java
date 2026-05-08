package com.seezoon.interfaces;

import com.seezoon.application.student.dto.CreateStudentCmd;
import com.seezoon.application.student.dto.DeleteStudentCmd;
import com.seezoon.application.student.dto.StudentDetailQry;
import com.seezoon.application.student.dto.StudentPageQry;
import com.seezoon.application.student.dto.UpdateStudentCmd;
import com.seezoon.application.student.dto.clientobject.StudentCO;
import com.seezoon.application.student.dto.clientobject.StudentDetailCO;
import com.seezoon.application.student.executor.CreateStudentCmdExe;
import com.seezoon.application.student.executor.DeleteStudentCmdExe;
import com.seezoon.application.student.executor.StudentDetailQryExe;
import com.seezoon.application.student.executor.StudentPageQryExe;
import com.seezoon.application.student.executor.UpdateStudentCmdExe;
import com.seezoon.infrastructure.dto.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学生信息管理
 */
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/student")
@Tag(name = "学生信息管理")
public class StudentController {

    private final CreateStudentCmdExe createStudentCmdExe;
    private final UpdateStudentCmdExe updateStudentCmdExe;
    private final DeleteStudentCmdExe deleteStudentCmdExe;
    private final StudentPageQryExe studentPageQryExe;
    private final StudentDetailQryExe studentDetailQryExe;

    @PostMapping("/create")
    @Operation(summary = "创建学生信息")
    public void createStudent(@RequestBody CreateStudentCmd cmd) {
        createStudentCmdExe.execute(cmd);
    }

    @PostMapping("/update")
    @Operation(summary = "更新学生信息")
    public void updateStudent(@RequestBody UpdateStudentCmd cmd) {
        updateStudentCmdExe.execute(cmd);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除学生信息")
    public void deleteStudent(@RequestBody DeleteStudentCmd cmd) {
        deleteStudentCmdExe.execute(cmd);
    }

    @PostMapping("/page")
    @Operation(summary = "获取学生信息")
    public Page<StudentCO> studentPage(@RequestBody StudentPageQry qry) {
        return studentPageQryExe.execute(qry);
    }

    @PostMapping("/detail")
    @Operation(summary = "查询学生详细信息")
    public StudentDetailCO getStudentDetail(@RequestBody StudentDetailQry qry) {
        return studentDetailQryExe.execute(qry);
    }
} 