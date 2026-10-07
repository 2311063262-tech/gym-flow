# ☸️ Kubernetes Deployment Guide

## Prerequisites

- Kubernetes cluster (v1.24+)
- kubectl configured
- Docker registry access
- At least 4GB RAM, 2 CPU available

## Quick Deploy

```bash
# 1. Create namespace and secrets
kubectl apply -f k8s/secrets.yaml

# 2. Create ConfigMaps
kubectl apply -f k8s/configmap.yaml

# 3. Deploy all services
kubectl apply -f k8s/deployment.yaml

# 4. Check status
kubectl get pods -n gym-auth
kubectl get services -n gym-auth
```

## Architecture

```
┌──────────────────────────────────┐
│   LoadBalancer (External IP)     │
│         Port 80                  │
└────────────┬─────────────────────┘
             │
             ▼
┌────────────────────────────────────┐
│   Auth Service (3 replicas)        │
│   - Spring Boot                    │
│   - JWT Authentication             │
│   - Auto-scaling (2-10 pods)       │
└────────────┬───────────────────────┘
             │
             ▼
┌────────────────────────────────────┐
│   MySQL StatefulSet                │
│   - Persistent Volume              │
│   - 10GB Storage                   │
└────────────────────────────────────┘
```

## Step-by-Step Deployment

### 1. Prepare Docker Image

```bash
# Build and push image
docker build -t your-registry/auth-service:1.0.0 .
docker push your-registry/auth-service:1.0.0

# Update k8s/deployment.yaml with your image
# image: your-registry/auth-service:1.0.0
```

### 2. Create Namespace

```bash
kubectl create namespace gym-auth

# Or apply from secrets.yaml (includes namespace)
kubectl apply -f k8s/secrets.yaml
```

### 3. Configure Secrets

**Option A: Use provided secrets (NOT for production)**
```bash
kubectl apply -f k8s/secrets.yaml
```

**Option B: Create your own secrets (RECOMMENDED)**
```bash
# Generate secure passwords
MYSQL_ROOT_PASSWORD=$(openssl rand -base64 16)
MYSQL_PASSWORD=$(openssl rand -base64 16)
JWT_SECRET=$(openssl rand -base64 32)

# Create secrets
kubectl create secret generic mysql-secret \
  --from-literal=root-password=$MYSQL_ROOT_PASSWORD \
  --from-literal=username=gymuser \
  --from-literal=password=$MYSQL_PASSWORD \
  -n gym-auth

kubectl create secret generic jwt-secret \
  --from-literal=secret=$JWT_SECRET \
  -n gym-auth
```

### 4. Apply ConfigMaps

```bash
kubectl apply -f k8s/configmap.yaml

# Verify
kubectl get configmap -n gym-auth
```

### 5. Deploy Services

```bash
# Deploy MySQL StatefulSet
kubectl apply -f k8s/deployment.yaml

# Wait for MySQL to be ready
kubectl wait --for=condition=ready pod/mysql-0 -n gym-auth --timeout=300s

# Deploy Auth Service
kubectl apply -f k8s/deployment.yaml

# Check deployment
kubectl rollout status deployment/auth-service -n gym-auth
```

### 6. Verify Deployment

```bash
# Check pods
kubectl get pods -n gym-auth

# Expected output:
# NAME                            READY   STATUS    RESTARTS   AGE
# auth-service-xxx-yyy            1/1     Running   0          2m
# auth-service-xxx-zzz            1/1     Running   0          2m
# auth-service-xxx-www            1/1     Running   0          2m
# mysql-0                         1/1     Running   0          5m

# Check services
kubectl get svc -n gym-auth

# Check logs
kubectl logs -f deployment/auth-service -n gym-auth
kubectl logs -f mysql-0 -n gym-auth
```

### 7. Access the Application

```bash
# Get external IP (LoadBalancer)
kubectl get svc auth-service -n gym-auth

# Wait for EXTERNAL-IP to be assigned
# NAME           TYPE           EXTERNAL-IP      PORT(S)
# auth-service   LoadBalancer   35.123.45.67     80:32000/TCP

# Test the service
EXTERNAL_IP=$(kubectl get svc auth-service -n gym-auth -o jsonpath='{.status.loadBalancer.ingress[0].ip}')

curl -X POST http://$EXTERNAL_IP/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

## Scaling

### Manual Scaling

```bash
# Scale to 5 replicas
kubectl scale deployment auth-service --replicas=5 -n gym-auth

# Verify
kubectl get pods -n gym-auth
```

### Auto-Scaling (HPA)

Auto-scaling is configured in `k8s/deployment.yaml`:
- **Min replicas**: 2
- **Max replicas**: 10
- **CPU target**: 70%
- **Memory target**: 80%

```bash
# Check HPA status
kubectl get hpa -n gym-auth

# View HPA details
kubectl describe hpa auth-service-hpa -n gym-auth

# Example output:
# NAME                REFERENCE                  TARGETS         MINPODS   MAXPODS   REPLICAS
# auth-service-hpa    Deployment/auth-service    25%/70%         2         10        3
```

## Monitoring

### Health Checks

```bash
# Check liveness
kubectl exec -n gym-auth deployment/auth-service -- \
  wget -qO- http://localhost:8081/actuator/health/liveness

# Check readiness
kubectl exec -n gym-auth deployment/auth-service -- \
  wget -qO- http://localhost:8081/actuator/health/readiness
```

### Logs

```bash
# View logs from all pods
kubectl logs -f deployment/auth-service -n gym-auth --all-containers=true

# View logs from specific pod
kubectl logs -f auth-service-xxx-yyy -n gym-auth

# View MySQL logs
kubectl logs -f mysql-0 -n gym-auth

# Stream logs in real-time
kubectl logs -f deployment/auth-service -n gym-auth --tail=100
```

### Metrics

```bash
# Pod resource usage
kubectl top pods -n gym-auth

# Node resource usage
kubectl top nodes

# Prometheus metrics endpoint
kubectl port-forward deployment/auth-service 8081:8081 -n gym-auth
curl http://localhost:8081/actuator/prometheus
```

## Updates & Rollouts

### Update Image

```bash
# Update to new version
kubectl set image deployment/auth-service \
  auth-service=your-registry/auth-service:1.1.0 \
  -n gym-auth

# Watch rollout
kubectl rollout status deployment/auth-service -n gym-auth

# Check rollout history
kubectl rollout history deployment/auth-service -n gym-auth
```

### Rollback

```bash
# Rollback to previous version
kubectl rollout undo deployment/auth-service -n gym-auth

# Rollback to specific revision
kubectl rollout undo deployment/auth-service --to-revision=2 -n gym-auth

# Verify
kubectl rollout status deployment/auth-service -n gym-auth
```

### Zero-Downtime Updates

Rolling update is configured by default:
- Max unavailable: 25%
- Max surge: 25%

```yaml
strategy:
  type: RollingUpdate
  rollingUpdate:
    maxUnavailable: 1
    maxSurge: 1
```

## Troubleshooting

### Pods not starting

```bash
# Describe pod to see events
kubectl describe pod <pod-name> -n gym-auth

# Check logs
kubectl logs <pod-name> -n gym-auth --previous

# Common issues:
# - ImagePullBackOff: Check image name and registry credentials
# - CrashLoopBackOff: Check application logs
# - Pending: Check resource availability
```

### Database connection failed

```bash
# Check MySQL pod
kubectl logs mysql-0 -n gym-auth

# Test connection from auth-service pod
kubectl exec -it deployment/auth-service -n gym-auth -- \
  sh -c 'apt update && apt install -y mysql-client && mysql -h mysql-service -u gymuser -p'

# Check secrets
kubectl get secret mysql-secret -n gym-auth -o yaml
```

### Service not accessible

```bash
# Check service
kubectl describe svc auth-service -n gym-auth

# Check endpoints
kubectl get endpoints auth-service -n gym-auth

# Port forward for testing
kubectl port-forward service/auth-service 8081:80 -n gym-auth
curl http://localhost:8081/actuator/health
```

### High memory usage

```bash
# Check resource usage
kubectl top pods -n gym-auth

# Increase memory limits in deployment.yaml
resources:
  limits:
    memory: "2Gi"  # Increase from 1Gi

# Apply changes
kubectl apply -f k8s/deployment.yaml
```

## Backup & Restore

### Backup MySQL

```bash
# Create backup
kubectl exec mysql-0 -n gym-auth -- \
  mysqldump -u root -p$MYSQL_ROOT_PASSWORD auth_db > backup.sql

# Or use a Job
cat <<EOF | kubectl apply -f -
apiVersion: batch/v1
kind: Job
metadata:
  name: mysql-backup
  namespace: gym-auth
spec:
  template:
    spec:
      containers:
      - name: backup
        image: mysql:8.0
        command:
        - sh
        - -c
        - |
          mysqldump -h mysql-service -u root -p\$MYSQL_ROOT_PASSWORD auth_db > /backup/backup.sql
        volumeMounts:
        - name: backup
          mountPath: /backup
      restartPolicy: Never
      volumes:
      - name: backup
        persistentVolumeClaim:
          claimName: backup-pvc
EOF
```

### Restore MySQL

```bash
# Restore from backup
kubectl exec -i mysql-0 -n gym-auth -- \
  mysql -u root -p$MYSQL_ROOT_PASSWORD auth_db < backup.sql
```

## Cleanup

### Delete Everything

```bash
# Delete all resources
kubectl delete -f k8s/deployment.yaml
kubectl delete -f k8s/configmap.yaml
kubectl delete -f k8s/secrets.yaml

# Or delete namespace (removes everything)
kubectl delete namespace gym-auth
```

### Delete Specific Resources

```bash
# Delete deployment only
kubectl delete deployment auth-service -n gym-auth

# Delete service only
kubectl delete service auth-service -n gym-auth

# Keep PVC for data persistence
kubectl get pvc -n gym-auth
```

## Production Considerations

### Security

- [ ] Use private Docker registry
- [ ] Enable RBAC (Role-Based Access Control)
- [ ] Use Network Policies
- [ ] Rotate secrets regularly
- [ ] Enable Pod Security Policies
- [ ] Use service mesh (Istio/Linkerd)

### High Availability

- [ ] Multi-zone deployment
- [ ] MySQL replication (master-slave)
- [ ] Backup automation
- [ ] Disaster recovery plan
- [ ] Monitoring & alerting

### Performance

- [ ] Use PodDisruptionBudget
- [ ] Configure resource requests/limits
- [ ] Enable horizontal pod autoscaling
- [ ] Use Redis for caching
- [ ] CDN for static assets

## Advanced Configuration

### Ingress (instead of LoadBalancer)

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: auth-ingress
  namespace: gym-auth
spec:
  rules:
  - host: auth.gym.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: auth-service
            port:
              number: 80
```

### TLS/SSL

```bash
# Create TLS secret
kubectl create secret tls auth-tls \
  --cert=cert.pem \
  --key=key.pem \
  -n gym-auth

# Add to ingress
spec:
  tls:
  - hosts:
    - auth.gym.example.com
    secretName: auth-tls
```

## Resources

- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [kubectl Cheat Sheet](https://kubernetes.io/docs/reference/kubectl/cheatsheet/)
- [Horizontal Pod Autoscaler](https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale/)
- [StatefulSets](https://kubernetes.io/docs/concepts/workloads/controllers/statefulset/)

---

**Status**: ✅ Production-Ready
**Last Updated**: 2026-09-22
