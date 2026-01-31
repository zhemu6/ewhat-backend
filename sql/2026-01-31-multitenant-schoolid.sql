-- Multi-tenant migration: use school_id as tenant id
-- Strategy: backfill by existing relations; unknown legacy users/employees default to school_id=1.
-- Note: This repo does not use Flyway/Liquibase yet; run this manually in the target DB.

USE ewhat;

-- 1) User / Employee

ALTER TABLE tb_user
    ADD COLUMN school_id BIGINT UNSIGNED NULL COMMENT '学校id(多租户租户键)' AFTER id;

ALTER TABLE tb_employee
    ADD COLUMN school_id BIGINT UNSIGNED NULL COMMENT '学校id(校级管理员必填; 超级管理员可为空)' AFTER id,
    ADD COLUMN role TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '角色: 1=学校管理员, 2=超级管理员' AFTER school_id;

-- legacy defaults
UPDATE tb_user SET school_id = 1 WHERE school_id IS NULL;
UPDATE tb_employee SET school_id = 1 WHERE school_id IS NULL AND role = 1;

-- indexes
CREATE INDEX idx_user_school_id ON tb_user(school_id);
CREATE INDEX idx_employee_school_id ON tb_employee(school_id);

-- 2) Dish / Blog / Comment / Follow

ALTER TABLE tb_dish
    ADD COLUMN school_id BIGINT UNSIGNED NULL COMMENT '学校id(多租户租户键)' AFTER canteen_id;

ALTER TABLE tb_blog
    ADD COLUMN school_id BIGINT UNSIGNED NULL COMMENT '学校id(多租户租户键)' AFTER user_id;

ALTER TABLE tb_blog_comment
    ADD COLUMN school_id BIGINT UNSIGNED NULL COMMENT '学校id(多租户租户键)' AFTER blog_id;

ALTER TABLE tb_follow
    ADD COLUMN school_id BIGINT UNSIGNED NULL COMMENT '学校id(多租户租户键)' AFTER follow_user_id;

-- backfill by joins
UPDATE tb_dish d
    INNER JOIN tb_canteen c ON d.canteen_id = c.id
SET d.school_id = c.school_id
WHERE d.school_id IS NULL;

UPDATE tb_blog b
    INNER JOIN tb_dish d ON b.dish_id = d.id
SET b.school_id = d.school_id
WHERE b.school_id IS NULL;

UPDATE tb_blog_comment bc
    INNER JOIN tb_blog b ON bc.blog_id = b.id
SET bc.school_id = b.school_id
WHERE bc.school_id IS NULL;

UPDATE tb_follow f
    INNER JOIN tb_user u ON f.user_id = u.id
SET f.school_id = u.school_id
WHERE f.school_id IS NULL;

-- indexes
CREATE INDEX idx_dish_school_id ON tb_dish(school_id);
CREATE INDEX idx_blog_school_id ON tb_blog(school_id);
CREATE INDEX idx_blog_comment_school_id ON tb_blog_comment(school_id);
CREATE INDEX idx_follow_school_id ON tb_follow(school_id);

-- 3) Product chain

ALTER TABLE tb_seckill_product
    ADD COLUMN school_id BIGINT UNSIGNED NULL COMMENT '学校id(多租户租户键)' AFTER product_id;

ALTER TABLE tb_product_order
    ADD COLUMN school_id BIGINT UNSIGNED NULL COMMENT '学校id(多租户租户键)' AFTER product_id;

UPDATE tb_seckill_product sp
    INNER JOIN tb_product p ON sp.product_id = p.id
SET sp.school_id = p.school_id
WHERE sp.school_id IS NULL;

UPDATE tb_product_order po
    INNER JOIN tb_product p ON po.product_id = p.id
SET po.school_id = p.school_id
WHERE po.school_id IS NULL;

CREATE INDEX idx_seckill_product_school_id ON tb_seckill_product(school_id);
CREATE INDEX idx_product_order_school_id ON tb_product_order(school_id);

-- 4) Points

ALTER TABLE tb_points_record
    ADD COLUMN school_id BIGINT UNSIGNED NULL COMMENT '学校id(多租户租户键)' AFTER user_id;

UPDATE tb_points_record pr
    INNER JOIN tb_user u ON pr.user_id = u.id
SET pr.school_id = u.school_id
WHERE pr.school_id IS NULL;

CREATE INDEX idx_points_record_school_id ON tb_points_record(school_id);

-- 5) Optional: enforce NOT NULL after manual verification
-- ALTER TABLE tb_user MODIFY school_id BIGINT UNSIGNED NOT NULL;
-- ALTER TABLE tb_employee MODIFY school_id BIGINT UNSIGNED NULL;
-- ALTER TABLE tb_dish MODIFY school_id BIGINT UNSIGNED NOT NULL;
-- ALTER TABLE tb_blog MODIFY school_id BIGINT UNSIGNED NOT NULL;
-- ALTER TABLE tb_blog_comment MODIFY school_id BIGINT UNSIGNED NOT NULL;
-- ALTER TABLE tb_follow MODIFY school_id BIGINT UNSIGNED NOT NULL;
-- ALTER TABLE tb_seckill_product MODIFY school_id BIGINT UNSIGNED NOT NULL;
-- ALTER TABLE tb_product_order MODIFY school_id BIGINT UNSIGNED NOT NULL;
-- ALTER TABLE tb_points_record MODIFY school_id BIGINT UNSIGNED NOT NULL;
