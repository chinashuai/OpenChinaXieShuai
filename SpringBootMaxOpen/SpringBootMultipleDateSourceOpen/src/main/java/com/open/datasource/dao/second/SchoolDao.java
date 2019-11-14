package com.open.datasource.dao.second;

import com.open.datasource.vo.SchoolVo;
import org.apache.ibatis.annotations.Param;

/**
 * Created by shuai on 2017/5/28.
 */
public interface SchoolDao {

    SchoolVo findByName(@Param(value = "schoolName") String schoolName);
}
