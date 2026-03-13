# Employee-management

Backend quản lý nhân sự (employee/leave/payroll) viết bằng Spring Boot + Spring Security (JWT) + Spring Data JPA.

## Công nghệ

- Java 21
- Spring Boot
- Spring Web, Spring Security (JWT)
- Spring Data JPA (MySQL)
- Docker / Docker Compose (tuỳ chọn)

## Yêu cầu

- JDK 21
- Maven (hoặc dùng `mvnw`/`mvnw.cmd`)
- MySQL (local) **hoặc** Docker Desktop

## Cấu hình DB

Mặc định app dùng MySQL local theo [src/main/resources/application.properties](src/main/resources/application.properties):

- DB: `employee_db`
- Host/port: `localhost:3306`
- User: `root`
- Password: *(trống)*

Bạn có thể override bằng biến môi trường khi chạy (không cần sửa file):

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

## Chạy bằng Docker Compose (khuyến nghị nếu bạn muốn có sẵn MySQL)

1) Build JAR trước (vì [Dockerfile](Dockerfile) copy từ `target/*.jar`):

```bash
mvn -DskipTests clean package
```

2) Start containers:

```bash
docker compose up -d --build
```

- Backend: `http://localhost:8080`
- MySQL được map ra host port `3307` (xem [docker-compose.yml](docker-compose.yml)).

## Chạy local (không dùng Docker)

1) Tạo database `employee_db` trong MySQL local.

2) Chạy ứng dụng:

```bash
mvn spring-boot:run
```

Nếu MySQL local của bạn không giống cấu hình mặc định, chạy kiểu override:

```bash
SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3307/employee_db" \
SPRING_DATASOURCE_USERNAME="root" \
SPRING_DATASOURCE_PASSWORD="root" \
mvn spring-boot:run
```

## Tài khoản mặc định

Lần đầu DB trống, app sẽ seed user mặc định (xem [src/main/java/com/khai/em/config/DatabaseSeeder.java](src/main/java/com/khai/em/config/DatabaseSeeder.java)):

- `admin` / `admin123`
- `manager` / `manager123`
- `employee` / `employee123`

## API nhanh

Auth endpoints (xem [src/main/java/com/khai/em/controller/AuthController.java](src/main/java/com/khai/em/controller/AuthController.java)):

- `POST /api/auth/login`
- `GET /api/auth/me`
- `POST /api/auth/register`

Ví dụ login:

```bash
curl -X POST http://localhost:8080/api/auth/login \
	-H "Content-Type: application/json" \
	-d "{\"username\":\"admin\",\"password\":\"admin123\"}"
```

Sau đó dùng token JWT (Bearer) để gọi các API còn lại.
