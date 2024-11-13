package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.OrderModel;

public interface OrderSerive {
    //第一种，1 通过前端传过来活动id,校验商品id 对应活动是否已开始。
    //2 直接在下单接口里面 校验 对应商品是否有活动，活动是否开始，有则已秒杀价格下单
    OrderModel createOrder(Integer userId,Integer itemId,Integer promoId, Integer amount) throws BusinessException;
}
