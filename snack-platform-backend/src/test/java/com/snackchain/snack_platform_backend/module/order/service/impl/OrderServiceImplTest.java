package com.snackchain.snack_platform_backend.module.order.service.impl;

import com.snackchain.snack_platform_backend.common.exception.BusinessException;
import com.snackchain.snack_platform_backend.common.result.ResultCode;
import com.snackchain.snack_platform_backend.common.service.NotificationService;
import com.snackchain.snack_platform_backend.entity.*;
import com.snackchain.snack_platform_backend.enums.OrderStatus;
import com.snackchain.snack_platform_backend.mapper.*;
import com.snackchain.snack_platform_backend.module.order.dto.CreateOrderDTO;
import com.snackchain.snack_platform_backend.module.order.dto.OrderItemDTO;
import com.snackchain.snack_platform_backend.module.sku.service.StoreSkuService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 订单服务单元测试
 * 
 * 测试目的：验证订单服务的核心业务逻辑
 * 测试范围：
 * - 创建订单（正常流程、库存不足场景）
 * - 取消订单（库存回滚）
 * - 支付订单（生成自提码）
 * - 门店接单（状态变更）
 * 
 * @author Test
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("订单服务单元测试")
class OrderServiceImplTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @Mock
    private StoreMapper storeMapper;

    @Mock
    private StoreSkuMapper storeSkuMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private StoreSkuService storeSkuService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OrderServiceImpl orderService;

    // 测试数据
    private Store testStore;
    private StoreSku testSku;
    private Product testProduct;
    private Order testOrder;
    private OrderItem testOrderItem;

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
        testStore.setStatus(1); // 营业中

        // 初始化测试商品
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("测试零食");
        testProduct.setImage("test.jpg");
        testProduct.setPrice(new BigDecimal("10.00"));
        testProduct.setStatus(1);

        // 初始化测试SKU
        testSku = new StoreSku();
        testSku.setId(1L);
        testSku.setStoreId(1L);
        testSku.setProductId(1L);
        testSku.setPrice(new BigDecimal("10.00"));
        testSku.setStock(100);
        testSku.setStatus(1); // 上架

        // 初始化测试订单
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderNo("ORD20241213000001");
        testOrder.setUserId(1L);
        testOrder.setStoreId(1L);
        testOrder.setTotalAmount(new BigDecimal("20.00"));
        testOrder.setStatus(OrderStatus.PENDING_PAYMENT.getCode());

        // 初始化测试订单项
        testOrderItem = new OrderItem();
        testOrderItem.setId(1L);
        testOrderItem.setOrderId(1L);
        testOrderItem.setSkuId(1L);
        testOrderItem.setQuantity(2);
    }

    /**
     * 测试用例：正常创建订单
     * 
     * Given: 门店营业中，商品上架，库存充足
     * When: 用户提交订单
     * Then: 订单创建成功，库存被扣减
     */
    @Test
    @DisplayName("TC-ORDER-001: 正常创建订单，验证库存扣减")
    void testCreateOrder_Success() {
        // Given: 准备测试数据
        Long userId = 1L;
        CreateOrderDTO dto = new CreateOrderDTO();
        dto.setStoreId(1L);
        dto.setRemark("测试订单");
        
        OrderItemDTO itemDto = new OrderItemDTO();
        itemDto.setSkuId(1L);
        itemDto.setQuantity(2);
        dto.setItems(Arrays.asList(itemDto));

        // Mock: 模拟依赖行为
        when(storeMapper.selectById(1L)).thenReturn(testStore);
        when(storeSkuService.getById(1L)).thenReturn(testSku);
        when(storeSkuService.deductStock(1L, 2)).thenReturn(true);
        when(productMapper.selectById(1L)).thenReturn(testProduct);
        when(orderMapper.insert(any(Order.class))).thenReturn(1);
        when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);

        // When: 执行创建订单
        Order result = orderService.createOrder(userId, dto);

        // Then: 验证结果
        assertNotNull(result, "订单不应为空");
        assertEquals(userId, result.getUserId(), "用户ID应匹配");
        assertEquals(1L, result.getStoreId(), "门店ID应匹配");
        assertEquals(OrderStatus.PENDING_PAYMENT.getCode(), result.getStatus(), "订单状态应为待支付");
        assertEquals(new BigDecimal("20.00"), result.getTotalAmount(), "订单金额应正确计算");

        // 验证库存扣减被调用
        verify(storeSkuService, times(1)).deductStock(1L, 2);
        // 验证订单和订单项被插入
        verify(orderMapper, times(1)).insert(any(Order.class));
        verify(orderItemMapper, times(1)).insert(any(OrderItem.class));
    }

    /**
     * 测试用例：库存不足时创建订单失败
     * 
     * Given: 商品库存不足
     * When: 用户提交订单
     * Then: 抛出库存不足异常
     */
    @Test
    @DisplayName("TC-STOCK-002: 库存不足时无法下单")
    void testCreateOrder_InsufficientStock() {
        // Given: 准备测试数据，库存设为1，但购买数量为10
        Long userId = 1L;
        CreateOrderDTO dto = new CreateOrderDTO();
        dto.setStoreId(1L);
        
        OrderItemDTO itemDto = new OrderItemDTO();
        itemDto.setSkuId(1L);
        itemDto.setQuantity(10);
        dto.setItems(Arrays.asList(itemDto));

        // 设置库存不足
        testSku.setStock(1);

        // Mock: 模拟依赖行为
        when(storeMapper.selectById(1L)).thenReturn(testStore);
        when(storeSkuService.getById(1L)).thenReturn(testSku);

        // When & Then: 执行并验证异常
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> orderService.createOrder(userId, dto),
            "应抛出库存不足异常"
        );

        assertEquals(ResultCode.STOCK_NOT_ENOUGH.getCode(), exception.getCode(), 
            "错误码应为库存不足");
        
        // 验证库存扣减未被调用
        verify(storeSkuService, never()).deductStock(anyLong(), anyInt());
    }

    /**
     * 测试用例：取消订单后库存回滚
     * 
     * Given: 存在待支付订单
     * When: 用户取消订单
     * Then: 订单状态变为已取消，库存回滚
     */
    @Test
    @DisplayName("TC-STOCK-004: 取消订单后库存回滚")
    void testCancelOrder_StockRollback() {
        // Given: 准备待支付订单
        Long orderId = 1L;
        Long userId = 1L;
        
        testOrder.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        List<OrderItem> orderItems = Arrays.asList(testOrderItem);

        // Mock: 模拟依赖行为
        when(orderMapper.selectById(orderId)).thenReturn(testOrder);
        when(orderItemMapper.selectByOrderId(orderId)).thenReturn(orderItems);
        when(storeMapper.selectById(1L)).thenReturn(testStore);
        doNothing().when(storeSkuService).restoreStock(anyLong(), anyInt());
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        // When: 执行取消订单
        orderService.cancel(orderId, userId);

        // Then: 验证库存回滚被调用
        verify(storeSkuService, times(1)).restoreStock(1L, 2);
        
        // 验证订单状态更新
        verify(orderMapper, times(1)).updateById(argThat(order -> 
            OrderStatus.CANCELLED.getCode().equals(order.getStatus())
        ));
    }

    /**
     * 测试用例：支付成功生成自提码
     * 
     * Given: 存在待支付订单
     * When: 用户完成支付
     * Then: 订单状态变为待接单，生成6位自提码
     */
    @Test
    @DisplayName("TC-ORDER-001: 支付成功生成自提码")
    void testPayOrder_GeneratePickupCode() {
        // Given: 准备待支付订单
        Long orderId = 1L;
        Long userId = 1L;
        
        testOrder.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        testOrder.setUserId(userId);

        // Mock: 模拟依赖行为
        when(orderMapper.selectById(orderId)).thenReturn(testOrder);
        when(orderItemMapper.selectByOrderId(orderId)).thenReturn(Arrays.asList(testOrderItem));
        when(storeMapper.selectById(1L)).thenReturn(testStore);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        // When: 执行支付
        orderService.pay(orderId, userId);

        // Then: 验证订单更新
        verify(orderMapper, times(1)).updateById(argThat(order -> {
            // 验证状态变为待接单
            boolean statusCorrect = OrderStatus.PENDING_ACCEPT.getCode().equals(order.getStatus());
            // 验证生成了自提码（6位数字）
            boolean pickupCodeGenerated = order.getPickupCode() != null 
                && order.getPickupCode().matches("\\d{6}");
            // 验证记录了支付时间
            boolean payTimeRecorded = order.getPayTime() != null;
            
            return statusCorrect && pickupCodeGenerated && payTimeRecorded;
        }));
    }

    /**
     * 测试用例：门店接单状态变更
     * 
     * Given: 存在待接单订单
     * When: 门店管理员接单
     * Then: 订单状态变为已接单，记录接单时间
     */
    @Test
    @DisplayName("TC-ORDER-002: 门店接单状态变更")
    void testAcceptOrder_StatusChange() {
        // Given: 准备待接单订单
        Long orderId = 1L;
        Long storeId = 1L;
        
        testOrder.setStatus(OrderStatus.PENDING_ACCEPT.getCode());
        testOrder.setStoreId(storeId);

        // Mock: 模拟依赖行为
        when(orderMapper.selectById(orderId)).thenReturn(testOrder);
        when(orderItemMapper.selectByOrderId(orderId)).thenReturn(Arrays.asList(testOrderItem));
        when(storeMapper.selectById(storeId)).thenReturn(testStore);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        // When: 执行接单
        orderService.accept(orderId, storeId);

        // Then: 验证订单状态更新
        verify(orderMapper, times(1)).updateById(argThat(order -> {
            // 验证状态变为已接单
            boolean statusCorrect = OrderStatus.ACCEPTED.getCode().equals(order.getStatus());
            // 验证记录了接单时间
            boolean acceptTimeRecorded = order.getAcceptTime() != null;
            
            return statusCorrect && acceptTimeRecorded;
        }));
    }
}
