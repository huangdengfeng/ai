package com.seezoon.application.student.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 查询学生详细信息
 */
@Getter
@Setter
@ToString
public class StudentDetailQry {

    @Schema(description = "学生ID")
    @NotNull
    private Integer id;
}

