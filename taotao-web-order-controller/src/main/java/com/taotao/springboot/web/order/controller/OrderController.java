package com.taotao.springboot.web.order.controller;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.taotao.springboot.item.domain.pojo.TbItem;
import com.taotao.springboot.order.domain.request.OrderInfo;
import com.taotao.springboot.order.domain.result.TaotaoResult;
import com.taotao.springboot.order.export.OrderResource;
import com.taotao.springboot.sso.domain.pojo.TbUser;
import com.taotao.springboot.web.order.common.utils.CookieUtils;
import com.taotao.springboot.web.order.common.utils.JacksonUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Title: OrderController</p>
 * <p>Description: 订单管理Controller</p>
 * <p>Company: bupt.edu.cn</p>
 * <p>Created: 2018-05-07 00:13</p>
 * @author ChengTengfei
 * @version 1.0
 */
@Controller
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderResource orderResource;

    @Value("${CART_KEY}")
    private String CART_KEY;

    /**
     * 订单确认
     */
    @RequestMapping("/order")
    public String showOrderCart(HttpServletRequest request) {
        log.info("订单确认");
        // #1 用户必须登录
        TbUser user = (TbUser) request.getAttribute("user");
        log.info("订单确认 用户名={}", user.getUsername());
        // #2 根据用户信息，获取收货地址列表（使用静态数据），传递给页面
        // #3 从Cookie中，获取购物车商品列表
        List<TbItem> cartList = getCartItemList(request);
        log.info("订单确认 购物车={}", JacksonUtils.objectToJson(cartList));
        if(cartList==null || cartList.isEmpty()) {
            return "redirect:http://localhost:8086";
        }
        request.setAttribute("cartList", cartList);
        return "order";
    }

    // 从Cookie中，获取购物车商品列表
    private List<TbItem> getCartItemList(HttpServletRequest request) {
        String json = CookieUtils.getCookieValue(request, CART_KEY, true);
        if (StringUtils.isBlank(json)) {
            return new ArrayList<>();
        }
        return JacksonUtils.jsonToList(json, TbItem.class);
    }

    /**
     * 下单
     */
    @RequestMapping(value="/create", method= RequestMethod.POST)
    public String createOrder(OrderInfo orderInfo, Model model,
                              HttpServletRequest request, HttpServletResponse response) {
        log.info("下单, orderInfo={}", JacksonUtils.objectToJson(orderInfo));
        // #1 下单
        TaotaoResult result = orderResource.createOrder(orderInfo);
        log.info("下单, res={}", JacksonUtils.objectToJson(result));
        // #2 清空购物车
        CookieUtils.deleteCookie(request, response, CART_KEY);
        // #3 返辑视图
        model.addAttribute("orderId", result.getData().toString());
        model.addAttribute("payment", orderInfo.getPayment());
        // #4 预计送达时间，默认3天之后
        DateTime dateTime = new DateTime();
        dateTime = dateTime.plusDays(3);
        model.addAttribute("date", dateTime.toString("yyyy-MM-dd"));
        return "success";
    }

}