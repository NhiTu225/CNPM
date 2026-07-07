-- Tệp tạo cơ sở dữ liệu tương thích với SQL Server (T-SQL)
-- Tên CSDL: HotelBookingSystem

IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'HotelBookingSystem')
BEGIN
    CREATE DATABASE HotelBookingSystem;
END
GO

USE HotelBookingSystem;
GO

-- Xóa các bảng nếu tồn tại để tránh xung đột khi chạy lại
IF OBJECT_ID('dbo.system_logs', 'U') IS NOT NULL DROP TABLE dbo.system_logs;
IF OBJECT_ID('dbo.invoices', 'U') IS NOT NULL DROP TABLE dbo.invoices;
IF OBJECT_ID('dbo.payments', 'U') IS NOT NULL DROP TABLE dbo.payments;
IF OBJECT_ID('dbo.bookings', 'U') IS NOT NULL DROP TABLE dbo.bookings;
IF OBJECT_ID('dbo.rooms', 'U') IS NOT NULL DROP TABLE dbo.rooms;
IF OBJECT_ID('dbo.users', 'U') IS NOT NULL DROP TABLE dbo.users;
GO

-- 1. Bảng người dùng
CREATE TABLE users (
    id INT IDENTITY(1,1) PRIMARY KEY,
    full_name NVARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('CUSTOMER', 'ADMIN', 'RECEPTIONIST'))
);

-- 2. Bảng phòng
CREATE TABLE rooms (
    id INT IDENTITY(1,1) PRIMARY KEY,
    room_number VARCHAR(20) UNIQUE NOT NULL,
    room_name NVARCHAR(100) NOT NULL,
    room_type NVARCHAR(50) NOT NULL,
    price DECIMAL(18, 2) NOT NULL,
    image VARCHAR(255),
    status VARCHAR(20) DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'BOOKED', 'OCCUPIED'))
);

-- 3. Bảng đặt phòng
CREATE TABLE bookings (
    id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT,
    room_id INT,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    total DECIMAL(18, 2) NOT NULL,
    status VARCHAR(20) CHECK (status IN ('BOOKED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED')),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE SET NULL
);

-- 4. Bảng thanh toán
CREATE TABLE payments (
    id INT IDENTITY(1,1) PRIMARY KEY,
    booking_id INT,
    payment_method NVARCHAR(30) NOT NULL,
    amount DECIMAL(18, 2) NOT NULL,
    payment_date DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE SET NULL
);

-- 5. Bảng hóa đơn (FR07)
CREATE TABLE invoices (
    id INT IDENTITY(1,1) PRIMARY KEY,
    booking_id INT,
    invoice_date DATETIME DEFAULT GETDATE(),
    total DECIMAL(18, 2) NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE SET NULL
);

-- 6. Bảng nhật ký hệ thống (NFR11)
CREATE TABLE system_logs (
    id INT IDENTITY(1,1) PRIMARY KEY,
    user_email VARCHAR(100),
    action NVARCHAR(255) NOT NULL,
    timestamp DATETIME DEFAULT GETDATE()
);
GO

-- Thêm dữ liệu mẫu
INSERT INTO users (full_name, email, password, role)
VALUES
    (N'Administrator', 'admin@gmail.com', '123456', 'ADMIN'),
    (N'Receptionist NV A', 'receptionist@gmail.com', '123456', 'RECEPTIONIST'),
    (N'Nguyen Van Customer', 'customer@gmail.com', '123456', 'CUSTOMER');

INSERT INTO rooms (room_number, room_name, room_type, price, image, status)
VALUES
    -- STANDARD (5 loại, mỗi loại 3 phòng)
    -- 1. Standard Cozy Single (101, 102, 103)
    ('101', N'Standard Cozy Single', N'Standard', 500000.00, 'https://images.unsplash.com/photo-1505691938895-1758d7feb511?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
    ('102', N'Standard Cozy Single', N'Standard', 500000.00, 'https://images.unsplash.com/photo-1505691938895-1758d7feb511?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
    ('103', N'Standard Cozy Single', N'Standard', 500000.00, 'https://images.unsplash.com/photo-1505691938895-1758d7feb511?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
    
    -- 2. Standard Twin Classic (104, 105, 106)
    ('104', N'Standard Twin Classic', N'Standard', 600000.00, 'https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
    ('105', N'Standard Twin Classic', N'Standard', 600000.00, 'https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
    ('106', N'Standard Twin Classic', N'Standard', 600000.00, 'https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),

    -- 3. Standard Double Garden (107, 108, 109)
    ('107', N'Standard Double Garden', N'Standard', 700000.00, 'https://images.unsplash.com/photo-1566665797739-1674de7a421a?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
    ('108', N'Standard Double Garden', N'Standard', 700000.00, 'https://images.unsplash.com/photo-1566665797739-1674de7a421a?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
    ('109', N'Standard Double Garden', N'Standard', 700000.00, 'https://images.unsplash.com/photo-1566665797739-1674de7a421a?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),

    -- 4. Standard Triple Suite (110, 111, 112)
    ('110', N'Standard Triple Suite', N'Standard', 850000.00, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
    ('111', N'Standard Triple Suite', N'Standard', 850000.00, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
    ('112', N'Standard Triple Suite', N'Standard', 850000.00, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),

    -- 5. Standard Family Oasis (113, 114, 115)
    ('113', N'Standard Family Oasis', N'Standard', 1000000.00, 'https://images.unsplash.com/photo-1584132967334-10e028bd69f7?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
    ('114', N'Standard Family Oasis', N'Standard', 1000000.00, 'https://images.unsplash.com/photo-1584132967334-10e028bd69f7?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
    ('115', N'Standard Family Oasis', N'Standard', 1000000.00, 'https://images.unsplash.com/photo-1584132967334-10e028bd69f7?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),

    -- DELUXE (5 loại, mỗi loại 3 phòng)
    -- 6. Deluxe Double View (201, 202, 203)
    ('201', N'Deluxe Double View', N'Deluxe', 1200000.00, 'https://images.unsplash.com/photo-1611891405110-5a30d2527000?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
    ('202', N'Deluxe Double View', N'Deluxe', 1200000.00, 'https://images.unsplash.com/photo-1611891405110-5a30d2527000?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
    ('203', N'Deluxe Double View', N'Deluxe', 1200000.00, 'https://images.unsplash.com/photo-1611891405110-5a30d2527000?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),

    -- 7. Deluxe King Premium (204, 205, 206)
    ('204', N'Deluxe King Premium', N'Deluxe', 1400000.00, 'https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
    ('205', N'Deluxe King Premium', N'Deluxe', 1400000.00, 'https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
    ('206', N'Deluxe King Premium', N'Deluxe', 1400000.00, 'https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),

    -- 8. Deluxe Twin Ocean (207, 208, 209)
    ('207', N'Deluxe Twin Ocean', N'Deluxe', 1500000.00, 'https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
    ('208', N'Deluxe Twin Ocean', N'Deluxe', 1500000.00, 'https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
    ('209', N'Deluxe Twin Ocean', N'Deluxe', 1500000.00, 'https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),

    -- 9. Deluxe Garden Bungalow (210, 211, 212)
    ('210', N'Deluxe Garden Bungalow', N'Deluxe', 1600000.00, 'https://images.unsplash.com/photo-1591088398332-8a7791972843?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
    ('211', N'Deluxe Garden Bungalow', N'Deluxe', 1600000.00, 'https://images.unsplash.com/photo-1591088398332-8a7791972843?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
    ('212', N'Deluxe Garden Bungalow', N'Deluxe', 1600000.00, 'https://images.unsplash.com/photo-1591088398332-8a7791972843?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),

    -- 10. Deluxe Honeymoon Suite (213, 214, 215)
    ('213', N'Deluxe Honeymoon Suite', N'Deluxe', 1800000.00, 'https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
    ('214', N'Deluxe Honeymoon Suite', N'Deluxe', 1800000.00, 'https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
    ('215', N'Deluxe Honeymoon Suite', N'Deluxe', 1800000.00, 'https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),

    -- EXECUTIVE (5 loại, mỗi loại 3 phòng)
    -- 11. Executive Suite VIP (301, 302, 303)
    ('301', N'Executive Suite VIP', N'Executive', 2500000.00, 'https://images.unsplash.com/photo-1598928506311-c55ded91a20c?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
    ('302', N'Executive Suite VIP', N'Executive', 2500000.00, 'https://images.unsplash.com/photo-1598928506311-c55ded91a20c?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
    ('303', N'Executive Suite VIP', N'Executive', 2500000.00, 'https://images.unsplash.com/photo-1598928506311-c55ded91a20c?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),

    -- 12. Executive Sky Palace (304, 305, 306)
    ('304', N'Executive Sky Palace', N'Executive', 3000000.00, 'https://images.unsplash.com/photo-1540518614846-7eded433c457?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
    ('305', N'Executive Sky Palace', N'Executive', 3000000.00, 'https://images.unsplash.com/photo-1540518614846-7eded433c457?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
    ('306', N'Executive Sky Palace', N'Executive', 3000000.00, 'https://images.unsplash.com/photo-1540518614846-7eded433c457?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),

    -- 13. Executive Presidential Suite (307, 308, 309)
    ('307', N'Executive Presidential Suite', N'Executive', 4500000.00, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
    ('308', N'Executive Presidential Suite', N'Executive', 4500000.00, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
    ('309', N'Executive Presidential Suite', N'Executive', 4500000.00, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),

    -- 14. Executive Royal Suite (310, 311, 312)
    ('310', N'Executive Royal Suite', N'Executive', 3800000.00, 'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
    ('311', N'Executive Royal Suite', N'Executive', 3800000.00, 'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
    ('312', N'Executive Royal Suite', N'Executive', 3800000.00, 'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),

    -- 15. Executive Terrace Club (313, 314, 315)
    ('313', N'Executive Terrace Club', N'Executive', 2800000.00, 'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
    ('314', N'Executive Terrace Club', N'Executive', 2800000.00, 'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
    ('315', N'Executive Terrace Club', N'Executive', 2800000.00, 'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=600&q=80', 'AVAILABLE');
GO