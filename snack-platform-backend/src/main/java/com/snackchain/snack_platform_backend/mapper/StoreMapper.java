package com.snackchain.snack_platform_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.snackchain.snack_platform_backend.entity.Store;
import org.apache.ibatis.annotations.Mapper;

/**
 * 门店Mapper
 */
@Mapper
public interface StoreMapper extends BaseMapper<Store> {
}