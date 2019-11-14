package com.open.logback.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by shuai on 2019/1/22.
 */
@RestController
public class TestPerformanceController {

    @RequestMapping(value = "/api/test/per/data")
    public void tr(@RequestParam(value = "t") int t) {
        t = t == 0 ? 10000 : t;
        notIncludeCallerData(t);
        includeCallerData(t);
    }

    public void notIncludeCallerData(int t) {
        Logger LOGGER = LoggerFactory.getLogger("notIncludeCallerData");
        long t1 = System.currentTimeMillis();
        int count = 0;
        while (count < t) {
            LOGGER.info("没有调用者信息的日志，打印当前的count[{}],打印一行日志,时间[{}]", count, System.currentTimeMillis());
            ++count;
        }
        LOGGER.info("没有调用者信息的日志，当前打印完成,花费时间为[{}]", System.currentTimeMillis() - t1);
        System.out.println("没有调用者信息的日志，当前打印完成,花费时间为[ " + (System.currentTimeMillis() - t1) + " ms]");
    }

    public void includeCallerData(int t) {
        Logger LOGGER = LoggerFactory.getLogger("includeCallerData");
        long t1 = System.currentTimeMillis();
        int count = 0;
        while (count < t) {
            LOGGER.info("有调用者信息的日志，打印当前的count[{}],打印一行日志,时间[{}]", count, System.currentTimeMillis());
            ++count;
        }
        LOGGER.info("有调用者信息的日志，当前打印完成,花费时间为[{}]", System.currentTimeMillis() - t1);
        System.out.println("有调用者信息的日志，当前打印完成,花费时间为[ " + (System.currentTimeMillis() - t1) + " ms]");
    }
}