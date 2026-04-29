# Getting Started

<cite>
**Referenced Files in This Document**
- [pom.xml](file://pom.xml)
- [swagger2md-core/pom.xml](file://swagger2md-core/pom.xml)
- [swagger2md-spring-boot-starter/pom.xml](file://swagger2md-spring-boot-starter/pom.xml)
- [swagger2md-demo/pom.xml](file://swagger2md-demo/pom.xml)
- [MarkdownApi.java](file://swagger2md-core/src/main/java/com/github/tentac/swagger2md/annotation/MarkdownApi.java)
- [MarkdownApiOperation.java](file://swagger2md-core/src/main/java/com/github/tentac/swagger2md/annotation/MarkdownApiOperation.java)
- [MarkdownApiParam.java](file://swagger2md-core/src/main/java/com/github/tentac/swagger2md/annotation/MarkdownApiParam.java)
- [UserController.java](file://swagger2md-demo/src/main/java/com/github/tentac/swagger2md/demo/controller/UserController.java)
- [Swagger2mdAutoConfiguration.java](file://swagger2md-spring-boot-starter/src/main/java/com/github/tentac/swagger2md/autoconfigure/Swagger2mdAutoConfiguration.java)
- [Swagger2mdEndpoint.java](file://swagger2md-spring-boot-starter/src/main/java/com/github/tentac/swagger2md/autoconfigure/Swagger2mdEndpoint.java)
- [Swagger2mdProperties.java](file://swagger2md-spring-boot-starter/src/main/java/com/github/tentac/swagger2md/autoconfigure/Swagger2mdProperties.java)
- [MarkdownGenerator.java](file://swagger2md-core/src/main/java/com/github/tentac/swagger2md/core/MarkdownGenerator.java)
- [application.yml](file://swagger2md-demo/src/main/resources/application.yml)
- [DemoApplication.java](file://swagger2md-demo/src/main/java/com/github/tentac/swagger2md/demo/DemoApplication.java)
</cite>

## Table of Contents
1. [Introduction](#introduction)
2. [Prerequisites](#prerequisites)
3. [Quick Setup](#quick-setup)
4. [Install Dependencies](#install-dependencies)
5. [Configure Your Application](#configure-your-application)
6. [Annotate Controllers](#annotate-controllers)
7. [Run and Access Documentation](#run-and-access-documentation)
8. [Understanding the Output](#understanding-the-output)
9. [Verification Checklist](#verification-checklist)
10. [Troubleshooting](#troubleshooting)
11. [Next Steps](#next-steps)

## Introduction
This guide helps you quickly set up and use the tentac project to generate Markdown-format API documentation from your Spring Boot controllers. It supports two modes:
- Standalone mode using custom annotations
- Spring Boot integration via a starter that exposes ready-to-use endpoints

The demo application demonstrates both Swagger2 annotations and the custom Markdown annotations working together.

## Prerequisites
- Java 17 or higher
- Apache Maven
- Spring Boot 3.2.5
- A Spring Boot web application (or a new one you are building)

These versions are defined in the parent POM and enforced across modules.

**Section sources**
- [pom.xml:21-31](file://pom.xml#L21-L31)

## Quick Setup
Follow these steps to get started quickly:

1. Add the required dependencies to your Spring Boot application’s Maven build.
2. Configure the swagger2md properties in your application configuration.
3. Annotate your REST controllers with the provided annotations.
4. Start your application and access the documentation endpoints.

## Install Dependencies
Choose one of the following approaches depending on your integration needs.

### Option A: Spring Boot Integration (Recommended)
Add the starter dependency to your Spring Boot application so that endpoints are auto-configured.

- Dependency coordinates:
  - Group: com.github.tentac
  - Artifact: swagger2md-spring-boot-starter
  - Version: Same as the parent project version

This starter brings in the core library and auto-configures:
- A Markdown documentation endpoint
- An LLM probe endpoint (and JSON variant)
- IP access filtering for the endpoints
- Configuration properties under swagger2md.*

Ensure your Spring Boot version matches the project’s managed version.

**Section sources**
- [swagger2md-demo/pom.xml:20-41](file://swagger2md-demo/pom.xml#L20-L41)
- [swagger2md-spring-boot-starter/pom.xml:19-48](file://swagger2md-spring-boot-starter/pom.xml#L19-L48)
- [pom.xml:27](file://pom.xml#L27)

### Option B: Standalone Usage (Custom Annotations Only)
If you want to keep things minimal and not rely on Spring Boot auto-configuration, add the core dependency and manage endpoints yourself.

- Dependency coordinates:
  - Group: com.github.tentac
  - Artifact: swagger2md-core
  - Version: Same as the parent project version

Then, use the core generator programmatically to produce Markdown documentation from your annotated controllers.

**Section sources**
- [swagger2md-core/pom.xml:19-49](file://swagger2md-core/pom.xml#L19-L49)

## Configure Your Application
Enable and configure swagger2md in your application configuration.

Key properties (prefix: swagger2md):
- enabled: true/false (default true)
- title: Documentation title
- description: Documentation description
- version: API version
- base-package: Package to scan for controllers (empty scans all)
- markdown-path: Path for Markdown endpoint (default /v2/markdown)
- llm-probe-path: Path for LLM probe endpoint (default /v2/llm-probe)
- llm-probe-enabled: Enable/disable LLM probe endpoints
- ip-whitelist: List of CIDR ranges allowed to access endpoints
- ip-blacklist: List of CIDR ranges denied from accessing endpoints

Example configuration is provided in the demo application.

**Section sources**
- [application.yml:8-24](file://application.yml#L8-L24)
- [Swagger2mdProperties.java:12-127](file://swagger2md-spring-boot-starter/src/main/java/com/github/tentac/swagger2md/autoconfigure/Swagger2mdProperties.java#L12-L127)

## Annotate Controllers
There are two complementary approaches demonstrated in the demo controller.

Approach 1: Use custom annotations for standalone mode
- Annotate the controller type with MarkdownApi to define tags and description
- Annotate each endpoint method with MarkdownApiOperation for summary and notes
- Annotate parameters with MarkdownApiParam for name, description, requirement, defaults, examples, and location

Approach 2: Keep existing Swagger2 annotations
- The demo controller also uses io.swagger.annotations.Api, ApiOperation, ApiParam alongside the custom annotations
- This enables compatibility with existing tooling while still generating Markdown docs

See the demo controller for a full example of both approaches applied to a typical CRUD API.

**Section sources**
- [MarkdownApi.java:8-24](file://swagger2md-core/src/main/java/com/github/tentac/swagger2md/annotation/MarkdownApi.java#L8-L24)
- [MarkdownApiOperation.java:8-27](file://swagger2md-core/src/main/java/com/github/tentac/swagger2md/annotation/MarkdownApiOperation.java#L8-L27)
- [MarkdownApiParam.java:8-33](file://swagger2md-core/src/main/java/com/github/tentac/swagger2md/annotation/MarkdownApiParam.java#L8-L33)
- [UserController.java:20-137](file://swagger2md-demo/src/main/java/com/github/tentac/swagger2md/demo/controller/UserController.java#L20-L137)

## Run and Access Documentation
Start your Spring Boot application and open these URLs in your browser or client:

- Markdown documentation: http://localhost:8080/v2/markdown
- LLM probe (Markdown): http://localhost:8080/v2/llm-probe
- LLM probe (JSON): http://localhost:8080/v2/llm-probe/json

The demo application defines the server port and swagger2md endpoints in its configuration.

**Section sources**
- [DemoApplication.java:6-12](file://swagger2md-demo/src/main/java/com/github/tentac/swagger2md/demo/DemoApplication.java#L6-L12)
- [application.yml:1-2](file://swagger2md-demo/src/main/resources/application.yml#L1-L2)
- [application.yml:14-16](file://swagger2md-demo/src/main/resources/application.yml#L14-L16)

## Understanding the Output
- Markdown documentation endpoint returns human-readable API documentation in Markdown format.
- LLM probe endpoints return structured content optimized for Large Language Model consumption:
  - Markdown variant for general use
  - JSON variant for programmatic consumption by LLMs

The endpoints are exposed by an auto-configured REST controller that delegates to the core generator and a probe generator.

**Section sources**
- [Swagger2mdEndpoint.java:40-71](file://swagger2md-spring-boot-starter/src/main/java/com/github/tentac/swagger2md/autoconfigure/Swagger2mdEndpoint.java#L40-L71)

## Verification Checklist
- Java and Maven versions meet prerequisites
- Your Spring Boot application includes the swagger2md starter or core dependency
- swagger2md.enabled is true in your configuration
- At least one controller is annotated with MarkdownApi and methods with MarkdownApiOperation
- Application starts without errors
- Endpoints return content when accessed

If you encounter issues, see the Troubleshooting section below.

## Troubleshooting
Common setup issues and fixes:

- Spring Boot version mismatch
  - Ensure your project uses Spring Boot 3.2.5 to align with the parent POM.
  - Verify your dependency management honors the parent’s property for spring-boot.version.

- No endpoints found
  - Confirm your controllers are in the scanned package (base-package) or leave it empty to scan all.
  - Ensure your controller methods are annotated with @RestController and include MarkdownApiOperation.

- Endpoints not accessible
  - Check ip-whitelist/ip-blacklist configuration if access filtering is enabled.
  - Verify the endpoint paths (markdown-path, llm-probe-path) match your configuration.

- LLM probe disabled
  - Set llm-probe-enabled to true if you need the LLM probe endpoints.

- Conflicts with Swagger2 annotations
  - The demo shows both Swagger2 and Markdown annotations coexisting; ensure both sets are present where needed.

**Section sources**
- [pom.xml:27](file://pom.xml#L27)
- [Swagger2mdProperties.java:15-43](file://swagger2md-spring-boot-starter/src/main/java/com/github/tentac/swagger2md/autoconfigure/Swagger2mdProperties.java#L15-L43)
- [application.yml:17-24](file://swagger2md-demo/src/main/resources/application.yml#L17-L24)

## Next Steps
- Customize swagger2md.* properties to fit your API metadata and security posture
- Extend controller annotations to cover all endpoints and parameters
- Integrate the Markdown or JSON probe outputs into your CI/CD or documentation pipeline
- Explore advanced configuration such as base-package scoping and IP access lists