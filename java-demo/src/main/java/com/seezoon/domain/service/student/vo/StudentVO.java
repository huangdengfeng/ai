package com.seezoon.domain.service.student.vo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentVO {

    /**
     * 学生ID
     */
    private Integer id;
    /**
     * 学号
     */
    @NotEmpty
    private String no;

    /**
     * 姓名
     */
    @NotEmpty
    private String name;

    /**
     * 性别
     *
     * @see com.seezoon.domain.dao.types.StudentInfoSex
     */
    @NotNull
    private Byte sex;

    /**
     * 介绍
     */
    private String introduce;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 手机号
     */
    private String mobile;
    /**
     * 状态
     *
     * @see com.seezoon.domain.dao.types.DbRecordStatus
     */
    @NotNull
    private Byte status;
}
