# BlockchainConfigRepository 数据库交互说明

本文档回答两个问题：
- `BlockchainConfigRepository` 的数据库查询到底在哪里实现？
- 当前项目从接口到数据库的完整交互流程是什么？

## 1. Repository 本身有没有“手写 SQL 实现”？

没有。

`BlockchainConfigRepository` 是一个接口：
- `src/main/java/com/example/springdemo/repository/BlockchainConfigRepository.java`

它继承 `JpaRepository<BlockchainConfig, Long>`，由 Spring Data JPA 在运行时自动生成代理实现。

也就是说：
- 你写的是“方法签名”
- 框架在启动时根据方法名规则和实体映射，自动生成查询逻辑

## 2. 底层实现在哪一层发生？

从框架层看，核心链路是：

1. Spring Data JPA 为 Repository 接口创建动态代理（`SimpleJpaRepository` 体系）
2. 解析方法名（例如 `findByEnabledOrderByIdDesc`、`existsByChainCodeIgnoreCase`）
3. 生成 JPQL/Criteria 查询
4. Hibernate 将查询翻译为 MySQL SQL
5. 通过 JDBC + Hikari 数据源执行 SQL
6. 查询结果映射回 `BlockchainConfig` 实体对象

相关配置：
- 数据源：`src/main/resources/application.yml` -> `spring.datasource.*`
- JPA/Hibernate：`src/main/resources/application.yml` -> `spring.jpa.*`
- SQL 日志：`logging.level.org.hibernate.SQL: debug`

## 3. 本项目调用链（从 API 到 DB）

以区块链配置接口为例：

1. Controller 接收请求  
   `src/main/java/com/example/springdemo/controller/BlockchainConfigApiController.java`
2. Controller 调用 Biz 接口  
   `BlockchainConfigBiz`
3. Service 实现 Biz 并执行业务校验  
   `src/main/java/com/example/springdemo/service/BlockchainConfigService.java`
4. Service 调用 Repository 方法
5. Repository 代理执行数据库查询/写入
6. 结果返回给 Controller，再返回 JSON

## 4. 关键方法与数据库行为映射

`BlockchainConfigRepository` 中的方法与行为：

- `findAllByOrderByIdDesc()`  
  查询全部并按 `id desc` 排序

- `findByEnabledOrderByIdDesc(Boolean enabled)`  
  按 `enabled` 过滤并按 `id desc` 排序

- `existsByBlockchainId(Integer blockchainId)`  
  判断 `blockchain_id` 是否已存在（唯一性校验）

- `existsByBlockchainIdAndIdNot(Integer blockchainId, Long id)`  
  更新场景下排除自身主键做唯一性校验

- `existsByChainCodeIgnoreCase(String chainCode)`  
  判断 `chain_code`（忽略大小写）是否已存在

- `existsByChainCodeIgnoreCaseAndIdNot(String chainCode, Long id)`  
  更新场景下排除自身做 `chain_code` 唯一性校验

- `findByBlockchainId(Integer blockchainId)`  
  根据业务链ID查询单条配置

- `findByChainCodeIgnoreCase(String chainCode)`  
  根据链简称（忽略大小写）查询单条配置

## 5. create / update 的实际数据库交互流程

### 5.1 create

`BlockchainConfigService#create(...)` 会按顺序执行：

1. 参数标准化与校验（`blockchainId`、`chainCode`、`chainName`）
2. `existsByBlockchainId(...)` 检查业务ID唯一
3. `existsByChainCodeIgnoreCase(...)` 检查链简称唯一
4. `save(entity)` 执行插入

对应数据库表：
- `blockchain_config`

### 5.2 update

`BlockchainConfigService#update(...)` 会按顺序执行：

1. `findById(id)` 查询记录是否存在
2. 参数标准化与校验
3. `existsByBlockchainIdAndIdNot(...)` 检查业务ID唯一
4. `existsByChainCodeIgnoreCaseAndIdNot(...)` 检查链简称唯一
5. `save(entity)` 执行更新

## 6. 实体与表映射关系

实体：
- `src/main/java/com/example/springdemo/domain/BlockchainConfig.java`

表：
- `blockchain_config`

关键字段映射：
- `id` <-> 主键
- `blockchainId` <-> `blockchain_id`
- `chainCode` <-> `chain_code`
- `chainName` <-> `chain_name`
- `enabled` <-> `enabled`
- `createTime` <-> `create_time`
- `updateTime` <-> `update_time`

## 7. 总结

`BlockchainConfigRepository` 不直接写 SQL。  
真正的底层查询实现由 Spring Data JPA + Hibernate 在运行时生成并执行。  
你在项目里主要关注：
- Repository 方法签名是否准确
- Service 层业务校验顺序是否合理
- 实体字段与表结构是否一致
