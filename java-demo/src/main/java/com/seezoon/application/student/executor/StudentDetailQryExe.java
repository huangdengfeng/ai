package com.seezoon.application.student.executor;

import com.seezoon.application.student.dto.StudentDetailQry;
import com.seezoon.application.student.dto.clientobject.StudentDetailCO;
import com.seezoon.domain.dao.mapper.StudentInfoMapper;
import com.seezoon.domain.dao.po.StudentInfoPO;
import com.seezoon.infrastructure.error.ErrorCode;
import com.seezoon.infrastructure.exception.ExceptionFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * 查询学生详细信息
 */
@RequiredArgsConstructor
@Slf4j
@Component
@Validated
public class StudentDetailQryExe {

    private final StudentInfoMapper studentInfoMapper;

    public StudentDetailCO execute(@Valid @NotNull StudentDetailQry qry) {
        log.info("detail qry:{}",qry);

        StudentInfoPO po = studentInfoMapper.selectByPrimaryKey(qry.getId());
        if (po == null) {
            log.error("student not exists id:{}", qry.getId());
            throw ExceptionFactory.bizException(ErrorCode.RECORD_NOT_EXISTS);
        }

        StudentDetailCO co = new StudentDetailCO();
        co.setId(po.getId());
        co.setNo(po.getNo());
        co.setName(po.getName());
        co.setSex(po.getSex());
        co.setIntroduce(po.getIntroduce());
        co.setBirthday(po.getBirthday());
        co.setMobile(po.getMobile());
        co.setStatus(po.getStatus());
        co.setCreateTime(po.getCreateTime());
        co.setUpdateTime(po.getUpdateTime());

        return co;
    }
}

