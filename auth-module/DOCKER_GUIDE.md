# 🐳 Docker Deployment Guide

## Prerequisites

- Docker Desktop 4.0+ installed
- Docker Compose V2 installed
- At least 4GB RAM available
- Ports 80, 8081, 3307 available

## Quick Start

### 1. Build and Run with Docker Compose

```bash
# Clone the repository
cd auth-module

# Copy environment template
cp .env.example .env

# Edit .env with your values (optional)
# nano .env

# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f
```

### 2. Access the Application

- **Frontend**: http://localhost
- **API**: http://localhost:8081
- **Health Check**: http://localhost:8081/actuator/health
- **MySQL**: localhost:3307

### 3. Test Login

```bash
# Test with admin account
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

## Build Docker Image Only

```bash
# Build image
docker build -t auth-service:1.0.0 .

# Run container
docker run -d \
  -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/auth_db \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=yourpassword \
  -e JWT_SECRET=YourSecretKey \
  --name auth-service \
  auth-service:1.0.0
```

## Docker Compose Commands

```bash
# Start services
docker-compose up -d

# Stop services
docker-compose down

# Stop and remove volumes (⚠️ deletes database data)
docker-compose down -v

# View logs
docker-compose logs -f auth-service
docker-compose logs -f mysql

# Restart specific service
docker-compose restart auth-service

# Scale auth service (3 instances)
docker-compose up -d --scale auth-service=3

# Check service status
docker-compose ps

# Execute command in container
docker-compose exec auth-service bash
docker-compose exec mysql mysql -u root -p
```

## Environment Variables

Create `.env` file from `.env.example`:

```env
# MySQL Configuration
MYSQL_ROOT_PASSWORD=strongrootpassword
MYSQL_USER=gymuser
MYSQL_PASSWORD=strongpassword

# JWT Configuration
JWT_SECRET=YourVerySecure256BitKeyHere

# Spring Profile
SPRING_PROFILES_ACTIVE=prod

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:80,https://yourdomain.com
```

## Service Architecture

```
┌─────────────────┐
│  Nginx (Port 80)│
│  - Frontend     │
│  - Reverse Proxy│
└────────┬────────┘
         │
         ▼
┌─────────────────────────┐
│ Auth Service (Port 8081)│
│ - Spring Boot           │
│ - JWT Authentication    │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│  MySQL (Port 3307)      │
│  - auth_db              │
│  - Persistent Volume    │
└─────────────────────────┘
```

## Health Checks

Docker Compose includes health checks:

- **MySQL**: `mysqladmin ping` every 10s
- **Auth Service**: HTTP GET `/actuator/health` every 30s
- **Nginx**: Depends on auth-service health

## Troubleshooting

### Service won't start

```bash
# Check logs
docker-compose logs auth-service

# Check if port is available
netstat -ano | findstr :8081

# Restart services
docker-compose restart
```

### Database connection failed

```bash
# Wait for MySQL to be ready
docker-compose logs mysql

# Verify MySQL is running
docker-compose exec mysql mysql -u root -p -e "SHOW DATABASES;"

# Check network
docker network inspect auth-module_auth-network
```

### Frontend can't connect to backend

```bash
# Check Nginx configuration
docker-compose exec nginx nginx -t

# Check backend health
curl http://localhost:8081/actuator/health

# View Nginx logs
docker-compose logs nginx
```

### Reset everything

```bash
# Stop and remove all
docker-compose down -v

# Remove images
docker rmi auth-module-auth-service

# Start fresh
docker-compose up -d --build
```

## Production Deployment

### 1. Build optimized image

```bash
# Use multi-stage build
docker build -t your-registry/auth-service:1.0.0 .

# Push to registry
docker push your-registry/auth-service:1.0.0
```

### 2. Use external MySQL

```yaml
# docker-compose.prod.yml
services:
  auth-service:
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://your-mysql-server:3306/auth_db
      SPRING_DATASOURCE_USERNAME: ${DB_USER}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
```

### 3. SSL/TLS with Nginx

```bash
# Add certificates to nginx.conf
server {
    listen 443 ssl;
    ssl_certificate /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;
}
```

### 4. Use secrets management

```bash
# Use Docker secrets instead of environment variables
docker secret create jwt_secret jwt_secret.txt
docker secret create db_password db_password.txt
```

## Performance Tuning

### Java Options

```yaml
# docker-compose.yml
environment:
  JAVA_OPTS: >
    -Xms512m
    -Xmx1024m
    -XX:+UseG1GC
    -XX:MaxGCPauseMillis=200
```

### MySQL Optimization

```yaml
# docker-compose.yml
command:
  - --max_connections=200
  - --innodb_buffer_pool_size=1G
  - --query_cache_size=64M
```

## Monitoring

```bash
# View resource usage
docker stats

# Export metrics to Prometheus
curl http://localhost:8081/actuator/prometheus
```

## Backup and Restore

### Backup

```bash
# Backup MySQL data
docker-compose exec mysql mysqldump -u root -p auth_db > backup.sql

# Backup volume
docker run --rm -v auth-module_mysql_data:/data -v $(pwd):/backup \
  busybox tar czf /backup/mysql-backup.tar.gz /data
```

### Restore

```bash
# Restore from SQL dump
docker-compose exec -T mysql mysql -u root -p auth_db < backup.sql

# Restore from volume backup
docker run --rm -v auth-module_mysql_data:/data -v $(pwd):/backup \
  busybox tar xzf /backup/mysql-backup.tar.gz -C /
```

## Next Steps

- Set up CI/CD pipeline (see `.github/workflows/ci-cd.yml`)
- Deploy to Kubernetes (see `k8s/` directory)
- Configure monitoring and alerting
- Set up log aggregation

## Support

For issues, check:
- Docker logs: `docker-compose logs`
- Application logs: `docker-compose exec auth-service cat logs/spring.log`
- Health endpoint: http://localhost:8081/actuator/health
