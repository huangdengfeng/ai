package com.seezoon.application.student.dto;

import com.seezoon.infrastructure.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 获取学生信息
 */
@Getter
@Setter
@ToString
public class StudentPageQry extends PageQuery {

    @Schema(description = "学生ID")
    private Integer id;

    @Schema(description = "学号")
    private String no;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "状态：1、有效；2、无效")
    private Byte status;
}