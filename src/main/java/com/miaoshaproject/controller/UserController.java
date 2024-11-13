package com.miaoshaproject.controller;

import com.alibaba.druid.util.StringUtils;
import com.miaoshaproject.controller.viewobject.UserVO;
import com.miaoshaproject.dataobject.UserDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.CommonError;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Controller("user")
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
public class UserController extends BaseController{

    @Autowired
    UserService userService;

    @Autowired
    HttpServletRequest httpServletRequest;


    //用户登录
    @RequestMapping(value = "/login", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE})
    @ResponseBody
    public CommonReturnType login(@RequestParam(name = "telephone") String telephone,
                                  @RequestParam(name = "password") String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //入参校验
        if(org.apache.commons.lang3.StringUtils.isEmpty(telephone) || org.apache.commons.lang3.StringUtils.isEmpty(password)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        //用户登录服务，用来校验用户登录是否合法
        UserModel userModel = userService.validateLogin(telephone, encodeByMd5(password));
        //将登录凭证加入的用户登录成功的session
        httpServletRequest.getSession().setAttribute("IS_LOGIN",true);
        httpServletRequest.getSession().setAttribute("LOGIN_USER",userModel);

        return CommonReturnType.create(null);
    }


    //用户注册
    @RequestMapping(value = "/register", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE})
    @ResponseBody
    public CommonReturnType register(@RequestParam(name = "telephone") String telephone,
                                     @RequestParam(name = "name") String name,
                                     @RequestParam(name = "otpCode") String otpCode,
                                     @RequestParam(name = "gender") Integer gender,
                                     @RequestParam(name = "age") Integer age,
                                     @RequestParam(name = "password") String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //验证手机号和对应的验证码是否符合
        String inSessionOtpCode = (String) this.httpServletRequest.getSession().getAttribute(telephone);
        if(!StringUtils.equals(otpCode,inSessionOtpCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"短信验证码不符合");
        }
        //用户注册流程
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setAge(age);
        userModel.setTelephone(telephone);
        userModel.setGender(gender);
        userModel.setRegisterMode("byphone");
        userModel.setEncrptPassword(encodeByMd5(password));
        userService.register(userModel);
        return CommonReturnType.create(null);

    }

    private String encodeByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64Encoder = new BASE64Encoder();
        //加密
        String encode = base64Encoder.encode(md5.digest(str.getBytes("utf-8")));
        return encode;

    }

    //用户获取otp短信
    @RequestMapping(value = "/getotp", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE})
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam(name = "telephone")String telephone){
        //1，按照一定规则生成OTP 验证码
        Random random = new Random();
        int randomInt = random.nextInt(99999);
        randomInt += 10000;
        String otpCode = String.valueOf(randomInt);
        //2，将OTP 验证码 和用户手机号关联
        httpServletRequest.getSession().setAttribute(telephone,otpCode);
        System.out.println("tleephone ="+telephone + "   otpcode = "+ otpCode);
        //3, 将OTP 通过短信发送给用户 省略

        return CommonReturnType.create(null);
    }


    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name = "id") Integer id) throws BusinessException {
        //调用service 服务获取对应id的用户对象并返回给前端
        UserModel userModel = userService.getUserById(id);


        //若获取的用户信息不存在
        if(userModel == null){
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }
        //将核心领域模型对象转换为可供UI使用的viewobject
        UserVO userVO = convertFromUserModel(userModel);
        //返回通用对象
        return CommonReturnType.create(userVO);
    }

    private UserVO convertFromUserModel(UserModel userModel){
        if(userModel == null){
            return null;

        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel,userVO);
        return userVO;
    }


}
