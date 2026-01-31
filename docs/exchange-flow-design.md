# 兑换流程实现文档

## 概述

已完成完整的商品兑换流程，包括：**下单 → 支付（积分）→ 核销**。

## 订单状态流转

```
1(未支付) → 2(已支付) → 3(已核销)
    ↓
4(已取消) / 6(已退款)
```

| 状态码 | 状态 | 说明 |
|--------|------|------|
| 1 | 未支付 | 用户下单后15分钟内需支付 |
| 2 | 已支付 | 支付积分后生成兑换码 |
| 3 | 已核销 | 管理员在食堂核销完成 |
| 4 | 已取消 | 用户主动取消或过期自动取消 |
| 5 | 退款中 | 申请退款处理中 |
| 6 | 已退款 | 退款完成 |

## 数据库变更

### 新增字段

```sql
-- 订单表新增字段
ALTER TABLE tb_product_order 
ADD COLUMN order_type TINYINT DEFAULT 0 COMMENT '订单类型：0-普通，1-秒杀',
ADD COLUMN exchange_code VARCHAR(16) NULL COMMENT '兑换码',
ADD COLUMN exchange_code_img VARCHAR(255) NULL COMMENT '二维码图片',
ADD COLUMN expire_time TIMESTAMP NULL COMMENT '过期时间';
```

## API接口列表

### 用户端接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 普通下单 | POST | `/user/product-order/{id}` | 创建普通商品订单 |
| 秒杀下单 | POST | `/user/product-order/seckill/{id}` | 创建秒杀订单 |
| 支付订单 | POST | `/user/product-order/pay/{id}` | 使用积分支付 |
| 取消订单 | POST | `/user/product-order/cancel/{id}` | 取消未支付订单 |
| 订单列表 | GET | `/user/product-order/list` | 查询我的订单 |
| 订单详情 | GET | `/user/product-order/detail/{id}` | 查看订单详情 |

### 管理端接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 核销订单 | POST | `/admin/product-order/use` | 输入兑换码核销 |
| 订单列表 | GET | `/admin/product-order/page` | 分页查询订单 |

## 核心流程

### 1. 下单流程

```java
// 普通商品下单
POST /user/product-order/1
→ 生成订单ID
→ 状态：未支付(1)
→ 设置15分钟过期时间
→ 返回订单ID
```

### 2. 支付流程

```java
// 支付订单
POST /user/product-order/pay/{orderId}
→ 检查订单状态（必须为未支付）
→ 检查订单是否过期
→ 查询商品所需积分
→ 检查用户积分余额
→ 扣除用户积分（乐观锁）
→ 生成8位兑换码（如：A3B7C9D2）
→ 更新订单状态为已支付(2)
→ 记录积分流水
→ 返回订单信息（含兑换码）
```

### 3. 核销流程

```java
// 管理员核销
POST /admin/product-order/use?exchangeCode=A3B7C9D2
→ 查询订单（通过兑换码）
→ 验证管理员权限（同学校）
→ 检查订单状态（必须为已支付）
→ 更新订单状态为已核销(3)
→ 记录核销时间
→ 返回订单信息
```

## 兑换码生成规则

```java
private String generateExchangeCode() {
    String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // 去除易混淆字符
    StringBuilder code = new StringBuilder();
    for (int i = 0; i < 8; i++) {
        code.append(chars.charAt((int) (Math.random() * chars.length())));
    }
    return code.toString();
}
```

- 8位字母数字组合
- 去除易混淆字符（0,1,I,O）
- 数据库唯一索引约束

## 安全机制

1. **积分扣除乐观锁**：防止并发导致积分超扣
   ```java
   userService.update()
       .setSql("points = points - " + points)
       .eq("id", userId)
       .ge("points", points)  // 乐观锁
       .update();
   ```

2. **兑换码唯一性**：数据库唯一索引

3. **权限校验**：
   - 用户只能操作自己的订单
   - 管理员只能核销本校订单

4. **过期自动处理**：支付时检查，过期自动取消

## 积分流水

支付时自动记录积分流水：
- type=2（订单支付）
- points为负数（积分减少）
- 关联order_id

## 测试数据

已在 `sel/11_tb_product_order.sql` 中准备测试数据，包含各种状态的订单。

## 后续优化建议

1. **二维码生成**：接入二维码生成服务，将兑换码转为二维码图片
2. **过期自动取消**：使用定时任务扫描过期订单
3. **退款功能**：完善退款申请和审核流程
4. **消息通知**：支付成功、核销成功发送通知
5. **防刷限流**：秒杀接口添加限流保护
