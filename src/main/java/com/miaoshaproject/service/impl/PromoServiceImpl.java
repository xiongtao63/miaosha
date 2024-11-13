package com.miaoshaproject.service.impl;


import com.miaoshaproject.dao.PromoDOMapper;
import com.miaoshaproject.dataobject.PromoDO;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.model.PromoModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 *  File Name: PromoServiceImpl
 *  File Function Description
 *  <li></li>
 *  Version: V1.0
 * </p>
 *
 * @Author 23754
 *         <p>
 *         <li>Create Date：2024/11/7-13:58</li>
 *         <li>Revise Records</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>Revise Author: 23754 </li>
 *         <li>Revise Date: 2024/11/7-13:58</li>
 *         <li>Revise Content: </li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
@Service
public class PromoServiceImpl implements PromoService {

	@Autowired
	PromoDOMapper promoDOMapper;
	@Override
	public PromoModel getPromoByItemId(Integer itemId) {
		//获取对应商品的秒杀活动信息
		PromoDO promoDO = promoDOMapper.selectByItemId(itemId);

		//dataobject -》model
		PromoModel promoModel = convertFromDataObject(promoDO);
		if(promoModel == null){
			return null;
		}

		//判断当前活动是否即将开始或者正在进行
		if(promoModel.getStartDate().isAfterNow()){
			promoModel.setStatus(1);
		}else if(promoModel.getEndDate().isBeforeNow()){
			promoModel.setStatus(3);
		}else{
			promoModel.setStatus(2);
		}
		return promoModel;
	}
	private PromoModel convertFromDataObject(PromoDO promoDO){
		if(promoDO == null){
			return null;
		}
		PromoModel promoModel = new PromoModel();
		BeanUtils.copyProperties(promoDO,promoModel);
		promoModel.setPromoItemPrice(new BigDecimal(promoDO.getPromoItemPrice()));
		promoModel.setStartDate(new DateTime(promoDO.getStartDate()));
		promoModel.setEndDate(new DateTime(promoDO.getEndDate()));
		return promoModel;
	}
}