package com.app.mart.common.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 全局统一返回结果类
 * 功能：封装接口响应的状态码、提示信息、返回数据，前后端交互统一格式
 * 成功：code=200
 * 失败：code=500
 *
 * @author LunaMart
 */
@Data
@ApiModel(value = "统一返回结果")
public class R<T> {

    @ApiModelProperty(value = "响应码：200成功，500失败")
    private int code;

    @ApiModelProperty(value = "响应消息")
    private String msg;

    @ApiModelProperty(value = "响应数据")
    private T data;

    /**
     * 成功返回（无数据）
     */
    public static <T> R<T> ok() {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMsg("操作成功");
        return r;
    }

    /**
     * 成功返回（带数据）
     * @param data 返回的数据
     */
    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMsg("操作成功");
        r.setData(data);
        return r;
    }

    /**
     * 失败返回（自定义提示）
     * @param msg 错误提示信息
     */
    public static <T> R<T> fail(String msg) {
        R<T> r = new R<>();
        r.setCode(500);
        r.setMsg(msg);
        return r;
    }
}