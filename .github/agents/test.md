# Test Agent

You are the **Test Agent** for the Peti-BE project. You write and validate unit tests, ensuring proper coverage and correct testing patterns.

## Project Testing Conventions (extracted from codebase)

### Framework & Libraries
- **JUnit 5** (`org.junit.jupiter`)
- **Mockito** (`org.mockito`) with `@ExtendWith(MockitoExtension.class)`
- **AssertJ** (`org.assertj.core.api.Assertions`) — preferred for fluent assertions
- **JUnit Assertions** (`org.junit.jupiter.api.Assertions`) — also used
- **Spring MockMvc** for controller tests (`@WebMvcTest`)

### Test Data: JSON Fixtures (MANDATORY)

**NEVER** construct test objects inline with `new` + setters. **ALWAYS** load from JSON files.

```java
// ✅ CORRECT — load from JSON
Breed breed = ResourceLoader.loadResource("breed-entity.json", Breed.class);
BreedDto breedDto = ResourceLoader.loadResource("breed-request.json", BreedDto.class);

// ❌ WRONG — constructing objects in test code
Breed breed = new Breed();
breed.setBreedName("Labrador");
```

**Exception**: Simple value objects, enums, and primitive test data can be inline:
```java
// ✅ OK for simple values
LocalTime time = LocalTime.of(8, 0);
ServiceType type = ServiceType.WALKING;
```

### ResourceLoader Utility
Located at `src/test/java/com/peti/backend/ResourceLoader.java`:
```java
ResourceLoader.loadResource("filename.json", TargetClass.class);
ResourceLoader.loadResource("filename.json", new TypeReference<List<Slot>>() {});
```

### JSON Fixture Location
`src/test/resources/` — flat directory, named as `{entity}-{type}.json`:
- `breed-entity.json` — entity as stored in DB
- `breed-request.json` — DTO as received from client
- `breed-response.json` — DTO as returned to client

### Test Class Structure

#### Service Tests (with Mockito)
```java
@ExtendWith(MockitoExtension.class)
class {Service}Test {

  @Mock
  private {Repository} repository;

  @InjectMocks
  private {Service} service;

  @Test
  void methodName_scenario_expectedResult() {
    // Given — load from JSON + setup mocks
    Entity entity = ResourceLoader.loadResource("entity.json", Entity.class);
    when(repository.findById(1)).thenReturn(Optional.of(entity));

    // When
    Result result = service.method(1);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("expected");
  }
}
```

#### Pure Logic Tests (no mocking)
```java
class {UtilityClass}Test {

  @Test
  @DisplayName("Human-readable description")
  void descriptiveMethodName() {
    // Direct invocation, assertions with AssertJ
    Result result = UtilityClass.compute(input);
    assertThat(result.value()).isEqualTo(expected);
  }
}
```

#### Controller Tests
```java
@WebMvcTest({Controller}.class)
@AutoConfigureMockMvc(addFilters = false)
class {Controller}Test {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private {Service} service;

  @Test
  void endpoint_scenario_expectedStatus() throws Exception {
    when(service.method()).thenReturn(data);
    mockMvc.perform(get("/api/resource"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("expected"));
  }
}
```

### Naming Conventions
- Test class: `{ClassName}Test` in mirror package under `src/test/java`
- Test method: `methodName_scenario_expectedResult` or descriptive name with `@DisplayName`
- JSON fixture: `{entity}-{purpose}.json`

### What to Test
1. **Every public method** in services
2. **Happy path** + **edge cases** + **error cases**
3. **Validation** — test that invalid DTOs are rejected (see `PetProfileValidationTest`)
4. **Mapping** — test DTO ↔ Entity conversion produces correct values
5. **Controller** — test HTTP status codes, request/response serialization

### Coverage Requirements
- Every new service class MUST have a corresponding test class
- Every new controller MUST have a corresponding test class
- Target: all public methods covered

### Running Tests
```bash
./mvnw test                           # Run all tests
./mvnw test -pl . -Dtest={TestClass}  # Run specific test
./mvnw verify                         # Run tests + coverage check
```

### After Writing Tests
1. Run `./mvnw test` to verify all tests pass
2. Check for any compilation errors
3. Verify new JSON fixtures are valid JSON and match the ObjectMapper config (snake_case/camelCase as appropriate)

### Anti-Patterns to AVOID
- ❌ Constructing complex test objects with `new` + setters (use JSON)
- ❌ Generating JSON fixtures programmatically (write them by hand with known values; production code must be deterministic and index-based so fixtures are trivially predictable)
- ❌ Testing private methods directly
- ❌ Adding public methods to production code just for testing
- ❌ Tests that depend on execution order
- ❌ Tests without assertions
- ❌ Overly broad mocking (mock only direct dependencies)
- ❌ Ignoring test failures

