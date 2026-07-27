# KẾ HOẠCH BÀN GIAO & NHIỆM VỤ THỰC HIỆN DỰ ÁN LẨU TỨ QUÝ

## 1. KẾ HOẠCH BÀN GIAO THEO 4 GIAI ĐOẠN

### GIAI ĐOẠN 1 — Nền tảng, Bảo mật & Xác thực
- Thêm file `LauTuQuyApplication.java` & `application.yml` (kết nối cơ sở dữ liệu MySQL `lautuquy_db`).
- Tạo Entity `Account` & `AccountRepository`.
- Cấu hình `SecurityConfig` phân quyền theo 3 vùng URL:
  - `/customer/**` -> Dành cho Role `CUSTOMER`
  - `/staff/**` -> Dành cho Role `STAFF`
  - `/admin/**` -> Dành cho Role `ADMIN`
- Cài đặt `AccountService` (chức năng đăng ký, đổi mật khẩu, khóa/mở khóa tài khoản).
- Xây dựng `AuthController` & các View (`login.html`, `register.html`, `error/403.html`).
- Tạo `GlobalExceptionHandler` và `ResourceNotFoundException`.
- Xây dựng `AccountAdminController` phục vụ Admin quản lý tài khoản.
- **Tiêu chuẩn nghiệm thu Giai đoạn 1**: 
  - Đăng nhập thành công với 3 vai trò (ADMIN, STAFF, CUSTOMER) và truy cập đúng vùng URL được cấp quyền.
  - Tài khoản có `status = 'LOCKED'` (ví dụ: `khachE_vipham` trong CSDL mẫu) tuyệt đối không thể đăng nhập.

---

### GIAI ĐOẠN 2 — Quản lý Thực đơn & Đặt bàn trước
- Xây dựng các Entity & Repository: `Category`, `Dish`, `TableType`, `RestaurantTable`.
- Tạo các Controller quản trị (Admin CRUD): `CategoryAdminController`, `DishAdminController`, `TableAdminController`.
- Xây dựng View cho Khách hàng: `MenuController` (xem thực đơn, phân trang, lọc món).
- Xây dựng các Entity đặt bàn trước: `Booking`, `BookingPreorder`, `BookingPreorderId` (Composite PK).
- Cài đặt `BookingServiceImpl`: Xử lý tạo đồng thời `Booking` (trạng thái `PENDING`) và danh sách `BookingPreorder`.
- Xây dựng các View Khách hàng: `menu.html`, `booking-form.html`, `booking-history.html`.
- **Tiêu chuẩn nghiệm thu Giai đoạn 2**: 
  - Admin CRUD đầy đủ món ăn, danh mục, loại bàn. Món `OUT_OF_STOCK` ẩn nút gọi/đặt trên View Khách hàng.
  - Khách hàng tạo lịch đặt bàn mới, kiểm tra CSDL sinh ra 1 bản ghi `bookings` (trạng thái `PENDING`) và các bản ghi món đặt trước trong `booking_preorders`.

---

### GIAI ĐOẠN 3 — Duyệt Bàn, Xếp Bàn & Gọi món tại bàn
- Cập nhật `BookingServiceImpl`: Xử lý chuyển trạng thái `PENDING` -> `CONFIRMED` / `CANCELLED`, và `CONFIRMED` -> `SEATED` (gán bàn, đổi trạng thái bàn từ `EMPTY` sang `SERVING`).
- Controller Nhân viên: `BookingManageController`.
- Xây dựng các Entity & Repository phục vụ gọi món: `Order`, `OrderItem`.
- Cài đặt `OrderServiceImpl`: Tạo order, thêm/sửa món, lưu giá snapshot vào `actual_price`.
- Tạo AJAX Endpoint: `ApiTableStatusController` giúp tự động làm mới trạng thái bàn (`NFR-03`).
- Xây dựng các View Nhân viên: `staff/table-board.html`, `staff/order-detail.html`.
- **Tiêu chuẩn nghiệm thu Giai đoạn 3**: 
  - Nhân viên tiếp nhận đơn đặt bàn (`CONFIRMED`), xếp bàn (`SEATED`), trạng thái bàn tự động đổi thành `SERVING`.
  - Tạo đơn gọi món mới tại bàn, lưu giá món vào `actual_price` đúng thời điểm order (dù giá gốc trong bảng `dishes` thay đổi sau đó).

---

### GIAI ĐOẠN 4 — Thanh toán Transaction, Feedback, Báo cáo & Kiểm thử
- Xây dựng Entity `Invoice` & `InvoiceRepository`.
- **CỰC KỲ QUAN TRỌNG**: Cài đặt `InvoiceServiceImpl` bắt buộc dùng `@Transactional(rollbackFor = Exception.class)`:
  - Tạo `Invoice` (`PAID`).
  - Cập nhật trạng thái `Order` thành `COMPLETED`.
  - Cập nhật trạng thái `RestaurantTable` từ `SERVING` về `DIRTY` hoặc `EMPTY`.
  - Đảm bảo rollback toàn bộ giao dịch nếu xảy ra lỗi ở bất kỳ bước nào trong quá trình thanh toán.
- Bổ sung CSDL: Chạy migration SQL tạo bảng `feedbacks` (`id`, `account_id`, `dish_id`, `content`, `reply`, `created_at`).
- Xây dựng Entity `Feedback`, `FeedbackService`, `FeedbackController` (Khách gửi AJAX) & `FeedbackAdminController` (Admin phản hồi).
- Xây dựng `ReportServiceImpl` & `ReportController` (Dashboard thống kê doanh thu theo ngày/tháng/năm).
- Xây dựng các View: `staff/invoice-print.html`, `admin/dashboard.html`, `customer/feedback.html`.
- **Tiêu chuẩn nghiệm thu Giai đoạn 4**: 
  - Giả lập lỗi giữa chừng khi thanh toán (ví dụ: throw Exception trước khi cập nhật bàn), kiểm tra toàn bộ thao tác bị Rollback hoàn toàn.
  - Gửi feedback bằng AJAX hoạt động mượt mà không reload trang.
  - Dashboard hiển thị đúng biểu đồ/số liệu doanh thu tổng hợp từ các `invoices` đã thanh toán.

---

## 2. NHIỆM VỤ CỦA BẠN & HƯỚNG DẪN THỰC HIỆN

### A. QUY TRÌNH THỰC HIỆN THEO NGUYÊN TẮC TỪNG GIAI ĐOẠN (STEP-BY-STEP)
1. **Tuân thủ cấu trúc**: Viết code đúng phân tầng `Controller -> Service -> Repository -> Entity/DTO` và tổ chức đúng thư mục đã định nghĩa tại Mục 3.
2. **Không bỏ qua bước**: Hoàn thành dứt điểm từng tầng code (Entity -> Repository -> Service -> Controller -> View) của từng Giai đoạn trước khi chuyển sang các phần tiếp theo.
3. **Chất lượng code chuẩn Production**:
   - Viết Java code sạch, đặt tên biến/hàm tuân thủ chuẩn CamelCase, có comment giải thích các logic xử lý phức tạp.
   - Luôn bắt lỗi bằng `GlobalExceptionHandler` để trả về giao diện báo lỗi thân thiện (`403.html`, `404.html`, `500.html`), không để lộ StackTrace ra giao diện người dùng.

---

### B. CHUẨN KỂT BÀN GIAO & DỪNG KIỂM TRA (CHECKPOINT PROTOCOL)

Để đảm bảo chất lượng và khả năng kiểm soát dự án, **Agent phải tuân thủ nghiêm ngặt Giao thức Báo cáo & Chờ Duyệt**:

* **Bước 1**: Bắt đầu thực thi các công việc của **GIAI ĐOẠN N** (Bắt đầu từ Giai đoạn 1).
* **Bước 2**: Sau khi hoàn thành toàn bộ công việc và cấu trúc file của Giai đoạn N, **DỪNG LẠI** và thực hiện báo cáo tóm tắt bao gồm:
  - Danh sách các file/class đã được khởi tạo hoặc chỉnh sửa.
  - Kết quả đối chiếu với **Tiêu chuẩn nghiệm thu** của Giai đoạn N.
  - Cung cấp các kịch bản test nhanh (Test cases) hoặc câu lệnh kiểm thử nếu có.
* **Bước 3**: **Gửi thông báo chờ phản hồi**: *"Tôi đã hoàn thành GIAI ĐOẠN N. Mời bạn kiểm tra và phản hồi. Sau khi nhận được xác nhận từ bạn, tôi sẽ lập tức bắt đầu GIAI ĐOẠN N+1."*
* **Bước 4**: **CHỈ CHUYỂN SANG GIAI ĐOẠN TIẾP THEO** khi nhận được câu lệnh xác nhận từ phía người dùng (ví dụ: "Đã duyệt Giai đoạn 1, tiến hành Giai đoạn 2").

---

### C. BẮT ĐẦU DỰ ÁN (ACTION ITEM)
Hãy bắt đầu ngay bây giờ bằng việc lập cấu trúc thư mục, khởi tạo file cấu hình cơ bản (`application.yml`, `LauTuQuyApplication.java`) và tiến hành **GIAI ĐOẠN 1**.