package com.open.datasource.vo;

/**
 * Created by shuai on 2017/5/28.
 */
public class SchoolVo {

    private Long id;

    private String SchoolName;

    private String SchoolDescribe;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSchoolName() {
        return SchoolName;
    }

    public void setSchoolName(String schoolName) {
        SchoolName = schoolName;
    }

    public String getSchoolDescribe() {
        return SchoolDescribe;
    }

    public void setSchoolDescribe(String schoolDescribe) {
        SchoolDescribe = schoolDescribe;
    }
}
