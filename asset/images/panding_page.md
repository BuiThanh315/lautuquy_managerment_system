Trang chủ (Landing Page)
Tông màu đỏ cà chua – cam ấm – kem, tiêu đề font-heading, responsive.
1. Hero section: ảnh pizza lớn làm nền (Unsplash thật + gradient overlay tối), tiêu đề ấn tượng, CTA "Xem thực đơn" cuộn mượt xuống #menu
2. Section "Toàn bộ thực đơn" (id="menu"):
    Fetch GET {API_URL}/products
    Chia riêng theo category (không dùng tab lọc chung) — mỗi category 1 section, nền xen kẽ trắng/cam nhạt
    Nav dạng pill ở đầu, bấm cuộn mượt tới section (xử lý slug có dấu tiếng Việt)
    Thứ tự category: Pizza → Pizza Chay → Pizza Đặc Biệt → Đồ Ăn Nhanh → Đồ Uống
    Card sản phẩm: ảnh, nhãn category góc trên trái, tên, mô tả (line-clamp 2 dòng), giá, hover nâng card + zoom ảnh
    Có fallback data tĩnh (4-5 món mẫu) phòng khi json-server chưa chạy
3. Section giới thiệu quán (nền cam nhạt)
4. Footer: tên quán + link #menu + link /admin
