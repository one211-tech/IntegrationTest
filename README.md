 Build Docker Images

Before running the full stack, build Docker images for each service:

# 1. SQL Controller & Executor

## Clone the repository
git clone https://github.com/one211-tech/sql.git

### Build the Docker Image
- Instructions to build the Docker image are available in the repository’s README.

# 2. Frontend (React App)

# Clone the repository
git clone https://github.com/admin-one211/application-frontend.git

### Build the Docker Image
- Instructions to build the Docker image are available in the repository’s README.
  
# 3. Backend (Spring Boot Application)

## Clone the repository
git clone https://github.com/admin-one211/application.git

### Build the Docker Image
- Instructions to build the Docker image are available in the repository’s README.


### After building all images, you can start the entire system using Docker Compose:
```bash
* docker compose up -d
```

### This will launch:
PostgreSQL database,
Spring Boot backend,
React frontend,
Flight SQL Controller & Executor

## Run Integration Tests (Cluster Testing)

After building and starting all services with Docker Compose, you can verify the setup by running the integration test for cluster functionality.
### If no cluster exists yet:
```bash
* mvn -Dtest=com.one211.IntegrationTest.service.SetupForIntegrationTest
```
### If a test cluster is already present, skip SetupForIntegrationTest.java and instead run:
```bash
* mvn -Dtest=com.one211.IntegrationTest.service.IntegrationTest
```

