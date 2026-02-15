package com.snackchain.snack_platform_backend.module.promotion.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.snackchain.snack_platform_backend.common.annotation.OperationLog;
import com.snackchain.snack_platform_backend.common.result.Result;
import com.snackchain.snack_platform_backend.entity.Promotion;
import com.snackchain.snack_platform_backend.module.promotion.dto.PromotionDTO;
import com.snackchain.snack_platform_backend.module.promotion.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 促销活动控制器（管理员端 + 消费者端）
 *
 * What: 管理端CRUD + 消费者端查询进行中的促销活动
 * Why: 限时促销/秒杀是电商核心营销手段
 * Why good: CAS乐观锁防超卖 + 时间校验保证促销规则
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "促销活动")
public class PromotionController {

    private final PromotionService promotionService;

    // ========== 管理端接口 ==========

    @Operation(summary = "创建促销活动")
    @PostMapping("/admin/promotions")
    @OperationLog(module = "促销管理", operation = "创建促销活动")
    public Result<Promotion> create(@Valid @RequestBody PromotionDTO dto) {
        Promotion promotion = promotionService.create(dto);
        return Result.success(promotion);
    }

    @Operation(summary = "更新促销活动")
    @PutMapping("/admin/promotions/{id}")
    @OperationLog(module = "促销管理", operation = "更新促销活动")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody PromotionDTO dto) {
        promotionService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除促销活动")
    @DeleteMapping("/admin/promotions/{id}")
    @OperationLog(module = "促销管理", operation = "删除促销活动")
    public Result<Void> delete(@PathVariable Long id) {
        promotionService.delete(id);
        return Result.success();
    }

    @Operation(summary = "分页查询促销活动")
    @GetMapping("/admin/promotions")
    public Result<IPage<Promotion>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(promotionService.page(pageNum, pageSize));
    }

    // ========== 消费者端接口 ==========

    @Operation(summary = "获取进行中的促销活动")
    @GetMapping("/promotions/active")
    public Result<List<Promotion>> listActive() {
        return Result.success(promotionService.listActive());
    }
}
