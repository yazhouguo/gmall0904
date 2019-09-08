package com.atguigu.com.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    @Reference
    UserService userService;

    @GetMapping("trade")
    public UserInfo trade(String userid){
        UserInfo userInfo = userService.getUserInfoById(userid);
        return userInfo;
    }
}
