# 以太坊地址生成功能说明（Spring Boot）

本文档用于单独说明本项目中“以太坊地址生成”模块的实现逻辑与使用方式。

## 功能概览

已实现能力：
- 页面端生成以太坊地址并展示结果
- 后端接口生成 `secp256k1` 公私钥对与地址
- 数据库存储 `userId`、公钥、私钥、地址、创建时间
- 支持按 `userId` 查询历史记录

页面入口：
- `http://localhost:8080/eth-address`

## 代码结构（分层）

该模块严格遵循 Spring Boot 分层架构：

1. 表现层（Controller / View）
- 页面路由：`src/main/java/com/example/springdemo/controller/PageController.java`
- API 控制器：`src/main/java/com/example/springdemo/controller/EthWalletApiController.java`
- 页面模板：`src/main/resources/templates/eth-address.html`
- 前端脚本：`src/main/resources/static/eth-address.js`

2. 业务层（Service）
- `src/main/java/com/example/springdemo/service/EthereumWalletService.java`
- 负责密钥生成、地址计算、数据保存、查询逻辑

3. 持久层（Repository）
- `src/main/java/com/example/springdemo/repository/EthWalletRepository.java`
- 基于 `JpaRepository` 提供 CRUD 与按用户查询

4. 领域层（Entity）
- `src/main/java/com/example/springdemo/domain/EthWallet.java`
- 映射表字段：`id/userId/privateKey/publicKey/address/createdAt`

## 地址生成实现逻辑

在 `EthereumWalletService` 中实现：

1. 生成密钥对
- 使用 `EC` 算法 + `secp256k1` 曲线生成 KeyPair

2. 组装公钥
- 公钥使用未压缩格式：`0x04 + X(32字节) + Y(32字节)`

3. 计算地址
- 对未压缩公钥去掉首字节 `0x04` 后做 `Keccak-256`
- 取哈希结果后 20 字节作为地址
- 统一输出 `0x` 前缀的十六进制字符串

4. 落库
- 保存 `userId`、`privateKey`、`publicKey`、`address`
- `createdAt` 通过 `@PrePersist` 自动写入

依赖：
- `org.bouncycastle:bcprov-jdk18on`（用于 `Keccak-256` 与加密提供方）

## API 说明

### 1) 生成并保存地址

- 方法：`POST`
- 路径：`/api/eth-addresses/generate`
- 请求体：

```json
{
  "userId": 1001
}
```

- 响应示例：

```json
{
  "id": 1,
  "userId": 1001,
  "privateKey": "0x...",
  "publicKey": "0x...",
  "address": "0x...",
  "createdAt": "2026-02-18T05:41:12.123456Z"
}
```

### 2) 查询地址记录

- 方法：`GET`
- 路径：`/api/eth-addresses`
- 可选参数：`userId`

示例：
- 查询全部：`GET /api/eth-addresses`
- 查询指定用户：`GET /api/eth-addresses?userId=1001`

### 3) 参数校验

- `userId` 必须为正整数
- 不符合规则时返回 `400 Bad Request`

## 页面使用方式

1. 启动项目：

```bash
mvn spring-boot:run
```

2. 打开页面：
- `http://localhost:8080/eth-address`

3. 操作步骤：
- 输入 `userId`
- 点击 `Generate & Save`
- 页面会展示最新生成结果，并刷新历史列表
- 点击 `Refresh List` 可按当前输入的 `userId` 过滤查询

## 数据库说明

JPA 会自动创建/更新 `eth_wallet` 表（`ddl-auto=update`）。

表字段（核心）：
- `id`：主键，自增
- `user_id`：用户 ID
- `private_key`：私钥（`0x` 前缀）
- `public_key`：公钥（`0x` 前缀）
- `address`：以太坊地址（`0x` 前缀）
- `created_at`：创建时间

## 注意事项（生产建议）

- 当前示例会明文保存私钥，适合开发演示，不适合生产。
- 生产环境建议至少进行：
  - 私钥加密存储（如 KMS/HSM）
  - 严格访问控制与审计日志
  - 敏感字段脱敏展示
