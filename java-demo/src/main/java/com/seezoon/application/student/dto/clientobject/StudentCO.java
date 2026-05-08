package com.seezoon.application.student.dto.clientobject;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.seezoon.infrastructure.constants.Constants;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 学生信息客户端对象
 */
@Getter
@Setter
@ToString
public class StudentCO {

    @Schema(description = "学生ID")
    private Integer id;

    @Schema(description = "学号")
    private String no;

    @Schema(description = "姓名")
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

    @JsonFormat(pattern = Constants.DATETIME_PATTERN)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
} 