create database if not exists ewhat;

-- 管理员信息表
create table if not exists tb_employee
(
    id          bigint auto_increment comment '主键' primary key,
    name        varchar(32)                            not null comment '姓名',
    username    varchar(32)                            not null comment '用户名',
    password    varchar(128) default ''                null comment '密码，加密存储',
    phone       varchar(11)                            not null comment '手机号',
    sex         varchar(2)                             not null comment '性别',
    id_number   varchar(18)                            not null comment '身份证号',
    status      int          default 1                 not null comment '状态 0:禁用，1:启用',
    create_time timestamp    default CURRENT_TIMESTAMP null comment '创建时间',
    update_time timestamp    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint idx_username unique (username)
) comment '管理员信息' collate = utf8mb4_general_ci
                       row_format = COMPACT;


-- 用户表
create table tb_user
(
    id          bigint auto_increment comment '主键' primary key,
    openid      varchar(45)                         null comment '微信用户唯一标识',
    name        varchar(32)                         null comment '姓名',
    phone       varchar(11)                         null comment '手机号',
    sex         varchar(2)                          null comment '性别',
    id_number   varchar(18)                         null comment '身份证号',
    avatar      varchar(500)                        null comment '头像',
    create_time timestamp default CURRENT_TIMESTAMP null comment '创建时间',
    update_time timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
) comment '用户信息' collate = utf8mb4_general_ci
                     row_format = COMPACT;

-- 学校表
create table tb_school
(
    id          bigint unsigned auto_increment comment '主键' primary key,
    name        varchar(128)                        not null comment '学校名称',
    images      varchar(1024)                       not null comment '图片，多个图片以'',''隔开',
    address     varchar(255)                        not null comment '地址',
    description varchar(500)                        null comment '简介',
    status      int       default 1                 not null comment '状态 0:禁用，1:启用',
    create_time timestamp default CURRENT_TIMESTAMP null comment '创建时间',
    update_time timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
) comment '学校信息' collate = utf8mb4_general_ci
                     row_format = COMPACT;

-- 食堂表
create table tb_canteen
(
    id          bigint unsigned auto_increment comment '主键' primary key,
    name        varchar(128)                        not null comment '食堂名称',
    school_id   bigint unsigned                     not null comment '学校的id',
    images      varchar(1024)                       not null comment '图片，多个图片以'',''隔开',
    description varchar(500)                        null comment '简介',
    address     varchar(255)                        not null comment '地址',
    x           double unsigned                     not null comment '经度',
    y           double unsigned                     not null comment '维度',
    open_hours  varchar(32)                         null comment '营业时间，例如 10:00-22:00',
    status      int       default 1                 not null comment '状态 0:禁用，1:启用',
    create_time timestamp default CURRENT_TIMESTAMP null comment '创建时间',
    update_time timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
) comment '学校信息' collate = utf8mb4_general_ci
                     row_format = COMPACT;

-- 菜品表
create table if not exists tb_dish
(
    id             bigint unsigned auto_increment comment '主键' primary key,
    name           varchar(128)                            not null comment '餐品名称',
    canteen_id     BIGINT UNSIGNED                         not null COMMENT '食堂id，关联tb_canteen.id',
    images         varchar(1024)                           not null comment '图片，多个图片以'',''隔开',
    price          DECIMAL(10, 2)                          not null COMMENT '价格',
    original_price DECIMAL(10, 2)                          null COMMENT '原价（用于显示折扣）',
    description    varchar(500)                            null comment '简介',
    location       varchar(128)                            not null comment '售卖位置（如：一楼2号窗口）',
    sort           INT           DEFAULT 0                 NOT NULL COMMENT '排序字段，数字越小越靠前',
    rating         DECIMAL(3, 2) DEFAULT 0.00              NOT NULL COMMENT '评分',
    rating_count   INT           DEFAULT 0                 NOT NULL COMMENT '评分人数',
    tags           VARCHAR(255)                            NULL COMMENT '标签，多个以逗号分隔（如：辣,推荐,新品）',
    is_recommend   TINYINT       DEFAULT 0                 NOT NULL COMMENT '是否推荐 0:否，1:是',
    status         int           default 1                 not null comment '状态 0:禁用，1:启用',
    create_time    timestamp     default CURRENT_TIMESTAMP null comment '创建时间',
    update_time    timestamp     default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
) comment '餐品信息' collate = utf8mb4_general_ci
                     row_format = COMPACT;


-- 博文表
create table tb_blog
(
    id          bigint unsigned auto_increment comment '主键' primary key,
    dish_id     bigint                                   not null comment '菜品id',
    user_id     bigint unsigned                          not null comment '用户id',
    title       varchar(255) collate utf8mb4_unicode_ci  not null comment '标题',
    images      varchar(2048)                            not null comment '探店的照片，最多9张，多张以","隔开',
    content     varchar(2048) collate utf8mb4_unicode_ci not null comment '探店的文字描述',
    liked       int unsigned default '0'                 null comment '点赞数量',
    comments    int unsigned                             null comment '评论数量',
    status      int          default 1                   not null comment '状态 0:禁用，1:启用',
    create_time timestamp    default CURRENT_TIMESTAMP   not null comment '创建时间',
    update_time timestamp    default CURRENT_TIMESTAMP   not null on update CURRENT_TIMESTAMP comment '更新时间'
) comment '博文信息'
    collate = utf8mb4_general_ci
    row_format = COMPACT;

-- 博文评论表
create table tb_blog_comments
(
    id          bigint unsigned auto_increment comment '主键' primary key,
    user_id     bigint unsigned                     not null comment '用户id',
    blog_id     bigint unsigned                     not null comment '博文id',
    parent_id   bigint unsigned                     not null comment '关联的1级评论id，如果是一级评论，则值为0',
    answer_id   bigint unsigned                     not null comment '回复的评论id',
    content     varchar(255)                        not null comment '回复的内容',
    liked       int unsigned                        null comment '点赞数',
    status      int       default 1                 not null comment '状态 0:禁用，1:启用，2：被举报',
    create_time timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
) comment '博文评论'
    collate = utf8mb4_general_ci
    row_format = COMPACT;




