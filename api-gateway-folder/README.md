# API Gateway - GymFlow Microservices

## Mô tả
API Gateway đóng vai trò entry point duy nhất cho hệ thống, routing requests đến các microservices tương ứng.

## Cấu hình
- **Port:** 8080
- **Framework:** Spring Cloud Gateway
- **Spring Boot:** 3.2.0

## Yêu cầu
- Java 17+
- Maven 3.6+

## Cách chạy
```bash
mvn clean install
mvn spring-boot:run
```

## Routing Configuration

Gateway routing requests đến các service:
- `/api/auth/**` → auth-service (8081)
- `/api/plans/**` → membership-service (8082)
- `/api/memberships/**` → membership-service (8082)
- `/api/payments/**` → membership-service (8082)
- `/api/trainers/**` → operations-service (8083)
- `/api/schedules/**` → operations-service (8083)
- `/api/checkins/**` → operations-service (8083)
- `/api/reports/**` → report-service (8084)
- `/api/public/**` → report-service (8084)

## Security
- Block tất cả requests đến `/internal/**` (403 Forbidden)
- CORS configuration cho frontend
- RewritePath để strip prefix

## Tính năng
- Centralized routing
- Load balancing
- CORS handling
- Security filtering
- Path rewriting
