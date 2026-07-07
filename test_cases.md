# Tài liệu Kịch bản Kiểm thử (Test Cases)
## Dự án: Hotel Booking System (Hệ thống đặt phòng khách sạn)
## Dữ liệu Kiểm thử Trích xuất Từ Cơ sở Dữ liệu Thực tế (HotelBookingSystem)

Tài liệu này đặc tả chi tiết các kịch bản kiểm thử (Test Cases) ánh xạ trực tiếp đến cấu trúc các bảng (users, rooms, bookings, payments, invoices) và dữ liệu mẫu hiện có trong cơ sở dữ liệu.

---

## Cấu trúc Cơ sở Dữ liệu Thực tế Sử dụng Kiểm thử

*   **Bảng users**: Cột id (INT), full_name (NVARCHAR), email (VARCHAR), password (VARCHAR), role (VARCHAR: 'CUSTOMER', 'ADMIN', 'RECEPTIONIST').
*   **Bảng rooms**: Cột id (INT), room_number (VARCHAR), room_name (NVARCHAR), room_type (NVARCHAR), price (DECIMAL), image (VARCHAR), status (VARCHAR: 'AVAILABLE', 'BOOKED', 'OCCUPIED').
*   **Bảng bookings**: Cột id (INT), user_id (INT), room_id (INT), check_in (DATE), check_out (DATE), total (DECIMAL), status (VARCHAR: 'BOOKED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED').
*   **Bảng payments**: Cột id (INT), booking_id (INT), payment_method (NVARCHAR), amount (DECIMAL), payment_date (DATETIME).
*   **Bảng invoices**: Cột id (INT), booking_id (INT), invoice_date (DATETIME), total (DECIMAL).

---

## Chi tiết Kịch bản Kiểm thử (Test Cases Details)

### FR01: Đăng ký người dùng (User Registration)
*Ánh xạ trực tiếp đến bảng users*

#### TC_FR01_01: Đăng ký tài khoản khách hàng mới thành công (Positive)
*   **Tiền điều kiện:** Email đăng ký chưa tồn tại trong bảng users.
*   **Các bước thực hiện:**
    1. Trên giao diện đăng ký, nhập thông tin:
       * Họ và tên: Nguyen Van Customer
       * Email: newcustomer@gmail.com
       * Mật khẩu: 123456
    2. Nhấn nút "Đăng ký".
*   **Kết quả mong đợi:**
    *   Hệ thống thực hiện thành công câu lệnh:
        ```sql
        INSERT INTO users (full_name, email, password, role) 
        VALUES (N'Nguyen Van Customer', 'newcustomer@gmail.com', '123456', 'CUSTOMER');
        ```
    *   Một bản ghi mới được thêm vào bảng users.
    *   Hiển thị thông báo "Đăng ký thành công".

#### TC_FR01_02: Đăng ký thất bại do trùng Email đã tồn tại (Negative)
*   **Tiền điều kiện:** Trong bảng users đã có bản ghi có email customer@gmail.com.
*   **Các bước thực hiện:**
    1. Nhập thông tin đăng ký với email: customer@gmail.com.
    2. Nhấn "Đăng ký".
*   **Kết quả mong đợi:**
    *   Hệ thống chặn gửi yêu cầu hoặc Database quăng lỗi trùng khóa duy nhất (UNIQUE KEY constraint violation trên cột email).
    *   Hiển thị thông báo lỗi: "Email đã tồn tại".

#### TC_FR01_03: Đăng nhập thành công (Positive)
*   **Các bước thực hiện:**
    1. Nhập Email: customer@gmail.com và Mật khẩu: 123456.
    2. Nhấn "Đăng nhập".
*   **Kết quả mong đợi:**
    *   Hệ thống thực thi câu lệnh SQL:
        ```sql
        SELECT * FROM users WHERE email = 'customer@gmail.com' AND password = '123456';
        ```
    *   Trả về thông tin người dùng có role = 'CUSTOMER', đăng nhập thành công và chuyển hướng đến trang dành riêng cho Khách hàng.

---

### FR02: Quản lý vai trò (Role Management)
*Ánh xạ đến cột role trong bảng users*

#### TC_FR02_01: Chặn khách hàng truy cập giao diện quản lý Admin (Security)
*   **Tiền điều kiện:** Tài khoản đăng nhập hiện tại có role = 'CUSTOMER' (Ví dụ: customer@gmail.com).
*   **Các bước thực hiện:**
    1. Cố tình truy cập trực tiếp vào AdminView.fxml hoặc lớp điều khiển AdminController.
*   **Kết quả mong đợi:**
    *   Hệ thống kiểm tra vai trò người dùng hiện tại và từ chối hiển thị giao diện, ghi nhật ký lỗi bảo mật.

---

### FR03 & FR04: Tìm kiếm & Hiển thị phòng trống (Room Search & Availability)
*Ánh xạ trực tiếp đến bảng rooms và bookings*

#### TC_FR03_01: Tìm phòng Standard trống trong khoảng thời gian hợp lệ (Positive)
*   **Tiền điều kiện:** Bảng rooms có phòng số 101 (room_type = N'Standard', price = 500000.00, status = 'AVAILABLE').
*   **Các bước thực hiện:**
    1. Chọn loại phòng: Standard.
    2. Chọn ngày nhận phòng: 2026-10-10, ngày trả phòng: 2026-10-12.
    3. Nhấn "Tìm phòng".
*   **Kết quả mong đợi:**
    *   Hệ thống kiểm tra bảng bookings để tìm các phòng loại Standard không có lịch đặt trùng hoặc lịch đặt đó đã bị CANCELLED.
    *   Hiển thị danh sách kết quả chứa phòng 101 với giá 500,000 VND / đêm và trạng thái phòng hiển thị trực quan là trống (AVAILABLE).

---

### FR05 & FR06: Đặt phòng & Thanh toán (Room Booking & Payment)
*Ánh xạ trực tiếp đến bảng bookings và payments*

#### TC_FR05_01: Khách đặt phòng và thanh toán thành công (Positive)
*   **Tiền điều kiện:** Phòng 101 (id = 1) đang trống, khách hàng customer@gmail.com (id = 3) đã đăng nhập.
*   **Các bước thực hiện:**
    1. Tiến hành đặt phòng 101 từ ngày 2026-10-10 đến 2026-10-12 (2 đêm).
    2. Chọn phương thức thanh toán: Credit Card và thực hiện thanh toán số tiền 1,000,000 VND.
    3. Xác nhận hoàn tất đặt phòng.
*   **Kết quả mong đợi:**
    *   Bảng bookings được thêm bản ghi mới:
        ```sql
        INSERT INTO bookings (user_id, room_id, check_in, check_out, total, status) 
        VALUES (3, 1, '2026-10-10', '2026-10-12', 1000000.00, 'BOOKED');
        ```
    *   Bảng payments được thêm bản ghi ghi nhận giao dịch:
        ```sql
        INSERT INTO payments (booking_id, payment_method, amount, payment_date) 
        VALUES (1, N'Credit Card', 1000000.00, GETDATE());
        ```
    *   Trạng thái phòng 101 trong bảng rooms chuyển từ AVAILABLE sang BOOKED (hoặc OCCUPIED khi khách check-in).

---

### FR07: Tạo hóa đơn (Invoice Generation)
*Ánh xạ trực tiếp đến bảng invoices*

#### TC_FR07_01: Tự động xuất hóa đơn khi thanh toán hoàn tất (Positive)
*   **Tiền điều kiện:** Đơn đặt phòng có id = 1 vừa được thanh toán thành công.
*   **Các bước thực hiện:**
    1. Khách hàng/Lễ tân xác nhận thanh toán.
*   **Kết quả mong đợi:**
    *   Bảng invoices tự động thêm mới một hóa đơn tham chiếu đến booking_id = 1:
        ```sql
        INSERT INTO invoices (booking_id, total) VALUES (1, 1000000.00);
        ```
    *   Hệ thống tải hoặc hiển thị hóa đơn có chứa các trường thông tin: Mã hóa đơn (tự sinh), Mã đặt phòng 1, Ngày tạo hóa đơn (ngày hiện tại), Tổng tiền 1,000,000 VND.

---

### FR08 & FR09: Admin quản lý phòng & giá phòng (Room & Pricing Management)
*Ánh xạ trực tiếp đến bảng rooms*

#### TC_FR08_01: Admin thêm phòng mới thành công (Positive)
*   **Các bước thực hiện:**
    1. Đăng nhập tài khoản admin@gmail.com.
    2. Nhập thông tin phòng mới: Số phòng 402, Tên phòng: Executive Presidential Suite, Loại phòng: Executive, Giá phòng: 4500000.00, Trạng thái: AVAILABLE.
    3. Nhấn "Thêm".
*   **Kết quả mong đợi:**
    *   Thực hiện câu lệnh SQL:
        ```sql
        INSERT INTO rooms (room_number, room_name, room_type, price, status) 
        VALUES ('402', N'Executive Presidential Suite', N'Executive', 4500000.00, 'AVAILABLE');
        ```
    *   Phòng 402 hiển thị trong danh sách quản trị.

---

### FR10: Nghiệp vụ Lễ tân (Reservation Handling)
*Ánh xạ trực tiếp đến bảng bookings và rooms*

#### TC_FR10_01: Lễ tân cập nhật trạng thái khi khách nhận phòng (Check-in) (Positive)
*   **Tiền điều kiện:** Đơn đặt phòng có id = 1 đang ở trạng thái BOOKED, hôm nay là ngày check-in.
*   **Các bước thực hiện:**
    1. Lễ tân nhấn vào nút "Check-in" trên giao diện quản lý đơn đặt phòng id = 1.
*   **Kết quả mong đợi:**
    *   Cập nhật trạng thái trong bảng bookings:
        ```sql
        UPDATE bookings SET status = 'CHECKED_IN' WHERE id = 1;
        ```
    *   Cập nhật trạng thái phòng thực tế trong bảng rooms:
        ```sql
        UPDATE rooms SET status = 'OCCUPIED' WHERE id = (SELECT room_id FROM bookings WHERE id = 1);
        ```

---

### FR13 & FR14: Báo cáo Doanh thu & Tỷ lệ lấp đầy (Reports & Analytics)
*Ánh xạ trực tiếp đến bảng invoices và bookings*

#### TC_FR13_01: Truy vấn doanh thu theo tháng chính xác (Positive)
*   **Các bước thực hiện:**
    1. Admin nhấn vào tab "Báo cáo doanh thu tháng".
*   **Kết quả mong đợi:**
    *   Hệ thống thực thi câu truy vấn SQL:
        ```sql
        SELECT CONVERT(VARCHAR(7), invoice_date, 120) AS Month, SUM(total) AS Total
        FROM invoices
        GROUP BY CONVERT(VARCHAR(7), invoice_date, 120)
        ORDER BY Month ASC;
        ```
    *   Trả về kết quả doanh thu tổng hợp chính xác theo từng tháng và vẽ biểu đồ cột lên UI của Admin.
