package com.unicom.betterworry.util;


import com.unicom.betterworry.being.ResultBean;
import com.unicom.betterworry.being.ResultCode;

public class ResultUtils {

    /**
     * 请求接口成功并有返回数据
     */
    public static <T> ResultBean success(T data) {
        ResultBean result = new ResultBean();
        result.setSuccess(true);
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMsg());
        result.setData(data);
        return result;
    }

    /**
     * 请求接口成功没有返回数据
     */
    public static ResultBean success() {
        return success(null);
    }

    /**
     * 请求失败
     */
    public static ResultBean error(String message) {
        ResultBean result = new ResultBean();
        result.setSuccess(false);
        result.setCode(ResultCode.FAILED.getCode());
        result.setMessage(message);
        return result;
    }

    /**
     * 方法描述
     */
    public static ResultBean error(ResultCode resultCode) {
        ResultBean result = new ResultBean();
        result.setSuccess(false);
        result.setCode(resultCode.getCode());
        result.setMessage(resultCode.getMsg());
        return result;
    }
}
