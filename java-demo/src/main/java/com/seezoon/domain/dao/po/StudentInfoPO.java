package com.seezoon.domain.dao.po;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentInfoPO {

    /**
     * 学生ID NOT NULL
     */
    private Integer id;
    /**
     * 学号  NOT NULL
     */
    private String no;
    /**
     * 姓名  NOT NULL
     */
    private String name;
    /**
     * 性别：1、男；2、女 NOT NULL
     *
     * @see com.seezoon.domain.dao.types.StudentInfoSex
     */
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
     * 状态：1、有效；2、无效；  NOT NULL
     *
     * @see com.seezoon.domain.dao.types.DbRecordStatus
     */

    private Byte status;
    /**
     * 创建时间  NOT NULL
     */
    private LocalDateTime createTime;
    /**
     * 更新时间  NOT NULL
     */
    private LocalDateTime updateTime;


    @Getter
    @Setter
    public static class Condition {

        /**
         * 学生ID
         */
        private Integer id;
        /**
         * 学号
         */
        private String no;
        /**
         * 姓名
         */
        private String name;

        /**
         * 手机号
         */
        private String mobile;

        /**
         * 状态
         *
         * @see com.seezoon.domain.dao.types.DbRecordStatus
         */
        private Byte status;
    }
}