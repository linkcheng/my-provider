-- 创建表
CREATE TABLE IF NOT EXISTS`element_config` (
  `name` varchar(64) NOT NULL DEFAULT '' COMMENT '因子名称',
  `path` varchar(256) NOT NULL DEFAULT '' COMMENT '因子请求地址',
  `is_active` tinyint(1) unsigned NOT NULL DEFAULT '1' COMMENT '是否有效',
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='因子配置表';
