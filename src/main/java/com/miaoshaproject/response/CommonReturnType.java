package com.miaoshaproject.response;

public class CommonReturnType {
    //表明对应请求，服务器处理的结果，success fail
    private String status;
    //如果status = success  则data 内返回给前端使用json数据
    //如果status = fail 则data 内使用通用的错误码格式
    private Object data;

//    定义一个通用的创建方法
    public static CommonReturnType create(Object result){
        return CommonReturnType.create(result,"success");
    }

    public static CommonReturnType create(Object result,String status){
        CommonReturnType type = new CommonReturnType();
        type.setStatus(status);
        type.setData(result);
        return type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
