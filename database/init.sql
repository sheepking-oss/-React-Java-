-- 校园二手交易平台数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS campus_secondhand DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE campus_secondhand;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码',
  `nickname` VARCHAR(50) NOT NULL COMMENT '昵称',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `student_id` VARCHAR(50) DEFAULT NULL COMMENT '学号',
  `school` VARCHAR(100) DEFAULT NULL COMMENT '学校',
  `gender` TINYINT DEFAULT 0 COMMENT '性别 0:未知 1:男 2:女',
  `signature` VARCHAR(200) DEFAULT NULL COMMENT '个性签名',
  `credit_score` DECIMAL(5,2) DEFAULT 100.00 COMMENT '信誉分',
  `trade_count` INT DEFAULT 0 COMMENT '交易次数',
  `success_count` INT DEFAULT 0 COMMENT '成功交易次数',
  `status` TINYINT DEFAULT 1 COMMENT '状态 0:禁用 1:正常',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 商品表
CREATE TABLE IF NOT EXISTS `product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `title` VARCHAR(100) NOT NULL COMMENT '商品标题',
  `description` TEXT NOT NULL COMMENT '商品描述',
  `price` DECIMAL(10,2) NOT NULL COMMENT '售价',
  `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
  `category` VARCHAR(50) NOT NULL COMMENT '分类',
  `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '封面图片',
  `status` TINYINT DEFAULT 1 COMMENT '状态 0:下架 1:在售 2:已售出',
  `view_count` INT DEFAULT 0 COMMENT '浏览次数',
  `favorite_count` INT DEFAULT 0 COMMENT '收藏次数',
  `location` VARCHAR(200) DEFAULT NULL COMMENT '交易地点',
  `tags` VARCHAR(200) DEFAULT NULL COMMENT '标签',
  `condition` VARCHAR(50) DEFAULT NULL COMMENT '成色',
  `is_negotiable` TINYINT DEFAULT 0 COMMENT '是否可议价 0:否 1:是',
  `audit_status` TINYINT DEFAULT 0 COMMENT '审核状态 0:待审核 1:已通过 2:已拒绝',
  `audit_reason` VARCHAR(500) DEFAULT NULL COMMENT '审核原因',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 商品图片表
CREATE TABLE IF NOT EXISTS `product_image` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品图片表';

-- 收藏表
CREATE TABLE IF NOT EXISTS `favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 消息表
CREATE TABLE IF NOT EXISTS `message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `from_user_id` BIGINT NOT NULL COMMENT '发送者ID',
  `to_user_id` BIGINT NOT NULL COMMENT '接收者ID',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `image_url` VARCHAR(500) DEFAULT NULL COMMENT '图片URL',
  `is_read` TINYINT DEFAULT 0 COMMENT '是否已读 0:未读 1:已读',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_from_user` (`from_user_id`),
  KEY `idx_to_user` (`to_user_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- 订单表
CREATE TABLE IF NOT EXISTS `order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `seller_id` BIGINT NOT NULL COMMENT '卖家ID',
  `buyer_id` BIGINT NOT NULL COMMENT '买家ID',
  `price` DECIMAL(10,2) NOT NULL COMMENT '成交价格',
  `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '订单状态',
  `buyer_address` VARCHAR(500) DEFAULT NULL COMMENT '收货地址',
  `buyer_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `buyer_name` VARCHAR(50) DEFAULT NULL COMMENT '收货人姓名',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `ship_time` DATETIME DEFAULT NULL COMMENT '发货时间',
  `receive_time` DATETIME DEFAULT NULL COMMENT '收货时间',
  `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` VARCHAR(500) DEFAULT NULL COMMENT '取消原因',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_seller_id` (`seller_id`),
  KEY `idx_buyer_id` (`buyer_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 换物申请表
CREATE TABLE IF NOT EXISTS `exchange` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '换物申请ID',
  `initiator_id` BIGINT NOT NULL COMMENT '发起者ID',
  `target_id` BIGINT NOT NULL COMMENT '目标用户ID',
  `initiator_product_id` BIGINT DEFAULT NULL COMMENT '发起者商品ID',
  `target_product_id` BIGINT NOT NULL COMMENT '目标商品ID',
  `initiator_description` TEXT COMMENT '换物描述',
  `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态',
  `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
  `reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '拒绝原因',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_initiator_id` (`initiator_id`),
  KEY `idx_target_id` (`target_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='换物申请表';

-- 举报表
CREATE TABLE IF NOT EXISTS `report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  `reporter_id` BIGINT NOT NULL COMMENT '举报者ID',
  `reported_user_id` BIGINT NOT NULL COMMENT '被举报用户ID',
  `product_id` BIGINT DEFAULT NULL COMMENT '相关商品ID',
  `type` VARCHAR(50) NOT NULL COMMENT '举报类型',
  `reason` TEXT NOT NULL COMMENT '举报原因',
  `image_urls` TEXT COMMENT '图片URL列表',
  `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态',
  `handler_id` BIGINT DEFAULT NULL COMMENT '处理者ID',
  `handle_result` TEXT COMMENT '处理结果',
  `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_reporter_id` (`reporter_id`),
  KEY `idx_reported_user_id` (`reported_user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报表';

-- 评价表
CREATE TABLE IF NOT EXISTS `review` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `reviewer_id` BIGINT NOT NULL COMMENT '评价者ID',
  `reviewee_id` BIGINT NOT NULL COMMENT '被评价者ID',
  `rating` INT NOT NULL COMMENT '评分 1-5',
  `content` TEXT NOT NULL COMMENT '评价内容',
  `image_urls` TEXT COMMENT '图片URL列表',
  `is_anonymous` TINYINT DEFAULT 0 COMMENT '是否匿名 0:否 1:是',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`),
  KEY `idx_reviewer_id` (`reviewer_id`),
  KEY `idx_reviewee_id` (`reviewee_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- 审核日志表
CREATE TABLE IF NOT EXISTS `audit_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '审核日志ID',
  `target_type` VARCHAR(50) NOT NULL COMMENT '目标类型',
  `target_id` BIGINT NOT NULL COMMENT '目标ID',
  `auditor_id` BIGINT DEFAULT NULL COMMENT '审核者ID',
  `before_status` INT DEFAULT NULL COMMENT '审核前状态',
  `after_status` INT DEFAULT NULL COMMENT '审核后状态',
  `reason` VARCHAR(500) DEFAULT NULL COMMENT '审核原因',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核日志表';

-- 插入测试数据
-- 插入测试用户（密码：123456，使用BCrypt加密）
INSERT INTO `user` (`username`, `password`, `nickname`, `phone`, `school`, `credit_score`, `trade_count`, `success_count`, `status`) VALUES
('testuser1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '张三', '13800138001', 'XX大学', 95.50, 10, 9, 1),
('testuser2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '李四', '13800138002', 'XX大学', 88.00, 5, 4, 1);

-- 插入测试商品
INSERT INTO `product` (`user_id`, `title`, `description`, `price`, `original_price`, `category`, `condition`, `is_negotiable`, `status`, `audit_status`) VALUES
(1, '二手笔记本电脑 ThinkPad X1 Carbon', '自用ThinkPad X1 Carbon，2020款，i5处理器，8G内存，256G固态硬盘，成色95新，电池健康度85%，适合学生党编程、办公使用。', 2999.00, 6999.00, '数码产品', '几乎全新', 1, 1, 1),
(1, '高等数学教材 第七版', '同济版高等数学第七版，上下册，有少量笔记，不影响使用，适合大一新生。', 35.00, 78.00, '书籍教材', '轻微使用', 0, 1, 1),
(2, '闲置自行车 美利达勇士500', '美利达勇士500山地自行车，27速变速，碟刹，适合校园代步、周末骑行。保养良好，8成新。', 800.00, 1899.00, '运动器材', '明显使用痕迹', 1, 1, 1),
(2, '小米手环6 NFC版', '小米手环6 NFC版，支持门禁卡、公交卡，心率监测，睡眠监测。使用半年，功能完好。', 120.00, 249.00, '数码产品', '轻微使用', 0, 1, 1);
