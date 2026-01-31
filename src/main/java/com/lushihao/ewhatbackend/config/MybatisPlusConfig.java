package com.lushihao.ewhatbackend.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.lushihao.ewhatbackend.context.TenantContextHolder;
import com.lushihao.ewhatbackend.exception.ErrorCode;
import com.lushihao.ewhatbackend.exception.ThrowUtils;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * MyBatisPlus分页配置
 * @author lushihao
 */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // Multi-tenant (tenant id = schoolId, column = school_id)
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                Long schoolId = TenantContextHolder.getSchoolId();
                ThrowUtils.throwIf(schoolId == null, ErrorCode.PARAMS_ERROR,"Tenant schoolId is not set");
                return new LongValue(schoolId);
            }

            @Override
            public String getTenantIdColumn() {
                return "school_id";
            }

            @Override
            public boolean ignoreTable(String tableName) {
                if (TenantContextHolder.isTenantBypass()) {
                    return true;
                }
                // global table
                return "tb_school".equalsIgnoreCase(tableName);
            }
        }));

        // 添加分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
