package com.snackchain.snack_platform_backend.module.store.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.snackchain.snack_platform_backend.entity.Store;

import java.math.BigDecimal;
import java.util.List;

/**
 * 门店服务接口
 */
public interface StoreService {
    
    /**
     * 获取所有营业中的门店列表
     */
    List<Store> listOpen();
    
    /**
     * 分页获取所有门店（管理端）
     */
    IPage<Store> page(int pageNum, int pageSize, String keyword);
    
    /**
     * 根据ID获取门店
     */
    Store getById(Long id);
    
    /**
     * 创建门店
     */
    void create(String name, String address, String phone, String image, 
                BigDecimal longitude, BigDecimal latitude, String businessHours);
    
    /**
     * 更新门店
     */
    void update(Long id, String name, String address, String phone, String image,
                BigDecimal longitude, BigDecimal latitude, String businessHours, Integer status);
    
    /**
     * 删除门店
     */
    void delete(Long id);
    
    /**
     * 根据经纬度获取附近门店
     */
    List<Store> listNearby(BigDecimal longitude, BigDecimal latitude, Integer limit);
}