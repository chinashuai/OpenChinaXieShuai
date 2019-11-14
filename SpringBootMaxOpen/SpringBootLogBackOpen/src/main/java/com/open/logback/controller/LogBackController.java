package com.open.logback.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * Created by shuai on 2017/5/28.
 */
@Controller
public class LogBackController {
    private final static Logger LOGGER = LoggerFactory.getLogger(LogBackController.class);

    @RequestMapping(value = "/api/v1/logback/testLog")
    @ResponseBody
    public String demoReturnSuccess() {
        LOGGER.error("这是一个error的测试消息");
        return "ok";
    }




}
