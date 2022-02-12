package com.unicom.betterworry.being;

/**
 * 响应编码枚举类
 */
public enum ResultCode {
    /**
     * 成功
     */
    SUCCESS("200", "success"),
    /**
     * 失败
     */
    FAILED("000", "failed"),
    /**
     * 没权限
     */
    NO_PERMISSION("400", "no permission");

    private String code;

    private String msg;

    ResultCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
