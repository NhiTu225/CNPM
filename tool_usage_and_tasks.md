# Báo cáo phân công công việc và công cụ sử dụng (Group Assignment & Tool Evidence)
## Dự án: Hệ thống quản lý đặt phòng khách sạn (Hotel Booking System)
## Nhóm: Nhóm XY

Tài liệu này ghi lại thông tin phân chia công việc cho các thành viên trong nhóm và các công cụ được sử dụng trong quá trình làm đồ án môn học.

---

## 1. Phân chia công việc thành viên (Task Assignment)

*   **Member 1 (Leader):** brainstorming ideas, supporting team members, building database schema, connecting with Database (MySQL/SQL Server), and developing login, registration, and role management.
*   **Member 2:** designing the customer interface (UI/FXML) and implementing the room search and availability display features.
*   **Member 3:** implementing the online room booking and cancellation features, and handling payment transaction logic.
*   **Member 4:** building the receptionist dashboard interface, handling reservation updates (check-in/check-out), and setting up automated email confirmations and reminders.
*   **Member 5:** designing the admin interface for room & pricing management, coding the CRUD operations for room details, and generating invoice/receipt PDFs.
*   **Member 6:** coding the statistical/analytic queries for revenue and occupancy reports, building charts for the admin dashboard, and researching & executing testing tools.

*\*Mọi người trong nhóm hỗ trợ lẫn nhau trong quá trình làm việc.*

---

## 2. Các công cụ sử dụng trong dự án (Tool Usage)

Nhóm đã sử dụng các công cụ sau để hoàn thành đồ án:

### Quản lý phiên bản (Git & GitHub)
Nhóm dùng Git và GitHub để quản lý mã nguồn và làm việc nhóm:
*   Khởi tạo Git ban đầu:
    ```bash
    git init
    git branch -M main
    ```
*   Thêm file và commit code:
    ```bash
    git add .
    git commit -m "Initial commit - Hotel Booking System project setup"
    ```
*   Đẩy code lên repository của nhóm trên GitHub:
    ```bash
    git remote add origin https://github.com/NhiTu225/CNPM
    git push -u origin main
    ```

### Quản lý thư viện và Build dự án (Apache Maven)
Dự án được quản lý bằng Maven để tự động tải các thư viện cần thiết như JavaFX và driver kết nối SQL Server/MySQL:
*   File cấu hình: `pom.xml`
*   Lệnh biên dịch thử:
    ```bash
    mvn compile
    ```
*   Lệnh build ứng dụng ra file jar:
    ```bash
    mvn clean package -DskipTests
    ```

### Cơ sở dữ liệu (SQL Server / MySQL)
*   Sử dụng kịch bản SQL ở đường dẫn `src/main/resources/sql/hotel.sql` để tạo bảng và nhập dữ liệu ban đầu cho các bảng `users`, `rooms`, `bookings`, `payments`, `invoices`.
*   Sử dụng Java JDBC (`DBConnection.java`) để kết nối từ Java app sang cơ sở dữ liệu.

### Đóng gói ứng dụng (Docker)
*   Viết file `Dockerfile` ở thư mục gốc để đóng gói ứng dụng chạy độc lập trên mọi máy.
