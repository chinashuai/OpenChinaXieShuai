package com.open.logbackappender.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by shuai on 2017/5/28.
 */
@Controller
public class LogBackAppendersController {

    @RequestMapping("/api/v1/logback/log")
    @ResponseBody
    public Integer log(@RequestParam(required = false) String localIp,
                       @RequestParam(required = true) String localSystemName, @RequestParam(required = true) String env,
                       @RequestParam(required = true) String body) {
        System.out.println("结果为：");
        System.out.println(body);
        return 1;
    }

//    public static void main(String[] args) throws IOException {
//        HttpClient client = new HttpClient();
//        client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("admin", "shuai"));
//        GetMethod getMethod = new GetMethod("http://39.106.96.209:15672/api/nodes");
//        client.executeMethod(getMethod);
//        String a = getMethod.getResponseBodyAsString();
//        System.out.println(a);
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//    }
}
