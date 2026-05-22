package com.snackchain.snack_platform_backend.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.snackchain.snack_platform_backend.entity.CartItem;
import com.snackchain.snack_platform_backend.entity.Store;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("MyBatis-Plus 自动填充配置测试")
class MyBatisPlusConfigTest {

    private final MetaObjectHandler metaObjectHandler = new MyBatisPlusConfig().metaObjectHandler();

    @Test
    @DisplayName("insertFill 应填充 CartItem 的 createdAt 和 updatedAt")
    void insertFill_shouldFillCartItemCreatedAtAndUpdatedAt() {
        CartItem cartItem = new CartItem();
        MetaObject metaObject = SystemMetaObject.forObject(cartItem);

        metaObjectHandler.insertFill(metaObject);

        assertNotNull(cartItem.getCreatedAt());
        assertNotNull(cartItem.getUpdatedAt());
    }

    @Test
    @DisplayName("insertFill 不应破坏现有 createTime 和 updateTime 填充")
    void insertFill_shouldKeepExistingCreateTimeAndUpdateTime() {
        Store store = new Store();
        MetaObject metaObject = SystemMetaObject.forObject(store);

        metaObjectHandler.insertFill(metaObject);

        assertNotNull(store.getCreateTime());
        assertNotNull(store.getUpdateTime());
    }

    @Test
    @DisplayName("updateFill 应填充 CartItem 的 updatedAt")
    void updateFill_shouldFillCartItemUpdatedAt() {
        CartItem cartItem = new CartItem();
        MetaObject metaObject = SystemMetaObject.forObject(cartItem);

        metaObjectHandler.updateFill(metaObject);

        assertNotNull(cartItem.getUpdatedAt());
    }
}
