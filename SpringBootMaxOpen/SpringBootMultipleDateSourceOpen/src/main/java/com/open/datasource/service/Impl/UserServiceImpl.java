package com.open.datasource.service.Impl;

import com.open.datasource.dao.master.UserDao;
import com.open.datasource.dao.second.SchoolDao;
import com.open.datasource.service.UserService;
import com.open.datasource.vo.SchoolVo;
import com.open.datasource.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by shuai on 2017/5/28.
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;
    @Autowired
    private SchoolDao schoolDao;

    public UserVo getUser(Long id) {
        UserVo userVo = userDao.findById(id);
        SchoolVo schoolVo = schoolDao.findByName("清华");
        userVo.setSchoolVo(schoolVo);
        return userVo;
    }

}
