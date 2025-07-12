# Peti Backend Application

This guide walks you through building and running the Peti backend application using Docker and Docker Compose.

## Prerequisites

- Docker Desktop installed and running on your system
- Git (to clone the repository)

## Running the App

Follow these steps to build and run the application using Docker.

### 1. Build the Docker Image

In the project root directory, run the following command:

```bash
docker build -t peti/backend:v1.1 .
```

> **ℹ️ Note:** You can replace `v1.1` with any other tag you prefer.

### 2. Update Docker Compose Configuration

Open your `docker-compose.yml` file and update the image field under the app service to match the tag you used:

```yaml
app:
  image: peti/backend:v1.1
```

### 3. Check Port Availability

Ensure the following ports are not already in use on your machine:

- **5437** (PostgreSQL database)
- **8083** (Application)

**On Windows:**
You can check this by:
1. Press `Win + R`
2. Type `resmon.exe` and hit Enter
3. Go to the Network tab and look for ports in use

**On macOS/Linux:**
```bash
# Check if ports are in use
lsof -i :5437
lsof -i :8083
```

### 4. Start the Application

Run the following command:

```bash
docker compose up -d
```

This will start all the services in detached mode.

### 5. Verify the Setup

Once all services are up, test the application by navigating to:

```
http://localhost:8083/api/ping
```

You should receive an `ok` response, indicating the setup is working correctly.

## Service Information

### Application
- **URL:** http://localhost:8083
- **Health Check:** http://localhost:8083/api/ping

### Database
- **Host:** localhost
- **Port:** 5437
- **Database:** peti
- **Username:** seller
- **Password:** 1111

## Troubleshooting

### Common Issues

**Port Already in Use:**
If you encounter port conflicts, either:
- Stop the conflicting service
- Modify the port mappings in `docker-compose.yml`

**Database Connection Issues:**
- Ensure PostgreSQL container is healthy: `docker compose ps`
- Check logs: `docker compose logs postgres`

**Application Won't Start:**
- Check application logs: `docker compose logs app`
- Verify the Docker image was built successfully

### Useful Commands

```bash
# View running containers
docker compose ps

# View logs for all services
docker compose logs

# View logs for specific service
docker compose logs app
docker compose logs postgres

# Stop all services
docker compose down

# Rebuild and restart
docker compose down
docker build -t peti/backend:v1.1 .
docker compose up -d
```

## Development

### Database Access

You can connect to the PostgreSQL database using any PostgreSQL client:

- **Connection String:** `postgresql://petiuser:petipassword@localhost:5437/petidb`

### API Documentation

Once the application is running, you can access:
- API endpoints at `http://localhost:8083`
- Health check at `http://localhost:8083/api/ping`
- Swagger `http://localhost:8082/swagger-ui/index.html`




### Info to read to get known with project set up

- Spring security configuration: https://medium.com/@minadev/authentication-and-authorization-with-spring-security-bf22e985f2cb
- https://medium.com/@ihor.polataiko/spring-security-guide-part-1-introduction-c2709ff1bd98
- https://www.baeldung.com/spring-deprecated-websecurityconfigureradapter
- 


### Service Specification
For countries code uses ISO 3166-1 alpha-2 standard.
