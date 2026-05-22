package com.snackchain.snack_platform_backend.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 配置。
 */
@Configuration
@MapperScan("com.snackchain.snack_platform_backend.mapper")
public class MyBatisPlusConfig {

    /**
     * 分页插件。
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * 自动填充处理器。
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                LocalDateTime now = LocalDateTime.now();
                fillIfAbsent(metaObject, "createTime", now);
                fillIfAbsent(metaObject, "updateTime", now);
                // 兼容部分实体使用 createdAt/updatedAt 命名。
                fillIfAbsent(metaObject, "createdAt", now);
                fillIfAbsent(metaObject, "updatedAt", now);
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                LocalDateTime now = LocalDateTime.now();
                fillIfPresent(metaObject, "updateTime", now);
                fillIfPresent(metaObject, "updatedAt", now);
            }

            private void fillIfAbsent(MetaObject metaObject, String fieldName, LocalDateTime value) {
                if (metaObject.hasSetter(fieldName) && getFieldValByName(fieldName, metaObject) == null) {
                    setFieldValByName(fieldName, value, metaObject);
                }
            }

            private void fillIfPresent(MetaObject metaObject, String fieldName, LocalDateTime value) {
                if (metaObject.hasSetter(fieldName)) {
                    setFieldValByName(fieldName, value, metaObject);
                }
            }
        };
    }
}
