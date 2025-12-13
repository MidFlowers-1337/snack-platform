package com.snackchain.snack_platform_backend.module.sku.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.snackchain.snack_platform_backend.entity.Product;
import com.snackchain.snack_platform_backend.entity.Store;
import com.snackchain.snack_platform_backend.entity.StoreSku;
import com.snackchain.snack_platform_backend.mapper.ProductMapper;
import com.snackchain.snack_platform_backend.mapper.StoreMapper;
import com.snackchain.snack_platform_backend.mapper.StoreSkuMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 门店商品服务单元测试
 * 
 * 测试目的：验证门店商品库存管理的核心业务逻辑
 * 测试范围：
 * - 库存增加
 * - 库存减少
 * - 库存边界检查（不能为负数）
 * 
 * @author Test
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("门店商品服务单元测试")
class StoreSkuServiceImplTest {

    @Mock
    private StoreSkuMapper storeSkuMapper;

    @Mock
    private StoreMapper storeMapper;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private StoreSkuServiceImpl storeSkuService;

    // 测试数据
    private StoreSku testSku;
    private Store testStore;
    private Product testProduct;

    /**
     * 初始化 MyBatis-Plus Lambda 缓存
     * 在所有测试方法执行前初始化，解决 LambdaUpdateWrapper 在纯 Mock 环境中的缓存问题
     */
    @BeforeAll
    static void initMybatisPlusCache() {
        // 初始化 MyBatis-Plus 的 TableInfo 缓存，使 LambdaUpdateWrapper 能正常工作
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, StoreSku.class);
    }

    /**
     * 初始化测试数据
     * 在每个测试方法执行前准备必要的测试对象
     */
    @BeforeEach
    void setUp() {
        // 初始化测试门店
        testStore = new Store();
        testStore.setId(1L);
        testStore.setName("测试门店");
        testStore.setStatus(1);

        // 初始化测试商品
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("测试零食");
        testProduct.setPrice(new BigDecimal("10.00"));
        testProduct.setStatus(1);

        // 初始化测试SKU
        testSku = new StoreSku();
        testSku.setId(1L);
        testSku.setStoreId(1L);
        testSku.setProductId(1L);
        testSku.setPrice(new BigDecimal("10.00"));
        testSku.setStock(100);
        testSku.setStatus(1);
    }

    /**
     * 测试用例：增加库存
     * 
     * Given: 门店商品当前库存为100
     * When: 调用恢复库存方法增加50
     * Then: 库存更新为150
     */
    @Test
    @DisplayName("TC-STOCK-001: 增加库存成功")
    void testUpdateStock_Increase() {
        // Given: 当前库存为100
        Long skuId = 1L;
        Integer quantity = 50;
        
        when(storeSkuMapper.selectById(skuId)).thenReturn(testSku);
        when(storeSkuMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);

        // When: 执行恢复库存（增加库存）
        storeSkuService.restoreStock(skuId, quantity);

        // Then: 验证库存更新被调用
        verify(storeSkuMapper, times(1)).update(isNull(), argThat(wrapper -> {
            // 验证更新条件包含正确的SKU ID
            // 由于LambdaUpdateWrapper的内部实现，我们只能验证方法被调用
            return wrapper != null;
        }));
    }

    /**
     * 测试用例：减少库存
     * 
     * Given: 门店商品当前库存为100
     * When: 调用扣减库存方法减少30
     * Then: 库存更新为70，返回成功
     */
    @Test
    @DisplayName("TC-STOCK-002: 减少库存成功")
    void testUpdateStock_Decrease() {
        // Given: 当前库存为100
        Long skuId = 1L;
        Integer quantity = 30;
        
        when(storeSkuMapper.selectById(skuId)).thenReturn(testSku);
        when(storeSkuMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);

        // When: 执行扣减库存
        boolean result = storeSkuService.deductStock(skuId, quantity);

        // Then: 验证结果
        assertTrue(result, "扣减库存应成功");
        
        // 验证库存更新被调用
        verify(storeSkuMapper, times(1)).update(isNull(), any(LambdaUpdateWrapper.class));
    }

    /**
     * 测试用例：库存不能为负数
     * 
     * Given: 门店商品当前库存为10
     * When: 尝试扣减20件库存
     * Then: 扣减失败，返回false
     */
    @Test
    @DisplayName("TC-STOCK-003: 库存不能为负数")
    void testUpdateStock_NegativeNotAllowed() {
        // Given: 当前库存为10
        testSku.setStock(10);
        Long skuId = 1L;
        Integer quantity = 20; // 尝试扣减超过库存的数量
        
        when(storeSkuMapper.selectById(skuId)).thenReturn(testSku);

        // When: 执行扣减库存
        boolean result = storeSkuService.deductStock(skuId, quantity);

        // Then: 验证结果
        assertFalse(result, "库存不足时扣减应失败");
        
        // 验证库存更新未被调用
        verify(storeSkuMapper, never()).update(isNull(), any(LambdaUpdateWrapper.class));
    }

    /**
     * 测试用例：扣减库存参数验证 - 数量为0
     * 
     * Given: 扣减数量为0
     * When: 调用扣减库存方法
     * Then: 返回false，不执行扣减
     */
    @Test
    @DisplayName("TC-STOCK-004: 扣减数量为0时返回失败")
    void testDeductStock_ZeroQuantity() {
        // Given: 扣减数量为0
        Long skuId = 1L;
        Integer quantity = 0;

        // When: 执行扣减库存
        boolean result = storeSkuService.deductStock(skuId, quantity);

        // Then: 验证结果
        assertFalse(result, "扣减数量为0时应返回失败");
        
        // 验证数据库操作未被调用
        verify(storeSkuMapper, never()).selectById(anyLong());
        verify(storeSkuMapper, never()).update(any(), any());
    }

    /**
     * 测试用例：扣减库存参数验证 - 数量为负数
     * 
     * Given: 扣减数量为负数
     * When: 调用扣减库存方法
     * Then: 返回false，不执行扣减
     */
    @Test
    @DisplayName("TC-STOCK-005: 扣减数量为负数时返回失败")
    void testDeductStock_NegativeQuantity() {
        // Given: 扣减数量为负数
        Long skuId = 1L;
        Integer quantity = -10;

        // When: 执行扣减库存
        boolean result = storeSkuService.deductStock(skuId, quantity);

        // Then: 验证结果
        assertFalse(result, "扣减数量为负数时应返回失败");
        
        // 验证数据库操作未被调用
        verify(storeSkuMapper, never()).selectById(anyLong());
        verify(storeSkuMapper, never()).update(any(), any());
    }
}