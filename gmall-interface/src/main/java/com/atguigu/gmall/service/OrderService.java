package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OrderInfo;

public interface OrderService {
    public void saveOrder(OrderInfo orderInfo);

    public String getToken(String userId);

    public boolean verifyToken(String userId,String token);

}
