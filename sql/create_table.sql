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
    school_id     bigint unsigned                     not null comment '学校的id',
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
