package com.snackchain.snack_platform_backend.module.store.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snackchain.snack_platform_backend.common.exception.BusinessException;
import com.snackchain.snack_platform_backend.common.result.ResultCode;
import com.snackchain.snack_platform_backend.entity.Store;
import com.snackchain.snack_platform_backend.mapper.StoreMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 门店服务单元测试
 * 
 * 测试目的：验证门店服务的核心业务逻辑
 * 测试范围：
 * - 创建门店
 * - 更新门店
 * - 删除门店
 * - 开启/暂停营业
 * - 查询门店列表
 * - 附近门店查询
 * 
 * @author Test
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("门店服务单元测试")
class StoreServiceImplTest {

    @Mock
    private StoreMapper storeMapper;

    @InjectMocks
    private StoreServiceImpl storeService;

    // 测试数据
    private Store testStore;
    private Store testStore2;

    /**
     * 初始化测试数据
     * 在每个测试方法执行前准备必要的测试对象
     */
    @BeforeEach
    void setUp() {
        // 初始化测试门店1
        testStore = new Store();
        testStore.setId(1L);
        testStore.setName("测试门店1");
        testStore.setAddress("北京市朝阳区xxx街道");
        testStore.setPhone("010-12345678");
        testStore.setImage("store1.jpg");
        testStore.setLongitude(new BigDecimal("116.397128"));
        testStore.setLatitude(new BigDecimal("39.916527"));
        testStore.setBusinessHours("09:00-22:00");
        testStore.setStatus(1);
        testStore.setCreateTime(LocalDateTime.now());
        testStore.setUpdateTime(LocalDateTime.now());

        // 初始化测试门店2
        testStore2 = new Store();
        testStore2.setId(2L);
        testStore2.setName("测试门店2");
        testStore2.setAddress("北京市海淀区xxx街道");
        testStore2.setPhone("010-87654321");
        testStore2.setImage("store2.jpg");
        testStore2.setLongitude(new BigDecimal("116.310003"));
        testStore2.setLatitude(new BigDecimal("39.991957"));
        testStore2.setBusinessHours("08:00-21:00");
        testStore2.setStatus(1);
        testStore2.setCreateTime(LocalDateTime.now());
        testStore2.setUpdateTime(LocalDateTime.now());
    }

    /**
     * 测试用例：创建门店成功
     * 
     * Given: 门店信息
     * When: 创建门店
     * Then: 门店创建成功
     */
    @Test
    @DisplayName("TC-STORE-001: 创建门店成功")
    void testCreate_Success() {
        // Given: 准备创建参数
        String name = "新门店";
        String address = "上海市浦东新区xxx路";
        String phone = "021-12345678";
        String image = "new_store.jpg";
        BigDecimal longitude = new BigDecimal("121.473701");
        BigDecimal latitude = new BigDecimal("31.230416");
        String businessHours = "10:00-23:00";

        when(storeMapper.insert(any(Store.class))).thenReturn(1);

        // When: 执行创建门店
        storeService.create(name, address, phone, image, longitude, latitude, businessHours);

        // Then: 验证门店被插入数据库
        verify(storeMapper, times(1)).insert(argThat(store ->
            name.equals(store.getName()) &&
            address.equals(store.getAddress()) &&
            phone.equals(store.getPhone()) &&
            image.equals(store.getImage()) &&
            longitude.equals(store.getLongitude()) &&
            latitude.equals(store.getLatitude()) &&
            businessHours.equals(store.getBusinessHours()) &&
            store.getStatus() == 1
        ));
    }

    /**
     * 测试用例：创建门店时部分字段为空
     * 
     * Given: 部分门店信息为空
     * When: 创建门店
     * Then: 门店创建成功（允许部分字段为空）
     */
    @Test
    @DisplayName("TC-STORE-002: 创建门店时部分字段为空")
    void testCreate_PartialFields() {
        // Given: 只有必要字段
        String name = "简单门店";
        String address = "简单地址";

        when(storeMapper.insert(any(Store.class))).thenReturn(1);

        // When: 执行创建门店
        storeService.create(name, address, null, null, null, null, null);

        // Then: 验证门店被插入数据库
        verify(storeMapper, times(1)).insert(argThat(store ->
            name.equals(store.getName()) &&
            address.equals(store.getAddress()) &&
            store.getPhone() == null &&
            store.getImage() == null &&
            store.getLongitude() == null &&
            store.getLatitude() == null &&
            store.getBusinessHours() == null &&
            store.getStatus() == 1
        ));
    }

    /**
     * 测试用例：更新门店成功
     * 
     * Given: 门店存在
     * When: 更新门店信息
     * Then: 门店更新成功
     */
    @Test
    @DisplayName("TC-STORE-003: 更新门店成功")
    void testUpdate_Success() {
        // Given: 门店存在
        Long id = 1L;
        String newName = "更新后的门店";
        String newAddress = "更新后的地址";
        String newPhone = "010-99999999";
        String newImage = "updated.jpg";
        BigDecimal newLongitude = new BigDecimal("116.500000");
        BigDecimal newLatitude = new BigDecimal("40.000000");
        String newBusinessHours = "08:00-23:00";
        Integer newStatus = 0;

        when(storeMapper.selectById(id)).thenReturn(testStore);
        when(storeMapper.updateById(any(Store.class))).thenReturn(1);

        // When: 执行更新门店
        storeService.update(id, newName, newAddress, newPhone, newImage, 
                           newLongitude, newLatitude, newBusinessHours, newStatus);

        // Then: 验证门店被更新
        verify(storeMapper, times(1)).updateById(argThat(store ->
            newName.equals(store.getName()) &&
            newAddress.equals(store.getAddress()) &&
            newPhone.equals(store.getPhone()) &&
            newImage.equals(store.getImage()) &&
            newLongitude.equals(store.getLongitude()) &&
            newLatitude.equals(store.getLatitude()) &&
            newBusinessHours.equals(store.getBusinessHours()) &&
            newStatus.equals(store.getStatus())
        ));
    }

    /**
     * 测试用例：部分更新门店成功
     * 
     * Given: 门店存在
     * When: 只更新部分字段
     * Then: 只有非null字段被更新
     */
    @Test
    @DisplayName("TC-STORE-004: 部分更新门店成功")
    void testUpdate_PartialUpdate() {
        // Given: 门店存在
        Long id = 1L;
        String newName = "只更新名称";

        when(storeMapper.selectById(id)).thenReturn(testStore);
        when(storeMapper.updateById(any(Store.class))).thenReturn(1);

        // When: 执行部分更新
        storeService.update(id, newName, null, null, null, null, null, null, null);

        // Then: 验证只有名称被更新，其他字段保持原值
        verify(storeMapper, times(1)).updateById(argThat(store ->
            newName.equals(store.getName()) &&
            "北京市朝阳区xxx街道".equals(store.getAddress()) &&
            "010-12345678".equals(store.getPhone()) &&
            "store1.jpg".equals(store.getImage()) &&
            new BigDecimal("116.397128").equals(store.getLongitude()) &&
            new BigDecimal("39.916527").equals(store.getLatitude()) &&
            "09:00-22:00".equals(store.getBusinessHours()) &&
            store.getStatus() == 1
        ));
    }

    /**
     * 测试用例：更新不存在的门店失败
     * 
     * Given: 门店不存在
     * When: 尝试更新门店
     * Then: 抛出门店不存在异常
     */
    @Test
    @DisplayName("TC-STORE-005: 更新不存在的门店失败")
    void testUpdate_StoreNotFound() {
        // Given: 门店不存在
        Long id = 999L;
        when(storeMapper.selectById(id)).thenReturn(null);

        // When & Then: 执行并验证异常
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> storeService.update(id, "新名称", null, null, null, null, null, null, null),
            "应抛出门店不存在异常"
        );

        assertEquals(ResultCode.STORE_NOT_FOUND.getCode(), exception.getCode(),
            "错误码应为门店不存在");

        // 验证更新未被调用
        verify(storeMapper, never()).updateById(any(Store.class));
    }

    /**
     * 测试用例：删除门店成功
     * 
     * Given: 门店存在
     * When: 删除门店
     * Then: 门店删除成功
     */
    @Test
    @DisplayName("TC-STORE-006: 删除门店成功")
    void testDelete_Success() {
        // Given: 门店存在
        Long id = 1L;
        when(storeMapper.selectById(id)).thenReturn(testStore);
        when(storeMapper.deleteById(id)).thenReturn(1);

        // When: 执行删除门店
        storeService.delete(id);

        // Then: 验证门店被删除
        verify(storeMapper, times(1)).deleteById(id);
    }

    /**
     * 测试用例：删除不存在的门店失败
     * 
     * Given: 门店不存在
     * When: 尝试删除门店
     * Then: 抛出门店不存在异常
     */
    @Test
    @DisplayName("TC-STORE-007: 删除不存在的门店失败")
    void testDelete_StoreNotFound() {
        // Given: 门店不存在
        Long id = 999L;
        when(storeMapper.selectById(id)).thenReturn(null);

        // When & Then: 执行并验证异常
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> storeService.delete(id),
            "应抛出门店不存在异常"
        );

        assertEquals(ResultCode.STORE_NOT_FOUND.getCode(), exception.getCode(),
            "错误码应为门店不存在");

        // 验证删除未被调用
        verify(storeMapper, never()).deleteById(anyLong());
    }

    /**
     * 测试用例：开启营业（状态变更）
     * 
     * Given: 门店存在且已休息
     * When: 更新状态为营业中
     * Then: 门店状态变为营业中
     */
    @Test
    @DisplayName("TC-STORE-008: 开启营业成功")
    void testOpenStore_Success() {
        // Given: 门店存在且已休息
        Long id = 1L;
        testStore.setStatus(0);
        when(storeMapper.selectById(id)).thenReturn(testStore);
        when(storeMapper.updateById(any(Store.class))).thenReturn(1);

        // When: 执行开启营业
        storeService.update(id, null, null, null, null, null, null, null, 1);

        // Then: 验证门店状态变为营业中
        verify(storeMapper, times(1)).updateById(argThat(store ->
            store.getStatus() == 1
        ));
    }

    /**
     * 测试用例：暂停营业（状态变更）
     * 
     * Given: 门店存在且营业中
     * When: 更新状态为休息中
     * Then: 门店状态变为休息中
     */
    @Test
    @DisplayName("TC-STORE-009: 暂停营业成功")
    void testCloseStore_Success() {
        // Given: 门店存在且营业中
        Long id = 1L;
        testStore.setStatus(1);
        when(storeMapper.selectById(id)).thenReturn(testStore);
        when(storeMapper.updateById(any(Store.class))).thenReturn(1);

        // When: 执行暂停营业
        storeService.update(id, null, null, null, null, null, null, null, 0);

        // Then: 验证门店状态变为休息中
        verify(storeMapper, times(1)).updateById(argThat(store ->
            store.getStatus() == 0
        ));
    }

    /**
     * 测试用例：查询营业中的门店列表
     * 
     * Given: 存在营业中的门店
     * When: 查询营业中的门店列表
     * Then: 返回所有营业中的门店
     */
    @Test
    @DisplayName("TC-STORE-010: 查询营业中的门店列表")
    void testListOpen_Success() {
        // Given: 存在营业中的门店
        List<Store> openStores = Arrays.asList(testStore, testStore2);
        when(storeMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(openStores);

        // When: 执行查询
        List<Store> result = storeService.listOpen();

        // Then: 验证结果
        assertNotNull(result, "结果不应为空");
        assertEquals(2, result.size(), "应返回2个门店");
    }

    /**
     * 测试用例：查询门店列表为空
     * 
     * Given: 不存在营业中的门店
     * When: 查询门店列表
     * Then: 返回空列表
     */
    @Test
    @DisplayName("TC-STORE-011: 查询门店列表为空")
    void testListOpen_Empty() {
        // Given: 不存在营业中的门店
        when(storeMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        // When: 执行查询
        List<Store> result = storeService.listOpen();

        // Then: 验证结果
        assertNotNull(result, "结果不应为null");
        assertTrue(result.isEmpty(), "结果应为空列表");
    }

    /**
     * 测试用例：根据ID获取门店成功
     * 
     * Given: 门店存在
     * When: 根据ID获取门店
     * Then: 返回对应的门店
     */
    @Test
    @DisplayName("TC-STORE-012: 根据ID获取门店成功")
    void testGetById_Success() {
        // Given: 门店存在
        Long id = 1L;
        when(storeMapper.selectById(id)).thenReturn(testStore);

        // When: 执行查询
        Store result = storeService.getById(id);

        // Then: 验证结果
        assertNotNull(result, "结果不应为空");
        assertEquals(id, result.getId(), "ID应匹配");
        assertEquals("测试门店1", result.getName(), "名称应匹配");
    }

    /**
     * 测试用例：根据ID获取不存在的门店失败
     * 
     * Given: 门店不存在
     * When: 根据ID获取门店
     * Then: 抛出门店不存在异常
     */
    @Test
    @DisplayName("TC-STORE-013: 根据ID获取不存在的门店失败")
    void testGetById_NotFound() {
        // Given: 门店不存在
        Long id = 999L;
        when(storeMapper.selectById(id)).thenReturn(null);

        // When & Then: 执行并验证异常
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> storeService.getById(id),
            "应抛出门店不存在异常"
        );

        assertEquals(ResultCode.STORE_NOT_FOUND.getCode(), exception.getCode(),
            "错误码应为门店不存在");
    }

    /**
     * 测试用例：分页查询门店列表
     * 
     * Given: 存在门店
     * When: 分页查询门店
     * Then: 返回分页结果
     */
    @Test
    @DisplayName("TC-STORE-014: 分页查询门店列表")
    void testPage_Success() {
        // Given: 存在门店
        int pageNum = 1;
        int pageSize = 10;
        List<Store> stores = Arrays.asList(testStore, testStore2);
        
        Page<Store> mockPage = new Page<>(pageNum, pageSize);
        mockPage.setRecords(stores);
        mockPage.setTotal(2);
        
        when(storeMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        // When: 执行分页查询
        IPage<Store> result = storeService.page(pageNum, pageSize, null);

        // Then: 验证结果
        assertNotNull(result, "结果不应为空");
        assertEquals(2, result.getRecords().size(), "应返回2个门店");
        assertEquals(2, result.getTotal(), "总数应为2");
    }

    /**
     * 测试用例：分页查询带关键字
     * 
     * Given: 存在门店
     * When: 带关键字分页查询
     * Then: 返回符合条件的分页结果
     */
    @Test
    @DisplayName("TC-STORE-015: 分页查询带关键字")
    void testPage_WithKeyword() {
        // Given: 存在门店
        int pageNum = 1;
        int pageSize = 10;
        String keyword = "朝阳";
        
        List<Store> stores = Collections.singletonList(testStore);
        
        Page<Store> mockPage = new Page<>(pageNum, pageSize);
        mockPage.setRecords(stores);
        mockPage.setTotal(1);
        
        when(storeMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);

        // When: 执行带关键字的分页查询
        IPage<Store> result = storeService.page(pageNum, pageSize, keyword);

        // Then: 验证结果
        assertNotNull(result, "结果不应为空");
        assertEquals(1, result.getRecords().size(), "应返回1个门店");
    }

    /**
     * 测试用例：查询附近门店
     * 
     * Given: 存在营业中的门店
     * When: 根据经纬度查询附近门店
     * Then: 返回按距离排序的门店列表
     */
    @Test
    @DisplayName("TC-STORE-016: 查询附近门店")
    void testListNearby_Success() {
        // Given: 存在营业中的门店
        List<Store> openStores = Arrays.asList(testStore, testStore2);
        when(storeMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(openStores);

        // 用户位置（天安门附近）
        BigDecimal userLongitude = new BigDecimal("116.397428");
        BigDecimal userLatitude = new BigDecimal("39.90923");

        // When: 执行查询附近门店
        List<Store> result = storeService.listNearby(userLongitude, userLatitude, null);

        // Then: 验证结果
        assertNotNull(result, "结果不应为空");
        assertEquals(2, result.size(), "应返回2个门店");
        // 验证按距离排序（testStore更近）
        assertEquals(testStore.getId(), result.get(0).getId(), "第一个应是距离更近的门店");
    }

    /**
     * 测试用例：查询附近门店带数量限制
     * 
     * Given: 存在多个营业中的门店
     * When: 根据经纬度查询附近门店，限制返回数量
     * Then: 返回指定数量的门店
     */
    @Test
    @DisplayName("TC-STORE-017: 查询附近门店带数量限制")
    void testListNearby_WithLimit() {
        // Given: 存在营业中的门店
        List<Store> openStores = Arrays.asList(testStore, testStore2);
        when(storeMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(openStores);

        BigDecimal userLongitude = new BigDecimal("116.397428");
        BigDecimal userLatitude = new BigDecimal("39.90923");
        Integer limit = 1;

        // When: 执行查询附近门店，限制返回1个
        List<Store> result = storeService.listNearby(userLongitude, userLatitude, limit);

        // Then: 验证结果
        assertNotNull(result, "结果不应为空");
        assertEquals(1, result.size(), "应只返回1个门店");
    }

    /**
     * 测试用例：查询附近门店时坐标为空
     * 
     * Given: 存在营业中的门店
     * When: 不提供经纬度查询门店
     * Then: 返回所有营业中的门店（不排序）
     */
    @Test
    @DisplayName("TC-STORE-018: 查询附近门店时坐标为空")
    void testListNearby_NullCoordinates() {
        // Given: 存在营业中的门店
        List<Store> openStores = Arrays.asList(testStore, testStore2);
        when(storeMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(openStores);

        // When: 执行查询，不提供坐标
        List<Store> result = storeService.listNearby(null, null, null);

        // Then: 验证结果
        assertNotNull(result, "结果不应为空");
        assertEquals(2, result.size(), "应返回所有营业中的门店");
    }

    /**
     * 测试用例：查询附近门店时门店坐标为空
     * 
     * Given: 存在门店，但门店坐标为空
     * When: 根据经纬度查询附近门店
     * Then: 坐标为空的门店排在最后
     */
    @Test
    @DisplayName("TC-STORE-019: 查询附近门店时门店坐标为空")
    void testListNearby_StoreWithNullCoordinates() {
        // Given: 存在门店，其中一个坐标为空
        Store storeWithNullCoords = new Store();
        storeWithNullCoords.setId(3L);
        storeWithNullCoords.setName("无坐标门店");
        storeWithNullCoords.setLongitude(null);
        storeWithNullCoords.setLatitude(null);
        storeWithNullCoords.setStatus(1);

        List<Store> openStores = Arrays.asList(storeWithNullCoords, testStore);
        when(storeMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(openStores);

        BigDecimal userLongitude = new BigDecimal("116.397428");
        BigDecimal userLatitude = new BigDecimal("39.90923");

        // When: 执行查询附近门店
        List<Store> result = storeService.listNearby(userLongitude, userLatitude, null);

        // Then: 验证结果，坐标为空的门店应排在最后
        assertNotNull(result, "结果不应为空");
        assertEquals(2, result.size(), "应返回2个门店");
        assertEquals(testStore.getId(), result.get(0).getId(), "有坐标的门店应排在前面");
        assertEquals(storeWithNullCoords.getId(), result.get(1).getId(), "无坐标的门店应排在最后");
    }
}