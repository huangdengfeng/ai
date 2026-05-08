CREATE DATABASE IF NOT EXISTS `java_demo_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `java_demo_db`;

DROP TABLE IF EXISTS `student_info`;
CREATE TABLE `student_info` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '学生ID',
    `no` varchar(64) NOT NULL COMMENT '学号',
    `name` varchar(255) NOT NULL COMMENT '姓名',
    `sex` tinyint  NOT NULL COMMENT '性别：1、男；2、女',
    `introduce` text DEFAULT NULL COMMENT '介绍',
    `birthday` date DEFAULT NULL COMMENT '生日',
    `mobile`   varchar(45)  DEFAULT NULL COMMENT '手机号' ,
    `status` tinyint NOT NULL COMMENT '状态：1、有效；2、无效',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_time` datetime NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uidx_no` (`no`),
    KEY `idx_name` (`name`),
    KEY `idx_mobile` (`mobile`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=10000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT '学生信息表';