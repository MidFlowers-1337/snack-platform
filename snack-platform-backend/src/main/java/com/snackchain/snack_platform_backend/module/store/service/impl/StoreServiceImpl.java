package com.snackchain.snack_platform_backend.module.store.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snackchain.snack_platform_backend.common.exception.BusinessException;
import com.snackchain.snack_platform_backend.common.result.ResultCode;
import com.snackchain.snack_platform_backend.entity.Store;
import com.snackchain.snack_platform_backend.mapper.StoreMapper;
import com.snackchain.snack_platform_backend.module.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 门店服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {
    
    private final StoreMapper storeMapper;
    
    @Override
    public List<Store> listOpen() {
        LambdaQueryWrapper<Store> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Store::getStatus, 1)
               .orderByDesc(Store::getCreateTime);
        return storeMapper.selectList(wrapper);
    }
    
    @Override
    public IPage<Store> page(int pageNum, int pageSize, String keyword) {
        Page<Store> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Store> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Store::getName, keyword)
                   .or()
                   .like(Store::getAddress, keyword);
        }
        wrapper.orderByDesc(Store::getCreateTime);
        
        return storeMapper.selectPage(page, wrapper);
    }
    
    @Override
    public Store getById(Long id) {
        Store store = storeMapper.selectById(id);
        if (store == null) {
            throw new BusinessException(ResultCode.STORE_NOT_FOUND);
        }
        return store;
    }
    
    @Override
    @Transactional
    public void create(String name, String address, String phone, String image,
                       BigDecimal longitude, BigDecimal latitude, String businessHours) {
        Store store = new Store();
        store.setName(name);
        store.setAddress(address);
        store.setPhone(phone);
        store.setImage(image);
        store.setLongitude(longitude);
        store.setLatitude(latitude);
        store.setBusinessHours(businessHours);
        store.setStatus(1);  // 默认营业中
        
        storeMapper.insert(store);
        log.info("创建门店成功: {}", name);
    }
    
    @Override
    @Transactional
    public void update(Long id, String name, String address, String phone, String image,
                       BigDecimal longitude, BigDecimal latitude, String businessHours, Integer status) {
        Store store = getById(id);
        
        if (name != null) {
            store.setName(name);
        }
        if (address != null) {
            store.setAddress(address);
        }
        if (phone != null) {
            store.setPhone(phone);
        }
        if (image != null) {
            store.setImage(image);
        }
        if (longitude != null) {
            store.setLongitude(longitude);
        }
        if (latitude != null) {
            store.setLatitude(latitude);
        }
        if (businessHours != null) {
            store.setBusinessHours(businessHours);
        }
        if (status != null) {
            store.setStatus(status);
        }
        
        storeMapper.updateById(store);
        log.info("更新门店成功: {}", id);
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        Store store = getById(id);
        storeMapper.deleteById(id);
        log.info("删除门店成功: {}", store.getName());
    }
    
    @Override
    public List<Store> listNearby(BigDecimal longitude, BigDecimal latitude, Integer limit) {
        // 简单实现：获取所有营业中的门店，按距离排序
        // 实际生产环境应使用空间索引或专门的地理位置服务
        List<Store> stores = listOpen();
        
        if (longitude != null && latitude != null) {
            // 计算距离并排序，处理门店坐标为null的情况
            stores.sort((s1, s2) -> {
                double dist1 = calculateDistanceSafe(latitude.doubleValue(), longitude.doubleValue(), s1);
                double dist2 = calculateDistanceSafe(latitude.doubleValue(), longitude.doubleValue(), s2);
                return Double.compare(dist1, dist2);
            });
        }
        
        if (limit != null && limit > 0 && stores.size() > limit) {
            return stores.subList(0, limit);
        }
        return stores;
    }
    
    /**
     * 安全计算距离，处理门店坐标为null的情况
     * 如果门店坐标为null，返回最大距离值，使其排在最后
     */
    private double calculateDistanceSafe(double userLat, double userLon, Store store) {
        if (store.getLatitude() == null || store.getLongitude() == null) {
            // 坐标缺失的门店排在最后
            return Double.MAX_VALUE;
        }
        return calculateDistance(userLat, userLon,
                store.getLatitude().doubleValue(), store.getLongitude().doubleValue());
    }
    
    /**
     * 计算两点之间的距离（Haversine公式）
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 地球半径（公里）
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}