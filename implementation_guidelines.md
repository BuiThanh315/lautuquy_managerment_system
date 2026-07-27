# NHIỆM VỤ CỦA BẠN & HƯỚNG DẪN THỰC HIỆN
## Hệ thống Quản lý Nhà hàng Lẩu Tứ Quý

---
### A. PHÂN TÍCH TÀI LIỆU
1. **Phân tích và ghi nhận các tài liệu**: 
    - @cau_truc_project_lautuquy.md : Ghi nhận và xây dựng cấu trúc thư mục dự án.
    - @plan_and_task.md : Lập kế hoạch và phân chia công việc theo từng giai đoạn.
    - @project_overview.md : Tổng quan về dự án và các yêu cầu.

### B. QUY TRÌNH THỰC HIỆN THEO NGUYÊN TẮC TỪNG GIAI ĐOẠN (STEP-BY-STEP)

1. **Tuân thủ cấu trúc & phân tầng**: 
   - Viết code đúng phân tầng `Controller -> Service -> Repository -> Entity/DTO`[cite: 1].
   - Tổ chức đúng cấu trúc thư mục đã quy định (`com.lautuquy.management`)[cite: 1].

2. **Không bỏ qua bước**: 
   - Hoàn thành dứt điểm từng tầng code (Entity -> Repository -> Service -> Controller -> View) của từng Giai đoạn trước khi chuyển sang giai đoạn tiếp theo[cite: 3].

3. **Chất lượng code chuẩn Production**:
   - Viết Java code sạch, đặt tên biến/hàm tuân thủ chuẩn CamelCase, có comment giải thích các logic xử lý phức tạp.
   - Luôn bắt lỗi bằng `GlobalExceptionHandler` để trả về giao diện báo lỗi thân thiện (`403.html`, `404.html`, `500.html`), không để lộ StackTrace ra giao diện người dùng[cite: 1].

---

### C. CHUẨN KẾT BÀN GIAO & DỪNG KIỂM TRA (CHECKPOINT PROTOCOL)

Để đảm bảo tiến độ và khả năng kiểm soát chất lượng code, **Agent bắt buộc phải tuân thủ Giao thức Báo cáo & Chờ Duyệt sau**:

* **Bước 1**: Bắt đầu thực hiện các công việc thuộc **GIAI ĐOẠN N** (Khởi đầu từ Giai đoạn 1)[cite: 3].
* **Bước 2**: Sau khi viết xong toàn bộ code và cấu trúc file cho Giai đoạn N, **BẮT BUỘC DỪNG LẠI** và báo cáo tóm tắt gồm:
  - Danh sách toàn bộ các file/class đã khởi tạo hoặc chỉnh sửa[cite: 3].
  - Đánh giá mức độ hoàn thành dựa trên **Tiêu chuẩn nghiệm thu** của Giai đoạn N[cite: 3].
  - Gợi ý các bước hoặc câu lệnh kiểm thử (Test cases)[cite: 3].
* **Bước 3**: **Xuất thông báo chờ người dùng xác nhận**:
  > *"Tôi đã hoàn thành xong GIAI ĐOẠN N. Mời bạn kiểm tra và nghiệm thu. Vui lòng phản hồi xác nhận để tôi tiến hành GIAI ĐOẠN N+1."*[cite: 3]
* **Bước 4**: **KHÔNG TỰ Ý CHUYỂN SANG GIAI ĐOẠN MỚI** khi chưa nhận được câu lệnh duyệt từ phía người dùng[cite: 3].

---

### D. BẮT ĐẦU THỰC THI (ACTION ITEM)
Chờ tôi duyệt kế hoạch xong thì sẽ bắt đầu ngay bằng việc lập cấu trúc dự án, tạo file cấu hình cơ bản (`application.yml`, `LauTuQuyApplication.java`) và tiến hành các công việc của **GIAI ĐOẠN 1**[cite: 3].