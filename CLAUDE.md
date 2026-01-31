# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**ewhat-backend** is a multi-tenant campus service platform built with Spring Boot 3.5.6 and Java 21. The system serves multiple schools with separate tenant isolation.

## Core Architecture

### Multi-Tenancy

The system uses **school-based multi-tenancy** where each school (`school_id`) is isolated:

- **Tenant Context**: `TenantContextHolder` (context/TenantContextHolder.java:8) holds `schoolId`, `role`, and `tenantBypass` flags in ThreadLocal
- **Tenant Isolation**: `MybatisPlusConfig` (config/MybatisPlusConfig.java:29) auto-injects `school_id` into queries via `TenantLineInnerInterceptor`
- **Global Tables**: `tb_school` is excluded from tenant filtering (line 48)
- **Roles**: 1 = school admin, 2 = super admin
- **Bypass Mode**: Set via `TenantContextHolder.setTenantBypass(true)` for cross-tenant operations

**Important**: Always ensure `TenantContextHolder.setSchoolId()` is called before tenant-scoped operations. The interceptor throws if schoolId is null.

### Authentication Flow

JWT-based dual authentication:

1. **Admin Flow** (`/admin/**`):
   - Interceptor: `JwtTokenAdminInterceptor` (interceptor/JwtTokenAdminInterceptor.java:1)
   - Config: `JwtProperties.admin*` (application.yml)
   - Skips: `/admin/employee/login`

2. **User Flow** (`/user/**`):
   - Interceptor: `JwtTokenUserInterceptor` (interceptor/JwtTokenUserInterceptor.java:1)
   - Config: `JwtProperties.user*`
   - Skips: `/user/user/login`, `/user/school/list`, `/user/shop/status`

Registration in `WebMvcConfiguration` (config/WebMvcConfiguration.java:19).

### Layered Architecture

```
controller/  ->  service/  ->  mapper/  ->  database
    |            |           |
    v            v           v
  model/dto  model/vo   model/entity
```

- **Controllers**: Handle HTTP requests, validation, delegate to services
- **Services**: Business logic, transactions (`@Transactional`), coordinate multiple mappers
- **Mappers**: MyBatis interfaces + XML (src/main/resources/mapper/*.xml)
- **Models**:
  - `entity/`: Database entities (map to `tb_*` tables)
  - `dto/`: Request DTOs
  - `vo/`: Response VOs
  - `query/`: Query criteria objects

### Response Wrapper

All controllers return `BaseResponse<T>`:

```java
// Success
return ResultUtils.success(data);

// Business error
ThrowUtils.throwIf(condition, ErrorCode.PARAMS_ERROR, "message");
```

`GlobalExceptionHandler` (exception/GlobalExceptionHandler.java:1) converts `BusinessException` to error responses.

## Key Integrations

| Component | Purpose | Config |
|-----------|---------|--------|
| MyBatis-Plus | ORM, multi-tenancy, pagination | `MybatisPlusConfig` |
| Redisson | Distributed lock, caching | `RedisIdWorker` |
| Aliyun OSS / Tencent COS | File storage | `CosManager`, `CosClientConfig` |
| WeChat Pay | Payment integration | `ewhat.wechat.*` properties |
| Springdoc | OpenAPI/Swagger UI | `/api/doc.html` |

## Database Schema

Tables use `tb_` prefix. Key tenant-scoped tables include `user`, `employee`, `blog`, `product`, `order`, etc. `tb_school` is the global tenant registry.

## API Documentation

- Swagger UI: `http://localhost:8123/api/doc.html`
- OpenAPI JSON: `http://localhost:8123/api/v3/api-docs/default`
- Export script: `./scripts/export-openapi.sh` or `./scripts/export-openapi.ps1`

## Development Notes

- Use `@EnableAspectJAutoProxy(exposeProxy = true)` for AOP proxy injection (EwhatBackendApplication.java:8)
- Mapper scan: `@MapperScan("com.lushihao.ewhatbackend.mapper")`
- Lombok `@Slf4j` is standard for logging
- Time fields: Prefer `java.time.LocalDateTime` over legacy Date types
