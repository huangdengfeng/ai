package com.seezoon.domain.dao.mapper;

import com.seezoon.domain.dao.po.StudentInfoPO;
import com.seezoon.domain.dao.po.StudentInfoPO.Condition;
import java.util.List;

public interface StudentInfoMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(StudentInfoPO row);

    StudentInfoPO selectByPrimaryKey(Integer id);

    StudentInfoPO selectByPrimaryKeyForUpdate(Integer id);

    List<StudentInfoPO> selectByCondition(Condition condition);

    /**
     * 根据学号查询学生信息
     */
    StudentInfoPO selectByNo(String no);

    int updateByPrimaryKeySelective(StudentInfoPO row);

    int updateByPrimaryKey(StudentInfoPO row);
}