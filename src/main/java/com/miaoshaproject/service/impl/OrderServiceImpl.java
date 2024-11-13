package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.OrderDOMapper;
import com.miaoshaproject.dao.SequenceDOMapper;
import com.miaoshaproject.dataobject.OrderDO;
import com.miaoshaproject.dataobject.SequenceDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.OrderSerive;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.OrderModel;
import com.miaoshaproject.service.model.PromoModel;
import com.miaoshaproject.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Service
public class OrderServiceImpl implements OrderSerive {

    @Autowired
    ItemService itemService;

    @Autowired
    UserService userService;

    @Autowired
    OrderDOMapper orderDOMapper;

    @Autowired
    SequenceDOMapper sequenceDOMapper;

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId,Integer promoId, Integer amount) throws BusinessException {
        //校验下单状态，下单商品是否存在，用户是存在，数量是否合法
        ItemModel itemModel = itemService.getItemById(itemId);
        if(itemModel == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"商品信息不存在");
        }
        UserModel userModel = userService.getUserById(userId);
        if(userModel == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"用户信息不存在");
        }
        if(amount<=0 || amount >99){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"数量信息不正确");
        }
        //校验活动信息
        if(promoId != null){
            PromoModel promoModel = itemModel.getPromoModel();
            //校验对应  对应活动是否对应该商品
            if(promoId.intValue() != itemModel.getPromoModel().getId() ){
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"活动信息不正确");
            }else if(itemModel.getPromoModel().getStatus() != 2){//校验活动是否开始
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"活动还未开始");
            }
        }

        //落单减库存
        boolean b = itemService.decreaseStock(itemId, amount);
        if(!b){
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }

        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        if(promoId != null){
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        }else{
            orderModel.setItemPrice(itemModel.getPrice());
        }
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));
        orderModel.setAmount(amount);
        orderModel.setItemId(itemId);
        orderModel.setPromoId(promoId);

        //生成交易流水号，订单号

        orderModel.setId(generateOrderNo());

        //订单入库
        OrderDO orderDO = convertOrderDOFromOrderModel(orderModel);
        orderDOMapper.insertSelective(orderDO);

        //加商品销量
        itemService.increaseSales(itemId,amount);
        //返回前端
        return orderModel;
    }

    @Transactional(propagation = REQUIRES_NEW)
    private String generateOrderNo(){
        //订单有16位
        StringBuffer stringBuffer = new StringBuffer();
        //前8位时间信息
        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-", "");
        stringBuffer.append(nowDate);
        //中间6位自增
        int sequence = 0;
        SequenceDO sequenceDO = sequenceDOMapper.getSequenceByName("order_info");
        sequence = sequenceDO.getCurrentValue();
        sequenceDO.setCurrentValue(sequenceDO.getCurrentValue() + sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);
        String sequenceStr = String.valueOf(sequence);
        for(int i = 0; i< 6- sequenceStr.length(); i++){
            stringBuffer.append(0);
        }
        stringBuffer.append(sequenceStr);

        //最后两位位分库分表 比如用户id 1000121  userid % 100,就不变 ，到时可以根据这个进行分库
        stringBuffer.append("00");
        return stringBuffer.toString();
    }

    private OrderDO convertOrderDOFromOrderModel(OrderModel orderModel){
        if(orderModel == null){
            return  null;
        }
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel,orderDO);
        orderDO.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderDO.setOrderPrice(orderModel.getOrderPrice().doubleValue());
        return orderDO;
    }
}
