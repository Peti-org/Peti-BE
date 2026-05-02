# Peti-BE Project Knowledge Cache

> **Last analyzed**: 2026-05-02
> This file is the local knowledge store so agents don't re-analyze the project structure each time.

---

## 1. Project Overview

Peti-BE is the **backend for a pet caretaker marketplace**. Pet owners can browse caretaker profiles, search available time slots, book services (walking, sitting, training, grooming, vet), manage pets, and process orders. Caretakers define recurring availability via RRULE schedules, set pricing per service/pet/weight-tier, and manage bookings. An admin role handles order oversight and reference-data management.

### Core Domain Concepts

| Concept | Description |
|---------|-------------|
| **User** | Registered user (pet owner or caretaker). Implements Spring `UserDetails`. |
| **Caretaker** | A user who offers pet services. Has JSONB preferences (services, pricing, weekly schedule). |
| **Pet** | A pet owned by a user. Has breed, birthday, and JSONB profile. |
| **Event** | A booking request from a user to a caretaker for a specific time range. |
| **Order** | A confirmed engagement derived from an Event. Has a state-machine lifecycle. |
| **Slot / CaretakerRRule** | Caretaker availability — legacy fixed slots and modern RRULE-based recurrence. |
| **ElasticSlotDocument** | Denormalized search document in Elasticsearch for fast catalog browsing. |
| **Article / Comment / Reaction** | Content module — blog articles with threaded comments and emoji reactions. |

---

## 2. Technical Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Java | 21 |
| Build tool | Maven (wrapper `mvnw` / `mvnw.cmd`) | — |
| Framework | Spring Boot | 3.5.0 |
| ORM | Spring Data JPA / Hibernate | (managed by Boot) |
| Database | PostgreSQL | 15 (Docker) |
| Schema management | Liquibase | 4.33.0 |
| Search engine | Elasticsearch | 8.12.0 (Docker) |
| Security | Spring Security + JWT (jjwt 0.12.6) | — |
| OAuth | Google OAuth2 (`google-api-client` 2.7.2) | — |
| API docs | SpringDoc OpenAPI 3 (`springdoc-openapi-starter-webmvc-ui` 2.8.8) | — |
| RRULE parsing | `lib-recur` 0.11.6 + `rfc5545-datetime` 0.2.4 | — |
| Code style | Google Java Style (Checkstyle, `checkstyle/checks.xml`) | — |
| Utilities | Lombok, Apache Commons Lang3 | — |
| Testing | JUnit 5, Mockito, AssertJ, Spring MockMvc | — |
| Containerization | Docker multi-stage build, Docker Compose | — |

---

## 3. Architecture Pattern

**Layered Clean Architecture** with feature-based sub-packaging inside each layer:

```
Controller  →  Service  →  Repository  →  Database / Elasticsearch
    ↓              ↓
   DTO         Entity (domain model)
```

### Layer Responsibilities

| Layer | Location | Responsibility |
|-------|----------|---------------|
| **Controller** | `controller/{domain}/` | HTTP handling, input validation, DTO ↔ response mapping |
| **Service** | `service/{domain}/` | Business logic, transaction boundaries, entity ↔ DTO mapping |
| **Repository** | `repository/` | Spring Data JPA interfaces + `elastic/ElasticSlotRepository` |
| **Model** | `model/domain/` | JPA entities (PostgreSQL) |
| | `model/elastic/` | Elasticsearch documents + value records |
| | `model/internal/` | Enums: `ServiceType`, `OrderStatus`, `EventStatus` |
| | `model/exception/` | `BadRequestException`, `NotFoundException` |
| **DTO** | `dto/{domain}/` | Request/response DTOs, separate from entities |
| **Security** | `security/` | JWT filter, security config, role annotations |
| **Config** | `config/` | `AppConfig` (ObjectMapper), `ElasticsearchConfig`, `HibernateConfig` |
| **Utils** | `utils/` | `MockDataBuilder`, `ElasticMockDataInitializer`, `DeepCloner` |

### Package Structure
```
com.peti.backend
├── config/                  # Spring configs (ObjectMapper, Elasticsearch, Hibernate)
├── controller/              # REST controllers by domain
│   ├── admin/               # AdminOrderController
│   ├── auth/                # AuthenticationController, OAuthController
│   ├── content/             # ArticleController, InteractionController
│   ├── maintenance/         # AdminSlotController, MaintenanceController
│   ├── order/               # OrderController
│   ├── slot/                # CatalogController, ElasticCatalogController, SlotController
│   └── user/                # BreedController, CaretakerController, CityController, PetController, UserController
│   EventController.java     # Booking events
│   InputParamsResolver.java # Extracts authenticated user from SecurityContext
│   RestExceptionHandler.java# Global @ControllerAdvice error handling
├── dto/                     # DTOs by domain
│   ├── caretacker/          # CaretakerDto, CaretakerPreferences (JSONB record with nested records), SimpleCaretakerDto
│   ├── content/             # ArticleDto, CommentDto, CursorPageResponse, Request*Dto
│   ├── elastic/             # ElasticSlotSearchRequest/Response, SlotSearchResult
│   ├── order/               # OrderDto, AdminOrderDto, OrderModificationDto, RequestOrderTransitionDto, UserInfoDto
│   ├── event/               # EventDto, RequestEventDto
│   ├── pet/                 # PetDto, PetProfile (JSONB record), RequestPetDto
│   ├── rrule/               # RequestRRuleDto, RRuleDto
│   ├── slot/                # SlotDto, RequestSlotDto, SlotFiltersDto, PagedSlotsResponse
│   └── user/                # AuthResponse, UserDto, Request*Dto
├── model/
│   ├── domain/              # JPA entities (15 classes, see Data Model below)
│   ├── elastic/             # ElasticSlotDocument + nested model records
│   ├── exception/           # BadRequestException, NotFoundException
│   ├── internal/            # ServiceType, OrderStatus, EventStatus (enums), TimeSlotPair
│   └── projection/          # UserProjection
├── repository/              # Spring Data JPA repos + elastic/ElasticSlotRepository
├── security/                # JWT auth filter, security config, role meta-annotations
├── service/                 # Business logic
│   ├── auth/                # AuthenticationService, GoogleAuthenticationService
│   ├── content/             # ArticleService, CommentService, ReactionService
│   ├── elastic/             # CapacityCalculator, CapacityTimelineBuilder, ElasticAggregationHelper,
│   │                        #   ElasticQueryBuilder, ElasticSlotAssembler, ElasticSlotCrudService,
│   │                        #   ElasticSlotSearchService, PriceCalculationService, PriceResolver,
│   │                        #   SlotGenerationService, SlotRangeResolver, SlotSearchResultMapper
│   ├── event/               # EventService, EventValidator, EventPriceCalculator, CaretakerSlotsRebuildTrigger
│   ├── order/               # OrderService, AdminOrderService, OrderStatusMachine
│   ├── slot/                # CaretakerRRuleService, RRuleSlotGenerator, SlotDivider, SlotGenerationScheduler, SlotService
│   └── user/                # BreedService, CaretakerService, CityService, PetService, RoleService, UserService
└── utils/                   # ElasticMockDataInitializer, MockDataBuilder
```

---

## 4. Data Model

### 4.1 PostgreSQL Entities (schema `peti`)

All primary keys are `UUID` (generated by JPA). JSONB columns use `@JdbcTypeCode(SqlTypes.JSON)`.

| Entity | Table | Key Relationships | Notes |
|--------|-------|-------------------|-------|
| **User** | `user` | → Role (M:1), → City (M:1), → Location (M:1, deprecated) | Implements `UserDetails` |
| **Role** | `role` | ← User (1:M) | int PK (`role_id`); names: USER, ADMIN, CARETAKER |
| **City** | `city` | ← User (1:M) | long PK (`city_id`) |
| **Location** | `location` | ← User (1:M) | Deprecated, slated for removal |
| **Caretaker** | `caretaker` | → User (M:1) | `caretaker_preference` JSONB → `CaretakerPreferences` record |
| **CaretakerRRule** | `caretaker_rrule` | → Caretaker (M:1) | RRULE string, dtstart/dtend, capacity, slot_type, interval_minutes |
| **Slot** | `caretaker_slot` | → Caretaker (M:1) | Legacy fixed-time slots (date, time_from, time_to, price) |
| **Pet** | `pet` | → User (M:1), → Breed (M:1) | `context` JSONB → `PetProfile` record |
| **Breed** | `breed` | ← Pet (1:M) | `pet_type` + `breed_name` |
| **Event** | `event` | → User (M:1), → Caretaker (M:1), → CaretakerRRule (M:1), ↔ Pet (M:M via join table) | `price` JSONB → `PriceDto`; status enum |
| **Order** | `"order"` | → User/client (M:1), → Caretaker (M:1), → Event (1:1) | State machine (OrderStatus), price + currency |
| **OrderModification** | `order_modification` | → Order (M:1) | Audit trail of status transitions |
| **Article** | `article` | → User/author (M:1) | `tags` JSONB list, content as TEXT |
| **Comment** | `comment` | → Article (M:1), → User (M:1), → Comment/parent (M:1, self-ref) | Threaded comments |
| **Reaction** | `reaction` | → Article (M:1), → User (M:1) | Emoji string, unique per user+article |

### 4.2 Entity Relationship Diagram (simplified)

```
  Role ──< User >── City
             │
        ┌────┴────┐
        │         │
   Caretaker    Pet >── Breed
     │    │
     │    └── CaretakerRRule
     │             │
     │         Event >──< Pet (M:M)
     │             │
     └─────── Order
                │
          OrderModification

  User ──< Article ──< Comment (self-referencing)
                   ──< Reaction
```

### 4.3 Elasticsearch Index

| Index | Document | Purpose |
|-------|----------|---------|
| `walking-slots` | `ElasticSlotDocument` | Denormalized caretaker+slot data for catalog search. Fields: caretakerId, name, rating, cityId, date, timeFrom, timeTo, serviceType, capacity segments with pricing. Regenerated from RRULE data. |

### 4.4 Enums

| Enum | Values |
|------|--------|
| `ServiceType` | WALKING, SITTING, TRAINING, GROOMING, VET, UNDEFINED |
| `OrderStatus` | RESERVED → DECLINED / DEFERRED_PAYMENT → EXECUTING → FINISHED / PAID / CANCELLED |
| `EventStatus` | CREATED, APPROVED, CANCELLED, DELETED |

---

## 5. API & Integration

### 5.1 REST Endpoints

#### Authentication (`/auth`)
| Method | Path | Access | Description |
|--------|------|--------|-------------|
| POST | `/auth/signup` | Public | Register new user |
| POST | `/auth/login` | Public | Login, returns JWT + refresh token |
| POST | `/auth/refresh-token` | Public | Refresh JWT |
| POST | `/auth/oauth/google` | Public | Google OAuth login/register |

#### Users (`/api/users`)
| Method | Path | Access | Description |
|--------|------|--------|-------------|
| GET | `/api/users` | Admin | List all users |
| GET | `/api/users/roles/refresh` | Admin | Refresh role cache |
| GET | `/api/users/me` | Authenticated | Get current user profile |
| PUT | `/api/users/me` | Authenticated | Update current user |
| PUT | `/api/users/me/password` | Authenticated | Change password |
| DELETE | `/api/users/me` | Authenticated | Soft-delete account |

#### Pets (`/api/pets`)
| Method | Path | Access | Description |
|--------|------|--------|-------------|
| POST | `/api/pets` | Authenticated | Create pet |
| GET | `/api/pets` | Authenticated | List my pets |
| GET | `/api/pets/{id}` | Authenticated | Get pet by ID |
| PUT | `/api/pets/{id}` | Authenticated | Update pet |
| DELETE | `/api/pets/{id}` | Authenticated | Delete pet |

#### Caretakers (`/api/caretakers`)
| Method | Path | Access | Description |
|--------|------|--------|-------------|
| GET | `/api/caretakers` | Admin | List all caretakers |
| GET | `/api/caretakers/me` | Caretaker | Get my caretaker profile |
| POST | `/api/caretakers` | Authenticated | Register as caretaker |
| PUT | `/api/caretakers` | Caretaker | Update caretaker preferences |

#### Cities & Breeds (`/api/cities`, `/api/breeds`)
| Method | Path | Access | Description |
|--------|------|--------|-------------|
| GET | `/api/cities/{id}` | Authenticated | Get city |
| GET | `/api/cities/country/{code}` | **Public** | Cities by country (registration) |
| POST/PUT/DELETE | `/api/cities/**` | Admin | CRUD cities |
| POST/PUT/DELETE | `/api/breeds/**` | Admin | CRUD breeds |

#### Slots & RRules (`/api/slots`)
| Method | Path | Access | Description |
|--------|------|--------|-------------|
| GET | `/api/slots/my` | Caretaker | List my slots |
| POST | `/api/slots` | Caretaker | Create slot |
| PUT | `/api/slots/{slotId}` | Caretaker | Update slot |
| DELETE | `/api/slots/{slotId}` | Caretaker | Delete slot |
| GET | `/api/slots/rrules` | Caretaker | List my RRULEs |
| POST | `/api/slots/rrules` | Caretaker | Create RRULE |
| PUT | `/api/slots/rrules/{rruleId}` | Caretaker | Update RRULE |
| DELETE | `/api/slots/rrules/{rruleId}` | Caretaker | Delete RRULE |

#### Catalog (Public Search)
| Method | Path | Access | Description |
|--------|------|--------|-------------|
| POST | `/api/slots/filter` | **Public** | Search slots (PostgreSQL) |
| POST | `/api/slots/calendar` | Authenticated | Calendar view |
| GET | `/api/slots/{slotId}` | Authenticated | Get single slot |
| POST | `/api/v2/catalog/search` | **Public** | Elasticsearch-powered search |

#### Events (`/api/events`)
| Method | Path | Access | Description |
|--------|------|--------|-------------|
| GET | `/api/events/caretaker/my` | Caretaker | List my events as caretaker |
| GET | `/api/events/user/my` | Authenticated | List my events as user |
| POST | `/api/events` | Authenticated | Create booking event |
| DELETE | `/api/events/{eventId}` | Authenticated | Cancel event |

#### Orders (`/api/orders`)
| Method | Path | Access | Description |
|--------|------|--------|-------------|
| POST | `/api/orders/from-event/{eventId}` | Authenticated | Create order from event |
| GET | `/api/orders/{orderId}` | Authenticated | Get order details |
| GET | `/api/orders/{orderId}/modifications` | Authenticated | Get order audit trail |
| GET | `/api/orders/my` | Authenticated | My orders (as client) |
| GET | `/api/orders/caretaker/my` | Caretaker | My orders (as caretaker) |
| POST | `/api/orders/{orderId}/decline` | Caretaker | Decline order |
| POST | `/api/orders/{orderId}/pay` | Authenticated | Mark as paid |
| POST | `/api/orders/{orderId}/cancel` | Authenticated | Cancel order |

#### Admin Orders (`/api/admin/orders`)
| Method | Path | Access | Description |
|--------|------|--------|-------------|
| GET | `/api/admin/orders` | Admin | List all orders (paged, filtered) |
| GET | `/api/admin/orders/{orderId}` | Admin | Get order with user info |
| GET | `/api/admin/orders/{orderId}/modifications` | Admin | Order audit trail |

#### Content (`/api/articles`, `/api/interactions`)
| Method | Path | Access | Description |
|--------|------|--------|-------------|
| GET | `/api/articles` | Authenticated | List articles (cursor paging) |
| GET | `/api/articles/{articleId}` | Authenticated | Get article |
| GET | `/api/articles/authors/{userId}/articles` | Authenticated | Articles by author |
| POST | `/api/articles` | Authenticated | Create article |
| GET | `/api/interactions/comments` | Authenticated | Comments for article |
| POST | `/api/interactions/comments` | Authenticated | Add comment |
| DELETE | `/api/interactions/comments/{commentId}` | Authenticated | Delete comment |
| POST | `/api/interactions/reactions` | Authenticated | Toggle reaction |

#### Maintenance
| Method | Path | Access | Description |
|--------|------|--------|-------------|
| GET | `/api/ping` | **Public** | Health check |
| POST | `/api/admin/slots/generate` | Admin | Trigger elastic slot regeneration |

### 5.2 External Integrations

| Integration | Usage | Config |
|-------------|-------|--------|
| **Google OAuth2** | Social login — exchange Google auth code for user identity | `google.oauth.*` properties; `GoogleAuthenticationService` |
| **Elasticsearch** | Full-text slot search, aggregation-based catalog | `spring.elasticsearch.*` properties; `ElasticsearchConfig` |

No other third-party APIs (payment, messaging, etc.) are currently integrated. [NEEDS MANUAL INPUT: future payment gateway plans?]

---

## 6. Authentication & Security

### Architecture
- **Stateless JWT** — no server-side sessions (`SessionCreationPolicy.STATELESS`)
- **JwtAuthenticationFilter** — extracts Bearer token from `Authorization` header, validates, sets `SecurityContext`
- **JwtService** — generates/validates access + refresh tokens using HMAC-SHA256 (`jjwt` library)
- **Passwords** — BCrypt hashed (`PasswordEncoder` bean in `ApplicationConfiguration`)

### Auth Flow
1. `POST /auth/signup` → register user, return `AuthResponse` (access + refresh tokens)
2. `POST /auth/login` → authenticate credentials, return tokens
3. `POST /auth/refresh-token` → exchange refresh token for new access token
4. `POST /auth/oauth/google` → exchange Google auth code → find-or-create user → return tokens

### Role-Based Access Control
- Roles stored in `role` table: `USER` (id=1), `ADMIN` (id=2), `CARETAKER` (id=3)
- Method-level security via custom meta-annotations:
  - `@HasAdminRole` — admin-only endpoints
  - `@HasCaretakerRole` — caretaker-only endpoints
  - `@HasUserRole` — any authenticated user
- Enabled via `@EnableMethodSecurity(securedEnabled = true)`

### Public Endpoints (no auth required)
- `/auth/**` — all authentication endpoints
- `/api/cities/country/**` — city lookup for registration
- `/api/slots/filter` — public slot search
- `/api/v2/catalog/**` — Elasticsearch catalog
- `/api/ping` — health check
- `/swagger-ui/**`, `/api-docs/**` — API documentation

### CORS
Allowed origins: `localhost:8080`, `localhost:8082`, `localhost:8083`

---

## 7. Development Workflow

### Database Migrations (Liquibase)
- **Master changelog**: `src/main/resources/db/changelog/changelog-master.yaml`
- **Initial schema**: `db/changelog/migration/0.0.1-init.sql`
- **Mock/seed data**: `db/changelog/mock-data/` (loaded via `includeAll`)
- **Incremental scripts**: `db/changelog/scripts/` (alphabetical prefix + date naming, e.g. `r-28-04-2026.sql`)
- **Format**: Liquibase formatted SQL with `-- changeset author:id` headers
- **Validation**: `spring.jpa.hibernate.ddl-auto=validate` ensures entity ↔ schema consistency
- **Docker init**: `db/setup/create-scheme.sql` + `grant-permissions.sql` run on container creation

### Testing Patterns

| Pattern | Description |
|---------|-------------|
| **Framework** | JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`) + AssertJ |
| **Service tests** | Mocked dependencies via `@Mock`/`@InjectMocks`; load test data from JSON fixtures |
| **Controller tests** | `@WebMvcTest` + `@AutoConfigureMockMvc(addFilters = false)` + `@MockBean` |
| **Validation tests** | Jakarta `Validator` directly on DTOs |
| **Pure logic tests** | No mocking, direct invocation (e.g., `CapacityCalculator`, `MockDataBuilder`) |
| **JSON fixtures** | All test entities loaded via `ResourceLoader.loadResource("file.json", Type.class)` — **never** inline `new` + setters |
| **Fixture location** | `src/test/resources/` — flat directory, `{entity}-{purpose}.json` naming |
| **ObjectMapper** | Test `ResourceLoader` mirrors production config: `FAIL_ON_UNKNOWN_PROPERTIES=false`, comments allowed, JSR-310 modules |
| **Comparison** | `assertThat(actual).usingRecursiveComparison().isEqualTo(expected)` for complex objects |

### Build Commands
```bash
.\mvnw.cmd clean compile          # Compile
.\mvnw.cmd test                   # Run all tests
.\mvnw.cmd test -Dtest={Class}    # Run specific test
.\mvnw.cmd verify                 # Tests + checks
```

### Docker Compose Services
| Service | Image | Port |
|---------|-------|------|
| PostgreSQL | `postgres:15` | 5437 → 5432 |
| Elasticsearch | `elasticsearch:8.12.0` | 9200 |

### Key Configuration (`application.properties`)
| Property | Value |
|----------|-------|
| `server.port` | 8083 |
| `spring.jpa.hibernate.ddl-auto` | validate |
| `spring.jpa.open-in-view` | false |
| `security.jwt.expiration-time` | 86400000 (1 day) |
| `security.jwt.refresh.expiration-time` | 1296000000 (15 days) |
| `elasticsearch.mock-data.enabled` | true |
| `elasticsearch.mock-data.caretakers` | 50 |

---

## Key Patterns
| Pattern | Implementation |
|---------|---------------|
| DI | Constructor injection via `@RequiredArgsConstructor` |
| DTO mapping | Static `convert(Entity)` / `convert(Entity, ...)` on DTO + private `toEntity(Dto)` in service |
| Validation | Jakarta `@NotEmpty`, `@NotNull` on DTOs, `@Valid` on controller params |
| Error handling | Global `RestExceptionHandler`, custom `BadRequestException`/`NotFoundException` |
| Deep cloning | `DeepCloner` component — serializes/deserializes JSONB records (`CaretakerPreferences`, `PetProfile`) via `ObjectMapper` to produce independent copies |
| Async | `@EnableAsync` on `AppConfig`; `@Async` on `CaretakerSlotsRebuildTrigger.rebuild()` methods |
| Arg resolvers | `CurrentCaretakerIdArgumentResolver` resolves `@CurrentCaretakerId UUID` from security context → `CaretakerService.getCaretakerIdByUserId()` |
| Annotations | `@CurrentUser` — injects `UserProjection` into controller methods; `@CurrentCaretakerId` — injects caretaker UUID |

### New/Refactored Classes (2026-05-02)

| Class | Package | Description |
|-------|---------|-------------|
| `DeepCloner` | `utils` | Component for deep-copying JSONB records via ObjectMapper serialization round-trip |
| `CurrentCaretakerIdArgumentResolver` | `config` | Spring `HandlerMethodArgumentResolver` for `@CurrentCaretakerId` annotation |
| `WebMvcConfig` | `config` | Registers custom argument resolvers |
| `@CurrentCaretakerId` | `security.annotation` | Parameter annotation to inject resolved caretaker UUID |
| `@CurrentUser` | `security.annotation` | Parameter annotation to inject `UserProjection` from security context |
| `RRuleDto.convert()` | `dto.rrule` | Static factory method moved from `CaretakerRRuleService.convertToDto()` to DTO |
| `CaretakerDto.convert()` | `dto.caretacker` | Static factory method for entity → DTO mapping |
| `SimpleCaretakerDto` | `dto.caretacker` | Converted from class to record with `convert()` factory |
| `PetDto.convert()` | `dto.pet` | Static factory accepting `Pet` + deep-copied `PetProfile` |
| `OrderStatusMachine` | `service.order` | Removed `resolveRole()`, `allowedTransitions()`, `canView()`; Role enum moved to bottom; uses `@NoArgsConstructor(access = PRIVATE)` |
| `OrderService` | `service.order` | Split `getOrderForActor` → `getOrderAsClient`/`getOrderAsCaretaker`; `transition()` now takes explicit `Role` parameter |
| `CaretakerRRuleService` | `service.slot` | Refactored: extracted `applyFields()` and `buildRRule()` private methods; replaced `SlotGenerationScheduler` with `CaretakerSlotsRebuildTrigger`; uses `findByRruleIdAndCaretaker_CaretakerId` |

### Test Coverage for New/Refactored Code

| Test Class | Package | Covers |
|------------|---------|--------|
| `DeepClonerTest` | `utils` | `deepCopyPreference()`, `deepCopyPetProfile()` — independent copies, null handling |
| `CurrentCaretakerIdArgumentResolverTest` | `config` | `supportsParameter()` (annotated UUID, wrong type, no annotation), `resolveArgument()` (found, not found → NotFoundException) |
| `CaretakerDtoTest` | `dto.caretacker` | `CaretakerDto.convert()` static factory |
| `SimpleCaretakerDtoTest` | `dto.caretacker` | `SimpleCaretakerDto.convert()` static factory |
| `PetDtoTest` | `dto.pet` | `PetDto.convert()` static factory |
| `CaretakerRRuleServiceTest` | `service` | Fixed: uses `CaretakerSlotsRebuildTrigger` mock, `findByRruleIdAndCaretaker_CaretakerId`, `RRuleDto.convert()` |
| `OrderServiceTest` | `service.order` | Fixed: uses `getOrderAsClient`/`getModificationsAsClient`, `transition()` with explicit `Role` |
| `OrderStatusMachineTest` | `service.order` | Fixed: removed tests for deleted `resolveRole()`, `allowedTransitions()`, `canView()` |
| `PetServiceTest` (both packages) | `service`, `service.user` | Fixed: added `DeepCloner` mock |
| `ElasticMockDataInitializerTest` | `utils` | Fixed: removed `mockDataEnabled` field (now `@ConditionalOnProperty`), removed `disabled_skips` test |
| Security | Method-level `@HasUserRole`/`@HasAdminRole`/`@HasCaretakerRole` |
| Testing | JSON fixtures via `ResourceLoader`, Mockito for service tests, MockMvc for controller tests |
| Immutable models | Java records in `elastic/model/` package and DTO records (`CaretakerPreferences`, `PetProfile`) |
| JSONB columns | `@JdbcTypeCode(SqlTypes.JSON)` on `Caretaker.caretakerPreference`, `Pet.context`, `Event.price`, `Article.tags` |
| Mock data | `MockDataBuilder` (index-based deterministic, no Random) + `ElasticMockDataInitializer` (Spring `@Service`, `@EventListener`) |

---

## Test Coverage Map
| Source Class | Has Test? |
|---|---|
| BreedService | ✅ |
| CityService | ✅ |
| UserService | ✅ |
| PetService | ✅ |
| RoleService | ✅ |
| SlotService | ✅ |
| SlotDivider | ✅ |
| CaretakerRRuleService | ✅ |
| RRuleSlotGenerator | ✅ |
| SlotGenerationScheduler | ✅ (skipped — needs manual review) |
| AuthenticationService | ✅ |
| ArticleService | ✅ |
| CommentService | ✅ |
| ReactionService | ✅ |
| CapacityCalculator | ✅ |
| CapacityTimelineBuilder | ✅ |
| ElasticQueryBuilder | ✅ |
| ElasticSlotAssembler | ✅ |
| ElasticSlotCrudService | ✅ |
| PriceCalculationService | ✅ |
| PriceResolver | ✅ |
| SlotGenerationService | ✅ |
| SlotRangeResolver | ✅ |
| SlotSearchResultMapper | ✅ |
| JwtService | ✅ |
| JwtAuthenticationFilter | ✅ |
| SecurityConfiguration | ✅ |
| EventService | ✅ |
| EventValidator | ✅ |
| EventPriceCalculator | ✅ |
| CaretakerSlotsRebuildTrigger | ✅ |
| OrderService | ✅ |
| OrderStatusMachine | ✅ |
| AdminOrderService | ✅ |
| MockDataBuilder | ✅ |
| ElasticMockDataInitializer | ✅ |
| DeepCloner | ✅ |
| CurrentCaretakerIdArgumentResolver | ✅ |
| CaretakerDto.convert() | ✅ |
| SimpleCaretakerDto.convert() | ✅ |
| PetDto.convert() | ✅ |
| CaretakerService | ❌ |
| GoogleAuthenticationService | ❌ |
| ElasticSlotSearchService | ❌ |
| ElasticAggregationHelper | ❌ |

---

## JSON Test Fixtures
`src/test/resources/` — flat directory:

admin-modifications-response.json, admin-order-response.json, breed-entity.json, breed-request.json, breed-response.json, caretaker-entity.json, caretaker-user-entity.json, city-entity.json, city-request.json, city-response.json, event-active-entity.json, event-created-response.json, event-entity.json, login-data.json, mock-builder-caretaker.json, mock-builder-preferences.json, mock-builder-role.json, mock-builder-rrule.json, mock-builder-user.json, order-entity.json, order-reserved-response.json, order-response.json, order-with-modifications-entity.json, pet-entity.json, pet-request.json, pet-response.json, pets-for-event.json, refresh-token-data.json, register-response.json, registration-data.json, rrule-create-request.json, rrule-entities.json, rrule-entity.json, rrule-for-event-entity.json, rrule-response.json, rrule-update-request.json, slot-create-request.json, slot-divider-pairs.json, slot-entities.json, slot-entity.json, slot-update-request.json, update-password-request.json, update-user-request.json, user-entity.json, user-projection-entity.json, user-response.json

---

## Liquibase
- Master: `db/changelog/changelog-master.yaml`
- Init: `db/changelog/migration/0.0.1-init.sql`
- Mock data: `db/changelog/mock-data/` (auto-included)
- Scripts: `db/changelog/scripts/` (alphabetical prefix + date naming)
- Format: Liquibase formatted SQL with `-- changeset author:id` headers
