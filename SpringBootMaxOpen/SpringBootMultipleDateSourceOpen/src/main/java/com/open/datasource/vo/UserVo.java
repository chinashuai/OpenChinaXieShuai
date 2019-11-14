package com.open.datasource.vo;

/**
 * Created by shuai on 2017/5/28.
 */
public class UserVo {

    private Long id;

    private String userName;

    private SchoolVo schoolVo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public SchoolVo getSchoolVo() {
        return schoolVo;
    }

    public void setSchoolVo(SchoolVo schoolVo) {
        this.schoolVo = schoolVo;
    }
}
