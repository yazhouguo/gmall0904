package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.enums.OrderStatus;
import com.atguigu.gmall.enums.ProcessStatus;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManageService;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.UserService;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Controller
public class OrderController {

    @Reference
    UserService userService;

    @Reference
    CartService cartService;

    @Reference
    OrderService orderService;

    @Reference
    ManageService manageService;

    @GetMapping("trade")
    @LoginRequire
    public String trade(HttpServletRequest request){
        String userId =(String) request.getAttribute("userId");
        //1.用户地址列表
        List<UserAddress> userAddressList = userService.getUserAddressList(userId);
        request.setAttribute("userAddressList",userAddressList);
        //用户需要结账的商品清单
        List<CartInfo> checkedCartList = cartService.getCheckedCartList(userId);

        BigDecimal totalAmount = new BigDecimal("0");
        for (CartInfo cartInfo : checkedCartList) {
            BigDecimal cartInfoAmount = cartInfo.getSkuPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
            totalAmount = totalAmount.add(cartInfoAmount);
        }

        String token = orderService.getToken(userId);

        request.setAttribute("tradeNo",token);

        request.setAttribute("checkedCartList",checkedCartList);

        request.setAttribute("totalAmount",totalAmount);


        return "trade";
    }

    @PostMapping("submitOrder")
    @LoginRequire
    public String submitOrder(OrderInfo orderInfo,HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");
        String tradeNo = request.getParameter("tradeNo");
        boolean isEnableToken = orderService.verifyToken(userId, tradeNo);
        if(!isEnableToken){
            request.setAttribute("errMsg","页面已失效,请重新结算!");
            return "tradeFail";
        }


        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        orderInfo.setCreateTime(new Date());
        orderInfo.setExpireTime(DateUtils.addMinutes(new Date(),15));
        orderInfo.sumTotalAmount();
        orderInfo.setUserId(userId);
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            SkuInfo skuInfo = manageService.getSkuInfo(orderDetail.getSkuId());
            orderDetail.setImgUrl(skuInfo.getSkuDefaultImg());
            orderDetail.setSkuName(skuInfo.getSkuName());

            if(!orderDetail.getOrderPrice().equals(skuInfo.getPrice())){
                request.setAttribute("errMsg","商品价格已发生变动请重新下单!");
                return "tradeFail";
            }
        }

        orderService.saveOrder(orderInfo);


        return "redirect://payment.gmall.com/index";

    }




}


















