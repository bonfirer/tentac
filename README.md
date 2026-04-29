# Tentac

[![Java](https://img.shields.io/badge/Java-17%2B-blue)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.x-brightgreen)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-yellow)](LICENSE)

**Tentac** is a Markdown-format API documentation generator for Spring Boot applications. Designed as an alternative to Swagger2/SpringFox, it produces clean, structured Markdown output that is optimized for both human readability and LLM (Large Language Model) consumption.

## Features

- **Markdown Output** — Generates API docs in Markdown format with tables, JSON examples, and cURL snippets
- **Swagger2 Compatible** — Works with existing `@Api`, `@ApiOperation`, `@ApiParam`, `@ApiModelProperty` annotations out of the box  
- **Standalone Mode** — Fully functional without Swagger2 on the classpath using `@MarkdownApi*` annotations
- **JSON Body Display** — Request/response bodies rendered as pretty-printed JSON with field description tables
- **LLM Probe** — Special `/v2/llm-probe` endpoint producing AI-optimized structured output
- **IP Access Control** — Whitelist/blacklist support with CIDR notation (e.g. `192.168.1.0/24`)
- **Zero Code Generation** — Pure runtime reflection, no annotation processing or code generation required

## Quick Start

### 1. Add Dependency

```xml
<dependency>
    <groupId>com.github.bonfirer</groupId>
    <artifactId>tentac-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. Configure

```yaml
tentac:
  enabled: true
  title: "My API"
  description: "My application API documentation"
  version: "1.0.0"
  base-package: "com.example.controller"
```

### 3. Annotate Your Controller

**Using Swagger2 annotations (compatible mode):**

```java
@RestController
@RequestMapping("/api/users")
@Api(tags = "User Management")
public class UserController {

    @GetMapping("/{id}")
    @ApiOperation(value = "Get user by ID")
    public User getUser(
            @PathVariable @ApiParam(value = "User ID", required = true) Long id) {
        return userService.findById(id);
    }

    @PostMapping
    @ApiOperation(value = "Create user")
    public User createUser(
            @RequestBody @ApiParam(value = "User data") User user) {
        return userService.create(user);
    }
}
```

**Using Tentac standalone annotations (no Swagger2 required):**

```java
@RestController
@RequestMapping("/api/products")
@MarkdownApi(tags = "Product Catalog", description = "Product management APIs")
public class ProductController {

    @GetMapping("/{sku}")
    @MarkdownApiOperation(value = "Get product by SKU",
                          notes = "Finds a product by its unique SKU code")
    public Product getProduct(
            @PathVariable @MarkdownApiParam(name = "sku", value = "Product SKU",
                                            required = true, in = "path") String sku) {
        return productService.findBySku(sku);
    }

    @PostMapping
    @MarkdownApiOperation(value = "Create product",
                          notes = "Adds a new product to the catalog")
    public Product createProduct(
            @RequestBody @MarkdownApiParam(value = "Product definition") Product product) {
        return productService.create(product);
    }
}
```

### 4. Annotate Model Fields

**Swagger2 mode:**

```java
public class User {
    @ApiModelProperty(value = "User ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "Username", required = true, example = "john_doe")
    private String username;
}
```

**Standalone mode:**

```java
public class Product {
    @MarkdownApiModelProperty(value = "Product SKU code", example = "p1")
    private String sku;

    @MarkdownApiModelProperty(value = "Product display name", required = true,
                              example = "Super Widget")
    private String name;
}
```

### 5. Access Documentation

Start your application and visit:

| Endpoint | Description |
|----------|-------------|
| `GET /v2/markdown` | Full Markdown API documentation |
| `GET /v2/llm-probe` | LLM-optimized structured output (Markdown) |
| `GET /v2/llm-probe/json` | LLM-optimized structured output (JSON) |

## Generated Output Example

The POST endpoint above produces:

````markdown
### `POST` /api/products

**Summary:** Create product

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
| `sku` | String | No | Product SKU code | p1 |
| `name` | String | Yes | Product display name | Super Widget |
| `price` | double | No | Product price in USD | 9.99 |
| `inStock` | boolean | No | Whether the product is currently in stock | true |

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

## Annotation Reference

### Controller-Level

| Annotation | Attribute | Description |
|-----------|-----------|-------------|
| `@MarkdownApi` | `tags` | Tag names for grouping APIs |
| | `description` | Description of the API group |
| | `hidden` | Set to `true` to exclude from docs |

### Method-Level

| Annotation | Attribute | Description |
|-----------|-----------|-------------|
| `@MarkdownApiOperation` | `value` | Short summary of the endpoint |
| | `notes` | Detailed description |
| | `tags` | Override class-level tags for this method |
| | `httpMethod` | HTTP method override (auto-detected normally) |

### Parameter-Level

| Annotation | Attribute | Description |
|-----------|-----------|-------------|
| `@MarkdownApiParam` | `name` | Parameter name |
| | `value` | Parameter description |
| | `required` | Whether the parameter is required |
| | `defaultValue` | Default value |
| | `example` | Example value |
| | `in` | Location: `query`, `path`, `header`, `body` |

### Field-Level

| Annotation | Attribute | Description |
|-----------|-----------|-------------|
| `@MarkdownApiModelProperty` | `value` | Field description |
| | `required` | Whether the field is required |
| | `example` | Example value |
| | `notes` | Additional details |

## Configuration Properties

All properties are under the `tentac` prefix.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `enabled` | boolean | `true` | Enable/disable Tentac |
| `title` | String | `API Documentation` | API title in the doc header |
| `description` | String | `""` | API description |
| `version` | String | `1.0.0` | API version |
| `base-package` | String | `""` | Base package to scan (empty = scan all) |
| `markdown-path` | String | `/v2/markdown` | Markdown endpoint path |
| `llm-probe-path` | String | `/v2/llm-probe` | LLM probe endpoint path |
| `llm-probe-enabled` | boolean | `true` | Enable/disable LLM probe endpoint |
| `ip-whitelist` | List\<String\> | `[]` | IP CIDR whitelist (e.g. `192.168.0.0/16`) |
| `ip-blacklist` | List\<String\> | `[]` | IP CIDR blacklist |

### IP Access Control Example

```yaml
tentac:
  ip-whitelist:
    - "127.0.0.1/32"        # localhost IPv4
    - "0:0:0:0:0:0:0:1/128" # localhost IPv6
    - "192.168.0.0/16"      # private LAN
    - "10.0.0.0/8"          # private network
  ip-blacklist:
    - "203.0.113.0/24"      # block specific range
```

## Module Structure

```
tentac/
├── tentac-core/                       # Core engine
│   ├── annotation/                    # @MarkdownApi, @MarkdownApiOperation, etc.
│   ├── core/                          # MarkdownGenerator, ApiScanner, etc.
│   └── model/                         # EndpointInfo, ParameterInfo
├── tentac-spring-boot-starter/        # Auto-configuration
│   ├── autoconfigure/                 # TentacAutoConfiguration, TentacEndpoint
│   ├── filter/                        # IpAccessFilter
│   └── probe/                         # LlmProbeGenerator
├── tentac-demo/                       # Demo with Swagger2 compatibility
└── tentac-demo-standalone/            # Demo without Swagger2 (standalone mode)
```

## Requirements

- Java 17+
- Spring Boot 3.2.x+
- Swagger2 annotations (`io.swagger:swagger-annotations`) — **optional**, for compatibility mode

## License

Apache 2.0
