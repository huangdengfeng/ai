package com.seezoon.application.student.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.seezoon.infrastructure.constants.Constants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 更新学生信息
 */
@Getter
@Setter
@ToString
public class UpdateStudentCmd {

    @Schema(description = "学生ID")
    @NotNull
    private Integer id;

    @Schema(description = "学号")
    @NotEmpty
    @Size(max = 64)
    private String no;

    @Schema(description = "姓名")
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

    @Schema(description = "状态：1、有效；2、停用")
    @NotNull
    private Byte status;
} 