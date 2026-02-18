# Spring实战架构

基于项目：`spring-demo`  
代码基线：当前工作区 `main` 分支（2026-02-18）

## 1. 项目总体架构（分层）

这是一个标准的 Spring Boot 单体分层架构，核心链路如下：

1. 表现层（Controller + View）
2. 业务层（Service）
3. 持久层（Repository / JPA）
4. 数据层（MySQL）

对应代码位置：

- 启动类：`src/main/java/com/example/springdemo/SpringDemoApplication.java`
- 页面控制器：`src/main/java/com/example/springdemo/controller/PageController.java`
- API 控制器：`src/main/java/com/example/springdemo/controller/NoteApiController.java`
- 业务服务：`src/main/java/com/example/springdemo/service/NoteService.java`
- 数据访问：`src/main/java/com/example/springdemo/repository/NoteRepository.java`
- 实体模型：`src/main/java/com/example/springdemo/domain/Note.java`

## 2. Spring Boot 基础启动机制（在本项目中的体现）

应用从 `SpringDemoApplication` 启动：

- `@SpringBootApplication` = `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`
- 自动扫描 `com.example.springdemo` 包下组件并注册到 IoC 容器
- 基于 `pom.xml` 中 starter 自动装配 Web、Thymeleaf、JPA、数据源等基础能力

依赖要点（`pom.xml`）：

- `spring-boot-starter-web`：MVC + 内嵌 Tomcat + JSON 序列化
- `spring-boot-starter-thymeleaf`：服务端模板渲染
- `spring-boot-starter-data-jpa`：Repository 抽象 + Hibernate ORM
- `mysql-connector-j`：MySQL 驱动
- `h2`：运行时可选内存数据库依赖（当前默认配置仍指向 MySQL）

## 3. 对外接口设计

### 3.1 页面接口（服务端渲染入口）

- `GET /` -> `PageController#index()` 返回模板名 `index`
- 模板文件：`src/main/resources/templates/index.html`
- 静态资源：`src/main/resources/static/app.js` 通过 `/app.js` 自动映射

### 3.2 REST API 接口

控制器：`NoteApiController`，基础路径 `/api/notes`

1. `GET /api/notes`
- 功能：查询所有便签
- 返回：`List<Note>`（JSON）
- 调用链：Controller -> `NoteService#list()` -> `NoteRepository#findAll()`

2. `POST /api/notes`
- 功能：创建便签
- 请求体：`{ "title": "...", "content": "..." }`
- 返回：保存后的 `Note`（包含 `id`、`createdAt`）
- 调用链：Controller -> `NoteService#create()` -> `NoteRepository#save()`

请求 DTO 形态（当前实现）：

- 使用 `record CreateNoteRequest(String title, String content)` 接收 JSON
- 当前未加 `@Valid` 等参数校验注解

## 4. 内部业务逻辑链路

### 4.1 查询链路（读）

1. 浏览器加载页面后执行 `app.js` 的 `loadNotes()`
2. 前端 `fetch('/api/notes')`
3. `NoteApiController#list()` 调用 `NoteService#list()`
4. `NoteService` 调用 `noteRepository.findAll()`
5. Spring Data JPA 生成 SQL，Hibernate 执行查询
6. 返回 JSON 给前端，前端按 `createdAt` 倒序渲染

### 4.2 新增链路（写）

1. 用户提交表单（标题、内容）
2. 前端 `fetch('/api/notes', { method: 'POST', body: ... })`
3. `NoteApiController#create()` 解析请求体
4. `NoteService#create(title, content)` 创建 `Note` 实体
5. `noteRepository.save(note)` 持久化
6. `@PrePersist` 回调设置 `createdAt`（若为空）
7. 返回保存结果，前端重新 `loadNotes()`

## 5. 底层数据库交互细节

### 5.1 数据源与连接池

配置文件：`src/main/resources/application.yml`

- URL 默认：`jdbc:mysql://localhost:3306/springdemo...`
- 用户名默认：`${SPRING_DATASOURCE_USERNAME:root}`
- 密码默认：`${SPRING_DATASOURCE_PASSWORD:Littleha233!}`
- 连接池：Spring Boot 默认使用 HikariCP

补充：`application-mysql.yml` 提供了 `mysql` profile 下同类配置。

### 5.2 ORM 映射

实体：`Note`

- `@Entity`：映射数据库表（默认表名通常为 `note`）
- `@Id + @GeneratedValue(strategy = IDENTITY)`：主键自增
- 字段：`id`, `title`, `content`, `createdAt`
- 生命周期回调：`@PrePersist` 在插入前填充创建时间

### 5.3 DDL 与 SQL 行为

- `spring.jpa.hibernate.ddl-auto=update`：启动时自动对齐表结构
- `logging.level.org.hibernate.SQL=debug`：打印 SQL
- Hibernate 命名策略通常会把 `createdAt` 映射为 `created_at`

可预期表结构（示意）：

```sql
CREATE TABLE note (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255),
  content VARCHAR(255),
  created_at TIMESTAMP
);
```

实际字段类型长度以运行时方言和自动建表结果为准。

## 6. 前后端协作方式

当前是“同仓单体 + 服务端模板 + 轻前端脚本”模式：

- 页面由 Thymeleaf 返回
- 业务数据由 REST API 提供
- 前端通过原生 `fetch` 与后端交互

优点是结构直观、上手快；代价是前端能力和工程化相对有限。

## 7. 当前实现的工程特征（优点与风险）

优点：

1. 分层清晰，职责边界明确
2. 代码量小，便于教学和快速迭代
3. 基于 Spring Data JPA，数据访问开发成本低

风险/改进点：

1. 配置中存在默认明文密码，建议改为纯环境变量或密钥管理
2. `POST /api/notes` 缺少输入校验与统一异常处理
3. `findAll()` 无分页，数据量大时会有性能问题
4. 缺少领域约束（如标题长度、非空策略）与接口版本化
5. 测试目前仅有 `contextLoads()`，业务回归保护不足

## 8. 推荐演进路线（从演示版到生产化）

1. 引入 DTO + `@Valid` + 全局异常处理（`@RestControllerAdvice`）
2. 新增分页查询接口（`Pageable`）与排序参数
3. 用 `MapStruct`/手写转换隔离 Entity 与 API 模型
4. 增加 Service/Controller 的单元测试与集成测试
5. 接入 Spring Security（认证、鉴权、审计）
6. 配置分环境化（dev/test/prod）与敏感信息托管

---

这份文档可以直接粘贴到 Notion，或通过 Notion 的 Markdown 导入功能创建页面：`Spring实战架构`。
