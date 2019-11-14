package com.open.datasource.controller;

import com.open.datasource.service.UserService;
import com.open.datasource.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by shuai on 2017/5/28.
 */
@Controller
public class TestController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/api/v1/test/getUser")
    @ResponseBody
    public UserVo getUser(@RequestParam(value = "id") Long id) {
        return userService.getUser(id);
    }

    @RequestMapping(value = "/api/v2/user/getUserInfoWithoutSec")
    @ResponseBody
    public String ewq(@RequestBody String a) {
        System.out.println("");
        System.out.println(a);
        return "dddddd";
    }


}
