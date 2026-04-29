# Tentac

[![Java](https://img.shields.io/badge/Java-17%2B-blue)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.x-brightgreen)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-yellow)](LICENSE)

**Tentac** 是一个面向 Spring Boot 应用的 Markdown 格式 API 文档生成工具。作为 Swagger2/SpringFox 的替代方案，它生成结构清晰、易于阅读的 Markdown 输出，同时针对大模型（LLM）消费进行了优化。

## 特性

- **Markdown 输出** — 以 Markdown 格式生成 API 文档，包含表格、JSON 示例和 cURL 命令
- **兼容 Swagger2** — 可无缝适配已有的 `@Api`、`@ApiOperation`、`@ApiParam`、`@ApiModelProperty` 注解
- **独立运行模式** — 无需引入 Swagger2 依赖，使用 `@MarkdownApi*` 系列注解即可独立工作
- **JSON 报文明文展示** — 请求/响应体以格式化的 JSON 展示，并附带字段说明表
- **大模型探测** — 提供 `/v2/llm-probe` 端点，输出针对 AI 优化的结构化文档
- **IP 访问控制** — 支持基于 CIDR 记法的 IP 白名单/黑名单（如 `192.168.1.0/24`）
- **零代码生成** — 纯运行时反射实现，无需注解处理器或代码生成

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.github.bonfirer</groupId>
    <artifactId>tentac-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. 配置

```yaml
tentac:
  enabled: true
  title: "我的 API"
  description: "我的应用 API 文档"
  version: "1.0.0"
  base-package: "com.example.controller"
```

### 3. 为 Controller 添加注解

**使用 Swagger2 注解（兼容模式）：**

```java
@RestController
@RequestMapping("/api/users")
@Api(tags = "用户管理")
public class UserController {

    @GetMapping("/{id}")
    @ApiOperation(value = "根据ID获取用户")
    public User getUser(
            @PathVariable @ApiParam(value = "用户ID", required = true) Long id) {
        return userService.findById(id);
    }

    @PostMapping
    @ApiOperation(value = "创建用户")
    public User createUser(
            @RequestBody @ApiParam(value = "用户数据") User user) {
        return userService.create(user);
    }
}
```

**使用 Tentac 独立注解（无需 Swagger2）：**

```java
@RestController
@RequestMapping("/api/products")
@MarkdownApi(tags = "商品目录", description = "商品管理接口")
public class ProductController {

    @GetMapping("/{sku}")
    @MarkdownApiOperation(value = "根据SKU获取商品",
                          notes = "通过唯一SKU编码查找商品")
    public Product getProduct(
            @PathVariable @MarkdownApiParam(name = "sku", value = "商品SKU",
                                            required = true, in = "path") String sku) {
        return productService.findBySku(sku);
    }

    @PostMapping
    @MarkdownApiOperation(value = "创建商品",
                          notes = "向目录中添加新商品")
    public Product createProduct(
            @RequestBody @MarkdownApiParam(value = "商品信息") Product product) {
        return productService.create(product);
    }
}
```

### 4. 为模型字段添加描述

**Swagger2 模式：**

```java
public class User {
    @ApiModelProperty(value = "用户ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "用户名", required = true, example = "john_doe")
    private String username;
}
```

**独立模式：**

```java
public class Product {
    @MarkdownApiModelProperty(value = "商品SKU编码", example = "p1")
    private String sku;

    @MarkdownApiModelProperty(value = "商品展示名称", required = true,
                              example = "超级小工具")
    private String name;
}
```

### 5. 访问文档

启动应用后访问以下端点：

| 端点 | 说明 |
|------|------|
| `GET /v2/markdown` | 完整 Markdown API 文档 |
| `GET /v2/llm-probe` | 大模型优化输出（Markdown 格式） |
| `GET /v2/llm-probe/json` | 大模型优化输出（JSON 格式） |

## 生成文档示例

上述 POST 接口生成的文档如下：

````markdown
### `POST` /api/products

**Summary:** 创建商品

**Request Body:** `Product`
```json
{
  "sku": "string",
  "name": "string",
  "price": 0.0,
  "inStock": true
}
```
**Fields:**
| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|--------|
| `sku` | String | No | 商品SKU编码 | p1 |
| `name` | String | Yes | 商品展示名称 | 超级小工具 |
| `price` | double | No | 商品价格（美元） | 9.99 |
| `inStock` | boolean | No | 是否有库存 | true |

**Response:** `Product`
```json
{
  "sku": "string",
  "name": "string",
  "price": 0.0,
  "inStock": true
}
```
**Example Request:**
```bash
curl -X POST \
  -H "Content-Type: application/json" \
  "http://localhost:8080/api/products" \
  -d '{"sku":"string","name":"string","price":0.0,"inStock":true}'
```
````

## 注解参考

### 类级别

| 注解 | 属性 | 说明 |
|------|------|------|
| `@MarkdownApi` | `tags` | 用于分组的标签名称 |
| | `description` | API 分组描述 |
| | `hidden` | 设为 `true` 则从文档中隐藏 |

### 方法级别

| 注解 | 属性 | 说明 |
|------|------|------|
| `@MarkdownApiOperation` | `value` | 接口简短摘要 |
| | `notes` | 详细描述 |
| | `tags` | 覆盖类级别标签 |
| | `httpMethod` | HTTP 方法覆盖（通常自动检测） |

### 参数级别

| 注解 | 属性 | 说明 |
|------|------|------|
| `@MarkdownApiParam` | `name` | 参数名称 |
| | `value` | 参数描述 |
| | `required` | 是否必填 |
| | `defaultValue` | 默认值 |
| | `example` | 示例值 |
| | `in` | 参数位置：`query`、`path`、`header`、`body` |

### 字段级别

| 注解 | 属性 | 说明 |
|------|------|------|
| `@MarkdownApiModelProperty` | `value` | 字段描述 |
| | `required` | 是否必填 |
| | `example` | 示例值 |
| | `notes` | 补充说明 |

## 配置属性

所有属性均使用 `tentac` 前缀。

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `enabled` | boolean | `true` | 启用/禁用 Tentac |
| `title` | String | `API Documentation` | 文档标题 |
| `description` | String | `""` | API 描述 |
| `version` | String | `1.0.0` | API 版本号 |
| `base-package` | String | `""` | 扫描的基础包路径（空=扫描全部） |
| `markdown-path` | String | `/v2/markdown` | Markdown 文档端点路径 |
| `llm-probe-path` | String | `/v2/llm-probe` | LLM 探测端点路径 |
| `llm-probe-enabled` | boolean | `true` | 启用/禁用 LLM 探测端点 |
| `ip-whitelist` | List\<String\> | `[]` | IP CIDR 白名单（如 `192.168.0.0/16`） |
| `ip-blacklist` | List\<String\> | `[]` | IP CIDR 黑名单 |

### IP 访问控制示例

```yaml
tentac:
  ip-whitelist:
    - "127.0.0.1/32"        # 本机 IPv4
    - "0:0:0:0:0:0:0:1/128" # 本机 IPv6
    - "192.168.0.0/16"      # 局域网
    - "10.0.0.0/8"          # 私有网络
  ip-blacklist:
    - "203.0.113.0/24"      # 封禁特定网段
```

## 模块结构

```
tentac/
├── tentac-core/                       # 核心引擎
│   ├── annotation/                    # @MarkdownApi、@MarkdownApiOperation 等注解
│   ├── core/                          # MarkdownGenerator、ApiScanner 等引擎类
│   └── model/                         # EndpointInfo、ParameterInfo 等模型
├── tentac-spring-boot-starter/        # 自动配置
│   ├── autoconfigure/                 # TentacAutoConfiguration、TentacEndpoint
│   ├── filter/                        # IpAccessFilter IP过滤
│   └── probe/                         # LlmProbeGenerator 大模型探测
├── tentac-demo/                       # 演示项目（兼容 Swagger2 模式）
└── tentac-demo-standalone/            # 演示项目（独立模式，无 Swagger2 依赖）
```

## 环境要求

- Java 17+
- Spring Boot 3.2.x+
- Swagger2 注解（`io.swagger:swagger-annotations`）— **可选**，仅兼容模式需要

## 许可证

Apache 2.0
