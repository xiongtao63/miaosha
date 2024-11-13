package com.miaoshaproject.service;

import com.miaoshaproject.service.model.PromoModel;

/**
 * <p>
 *  File Name: PromoService
 *  File Function Description
 *  <li></li>
 *  Version: V1.0
 * </p>
 *
 * @Author 23754
 *         <p>
 *         <li>Create Date：2024/11/7-13:56</li>
 *         <li>Revise Records</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>Revise Author: 23754 </li>
 *         <li>Revise Date: 2024/11/7-13:56</li>
 *         <li>Revise Content: </li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public interface PromoService {
	//根据itemid 获取即将进行的或正在进行的秒杀活动
	PromoModel getPromoByItemId(Integer itemId);
}
