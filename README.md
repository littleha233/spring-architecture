# ex-wallet-config

`ex-wallet-config` 是一个中心化交易所钱包的本地配置中心项目，当前只保留配置相关能力。

## 功能范围

当前仅包含：
- 币种配置：`/coin-config`
- 币种扩展参数配置：`/coin-chain-config`

已移除：
- ETH 地址生成
- ETH 提币流程
- Notes 示例

## 启动方式

```bash
mvn spring-boot:run
```

启动后访问：
- 首页：`http://localhost:8080/`
- 币种配置：`http://localhost:8080/coin-config`
- 币种扩展参数：`http://localhost:8080/coin-chain-config`

IDE 启动入口：
- `src/main/java/com/example/springdemo/ExWalletConfigApplication.java`

## 分层架构

项目遵循：`Controller -> Biz -> Service -> Repository -> Domain`

配置模块核心代码：
- `src/main/java/com/example/springdemo/controller/CoinApiController.java`
- `src/main/java/com/example/springdemo/controller/CoinChainConfigApiController.java`
- `src/main/java/com/example/springdemo/biz/CoinBiz.java`
- `src/main/java/com/example/springdemo/biz/CoinChainConfigBiz.java`
- `src/main/java/com/example/springdemo/service/CoinService.java`
- `src/main/java/com/example/springdemo/service/CoinChainConfigService.java`
- `src/main/java/com/example/springdemo/repository/CoinRepository.java`
- `src/main/java/com/example/springdemo/repository/CoinChainConfigRepository.java`

## 数据库与SQL

配置中心当前只使用两张表：
- `coin`
- `coin_chain_config`

SQL 文件：
- 统一建表：`src/main/resources/sql/schema.sql`
- 创建配置表：`src/main/resources/sql/migration/20260218_create_coin_config_tables.sql`
- 合并扩展字段：`src/main/resources/sql/migration/20260218_merge_coin_chain_extra_into_json.sql`
- 收敛清理旧表：`src/main/resources/sql/migration/20260218_drop_non_config_tables.sql`

## 配置

`src/main/resources/application.yml` 中已设置：

```yaml
spring:
  application:
    name: ex-wallet-config
```

默认数据库连接仍指向 `springdemo`，可通过环境变量覆盖：
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

## 模块文档

- `README-coin-config.md`
