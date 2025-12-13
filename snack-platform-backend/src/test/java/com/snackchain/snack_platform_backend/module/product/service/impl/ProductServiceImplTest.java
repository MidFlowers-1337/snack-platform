package com.snackchain.snack_platform_backend.module.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snackchain.snack_platform_backend.common.exception.BusinessException;
import com.snackchain.snack_platform_backend.common.result.ResultCode;
import com.snackchain.snack_platform_backend.entity.Category;
import com.snackchain.snack_platform_backend.entity.Product;
import com.snackchain.snack_platform_backend.mapper.CategoryMapper;
import com.snackchain.snack_platform_backend.mapper.ProductMapper;
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
 * 商品服务单元测试
 * 
 * 测试目的：验证商品服务的核心业务逻辑
 * 测试范围：
 * - 创建商品（正常流程、分类不存在场景）
 * - 更新商品
 * - 删除商品
 * - 上下架商品
 * - 查询商品列表
 * 
 * @author Test
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("商品服务单元测试")
class ProductServiceImplTest {

    @Mock
    private ProductMapper productMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    // 测试数据
    private Product testProduct;
    private Product testProduct2;
    private Category testCategory;

    /**
     * 初始化测试数据
     * 在每个测试方法执行前准备必要的测试对象
     */
    @BeforeEach
    void setUp() {
        // 初始化测试分类
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("零食");
        testCategory.setIcon("snack.png");
        testCategory.setSort(1);
        testCategory.setStatus(1);

        // 初始化测试商品1
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setCategoryId(1L);
        testProduct.setName("薯片");
        testProduct.setDescription("美味薯片");
        testProduct.setImage("chips.jpg");
        testProduct.setPrice(new BigDecimal("9.90"));
        testProduct.setStatus(1);
        testProduct.setCreateTime(LocalDateTime.now());
        testProduct.setUpdateTime(LocalDateTime.now());

        // 初始化测试商品2
        testProduct2 = new Product();
        testProduct2.setId(2L);
        testProduct2.setCategoryId(1L);
        testProduct2.setName("饼干");
        testProduct2.setDescription("香脆饼干");
        testProduct2.setImage("biscuit.jpg");
        testProduct2.setPrice(new BigDecimal("12.50"));
        testProduct2.setStatus(1);
        testProduct2.setCreateTime(LocalDateTime.now());
        testProduct2.setUpdateTime(LocalDateTime.now());
    }

    /**
     * 测试用例：创建商品成功
     * 
     * Given: 分类存在
     * When: 创建商品
     * Then: 商品创建成功
     */
    @Test
    @DisplayName("TC-PRODUCT-001: 创建商品成功")
    void testCreate_Success() {
        // Given: 分类存在
        Long categoryId = 1L;
        String name = "新商品";
        String description = "商品描述";
        String image = "new.jpg";
        BigDecimal price = new BigDecimal("15.00");

        when(categoryMapper.selectById(categoryId)).thenReturn(testCategory);
        when(productMapper.insert(any(Product.class))).thenReturn(1);

        // When: 执行创建商品
        productService.create(categoryId, name, description, image, price);

        // Then: 验证商品被插入数据库
        verify(productMapper, times(1)).insert(argThat(product ->
            categoryId.equals(product.getCategoryId()) &&
            name.equals(product.getName()) &&
            description.equals(product.getDescription()) &&
            image.equals(product.getImage()) &&
            price.equals(product.getPrice()) &&
            product.getStatus() == 1
        ));
    }

    /**
     * 测试用例：创建商品时分类不存在失败
     * 
     * Given: 分类不存在
     * When: 创建商品
     * Then: 抛出分类不存在异常
     */
    @Test
    @DisplayName("TC-PRODUCT-002: 创建商品时分类不存在失败")
    void testCreate_CategoryNotFound() {
        // Given: 分类不存在
        Long categoryId = 999L;
        when(categoryMapper.selectById(categoryId)).thenReturn(null);

        // When & Then: 执行并验证异常
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> productService.create(categoryId, "新商品", "描述", "image.jpg", new BigDecimal("10.00")),
            "应抛出分类不存在异常"
        );

        assertEquals(ResultCode.CATEGORY_NOT_FOUND.getCode(), exception.getCode(),
            "错误码应为分类不存在");

        // 验证商品未被插入
        verify(productMapper, never()).insert(any(Product.class));
    }

    /**
     * 测试用例：创建商品时分类ID为空
     * 
     * Given: 分类ID为null
     * When: 创建商品
     * Then: 商品创建成功（允许无分类）
     */
    @Test
    @DisplayName("TC-PRODUCT-003: 创建商品时分类ID为空")
    void testCreate_NullCategoryId() {
        // Given: 分类ID为null
        String name = "无分类商品";
        String description = "商品描述";
        String image = "new.jpg";
        BigDecimal price = new BigDecimal("15.00");

        when(productMapper.insert(any(Product.class))).thenReturn(1);

        // When: 执行创建商品
        productService.create(null, name, description, image, price);

        // Then: 验证商品被插入数据库
        verify(productMapper, times(1)).insert(argThat(product ->
            product.getCategoryId() == null &&
            name.equals(product.getName())
        ));

        // 验证未查询分类
        verify(categoryMapper, never()).selectById(anyLong());
    }

    /**
     * 测试用例：更新商品成功
     * 
     * Given: 商品存在，分类存在
     * When: 更新商品信息
     * Then: 商品更新成功
     */
    @Test
    @DisplayName("TC-PRODUCT-004: 更新商品成功")
    void testUpdate_Success() {
        // Given: 商品存在
        Long id = 1L;
        Long newCategoryId = 2L;
        String newName = "更新后的商品";
        String newDescription = "更新后的描述";
        String newImage = "updated.jpg";
        BigDecimal newPrice = new BigDecimal("20.00");
        Integer newStatus = 0;

        Category newCategory = new Category();
        newCategory.setId(2L);
        newCategory.setName("饮料");

        when(productMapper.selectById(id)).thenReturn(testProduct);
        when(categoryMapper.selectById(newCategoryId)).thenReturn(newCategory);
        when(productMapper.updateById(any(Product.class))).thenReturn(1);

        // When: 执行更新商品
        productService.update(id, newCategoryId, newName, newDescription, newImage, newPrice, newStatus);

        // Then: 验证商品被更新
        verify(productMapper, times(1)).updateById(argThat(product ->
            newCategoryId.equals(product.getCategoryId()) &&
            newName.equals(product.getName()) &&
            newDescription.equals(product.getDescription()) &&
            newImage.equals(product.getImage()) &&
            newPrice.equals(product.getPrice()) &&
            newStatus.equals(product.getStatus())
        ));
    }

    /**
     * 测试用例：部分更新商品成功
     * 
     * Given: 商品存在
     * When: 只更新部分字段
     * Then: 只有非null字段被更新
     */
    @Test
    @DisplayName("TC-PRODUCT-005: 部分更新商品成功")
    void testUpdate_PartialUpdate() {
        // Given: 商品存在
        Long id = 1L;
        String newName = "只更新名称";

        when(productMapper.selectById(id)).thenReturn(testProduct);
        when(productMapper.updateById(any(Product.class))).thenReturn(1);

        // When: 执行部分更新
        productService.update(id, null, newName, null, null, null, null);

        // Then: 验证只有名称被更新，其他字段保持原值
        verify(productMapper, times(1)).updateById(argThat(product ->
            newName.equals(product.getName()) &&
            product.getCategoryId().equals(1L) &&
            "美味薯片".equals(product.getDescription()) &&
            "chips.jpg".equals(product.getImage()) &&
            new BigDecimal("9.90").equals(product.getPrice()) &&
            product.getStatus() == 1
        ));
    }

    /**
     * 测试用例：更新商品时分类不存在失败
     * 
     * Given: 商品存在，但新分类不存在
     * When: 更新商品分类
     * Then: 抛出分类不存在异常
     */
    @Test
    @DisplayName("TC-PRODUCT-006: 更新商品时分类不存在失败")
    void testUpdate_CategoryNotFound() {
        // Given: 商品存在，但新分类不存在
        Long id = 1L;
        Long newCategoryId = 999L;

        when(productMapper.selectById(id)).thenReturn(testProduct);
        when(categoryMapper.selectById(newCategoryId)).thenReturn(null);

        // When & Then: 执行并验证异常
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> productService.update(id, newCategoryId, null, null, null, null, null),
            "应抛出分类不存在异常"
        );

        assertEquals(ResultCode.CATEGORY_NOT_FOUND.getCode(), exception.getCode(),
            "错误码应为分类不存在");

        // 验证商品未被更新
        verify(productMapper, never()).updateById(any(Product.class));
    }

    /**
     * 测试用例：更新不存在的商品失败
     * 
     * Given: 商品不存在
     * When: 尝试更新商品
     * Then: 抛出商品不存在异常
     */
    @Test
    @DisplayName("TC-PRODUCT-007: 更新不存在的商品失败")
    void testUpdate_ProductNotFound() {
        // Given: 商品不存在
        Long id = 999L;
        when(productMapper.selectById(id)).thenReturn(null);

        // When & Then: 执行并验证异常
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> productService.update(id, null, "新名称", null, null, null, null),
            "应抛出商品不存在异常"
        );

        assertEquals(ResultCode.PRODUCT_NOT_FOUND.getCode(), exception.getCode(),
            "错误码应为商品不存在");
    }

    /**
     * 测试用例：删除商品成功
     * 
     * Given: 商品存在
     * When: 删除商品
     * Then: 商品删除成功
     */
    @Test
    @DisplayName("TC-PRODUCT-008: 删除商品成功")
    void testDelete_Success() {
        // Given: 商品存在
        Long id = 1L;
        when(productMapper.selectById(id)).thenReturn(testProduct);
        when(productMapper.deleteById(id)).thenReturn(1);

        // When: 执行删除商品
        productService.delete(id);

        // Then: 验证商品被删除
        verify(productMapper, times(1)).deleteById(id);
    }

    /**
     * 测试用例：删除不存在的商品失败
     * 
     * Given: 商品不存在
     * When: 尝试删除商品
     * Then: 抛出商品不存在异常
     */
    @Test
    @DisplayName("TC-PRODUCT-009: 删除不存在的商品失败")
    void testDelete_ProductNotFound() {
        // Given: 商品不存在
        Long id = 999L;
        when(productMapper.selectById(id)).thenReturn(null);

        // When & Then: 执行并验证异常
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> productService.delete(id),
            "应抛出商品不存在异常"
        );

        assertEquals(ResultCode.PRODUCT_NOT_FOUND.getCode(), exception.getCode(),
            "错误码应为商品不存在");

        // 验证删除未被调用
        verify(productMapper, never()).deleteById(anyLong());
    }

    /**
     * 测试用例：商品上架成功
     * 
     * Given: 商品存在且已下架
     * When: 执行上架操作
     * Then: 商品状态变为上架
     */
    @Test
    @DisplayName("TC-PRODUCT-010: 商品上架成功")
    void testOnShelf_Success() {
        // Given: 商品存在且已下架
        Long id = 1L;
        testProduct.setStatus(0);
        when(productMapper.selectById(id)).thenReturn(testProduct);
        when(productMapper.updateById(any(Product.class))).thenReturn(1);

        // When: 执行上架
        productService.onShelf(id);

        // Then: 验证商品状态变为上架
        verify(productMapper, times(1)).updateById(argThat(product ->
            product.getStatus() == 1
        ));
    }

    /**
     * 测试用例：商品下架成功
     * 
     * Given: 商品存在且已上架
     * When: 执行下架操作
     * Then: 商品状态变为下架
     */
    @Test
    @DisplayName("TC-PRODUCT-011: 商品下架成功")
    void testOffShelf_Success() {
        // Given: 商品存在且已上架
        Long id = 1L;
        testProduct.setStatus(1);
        when(productMapper.selectById(id)).thenReturn(testProduct);
        when(productMapper.updateById(any(Product.class))).thenReturn(1);

        // When: 执行下架
        productService.offShelf(id);

        // Then: 验证商品状态变为下架
        verify(productMapper, times(1)).updateById(argThat(product ->
            product.getStatus() == 0
        ));
    }

    /**
     * 测试用例：上架不存在的商品失败
     * 
     * Given: 商品不存在
     * When: 尝试上架商品
     * Then: 抛出商品不存在异常
     */
    @Test
    @DisplayName("TC-PRODUCT-012: 上架不存在的商品失败")
    void testOnShelf_ProductNotFound() {
        // Given: 商品不存在
        Long id = 999L;
        when(productMapper.selectById(id)).thenReturn(null);

        // When & Then: 执行并验证异常
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> productService.onShelf(id),
            "应抛出商品不存在异常"
        );

        assertEquals(ResultCode.PRODUCT_NOT_FOUND.getCode(), exception.getCode(),
            "错误码应为商品不存在");
    }

    /**
     * 测试用例：查询上架商品列表
     * 
     * Given: 存在上架商品
     * When: 查询上架商品列表
     * Then: 返回所有上架商品
     */
    @Test
    @DisplayName("TC-PRODUCT-013: 查询上架商品列表")
    void testListOnShelf_Success() {
        // Given: 存在上架商品
        List<Product> products = Arrays.asList(testProduct, testProduct2);
        when(productMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(products);
        when(categoryMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(testCategory));

        // When: 执行查询
        List<Product> result = productService.listOnShelf();

        // Then: 验证结果
        assertNotNull(result, "结果不应为空");
        assertEquals(2, result.size(), "应返回2个商品");
    }

    /**
     * 测试用例：根据分类查询商品列表
     * 
     * Given: 存在指定分类的商品
     * When: 根据分类ID查询商品
     * Then: 返回该分类下的所有上架商品
     */
    @Test
    @DisplayName("TC-PRODUCT-014: 根据分类查询商品列表")
    void testListByCategoryId_Success() {
        // Given: 存在指定分类的商品
        Long categoryId = 1L;
        List<Product> products = Arrays.asList(testProduct, testProduct2);
        when(productMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(products);
        when(categoryMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(testCategory));

        // When: 执行查询
        List<Product> result = productService.listByCategoryId(categoryId);

        // Then: 验证结果
        assertNotNull(result, "结果不应为空");
        assertEquals(2, result.size(), "应返回2个商品");
    }

    /**
     * 测试用例：根据ID获取商品成功
     * 
     * Given: 商品存在
     * When: 根据ID获取商品
     * Then: 返回对应的商品
     */
    @Test
    @DisplayName("TC-PRODUCT-015: 根据ID获取商品成功")
    void testGetById_Success() {
        // Given: 商品存在
        Long id = 1L;
        when(productMapper.selectById(id)).thenReturn(testProduct);
        when(categoryMapper.selectById(1L)).thenReturn(testCategory);

        // When: 执行查询
        Product result = productService.getById(id);

        // Then: 验证结果
        assertNotNull(result, "结果不应为空");
        assertEquals(id, result.getId(), "ID应匹配");
        assertEquals("薯片", result.getName(), "名称应匹配");
        assertNotNull(result.getCategory(), "分类信息应被填充");
    }

    /**
     * 测试用例：根据ID获取不存在的商品失败
     * 
     * Given: 商品不存在
     * When: 根据ID获取商品
     * Then: 抛出商品不存在异常
     */
    @Test
    @DisplayName("TC-PRODUCT-016: 根据ID获取不存在的商品失败")
    void testGetById_NotFound() {
        // Given: 商品不存在
        Long id = 999L;
        when(productMapper.selectById(id)).thenReturn(null);

        // When & Then: 执行并验证异常
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> productService.getById(id),
            "应抛出商品不存在异常"
        );

        assertEquals(ResultCode.PRODUCT_NOT_FOUND.getCode(), exception.getCode(),
            "错误码应为商品不存在");
    }

    /**
     * 测试用例：分页查询商品列表
     * 
     * Given: 存在商品
     * When: 分页查询商品
     * Then: 返回分页结果
     */
    @Test
    @DisplayName("TC-PRODUCT-017: 分页查询商品列表")
    void testPage_Success() {
        // Given: 存在商品
        int pageNum = 1;
        int pageSize = 10;
        List<Product> products = Arrays.asList(testProduct, testProduct2);
        
        Page<Product> mockPage = new Page<>(pageNum, pageSize);
        mockPage.setRecords(products);
        mockPage.setTotal(2);
        
        when(productMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);
        when(categoryMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(testCategory));

        // When: 执行分页查询
        IPage<Product> result = productService.page(pageNum, pageSize, null, null, null);

        // Then: 验证结果
        assertNotNull(result, "结果不应为空");
        assertEquals(2, result.getRecords().size(), "应返回2个商品");
        assertEquals(2, result.getTotal(), "总数应为2");
    }

    /**
     * 测试用例：分页查询带筛选条件
     * 
     * Given: 存在商品
     * When: 带筛选条件分页查询
     * Then: 返回符合条件的分页结果
     */
    @Test
    @DisplayName("TC-PRODUCT-018: 分页查询带筛选条件")
    void testPage_WithFilters() {
        // Given: 存在商品
        int pageNum = 1;
        int pageSize = 10;
        Long categoryId = 1L;
        String keyword = "薯片";
        Integer status = 1;
        
        List<Product> products = Collections.singletonList(testProduct);
        
        Page<Product> mockPage = new Page<>(pageNum, pageSize);
        mockPage.setRecords(products);
        mockPage.setTotal(1);
        
        when(productMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mockPage);
        when(categoryMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(testCategory));

        // When: 执行带筛选条件的分页查询
        IPage<Product> result = productService.page(pageNum, pageSize, categoryId, keyword, status);

        // Then: 验证结果
        assertNotNull(result, "结果不应为空");
        assertEquals(1, result.getRecords().size(), "应返回1个商品");
    }
}