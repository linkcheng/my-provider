# hsb-provider

## 1. 项目介绍
hsb-model 以及 hsb-elements 项目对外提供服务的出口。

模块说明：
  * hsb-common: 公共模块
  * hsb-router-server: 服务路由模块
  * hsb-sidecar-server: 服务代理模块

配置文件说明：
  * application.yml: 说明当前环境 dev=开发、test=测试、pre=预发、prod=生产
  * application-${spring.profiles.active}.yml: 当前系统当前环境的应用配置
  * bootstrap.yml: 系统配置
  * logback-spring.xml: 日志配置


## 2. 项目依赖

1.环境依赖
  * java sdk 1.8
  * mvn 3.5.4+
  
2.服务依赖
  * MySQL: 5.6+
  * Redis: 3+
  * [Nacos](https://nacos.io/zh-cn/docs/quick-start.html): 1.2.0
  * [Sentinel](https://sentinelguard.io/zh-cn/docs/quick-start.html): 1.7.0
  
3.初始化数据依赖
  * MySQL 添加数据库 hsb_bi_router，字符集 utf8mb4, 并创建表
  ```sql
    -- 创建表
    CREATE TABLE IF NOT EXISTS`element_config` (
      `name` varchar(64) NOT NULL DEFAULT '' COMMENT '因子名称',
      `path` varchar(256) NOT NULL DEFAULT '' COMMENT '因子请求地址',
      `is_active` tinyint(1) unsigned NOT NULL DEFAULT '1' COMMENT '是否有效',
      `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
      `updated_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
      PRIMARY KEY (`name`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配置表';
  ```

## 3. 项目部署

1.打包
  ```shell
   mvn clean package -DskipTests
  ```

2. 启动
  * hsb-router-server, 默认端口 8000，可以通过 -Dserver.port 配置，hsb-router-server 可以单独部署
      ```shell
      java -jar [-Dserver.port=XXXX] hsb-router-server-1.0.0.jar
      ```
  * hsb-sidecar-server, 默认端口 11000，可以通过 -Dserver.port 配置，hsb-sidecar-server 需要与 hsb-model 和 hsb-elements 部署到一起
      ```shell
      java -jar [-Dserver.port=XXXX] hsb-sidecar-server-1.0.0.jar
      ```