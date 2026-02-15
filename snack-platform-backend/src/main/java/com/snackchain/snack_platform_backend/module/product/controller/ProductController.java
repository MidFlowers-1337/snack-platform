package com.snackchain.snack_platform_backend.module.product.controller;

import com.snackchain.snack_platform_backend.common.result.Result;
import com.snackchain.snack_platform_backend.mapper.OrderItemMapper;
import com.snackchain.snack_platform_backend.module.product.vo.HotProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品推荐控制器（消费者端）
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "商品推荐-消费者端")
public class ProductController {

    private final OrderItemMapper orderItemMapper;

    /**
     * 获取热销商品推荐
     */
    @Operation(summary = "获取热销商品推荐")
    @GetMapping("/recommendations/hot")
    public Result<List<HotProductVO>> getHotProducts(
            @RequestParam(defaultValue = "10") int limit) {
        List<HotProductVO> products = orderItemMapper.selectHotProducts(limit);
        return Result.success(products);
    }
}
