package com.seezoon.application.student.executor;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageSerializable;
import com.seezoon.application.student.dto.StudentPageQry;
import com.seezoon.application.student.dto.clientobject.StudentCO;
import com.seezoon.domain.dao.mapper.StudentInfoMapper;
import com.seezoon.domain.dao.po.StudentInfoPO;
import com.seezoon.infrastructure.dto.Page;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * 获取学生信息
 */
@RequiredArgsConstructor
@Slf4j
@Component
@Validated
public class StudentPageQryExe {

    private final StudentInfoMapper studentInfoMapper;

    public Page<StudentCO> execute(@Valid @NotNull StudentPageQry qry) {
        log.info("page qry:{}",qry);

        StudentInfoPO.Condition condition = new StudentInfoPO.Condition();
        condition.setId(qry.getId());
        condition.setName(qry.getName());
        condition.setNo(qry.getNo());
        condition.setMobile(qry.getMobile());
        condition.setStatus(qry.getStatus());
        PageHelper.startPage(qry.getPage(), qry.getPageSize());
        PageSerializable<StudentInfoPO> page = new PageSerializable<>(
                studentInfoMapper.selectByCondition(condition));
        List<StudentCO> data = new ArrayList<>();
        page.getList().forEach(item -> {
            StudentCO co = new StudentCO();
            co.setId(item.getId());
            co.setNo(item.getNo());
            co.setName(item.getName());
            co.setSex(item.getSex());
            co.setIntroduce(item.getIntroduce());
            co.setBirthday(item.getBirthday());
            co.setMobile(item.getMobile());
            co.setStatus(item.getStatus());
            co.setCreateTime(item.getCreateTime());
            co.setUpdateTime(item.getUpdateTime());
            data.add(co);
        });
        return new Page<>(page.getTotal(), data);
    }
} 