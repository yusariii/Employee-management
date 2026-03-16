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

Nếu MySQL không giống cấu hình mặc định, thay đổi trong [src/main/resources/application.properties](src/main/resources/application.properties):

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

### Cấu hình SMTP (Gmail)

Cấu hình mail nằm trong [src/main/resources/application.properties](src/main/resources/application.properties) và đọc từ biến môi trường:

- `EMAIL_USER`
- `EMAIL_PASS` — khuyến nghị dùng **Gmail App Password**
- `MAIL_FROM` (tuỳ chọn) — nếu không set thì mặc định dùng `MAIL_USERNAME`

Với Gmail, bạn thường cần:

1) Bật 2-Step Verification cho tài khoản Gmail
2) Tạo App Password (16 ký tự) và dùng nó làm `MAIL_PASSWORD`

Ví dụ `.env` (tham khảo file [.env.example](.env.example)):

```env
MAIL_USERNAME=your.email@gmail.com
MAIL_PASSWORD=xxxx xxxx xxxx xxxx
MAIL_FROM=Employee Management <your.email@gmail.com>
```

Gợi ý khi chạy local:

- Nếu chạy bằng IDE/Maven: set env vars trong cấu hình Run.
- Nếu chạy Docker Compose: truyền env vars vào container (tuỳ theo cách bạn chạy).
