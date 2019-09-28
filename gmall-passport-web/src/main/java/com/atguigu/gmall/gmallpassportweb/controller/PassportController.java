package com.atguigu.gmall.gmallpassportweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.util.JwtUtil;
import org.junit.Test;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Reference
     UserService userService;

    String jwtKey="atguigu";

    @GetMapping("index")
    public String index(@RequestParam("originUrl") String originUrl, Model model){
        model.addAttribute("originUrl",originUrl);
        return "index";
    }

    @PostMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo, HttpServletRequest request){

        UserInfo userInfoExist = userService.login(userInfo);
        if(userInfoExist!=null) {
            Map<String, Object> map = new HashMap<>();
            map.put("userId",userInfoExist.getId());
            map.put("nickName",userInfoExist.getNickName());
            String ipAddr = request.getHeader("X-forwarded-for");
            String token = JwtUtil.encode(jwtKey, map, ipAddr);
            return token;
        }
        return "file";
    }

    @Test
    public void test01(){
        String key = "atguigu";
        String ip="192.168.6.6";
        Map map = new HashMap();
        map.put("userId","123");
        map.put("nickName","zhangsan");
        String token = JwtUtil.encode(key, map, ip);
        System.out.println(token);
        Map<String, Object> decode = JwtUtil.decode(token, key, "192.168.6.6");
        System.out.println(decode);
    }


    @GetMapping("verify")
    @ResponseBody
    public String verify(@RequestParam("token")String token,@RequestParam("currentIP")String currentIp){
        //1.验证token
        Map<String, Object> userMap = JwtUtil.decode(token, jwtKey, currentIp);

        //2.验证缓存
        if(userMap!=null){
            String  userId = (String)userMap.get("userId");
            Boolean isLogin = userService.verify(userId);
            if(isLogin){
                return "success";
            }
        }
        return "fail";

    }

}


















