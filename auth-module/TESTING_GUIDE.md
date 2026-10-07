# 🧪 Testing Guide

## Overview

Auth Module includes comprehensive unit tests covering authentication, authorization, and user management functionality.

## Test Structure

```
backend/src/test/
├── java/dh13c8/nhom4/gym/
│   ├── controller/
│   │   └── AuthControllerTest.java      (10 tests)
│   ├── service/
│   │   ├── AuthServiceTest.java         (7 tests)
│   │   └── UserServiceTest.java         (12 tests)
│   └── ...
└── resources/
    └── application-test.properties
```

**Total: 29 unit tests**

---

## Running Tests

### Maven Command Line

```bash
# Run all tests
cd backend
mvn test

# Run specific test class
mvn test -Dtest=AuthServiceTest

# Run with coverage report
mvn test jacoco:report

# View coverage report
# Open: target/site/jacoco/index.html
```

### IDE (IntelliJ IDEA / Eclipse)

1. Right-click on `src/test/java`
2. Select "Run All Tests"
3. View results in Test Runner panel

### Docker

```bash
# Run tests in container
docker-compose run --rm auth-service mvn test
```

---

## Test Coverage

### AuthServiceTest (7 tests)

✅ **testLogin_Success**
- Tests successful login with valid credentials
- Verifies JWT token generation
- Checks user details in response

✅ **testLogin_InvalidCredentials**
- Tests login with wrong password
- Expects BadCredentialsException

✅ **testLogin_UserNotFound**
- Tests login with non-existent user
- Expects RuntimeException

✅ **testLogin_NullUsername**
- Tests login with null username
- Validates input validation

✅ **testLogin_EmptyPassword**
- Tests login with empty password
- Validates input validation

✅ **testLogin_JwtTokenGeneration**
- Tests JWT token format
- Verifies token starts with "eyJ"

✅ **testLogin_DifferentRoles**
- Tests login for ADMIN, TRAINER, MEMBER
- Verifies role-based token generation

### AuthControllerTest (10 tests)

✅ **testLogin_Success**
- HTTP POST to /api/auth/login
- Verifies 200 OK response
- Checks JSON response structure

✅ **testLogin_InvalidCredentials**
- Tests with wrong credentials
- Expects 401 Unauthorized

✅ **testLogin_MissingUsername**
- Tests with null username
- Expects 400 Bad Request

✅ **testLogin_MissingPassword**
- Tests with null password
- Expects 400 Bad Request

✅ **testLogin_EmptyRequestBody**
- Tests with empty JSON
- Expects 400 Bad Request

✅ **testLogin_InvalidJson**
- Tests with malformed JSON
- Expects 400 Bad Request

✅ **testLogin_TrainerRole**
- Tests trainer login
- Verifies TRAINER role in response

✅ **testLogin_MemberRole**
- Tests member login
- Verifies MEMBER role in response

✅ **testLogin_ResponseContainsJwtToken**
- Tests token presence in response
- Validates token is string

✅ **testLogin_ContentTypeValidation**
- Tests without Content-Type header
- Expects 415 Unsupported Media Type

### UserServiceTest (12 tests)

✅ **testGetAllUsers**
- Tests fetching all users
- Verifies list size

✅ **testGetUserById_Found**
- Tests finding user by ID
- Returns Optional with user

✅ **testGetUserById_NotFound**
- Tests with non-existent ID
- Returns empty Optional

✅ **testGetUserByUsername_Found**
- Tests finding user by username
- Returns Optional with user

✅ **testCreateUser**
- Tests user creation
- Verifies password encryption

✅ **testUpdateUser_Success**
- Tests updating user details
- Verifies changes saved

✅ **testUpdateUser_NotFound**
- Tests updating non-existent user
- Expects RuntimeException

✅ **testDeleteUser**
- Tests user deletion
- Verifies repository call

✅ **testExistsByUsername_True**
- Tests username exists
- Returns true

✅ **testExistsByUsername_False**
- Tests username doesn't exist
- Returns false

✅ **testCheckRole_Admin**
- Tests role checking
- Verifies role comparison

✅ **testCreateUser_PasswordEncryption**
- Tests password is encrypted
- Verifies BCrypt hash

✅ **testUpdateUser_DifferentRoles**
- Tests role changes
- Verifies role update

---

## Test Configuration

### application-test.properties

```properties
# H2 In-Memory Database
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver

# JPA
spring.jpa.hibernate.ddl-auto=create-drop

# JWT for testing
jwt.secret=TestSecretKeyForJWT
jwt.expiration=3600000

# Disable Actuator
management.endpoints.web.exposure.include=
```

### Test Dependencies (pom.xml)

- `spring-boot-starter-test` - Testing framework
- `spring-security-test` - Security testing
- `junit-jupiter` - JUnit 5
- `mockito-core` - Mocking framework
- `h2` - In-memory database

---

## Writing New Tests

### Example: Testing a new service method

```java
@ExtendWith(MockitoExtension.class)
class MyServiceTest {

    @Mock
    private MyRepository repository;

    @InjectMocks
    private MyService service;

    @Test
    void testMyMethod_Success() {
        // Arrange
        MyEntity entity = new MyEntity();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        // Act
        MyEntity result = service.getById(1L);

        // Assert
        assertNotNull(result);
        verify(repository).findById(1L);
    }
}
```

### Example: Testing a controller endpoint

```java
@SpringBootTest
@AutoConfigureMockMvc
class MyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testEndpoint() throws Exception {
        mockMvc.perform(get("/api/my-endpoint")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.field").value("expected"));
    }
}
```

---

## Integration Testing

### Manual API Testing

```bash
# 1. Start the application
docker-compose up -d

# 2. Test login
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# 3. Save the token
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# 4. Test protected endpoint
curl http://localhost:8081/api/users \
  -H "Authorization: Bearer $TOKEN"

# 5. Test create user
curl -X POST http://localhost:8081/api/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username":"newuser",
    "password":"password123",
    "fullName":"New User",
    "email":"new@gym.com",
    "phone":"0123456789",
    "role":"MEMBER"
  }'
```

### Postman Collection

Import `postman_collection.json` (create this):

```json
{
  "info": {
    "name": "Auth Module API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Login",
      "request": {
        "method": "POST",
        "header": [],
        "body": {
          "mode": "raw",
          "raw": "{\"username\":\"admin\",\"password\":\"123456\"}",
          "options": {
            "raw": {
              "language": "json"
            }
          }
        },
        "url": {
          "raw": "http://localhost:8081/api/auth/login",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8081",
          "path": ["api", "auth", "login"]
        }
      }
    }
  ]
}
```

---

## Continuous Integration Testing

Tests run automatically on:
- Every push to `main` or `develop`
- Every pull request
- Via GitHub Actions (`.github/workflows/ci-cd.yml`)

### CI Pipeline

1. **Build**: Compile Java code
2. **Test**: Run all unit tests
3. **Coverage**: Generate coverage report
4. **Report**: Upload to Codecov
5. **Security**: Scan with Trivy

---

## Test Best Practices

### ✅ DO
- Write tests for all new features
- Use descriptive test names
- Test both success and failure cases
- Mock external dependencies
- Use `@BeforeEach` for setup
- Verify method calls with `verify()`
- Test edge cases

### ❌ DON'T
- Test framework code (Spring, JPA)
- Use real database in unit tests
- Write tests that depend on each other
- Hardcode test data
- Skip assertions
- Ignore test failures

---

## Debugging Tests

### Failed Test

```bash
# Run single test with verbose output
mvn test -Dtest=AuthServiceTest#testLogin_Success -X

# Check logs
cat target/surefire-reports/dh13c8.nhom4.gym.service.AuthServiceTest.txt
```

### Test Coverage Gaps

```bash
# Generate coverage report
mvn test jacoco:report

# View report in browser
# target/site/jacoco/index.html

# Check coverage percentage
# Goal: > 80% coverage
```

---

## Performance Testing

### Load Testing with Apache Bench

```bash
# Install Apache Bench (ab)
# Windows: Download from Apache website
# Linux: sudo apt install apache2-utils

# Test login endpoint (100 requests, 10 concurrent)
ab -n 100 -c 10 -T 'application/json' \
  -p login.json \
  http://localhost:8081/api/auth/login

# login.json:
{"username":"admin","password":"123456"}
```

### Expected Results
- **Requests per second**: > 100
- **Average response time**: < 200ms
- **Failed requests**: 0

---

## Security Testing

### OWASP ZAP

```bash
# Run OWASP ZAP scan
docker run -t owasp/zap2docker-stable zap-baseline.py \
  -t http://localhost:8081
```

### SQL Injection Testing

```bash
# Test with malicious input
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin\" OR \"1\"=\"1","password":"anything"}'

# Expected: 401 Unauthorized (not vulnerable)
```

---

## Troubleshooting

### Tests fail with database error

**Solution**: Check `application-test.properties` uses H2, not MySQL

### Tests fail with JWT error

**Solution**: Verify `jwt.secret` is set in test properties

### Tests pass locally but fail in CI

**Solution**: Check Docker environment variables

### MockMvc returns 401 for protected endpoints

**Solution**: Add `@WithMockUser` annotation

---

## Next Steps

1. **Increase Coverage**: Aim for 90%+ code coverage
2. **Integration Tests**: Add tests that use real database
3. **E2E Tests**: Selenium/Cypress for frontend
4. **Performance Tests**: JMeter/Gatling for load testing
5. **Security Tests**: Regular vulnerability scans

---

## Resources

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [AssertJ Documentation](https://assertj.github.io/doc/)

---

**Happy Testing! 🧪**
