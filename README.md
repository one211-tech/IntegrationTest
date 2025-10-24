 Build Docker Images

Before running the full stack, build Docker images for each service:

1 SQL Controller & Executor

Repository: one211-tech/sql

# Clone the repository
git clone https://github.com/one211-tech/sql.git
cd sql

# Build the image
docker build -t flight-sql-duckdb:latest .

2 Frontend (React App)

Repository: admin-one211/application-frontend

# Clone the repository
git clone https://github.com/admin-one211/application-frontend.git
cd application-frontend

# Build the image
docker build -t react-app:latest .

3️⃣ Backend (Spring Boot Application)

Repository: admin-one211/application

# Clone the repository
git clone https://github.com/admin-one211/application.git
cd application

# Build the image
docker build -t spring-application:latest .


After building all images, you can start the entire system using Docker Compose:

docker compose up -d


This will launch:

PostgreSQL database

Spring Boot backend

React frontend

Flight SQL Controller & Executor
