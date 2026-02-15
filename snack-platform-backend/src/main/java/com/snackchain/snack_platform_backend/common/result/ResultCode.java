package com.snackchain.snack_platform_backend.common.result;

import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
public enum ResultCode {
    
    // 通用状态码
    SUCCESS(200, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或Token无效"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),
    
    // 用户相关 10xxx
    USER_EXISTS(10001, "用户名已存在"),
    USER_LOGIN_ERROR(10002, "用户名或密码错误"),
    USER_DISABLED(10003, "账号已被禁用"),
    
    // 分类相关 15xxx
    CATEGORY_NOT_FOUND(15001, "分类不存在"),
    
    // 门店相关 16xxx
    STORE_NOT_FOUND(16001, "门店不存在"),
    STORE_CLOSED(16002, "门店已休息"),
    
    // 商品相关 20xxx
    PRODUCT_NOT_FOUND(20001, "商品不存在"),
    PRODUCT_OFF_SHELF(20002, "商品已下架"),
    STOCK_NOT_ENOUGH(20003, "库存不足"),
    SKU_NOT_FOUND(20004, "门店商品不存在"),
    
    // 订单相关 30xxx
    ORDER_NOT_FOUND(30001, "订单不存在"),
    ORDER_STATUS_ERROR(30002, "订单状态不允许此操作"),
    PICKUP_CODE_INVALID(30003, "自提码无效"),
    ORDER_ALREADY_VERIFIED(30004, "订单已核销"),
    ORDER_NOT_BELONG_STORE(30005, "该订单不属于本店"),

    // 购物车相关 40xxx
    CART_ITEM_NOT_FOUND(40001, "购物车项不存在"),
    CART_ITEM_EXISTS(40002, "该商品已在购物车中"),

    // 密码相关 10xxx
    OLD_PASSWORD_ERROR(10004, "原密码不正确"),
    PASSWORD_NOT_MATCH(10005, "两次输入的密码不一致");
    
    private final Integer code;
    private final String message;
    
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}