package com.snackchain.snack_platform_backend.module.category.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.snackchain.snack_platform_backend.common.exception.BusinessException;
import com.snackchain.snack_platform_backend.common.result.ResultCode;
import com.snackchain.snack_platform_backend.entity.Category;
import com.snackchain.snack_platform_backend.mapper.CategoryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 分类服务单元测试
 * 
 * 测试目的：验证分类服务的核心业务逻辑
 * 测试范围：
 * - 创建分类
 * - 更新分类
 * - 删除分类
 * - 查询分类列表
 * - 根据ID获取分类
 * 
 * @author Test
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("分类服务单元测试")
class CategoryServiceImplTest {

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    // 测试数据
    private Category testCategory;
    private Category testCategory2;

    /**
     * 初始化测试数据
     * 在每个测试方法执行前准备必要的测试对象
     */
    @BeforeEach
    void setUp() {
        // 初始化测试分类1
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("零食");
        testCategory.setIcon("snack.png");
        testCategory.setSort(1);
        testCategory.setStatus(1);
        testCategory.setCreateTime(LocalDateTime.now());
        testCategory.setUpdateTime(LocalDateTime.now());

        // 初始化测试分类2
        testCategory2 = new Category();
        testCategory2.setId(2L);
        testCategory2.setName("饮料");
        testCategory2.setIcon("drink.png");
        testCategory2.setSort(2);
        testCategory2.setStatus(1);
        testCategory2.setCreateTime(LocalDateTime.now());
        testCategory2.setUpdateTime(LocalDateTime.now());
    }

    /**
     * 测试用例：创建分类成功
     * 
     * Given: 分类名称、图标、排序值
     * When: 调用创建分类方法
     * Then: 分类创建成功，数据被插入数据库
     */
    @Test
    @DisplayName("TC-CATEGORY-001: 创建分类成功")
    void testCreate_Success() {
        // Given: 准备创建参数
        String name = "新分类";
        String icon = "new.png";
        Integer sort = 3;

        when(categoryMapper.insert(any(Category.class))).thenReturn(1);

        // When: 执行创建分类
        categoryService.create(name, icon, sort);

        // Then: 验证分类被插入数据库
        verify(categoryMapper, times(1)).insert(argThat(category ->
            name.equals(category.getName()) &&
            icon.equals(category.getIcon()) &&
            sort.equals(category.getSort()) &&
            category.getStatus() == 1
        ));
    }

    /**
     * 测试用例：创建分类时排序值为空使用默认值
     * 
     * Given: 分类名称、图标，排序值为null
     * When: 调用创建分类方法
     * Then: 分类创建成功，排序值使用默认值0
     */
    @Test
    @DisplayName("TC-CATEGORY-002: 创建分类时排序值为空使用默认值")
    void testCreate_DefaultSort() {
        // Given: 准备创建参数，排序值为null
        String name = "新分类";
        String icon = "new.png";
        Integer sort = null;

        when(categoryMapper.insert(any(Category.class))).thenReturn(1);

        // When: 执行创建分类
        categoryService.create(name, icon, sort);

        // Then: 验证分类被插入数据库，排序值为默认值0
        verify(categoryMapper, times(1)).insert(argThat(category ->
            name.equals(category.getName()) &&
            icon.equals(category.getIcon()) &&
            category.getSort() == 0 &&
            category.getStatus() == 1
        ));
    }

    /**
     * 测试用例：更新分类成功
     * 
     * Given: 分类存在
     * When: 更新分类信息
     * Then: 分类更新成功
     */
    @Test
    @DisplayName("TC-CATEGORY-003: 更新分类成功")
    void testUpdate_Success() {
        // Given: 分类存在
        Long id = 1L;
        String newName = "更新后的分类";
        String newIcon = "updated.png";
        Integer newSort = 10;
        Integer newStatus = 0;

        when(categoryMapper.selectById(id)).thenReturn(testCategory);
        when(categoryMapper.updateById(any(Category.class))).thenReturn(1);

        // When: 执行更新分类
        categoryService.update(id, newName, newIcon, newSort, newStatus);

        // Then: 验证分类被更新
        verify(categoryMapper, times(1)).updateById(argThat(category ->
            newName.equals(category.getName()) &&
            newIcon.equals(category.getIcon()) &&
            newSort.equals(category.getSort()) &&
            newStatus.equals(category.getStatus())
        ));
    }

    /**
     * 测试用例：部分更新分类成功
     * 
     * Given: 分类存在
     * When: 只更新部分字段（其他字段为null）
     * Then: 只有非null字段被更新
     */
    @Test
    @DisplayName("TC-CATEGORY-004: 部分更新分类成功")
    void testUpdate_PartialUpdate() {
        // Given: 分类存在
        Long id = 1L;
        String newName = "只更新名称";

        when(categoryMapper.selectById(id)).thenReturn(testCategory);
        when(categoryMapper.updateById(any(Category.class))).thenReturn(1);

        // When: 执行部分更新（只更新名称）
        categoryService.update(id, newName, null, null, null);

        // Then: 验证只有名称被更新，其他字段保持原值
        verify(categoryMapper, times(1)).updateById(argThat(category ->
            newName.equals(category.getName()) &&
            "snack.png".equals(category.getIcon()) &&
            category.getSort() == 1 &&
            category.getStatus() == 1
        ));
    }

    /**
     * 测试用例：更新不存在的分类失败
     * 
     * Given: 分类不存在
     * When: 尝试更新分类
     * Then: 抛出分类不存在异常
     */
    @Test
    @DisplayName("TC-CATEGORY-005: 更新不存在的分类失败")
    void testUpdate_CategoryNotFound() {
        // Given: 分类不存在
        Long id = 999L;
        when(categoryMapper.selectById(id)).thenReturn(null);

        // When & Then: 执行并验证异常
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> categoryService.update(id, "新名称", null, null, null),
            "应抛出分类不存在异常"
        );

        assertEquals(ResultCode.CATEGORY_NOT_FOUND.getCode(), exception.getCode(),
            "错误码应为分类不存在");

        // 验证更新未被调用
        verify(categoryMapper, never()).updateById(any(Category.class));
    }

    /**
     * 测试用例：删除分类成功
     * 
     * Given: 分类存在
     * When: 删除分类
     * Then: 分类删除成功
     */
    @Test
    @DisplayName("TC-CATEGORY-006: 删除分类成功")
    void testDelete_Success() {
        // Given: 分类存在
        Long id = 1L;
        when(categoryMapper.selectById(id)).thenReturn(testCategory);
        when(categoryMapper.deleteById(id)).thenReturn(1);

        // When: 执行删除分类
        categoryService.delete(id);

        // Then: 验证分类被删除
        verify(categoryMapper, times(1)).deleteById(id);
    }

    /**
     * 测试用例：删除不存在的分类失败
     * 
     * Given: 分类不存在
     * When: 尝试删除分类
     * Then: 抛出分类不存在异常
     */
    @Test
    @DisplayName("TC-CATEGORY-007: 删除不存在的分类失败")
    void testDelete_CategoryNotFound() {
        // Given: 分类不存在
        Long id = 999L;
        when(categoryMapper.selectById(id)).thenReturn(null);

        // When & Then: 执行并验证异常
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> categoryService.delete(id),
            "应抛出分类不存在异常"
        );

        assertEquals(ResultCode.CATEGORY_NOT_FOUND.getCode(), exception.getCode(),
            "错误码应为分类不存在");

        // 验证删除未被调用
        verify(categoryMapper, never()).deleteById(anyLong());
    }

    /**
     * 测试用例：查询启用的分类列表
     * 
     * Given: 存在多个分类，部分启用部分禁用
     * When: 查询启用的分类列表
     * Then: 返回所有启用的分类，按排序值升序排列
     */
    @Test
    @DisplayName("TC-CATEGORY-008: 查询启用的分类列表")
    void testListEnabled_Success() {
        // Given: 存在启用的分类
        List<Category> enabledCategories = Arrays.asList(testCategory, testCategory2);
        when(categoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(enabledCategories);

        // When: 执行查询
        List<Category> result = categoryService.listEnabled();

        // Then: 验证结果
        assertNotNull(result, "结果不应为空");
        assertEquals(2, result.size(), "应返回2个分类");
        assertEquals("零食", result.get(0).getName(), "第一个分类应为零食");
        assertEquals("饮料", result.get(1).getName(), "第二个分类应为饮料");

        // 验证查询被调用
        verify(categoryMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试用例：查询所有分类列表
     * 
     * Given: 存在多个分类
     * When: 查询所有分类列表
     * Then: 返回所有分类，按排序值升序排列
     */
    @Test
    @DisplayName("TC-CATEGORY-009: 查询所有分类列表")
    void testListAll_Success() {
        // Given: 存在分类
        List<Category> allCategories = Arrays.asList(testCategory, testCategory2);
        when(categoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(allCategories);

        // When: 执行查询
        List<Category> result = categoryService.listAll();

        // Then: 验证结果
        assertNotNull(result, "结果不应为空");
        assertEquals(2, result.size(), "应返回2个分类");

        // 验证查询被调用
        verify(categoryMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试用例：查询分类列表为空
     * 
     * Given: 不存在任何分类
     * When: 查询分类列表
     * Then: 返回空列表
     */
    @Test
    @DisplayName("TC-CATEGORY-010: 查询分类列表为空")
    void testListAll_Empty() {
        // Given: 不存在分类
        when(categoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        // When: 执行查询
        List<Category> result = categoryService.listAll();

        // Then: 验证结果
        assertNotNull(result, "结果不应为null");
        assertTrue(result.isEmpty(), "结果应为空列表");
    }

    /**
     * 测试用例：根据ID获取分类成功
     * 
     * Given: 分类存在
     * When: 根据ID获取分类
     * Then: 返回对应的分类
     */
    @Test
    @DisplayName("TC-CATEGORY-011: 根据ID获取分类成功")
    void testGetById_Success() {
        // Given: 分类存在
        Long id = 1L;
        when(categoryMapper.selectById(id)).thenReturn(testCategory);

        // When: 执行查询
        Category result = categoryService.getById(id);

        // Then: 验证结果
        assertNotNull(result, "结果不应为空");
        assertEquals(id, result.getId(), "ID应匹配");
        assertEquals("零食", result.getName(), "名称应匹配");
    }

    /**
     * 测试用例：根据ID获取不存在的分类失败
     * 
     * Given: 分类不存在
     * When: 根据ID获取分类
     * Then: 抛出分类不存在异常
     */
    @Test
    @DisplayName("TC-CATEGORY-012: 根据ID获取不存在的分类失败")
    void testGetById_NotFound() {
        // Given: 分类不存在
        Long id = 999L;
        when(categoryMapper.selectById(id)).thenReturn(null);

        // When & Then: 执行并验证异常
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> categoryService.getById(id),
            "应抛出分类不存在异常"
        );

        assertEquals(ResultCode.CATEGORY_NOT_FOUND.getCode(), exception.getCode(),
            "错误码应为分类不存在");
    }
}