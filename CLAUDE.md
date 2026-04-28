# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Language and communication

- Respond to the user in Simplified Chinese.
- Write any added code comments in Simplified Chinese.

## Build, run, and test commands

Run commands from the repository root.

```bash
# Build all modules
mvn -f gulimall-Parent/pom.xml clean package -DskipTests

# Build one service module with required upstream modules
mvn -f gulimall-Parent/pom.xml -pl services/gulimall-product -am clean package -DskipTests

# Run a single service locally
mvn -f gulimall-Parent/services/gulimall-product/pom.xml spring-boot:run
mvn -f gulimall-Parent/services/gulimall-ware/pom.xml spring-boot:run
mvn -f gulimall-Parent/gateway/pom.xml spring-boot:run

# Run all tests
mvn -f gulimall-Parent/pom.xml test

# Run a single test class
mvn -f gulimall-Parent/renren-generator/pom.xml -Dtest=RenrenApplicationTests test

# renren-fast hardcodes skipTests=true in surefire, so force-enable tests when needed
mvn -f gulimall-Parent/renren-fast/pom.xml -DskipTests=false -Dtest=JwtTest test
```

- There is currently no dedicated lint task/plugin configured in the Maven poms.

## High-level architecture

- This repository is a Maven multi-module Spring Cloud project rooted at `gulimall-Parent`.
- Top-level modules are:
  - `services`: business microservices
  - `gateway`: Spring Cloud Gateway entrypoint
  - `common`: shared library used across services
  - `renren-fast`: admin/backend service exposed behind the gateway
  - `renren-generator`: standalone code generator module
- Parent dependency versions are managed in `gulimall-Parent/pom.xml` with Java 8, Spring Boot 2.6.6, Spring Cloud 2021.0.1, Spring Cloud Alibaba 2021.0.1.0, and MyBatis-Plus 3.5.x.

### Business services

- `services` contains these microservices:
  - `gulimall-product`
  - `gulimall-member`
  - `gulimall-coupon`
  - `gulimall-order`
  - `gulimall-ware`
  - `gulimall-thirdParty`
- Each service is its own Spring Boot application and typically uses `bootstrap.yaml` for Nacos config/discovery setup.
- Service names are the integration contract for discovery and Feign calls, e.g. `service-product`, `service-member`, `service-coupon`, `service-ware`, `service-thirdparty`.

### Configuration and service discovery

- Runtime configuration is Nacos-based rather than fully local-file-based.
- Service `bootstrap.yaml` files define `spring.application.name`, `spring.cloud.nacos.discovery`, and `spring.cloud.nacos.config`.
- Business services commonly pull shared Nacos configs through `extension-configs`, especially `mybatis.yaml` and `datasource-common.yaml` from `COMMON_GROUP`.
- When a service looks under-configured locally, check whether the missing datasource/MyBatis settings are expected to come from Nacos rather than from the repo.

### Gateway routing

- `gateway` is the public API entrypoint and rewrites `/api/**` traffic to downstream services.
- Current route patterns in `gateway/src/main/resources/bootstrap.yaml` include:
  - `/api/product/** -> lb://service-product -> /product/**`
  - `/api/member/** -> lb://service-member -> /member/**`
  - `/api/ware/** -> lb://service-ware -> /ware/**`
  - `/api/third/party/** -> lb://service-thirdparty`
  - fallback `/api/** -> lb://service-renrenfast -> /renren-fast/**`
- `renren-fast` therefore depends on keeping its servlet context path at `/renren-fast`.

### Shared code in `common`

- `common` is a cross-service dependency that bundles shared utilities, pagination helpers, transfer objects, validation groups, exception types, SQL/XSS helpers, and Spring Cloud dependencies used by multiple modules.
- Package naming inside `common` is intentionally flat. Imports such as `utils.R`, `utils.PageUtils`, `utils.Query`, `group.*`, `to.*`, `exception.RRException`, and `xss.SQLFilter` are established usage patterns in the repo.
- Do not “normalize” these into deeper `com.xjz...` packages unless you are doing a deliberate repository-wide refactor.

## Code patterns that matter in this repo

- Controllers usually follow generated CRUD style:
  - class-level `@RequestMapping("domain/entity")`
  - endpoints like `/list`, `/info/{id}`, `/save`, `/update`, `/delete`
  - responses built with `R.ok().put(...)`
- Response payloads are typically map-like via `utils.R`, not custom response DTO wrappers.
- Persistence follows standard MyBatis-Plus conventions across services:
  - DAO interfaces extend `BaseMapper<Entity>`
  - service implementations often extend `ServiceImpl<Dao, Entity>`
  - pagination is usually `new Query<T>().getPage(params)` wrapped by `new PageUtils(page)`
- Query parameter names for pagination and sorting come from `utils.Constant`, especially `page`, `limit`, `sidx`, and `order`.
- Inter-service calls are handled with OpenFeign clients in `*.feign` packages, using Nacos service names in `@FeignClient(...)`.

## Module-specific conventions to preserve

- `gulimall-product` contains the strongest custom validation flow in the repo:
  - grouped validation via `ValidationGroups.save` and `ValidationGroups.Update`
  - centralized controller validation handling in `product/exception/GulimallExceptionControllerAdvice`
  - validation failures are converted to `R.error(40000, ...)` with field details stored in `data`
- `renren-fast` is not just another service module; it is the admin backend mounted behind the gateway fallback route and runs with `server.servlet.context-path: /renren-fast`.
- `renren-generator` is a separate generator application with its own dependency set and tests; do not assume its conventions are identical to the business microservices.
