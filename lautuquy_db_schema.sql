-- =========================================================
-- LẨU TỨ QUÝ RESTAURANT MANAGEMENT SYSTEM
-- Database: lautuquy_db
-- Mô tả: Schema (10 bảng) + dữ liệu mẫu phục vụ kiểm thử
-- Engine: MySQL 8.x / InnoDB
-- =========================================================

DROP DATABASE IF EXISTS lautuquy_db;
CREATE DATABASE lautuquy_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE lautuquy_db;

-- =========================================================
-- 1. BẢNG accounts
-- =========================================================
CREATE TABLE accounts (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,               -- BCrypt hash
    full_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(100) UNIQUE,
    phone       VARCHAR(15),
    role        ENUM('CUSTOMER','STAFF','ADMIN') NOT NULL DEFAULT 'CUSTOMER',
    status      ENUM('ACTIVE','LOCKED') NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- =========================================================
-- 2. BẢNG categories
-- =========================================================
CREATE TABLE categories (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255)
) ENGINE=InnoDB;

-- =========================================================
-- 3. BẢNG dishes
-- =========================================================
CREATE TABLE dishes (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id  BIGINT NOT NULL,
    name         VARCHAR(150) NOT NULL,
    image_url    VARCHAR(255),
    price        DECIMAL(12,0) NOT NULL,
    description  VARCHAR(255),
    quantity     INT NOT NULL DEFAULT 50,
    status       ENUM('AVAILABLE','OUT_OF_STOCK') NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT fk_dishes_category FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE=InnoDB;

-- =========================================================
-- 4. BẢNG table_types
-- =========================================================
CREATE TABLE table_types (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    capacity INT NOT NULL,
    class    ENUM('REGULAR','VIP') NOT NULL DEFAULT 'REGULAR'
) ENGINE=InnoDB;

-- =========================================================
-- 5. BẢNG restaurant_tables
-- =========================================================
CREATE TABLE restaurant_tables (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_number  VARCHAR(10) NOT NULL UNIQUE,
    table_type_id BIGINT NOT NULL,
    status        ENUM('EMPTY','RESERVED','SERVING','DIRTY') NOT NULL DEFAULT 'EMPTY',
    CONSTRAINT fk_tables_type FOREIGN KEY (table_type_id) REFERENCES table_types(id)
) ENGINE=InnoDB;

-- =========================================================
-- 6. BẢNG bookings
-- =========================================================
CREATE TABLE bookings (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id     BIGINT NOT NULL,
    customer_name  VARCHAR(100) NOT NULL,
    customer_phone VARCHAR(15) NOT NULL,
    booking_date   DATE NOT NULL,
    booking_time   TIME NOT NULL,
    table_type_id  BIGINT NOT NULL,
    assigned_table_id BIGINT,
    special_notes  VARCHAR(255),
    status         ENUM('PENDING','CONFIRMED','SEATED','CANCELLED') NOT NULL DEFAULT 'PENDING',
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bookings_account   FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT fk_bookings_tabletype FOREIGN KEY (table_type_id) REFERENCES table_types(id),
    CONSTRAINT fk_bookings_table     FOREIGN KEY (assigned_table_id) REFERENCES restaurant_tables(id)
) ENGINE=InnoDB;

-- =========================================================
-- 7. BẢNG booking_preorders (Composite PK)
-- =========================================================
CREATE TABLE booking_preorders (
    booking_id BIGINT NOT NULL,
    dish_id    BIGINT NOT NULL,
    quantity   INT NOT NULL DEFAULT 1,
    PRIMARY KEY (booking_id, dish_id),
    CONSTRAINT fk_preorder_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_preorder_dish    FOREIGN KEY (dish_id)    REFERENCES dishes(id)
) ENGINE=InnoDB;

-- =========================================================
-- 8. BẢNG orders
-- =========================================================
CREATE TABLE orders (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT,
    account_id BIGINT NOT NULL,
    table_id   BIGINT NOT NULL,
    order_type ENUM('DINE_IN','TAKE_AWAY') NOT NULL DEFAULT 'DINE_IN',
    status     ENUM('PROCESSING','COMPLETED','CANCELLED') NOT NULL DEFAULT 'PROCESSING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_orders_account FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT fk_orders_table   FOREIGN KEY (table_id)   REFERENCES restaurant_tables(id)
) ENGINE=InnoDB;

-- =========================================================
-- 9. BẢNG order_items
-- =========================================================
CREATE TABLE order_items (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id     BIGINT NOT NULL,
    dish_id      BIGINT NOT NULL,
    quantity     INT NOT NULL DEFAULT 1,
    actual_price DECIMAL(12,0) NOT NULL,   -- snapshot giá tại thời điểm gọi món
    CONSTRAINT fk_orderitems_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_orderitems_dish  FOREIGN KEY (dish_id)  REFERENCES dishes(id)
) ENGINE=InnoDB;

-- =========================================================
-- 10. BẢNG invoices
-- =========================================================
CREATE TABLE invoices (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id       BIGINT NOT NULL UNIQUE,
    total_amount   DECIMAL(12,0) NOT NULL,
    final_amount   DECIMAL(12,0) NOT NULL,     -- sau giảm giá
    payment_method ENUM('CASH','BANK_TRANSFER') NOT NULL DEFAULT 'CASH',
    payment_status ENUM('UNPAID','PAID') NOT NULL DEFAULT 'UNPAID',
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_invoices_order FOREIGN KEY (order_id) REFERENCES orders(id)
) ENGINE=InnoDB;


-- =========================================================
-- =================  DỮ LIỆU MẪU (SAMPLE DATA)  ============
-- =========================================================

-- ---------------------------------------------------------
-- accounts: 1 Admin, 2 Staff, 5 Customer (1 bị LOCKED để test)
-- Mật khẩu mẫu (plaintext gốc trước khi hash BCrypt): "123456"
-- (GIỮ NGUYÊN TÀI KHOẢN THEO YÊU CẦU)
-- ---------------------------------------------------------
INSERT INTO accounts (id, username, password, full_name, email, phone, role, status, created_at) VALUES
(1, 'admin01',   '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQvq4a1', 'Nguyễn Văn Quản',  'admin@lautuquy.vn',    '0900000001', 'ADMIN',    'ACTIVE', '2026-06-01 08:00:00'),
(2, 'staff01',   '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQvq4a1', 'Trần Thị Phục Vụ', 'staff01@lautuquy.vn',  '0900000002', 'STAFF',    'ACTIVE', '2026-06-01 08:10:00'),
(3, 'staff02',   '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQvq4a1', 'Lê Văn Bàn',       'staff02@lautuquy.vn',  '0900000003', 'STAFF',    'ACTIVE', '2026-06-01 08:15:00'),
(4, 'khachA',    '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQvq4a1', 'Phạm Thu Hà',      'hapham@gmail.com',     '0911111111', 'CUSTOMER', 'ACTIVE', '2026-06-02 09:00:00'),
(5, 'khachB',    '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQvq4a1', 'Đỗ Minh Khang',    'khangdo@gmail.com',    '0922222222', 'CUSTOMER', 'ACTIVE', '2026-06-02 09:05:00'),
(6, 'khachC',    '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQvq4a1', 'Vũ Ngọc Lan',      'lanvu@gmail.com',      '0933333333', 'CUSTOMER', 'ACTIVE', '2026-06-03 10:00:00'),
(7, 'khachD',    '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQvq4a1', 'Bùi Anh Tuấn',     'tuanbui@gmail.com',    '0944444444', 'CUSTOMER', 'ACTIVE', '2026-06-03 10:30:00'),
(8, 'khachE_vipham', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQvq4a1', 'Hoàng Gian Lận',   'ganlanx@gmail.com',    '0955555555', 'CUSTOMER', 'LOCKED', '2026-06-04 11:00:00');

-- ---------------------------------------------------------
-- categories: 5 danh mục chính
-- ---------------------------------------------------------
INSERT INTO categories (id, name, description) VALUES
(1, 'Nước lẩu',        'Các loại nước lẩu đặc trưng: Thái, Kim Chi, Nấm...'),
(2, 'Đồ nhúng bò',      'Thịt bò tươi ngon thái mỏng nhúng lẩu'),
(3, 'Đồ nhúng hải sản', 'Tôm, mực, ngao, cá phi lê tươi sống'),
(4, 'Rau nấm tươi',     'Các loại rau củ nấm sạch dùng kèm lẩu'),
(5, 'Đồ uống & Trái cây','Nước ngọt, bia, nước ép trái cây giải khát');

-- ---------------------------------------------------------
-- dishes: các món ăn (Bò Wagyu quantity = 0 -> OUT_OF_STOCK để kiểm thử)
-- ---------------------------------------------------------
INSERT INTO dishes (id, category_id, name, image_url, price, description, quantity, status) VALUES
(1, 1, 'Nước lẩu Thái chua cay',   '/images/dishes/lau-thai.jpg',    89000,  'Vị chua cay nồng nàn chuẩn vị Thái', 50, 'AVAILABLE'),
(2, 1, 'Nước lẩu Kim Chi Hàn Quốc', '/images/dishes/lau-kimchi.jpg',  99000,  'Vị cay nồng đậm đà chuẩn vị Hàn',    50, 'AVAILABLE'),
(3, 1, 'Nước lẩu Nấm thanh vị',    '/images/dishes/lau-nam.jpg',     79000,  'Hương vị thanh ngọt, bổ dưỡng',       50, 'AVAILABLE'),
(4, 2, 'Ba chỉ bò Mỹ nhúng lẩu',   '/images/dishes/bo-my.jpg',       129000, 'Thịt ba chỉ bò Mỹ vân mỡ đều ngon',   50, 'AVAILABLE'),
(5, 2, 'Bò Wagyu Nhật Bản',        '/images/dishes/bo-wagyu.jpg',    259000, 'Thịt bò Wagyu cao cấp mềm mịn',       0,  'OUT_OF_STOCK'),
(6, 3, 'Tôm sú tươi sống',         '/images/dishes/tom-su.jpg',      149000, 'Tôm sú tươi nhúng lẩu giòn ngọt',     45, 'AVAILABLE'),
(7, 3, 'Mực ống tươi',             '/images/dishes/muc-ong.jpg',     109000, 'Mực ống tươi được làm sạch sẵn',      40, 'AVAILABLE'),
(8, 4, 'Nấm kim châm tươi',        '/images/dishes/nam-kim-cham.jpg',39000,  'Nấm kim châm trắng giòn',            50, 'AVAILABLE'),
(9, 4, 'Rau muống nhặt sẵn',       '/images/dishes/rau-muong.jpg',   25000,  'Rau muống xanh tươi',                50, 'AVAILABLE'),
(10, 5, 'Coca-Cola ướp lạnh lon',   '/images/dishes/coca.jpg',        15000,  'Lon 330ml giải khát cực đã',          50, 'AVAILABLE'),
(11, 5, 'Bia Tiger lon 330ml',     '/images/dishes/tiger.jpg',       25000,  'Bia Tiger mát lạnh',                 50, 'AVAILABLE');

-- ---------------------------------------------------------
-- table_types: REGULAR & VIP
-- ---------------------------------------------------------
INSERT INTO table_types (id, capacity, class) VALUES
(1, 4,  'REGULAR'),
(2, 6,  'REGULAR'),
(3, 8,  'VIP'),
(4, 10, 'VIP');

-- ---------------------------------------------------------
-- restaurant_tables: phủ đủ các trạng thái EMPTY, DIRTY, SERVING, RESERVED
-- ---------------------------------------------------------
INSERT INTO restaurant_tables (id, table_number, table_type_id, status) VALUES
(1, 'T01', 1, 'EMPTY'),
(2, 'T02', 1, 'DIRTY'),
(3, 'T03', 2, 'SERVING'),
(4, 'T04', 2, 'EMPTY'),
(5, 'V01', 3, 'RESERVED'),
(6, 'V02', 3, 'SERVING'),
(7, 'V03', 4, 'EMPTY'),
(8, 'T05', 1, 'EMPTY');

-- ---------------------------------------------------------
-- bookings: Mốc ngày giờ hợp lệ từ ngày hiện tại 2026-07-27 tới tương lai
-- ---------------------------------------------------------
INSERT INTO bookings (id, account_id, customer_name, customer_phone, booking_date, booking_time, table_type_id, assigned_table_id, special_notes, status, created_at) VALUES
(1, 4, 'Phạm Thu Hà',   '0911111111', '2026-07-27', '18:30:00', 3, NULL, 'Sinh nhật, cần không gian VIP',      'PENDING',   '2026-07-27 08:00:00'),
(2, 5, 'Đỗ Minh Khang', '0922222222', '2026-07-27', '19:30:00', 2, NULL, 'Yêu cầu ghế trẻ em',                 'CONFIRMED', '2026-07-27 09:15:00'),
(3, 6, 'Vũ Ngọc Lan',   '0933333333', '2026-07-28', '12:00:00', 3, NULL, 'Tiệc gia đình cuối tuần',            'CONFIRMED', '2026-07-27 09:30:00'),
(4, 7, 'Bùi Anh Tuấn',  '0944444444', '2026-07-27', '11:30:00', 3, 6,    'Khách đã tới, nhận bàn V02',          'SEATED',    '2026-07-27 10:00:00'),
(5, 4, 'Phạm Thu Hà',   '0911111111', '2026-07-29', '20:00:00', 1, NULL, 'Khách báo bận hủy đơn',               'CANCELLED', '2026-07-27 10:15:00');

-- ---------------------------------------------------------
-- booking_preorders: món đặt trước gắn với các booking mẫu
-- ---------------------------------------------------------
INSERT INTO booking_preorders (booking_id, dish_id, quantity) VALUES
(1, 1, 1),   -- Booking 1: 1 Nước lẩu Thái
(1, 4, 2),   -- Booking 1: 2 Phần ba chỉ bò Mỹ
(1, 6, 1),   -- Booking 1: 1 Phần tôm sú
(2, 2, 1),   -- Booking 2: 1 Nước lẩu Kim Chi
(2, 8, 2),   -- Booking 2: 2 Phần nấm kim châm
(3, 3, 1),   -- Booking 3: 1 Nước lẩu nấm
(3, 7, 2),   -- Booking 3: 2 Phần mực ống
(4, 1, 1),   -- Booking 4: 1 Nước lẩu Thái
(4, 4, 3);   -- Booking 4: 3 Phần ba chỉ bò Mỹ

-- ---------------------------------------------------------
-- orders: 4 đơn gọi món tại bàn
-- ---------------------------------------------------------
INSERT INTO orders (id, booking_id, account_id, table_id, order_type, status, created_at) VALUES
(1, 4, 2, 6, 'DINE_IN', 'PROCESSING', '2026-07-27 11:35:00'),  -- Đơn từ Booking 4, Bàn V02 (SERVING)
(2, NULL, 3, 3, 'DINE_IN', 'PROCESSING', '2026-07-27 10:45:00'),  -- Đơn vãng lai Bàn T03 (SERVING)
(3, NULL, 2, 2, 'DINE_IN', 'COMPLETED',  '2026-07-27 09:00:00'),  -- Đơn đã hoàn tất thanh toán Bàn T02 (DIRTY)
(4, NULL, 3, 4, 'DINE_IN', 'CANCELLED',  '2026-07-27 08:30:00');  -- Đơn hủy Bàn T04

-- ---------------------------------------------------------
-- order_items: chi tiết món ăn trong đơn
-- ---------------------------------------------------------
INSERT INTO order_items (id, order_id, dish_id, quantity, actual_price) VALUES
(1, 1, 1, 1, 89000),
(2, 1, 7, 2, 109000),
(3, 1, 11, 3, 25000),
(4, 2, 2, 1, 99000),
(5, 2, 4, 2, 129000),
(6, 2, 9, 1, 25000),
(7, 3, 3, 1, 79000),
(8, 3, 6, 1, 149000),
(9, 3, 10, 2, 15000),
(10, 4, 1, 1, 89000);

-- ---------------------------------------------------------
-- invoices: Hóa đơn thanh toán cho đơn 3
-- ---------------------------------------------------------
INSERT INTO invoices (id, order_id, total_amount, final_amount, payment_method, payment_status, created_at) VALUES
(1, 3, 258000, 245000, 'CASH', 'PAID', '2026-07-27 09:45:00');

-- =========================================================
-- HẾT FILE — sẵn sàng import bằng: mysql -u root -p < lautuquy_db_schema.sql
-- =========================================================
