 Build Docker Images

Before running the full stack, build Docker images for each service:

# 1. SQL Controller & Executor

## Clone the repository
git clone https://github.com/one211-tech/sql.git

## Build the image
docker build -t flight-sql-duckdb:latest .

# 2. Frontend (React App)

# Clone the repository
git clone https://github.com/admin-one211/application-frontend.git

## Build the image
docker build -t react-app:latest .

# 3. Backend (Spring Boot Application)

## Clone the repository
git clone https://github.com/admin-one211/application.git

## Build the image
docker build -t spring-application:latest .


After building all images, you can start the entire system using Docker Compose:

docker compose up -d


### This will launch:
*PostgreSQL database
*Spring Boot backend
*React frontend
*Flight SQL Controller & Executor

## Run Integration Tests (Cluster Testing)

After building and starting all services with Docker Compose, you can verify the setup by running the integration test for cluster functionality.
### If no cluster exists yet:
* mvn -Dtest=com.one211.restapi.service.IntegrationTest test
### If a test cluster is already present, skip IntegrationTest.java and instead run:
* mvn -Dtest=com.one211.restapi.service.ControllerIntegrationTest test

