# Cấu trúc Project — Hệ thống Quản lý Nhà hàng Lẩu Tứ Quý

> Dựa theo báo cáo phân tích & thiết kế hệ thống (Spring Boot 3.x, Spring MVC, Spring Data JPA, Thymeleaf, Spring Security, MySQL).

## Cấu trúc thư mục

```
src/main/java/com/lautuquy/management/
│
├── LauTuQuyApplication.java
│
├── config/
│   ├── SecurityConfig.java          # SecurityFilterChain, phân quyền URL theo role
│   ├── WebConfig.java               # Thymeleaf, resource handler, i18n (nếu cần)
│   └── PasswordEncoderConfig.java   # Bean BCryptPasswordEncoder
│
├── entity/
│   ├── Account.java
│   ├── Category.java
│   ├── Dish.java
│   ├── TableType.java
│   ├── RestaurantTable.java
│   ├── Booking.java
│   ├── BookingPreorder.java
│   ├── BookingPreorderId.java       # @Embeddable composite key
│   ├── Order.java
│   ├── OrderItem.java
│   ├── Invoice.java
│   └── Feedback.java                # bổ sung cho FR-06 (không có trong ERD gốc)
│
├── repository/
│   ├── AccountRepository.java
│   ├── CategoryRepository.java
│   ├── DishRepository.java
│   ├── TableTypeRepository.java
│   ├── RestaurantTableRepository.java
│   ├── BookingRepository.java
│   ├── BookingPreorderRepository.java
│   ├── OrderRepository.java
│   ├── OrderItemRepository.java
│   ├── InvoiceRepository.java
│   └── FeedbackRepository.java
│
├── service/
│   ├── AccountService.java / impl/AccountServiceImpl.java
│   ├── CategoryService.java / impl/CategoryServiceImpl.java
│   ├── DishService.java / impl/DishServiceImpl.java
│   ├── TableService.java / impl/TableServiceImpl.java        # gộp TableType + RestaurantTable
│   ├── BookingService.java / impl/BookingServiceImpl.java
│   ├── OrderService.java / impl/OrderServiceImpl.java
│   ├── InvoiceService.java / impl/InvoiceServiceImpl.java     # chứa @Transactional thanh toán
│   ├── FeedbackService.java / impl/FeedbackServiceImpl.java
│   └── ReportService.java / impl/ReportServiceImpl.java
│
├── controller/
│   ├── AuthController.java              # /register, /login, /logout
│   ├── customer/
│   │   ├── MenuController.java          # /customer/menu (FR-02 - view)
│   │   ├── BookingController.java       # /customer/booking/** (FR-03)
│   │   └── FeedbackController.java      # /customer/feedback (FR-06, AJAX)
│   ├── staff/
│   │   ├── BookingManageController.java # /staff/bookings/** duyệt, xếp bàn (FR-04)
│   │   ├── OrderController.java         # /staff/orders/** tạo & sửa order (FR-05)
│   │   └── InvoiceController.java       # /staff/invoices/** thanh toán, in hóa đơn
│   ├── admin/
│   │   ├── CategoryAdminController.java # CRUD Category (FR-02)
│   │   ├── DishAdminController.java     # CRUD Dish (FR-02)
│   │   ├── TableAdminController.java    # CRUD TableType + RestaurantTable (FR-04 gián tiếp)
│   │   ├── AccountAdminController.java  # CRUD/khóa tài khoản (FR-01)
│   │   ├── FeedbackAdminController.java # trả lời feedback (FR-06)
│   │   └── ReportController.java        # dashboard doanh thu (FR-07)
│   └── ApiTableStatusController.java    # REST/AJAX riêng cho reload bàn trống (NFR-03)
│
├── dto/
│   ├── request/  (RegisterRequest, BookingRequest, OrderItemRequest, PreorderItemRequest, ...)
│   └── response/ (BookingResponse, InvoiceResponse, RevenueReportResponse, ...)
│
└── exception/
    ├── GlobalExceptionHandler.java   # @ControllerAdvice (NFR-05)
    ├── ResourceNotFoundException.java
    ├── BookingConflictException.java
    └── InsufficientPermissionException.java

src/main/resources/
├── templates/
│   ├── auth/ (login.html, register.html)
│   ├── customer/ (menu.html, booking-form.html, booking-history.html, feedback.html)
│   ├── staff/ (booking-list.html, table-board.html, order-detail.html, invoice-print.html)
│   ├── admin/ (categories.html, dishes.html, tables.html, accounts.html, dashboard.html)
│   └── error/ (403.html, 404.html, 500.html)
├── static/ (css, js — chứa file AJAX request cho FR-06 & NFR-03)
└── application.yml
```
