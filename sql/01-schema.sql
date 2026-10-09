-- ============================================================================
--  个人博客系统 —— 建库建表脚本
--  数据库：MySQL 8.0 / 5.7+（字符集统一 utf8mb4，支持 emoji）
--
--  【重要】请用「执行 SQL 脚本 / 运行 SQL 文件」的方式执行本文件：
--      Navicat ：右键数据库 -> 运行 SQL 文件
--      DBeaver ：Alt+X（Execute SQL Script）
--      IDEA    ：选中文件右键 -> Run
--      千万不要用「执行当前语句」(Ctrl+Enter) 并全选，
--      那会把整个文件当一条语句发送，导致 1064 语法错误。
--
--  执行顺序：先执行本文件建库建表，再执行 02-data.sql 灌入初始数据。
--
--  【注意】本脚本会 DROP 已存在的同名表（保证可重复执行）。
--     如果库里已有你不想丢的数据，请先备份或去掉 DROP 语句。
-- ============================================================================

-- ---------------------------------------------------------------------------
-- 0. 数据库
-- ---------------------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS `blog_db`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE `blog_db`;

-- ---------------------------------------------------------------------------
-- 1. 清理旧表（注意顺序：先删关联表，再删主表）
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `t_user_token`;
DROP TABLE IF EXISTS `t_article_like`;
DROP TABLE IF EXISTS `t_article_tag`;
DROP TABLE IF EXISTS `t_comment`;
DROP TABLE IF EXISTS `t_article`;
DROP TABLE IF EXISTS `t_tag`;
DROP TABLE IF EXISTS `t_category`;
DROP TABLE IF EXISTS `t_user`;

-- ---------------------------------------------------------------------------
-- 2. 用户表
-- ---------------------------------------------------------------------------
CREATE TABLE `t_user` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`        VARCHAR(50)  NOT NULL                COMMENT '登录用户名（唯一）',
    `password`        VARCHAR(64)  NOT NULL                COMMENT '密码摘要（MD5，生产请换 BCrypt）',
    `nickname`        VARCHAR(50)  NOT NULL                COMMENT '昵称',
    `avatar`          VARCHAR(255) DEFAULT NULL            COMMENT '头像地址',
    `email`           VARCHAR(100) DEFAULT NULL            COMMENT '邮箱',
    `signature`       VARCHAR(255) DEFAULT NULL            COMMENT '个人简介',
    `role`            VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色：ADMIN / USER',
    `status`          TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1 正常，0 禁用',
    `create_time`     DATETIME     DEFAULT NULL            COMMENT '注册时间',
    `last_login_time` DATETIME     DEFAULT NULL            COMMENT '最近登录时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- ---------------------------------------------------------------------------
-- 3. 分类表（与文章一对多）
-- ---------------------------------------------------------------------------
CREATE TABLE `t_category` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`        VARCHAR(20)  NOT NULL                COMMENT '分类名称（唯一）',
    `description` VARCHAR(100) DEFAULT NULL            COMMENT '分类描述',
    `sort`        INT          NOT NULL DEFAULT 0      COMMENT '排序值，越小越靠前',
    `create_time` DATETIME     DEFAULT NULL            COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_sort` (`sort`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '文章分类表';

-- ---------------------------------------------------------------------------
-- 4. 标签表（与文章多对多）
-- ---------------------------------------------------------------------------
CREATE TABLE `t_tag` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`        VARCHAR(20) NOT NULL                COMMENT '标签名称（唯一）',
    `color`       VARCHAR(20) NOT NULL DEFAULT '#409EFF' COMMENT '标签颜色 #RRGGBB',
    `create_time` DATETIME    DEFAULT NULL            COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '文章标签表';

-- ---------------------------------------------------------------------------
-- 5. 文章表
--    说明：分类名 / 标签名 / 作者昵称一律不冗余存储，靠 ID 关联，避免改名后数据不一致
--    is_top 而不是 top：top 在部分数据库方言里是关键字的坑
-- ---------------------------------------------------------------------------
CREATE TABLE `t_article` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title`         VARCHAR(100) NOT NULL                COMMENT '标题',
    `summary`       VARCHAR(300) DEFAULT NULL            COMMENT '简介/摘要',
    `cover`         VARCHAR(255) DEFAULT NULL            COMMENT '封面图',
    `content`       LONGTEXT                             COMMENT 'Markdown 正文',
    `category_id`   BIGINT       DEFAULT NULL            COMMENT '分类 ID -> t_category.id',
    `author_id`     BIGINT       DEFAULT NULL            COMMENT '作者 ID -> t_user.id',
    `views`         INT          NOT NULL DEFAULT 0      COMMENT '浏览量',
    `likes`         INT          NOT NULL DEFAULT 0      COMMENT '点赞数',
    `comment_count` INT          NOT NULL DEFAULT 0      COMMENT '评论数（冗余，避免列表页 count）',
    `status`        TINYINT      NOT NULL DEFAULT 0      COMMENT '状态：0 草稿，1 已发布',
    `is_top`        TINYINT      NOT NULL DEFAULT 0      COMMENT '是否置顶：1 是，0 否',
    `create_time`   DATETIME     DEFAULT NULL            COMMENT '创建时间',
    `update_time`   DATETIME     DEFAULT NULL            COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_author_id` (`author_id`),
    -- 覆盖「前台列表」最常见的查询：WHERE status = 1 ORDER BY is_top DESC, create_time DESC
    KEY `idx_status_top_create` (`status`, `is_top`, `create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '文章表';

-- ---------------------------------------------------------------------------
-- 6. 文章-标签关联表（多对多中间表）
--    唯一索引双重保证：同一篇文章不会重复打同一个标签
-- ---------------------------------------------------------------------------
CREATE TABLE `t_article_tag` (
    `id`         BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `article_id` BIGINT NOT NULL                COMMENT '文章 ID',
    `tag_id`     BIGINT NOT NULL                COMMENT '标签 ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_tag` (`article_id`, `tag_id`),
    KEY `idx_tag_id` (`tag_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '文章标签关联表';

-- ---------------------------------------------------------------------------
-- 7. 文章点赞表
--    uk_article_user 从数据库层面保证「一个用户对一篇文章只能点一次赞」
-- ---------------------------------------------------------------------------
CREATE TABLE `t_article_like` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `article_id`  BIGINT   NOT NULL                COMMENT '文章 ID',
    `user_id`     BIGINT   NOT NULL                COMMENT '点赞用户 ID',
    `create_time` DATETIME DEFAULT NULL            COMMENT '点赞时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_user` (`article_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '文章点赞表';

-- ---------------------------------------------------------------------------
-- 8. 评论表（两级：parent_id 为空表示顶级评论）
-- ---------------------------------------------------------------------------
CREATE TABLE `t_comment` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `article_id`        BIGINT       NOT NULL                COMMENT '文章 ID',
    `user_id`           BIGINT       NOT NULL                COMMENT '评论人 ID',
    `content`           VARCHAR(500) NOT NULL                COMMENT '评论内容（纯文本）',
    `parent_id`         BIGINT       DEFAULT NULL            COMMENT '父评论 ID，NULL 表示顶级',
    `reply_to_nickname` VARCHAR(50)  DEFAULT NULL            COMMENT '被回复人昵称（冗余，仅用于展示）',
    `status`            TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1 显示，0 屏蔽',
    `create_time`       DATETIME     DEFAULT NULL            COMMENT '评论时间',
    PRIMARY KEY (`id`),
    KEY `idx_article_id` (`article_id`, `status`, `create_time`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '评论表';

-- ---------------------------------------------------------------------------
-- 9. JWT 令牌白名单表
--    作用：支持「退出登录立即失效」+「服务重启后登录态不丢失」
--    生产建议：这张表增长很快，真实项目通常改用 Redis（带 TTL 自动过期）
-- ---------------------------------------------------------------------------
CREATE TABLE `t_user_token` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `token`       VARCHAR(512) NOT NULL                COMMENT 'JWT 原文',
    `user_id`     BIGINT       NOT NULL                COMMENT '所属用户 ID',
    `expire_at`   BIGINT       NOT NULL                COMMENT '过期时间戳（毫秒）',
    `create_time` DATETIME     DEFAULT NULL            COMMENT '签发时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_token` (`token`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_expire_at` (`expire_at`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'JWT 令牌白名单表';

-- ---------------------------------------------------------------------------
-- 建表完成。接下来执行 02-data.sql 灌入初始数据。
-- ---------------------------------------------------------------------------
SELECT '建表完成，请继续执行 02-data.sql' AS tips;
