package com.open.datasource.dao.master;

import com.open.datasource.vo.UserVo;
import org.apache.ibatis.annotations.Param;

/**
 * Created by shuai on 2017/5/28.
 */
public interface UserDao {

    UserVo findById(@Param(value = "id") Long id);
}
