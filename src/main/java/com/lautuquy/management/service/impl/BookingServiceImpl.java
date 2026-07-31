package com.lautuquy.management.service.impl;

import com.lautuquy.management.dto.request.BookingRequest;
import com.lautuquy.management.dto.request.PreorderItemRequest;
import com.lautuquy.management.entity.*;
import com.lautuquy.management.exception.ResourceNotFoundException;
import com.lautuquy.management.repository.*;
import com.lautuquy.management.service.BookingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingPreorderRepository bookingPreorderRepository;
    private final AccountRepository accountRepository;
    private final TableTypeRepository tableTypeRepository;
    private final DishRepository dishRepository;
    private final RestaurantTableRepository restaurantTableRepository;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              BookingPreorderRepository bookingPreorderRepository,
                              AccountRepository accountRepository,
                              TableTypeRepository tableTypeRepository,
                              DishRepository dishRepository,
                              RestaurantTableRepository restaurantTableRepository) {
        this.bookingRepository = bookingRepository;
        this.bookingPreorderRepository = bookingPreorderRepository;
        this.accountRepository = accountRepository;
        this.tableTypeRepository = tableTypeRepository;
        this.dishRepository = dishRepository;
        this.restaurantTableRepository = restaurantTableRepository;
    }

    @Override
    @Transactional
    public Booking createBooking(String username, BookingRequest request) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (request.getBookingDate() == null) {
            throw new IllegalArgumentException("Ngày đặt bàn không được để trống.");
        }
        if (request.getBookingDate().isBefore(today)) {
            throw new IllegalArgumentException("Ngày đặt bàn không thể ở trong quá khứ.");
        }
        if (request.getBookingDate().isEqual(today)) {
            if (request.getBookingTime() == null) {
                throw new IllegalArgumentException("Giờ đặt bàn không được để trống.");
            }
            if (request.getBookingTime().isBefore(now)) {
                throw new IllegalArgumentException("Giờ đặt bàn không thể trước thời gian hiện tại đối với ngày hôm nay.");
            }
        }

        Account account = null;
        if (username != null && !username.trim().isEmpty()) {
            account = accountRepository.findByUsername(username).orElse(null);
        }

        TableType tableType = tableTypeRepository.findById(request.getTableTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Loại bàn", request.getTableTypeId()));

        Booking booking = new Booking();
        booking.setAccount(account);
        booking.setCustomerName(request.getCustomerName());
        booking.setCustomerPhone(request.getCustomerPhone());
        String email = request.getCustomerEmail();
        if ((email == null || email.isBlank()) && account != null) {
            email = account.getEmail();
        }
        booking.setCustomerEmail(email);
        booking.setBookingDate(request.getBookingDate());
        booking.setBookingTime(request.getBookingTime());
        booking.setTableType(tableType);
        booking.setSpecialNotes(request.getSpecialNotes());
        booking.setStatus(Booking.Status.PENDING);

        if (request.getTableId() != null) {
            RestaurantTable table = restaurantTableRepository.findById(request.getTableId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bàn ăn", request.getTableId()));
            booking.setAssignedTable(table);
        }

        Booking savedBooking = bookingRepository.save(booking);

        // Lưu danh sách món ăn đặt trước (Preorders)
        if (request.getPreorders() != null && !request.getPreorders().isEmpty()) {
            for (PreorderItemRequest preorderReq : request.getPreorders()) {
                if (preorderReq.getDishId() != null && preorderReq.getQuantity() != null && preorderReq.getQuantity() > 0) {
                    Dish dish = dishRepository.findById(preorderReq.getDishId())
                            .orElseThrow(() -> new ResourceNotFoundException("Món ăn", preorderReq.getDishId()));

                    // Kiểm tra nếu món bị hết hàng hoặc số lượng tồn kho không đủ
                    if (dish.getStatus() == Dish.Status.OUT_OF_STOCK || dish.getQuantity() == null || dish.getQuantity() < 1) {
                        throw new IllegalArgumentException("Món '" + dish.getName() + "' hiện tại đang tạm hết hàng.");
                    }
                    if (dish.getQuantity() < preorderReq.getQuantity()) {
                        throw new IllegalArgumentException("Món '" + dish.getName() + "' chỉ còn lại " + dish.getQuantity() + " suất.");
                    }

                    // Trừ số lượng món ăn trong kho
                    dish.setQuantity(dish.getQuantity() - preorderReq.getQuantity());
                    if (dish.getQuantity() < 1) {
                        dish.setStatus(Dish.Status.OUT_OF_STOCK);
                    }
                    dishRepository.save(dish);

                    BookingPreorder preorder = new BookingPreorder(savedBooking, dish, preorderReq.getQuantity());
                    bookingPreorderRepository.save(preorder);
                }
            }
        }

        return savedBooking;
    }

    @Override
    public List<Booking> getBookingsByAccount(String username) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản", username));
        List<Booking> accountBookings = bookingRepository.findByAccountIdOrderByCreatedAtDesc(account.getId());
        if (account.getPhone() != null && !account.getPhone().isBlank()) {
            List<Booking> phoneBookings = searchBookingsByPhoneOrEmail(account.getPhone());
            for (Booking b : phoneBookings) {
                if (accountBookings.stream().noneMatch(existing -> existing.getId().equals(b.getId()))) {
                    accountBookings.add(b);
                }
            }
        }
        if (account.getEmail() != null && !account.getEmail().isBlank()) {
            List<Booking> emailBookings = searchBookingsByPhoneOrEmail(account.getEmail());
            for (Booking b : emailBookings) {
                if (accountBookings.stream().noneMatch(existing -> existing.getId().equals(b.getId()))) {
                    accountBookings.add(b);
                }
            }
        }
        for (Booking b : accountBookings) {
            if (b.getCustomerEmail() == null || b.getCustomerEmail().isBlank()) {
                if (b.getAccount() != null && b.getAccount().getEmail() != null) {
                    b.setCustomerEmail(b.getAccount().getEmail());
                } else if (account.getEmail() != null) {
                    b.setCustomerEmail(account.getEmail());
                }
            }
        }
        accountBookings.sort((b1, b2) -> {
            if (b1.getCreatedAt() == null || b2.getCreatedAt() == null) return 0;
            return b2.getCreatedAt().compareTo(b1.getCreatedAt());
        });
        return accountBookings;
    }

    @Override
    public List<Booking> searchBookingsByPhoneOrEmail(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        String cleanKeyword = keyword.trim();
        String digitsOnly = cleanKeyword.replaceAll("[^0-9]", "");

        // Tập hợp chứa các định danh (Email, SĐT, Account ID) được mở rộng
        java.util.Set<String> targetEmails = new java.util.HashSet<>();
        java.util.Set<String> targetPhones = new java.util.HashSet<>();
        java.util.Set<Long> targetAccountIds = new java.util.HashSet<>();

        if (cleanKeyword.contains("@")) {
            targetEmails.add(cleanKeyword.toLowerCase());
        }
        if (!digitsOnly.isEmpty()) {
            targetPhones.add(digitsOnly);
            targetPhones.add(cleanKeyword);
        }

        // 1. Quét danh sách Account để khớp với keyword (khớp SĐT, Email, Username, FullName)
        List<Account> allAccounts = accountRepository.findAll();
        for (Account acc : allAccounts) {
            boolean accMatch = false;
            if (acc.getPhone() != null && !acc.getPhone().isBlank()) {
                String accDigits = acc.getPhone().replaceAll("[^0-9]", "");
                if (acc.getPhone().equalsIgnoreCase(cleanKeyword) || (!digitsOnly.isEmpty() && accDigits.equals(digitsOnly))) {
                    accMatch = true;
                }
            }
            if (acc.getEmail() != null && !acc.getEmail().isBlank()) {
                if (acc.getEmail().equalsIgnoreCase(cleanKeyword)) {
                    accMatch = true;
                }
            }
            if (acc.getUsername() != null && acc.getUsername().equalsIgnoreCase(cleanKeyword)) {
                accMatch = true;
            }
            if (acc.getFullName() != null && acc.getFullName().equalsIgnoreCase(cleanKeyword)) {
                accMatch = true;
            }

            if (accMatch) {
                targetAccountIds.add(acc.getId());
                if (acc.getEmail() != null && !acc.getEmail().isBlank()) {
                    targetEmails.add(acc.getEmail().toLowerCase());
                }
                if (acc.getPhone() != null && !acc.getPhone().isBlank()) {
                    targetPhones.add(acc.getPhone().replaceAll("[^0-9]", ""));
                    targetPhones.add(acc.getPhone().trim());
                }
            }
        }

        // 2. Lấy toàn bộ danh sách Booking để quét khớp
        List<Booking> allBookings = bookingRepository.findAll();

        // 3. Quét lần 1: Tìm các Booking trực tiếp khớp với keyword nhập vào
        for (Booking b : allBookings) {
            boolean isMatch = false;
            String bPhoneDigits = b.getCustomerPhone() != null ? b.getCustomerPhone().replaceAll("[^0-9]", "") : "";

            if (b.getCustomerPhone() != null && (b.getCustomerPhone().equalsIgnoreCase(cleanKeyword) || (!digitsOnly.isEmpty() && !bPhoneDigits.isEmpty() && bPhoneDigits.equals(digitsOnly)))) {
                isMatch = true;
            }
            if (b.getCustomerEmail() != null && b.getCustomerEmail().equalsIgnoreCase(cleanKeyword)) {
                isMatch = true;
            }
            if (b.getCustomerName() != null && b.getCustomerName().equalsIgnoreCase(cleanKeyword)) {
                isMatch = true;
            }
            if (b.getAccount() != null) {
                if (targetAccountIds.contains(b.getAccount().getId())) {
                    isMatch = true;
                }
                if (b.getAccount().getEmail() != null && b.getAccount().getEmail().equalsIgnoreCase(cleanKeyword)) {
                    isMatch = true;
                }
                String accPhoneDigits = b.getAccount().getPhone() != null ? b.getAccount().getPhone().replaceAll("[^0-9]", "") : "";
                if (b.getAccount().getPhone() != null && (b.getAccount().getPhone().equalsIgnoreCase(cleanKeyword) || (!digitsOnly.isEmpty() && !accPhoneDigits.isEmpty() && accPhoneDigits.equals(digitsOnly)))) {
                    isMatch = true;
                }
            }

            if (isMatch) {
                if (b.getCustomerEmail() != null && !b.getCustomerEmail().isBlank()) {
                    targetEmails.add(b.getCustomerEmail().toLowerCase());
                }
                if (b.getCustomerPhone() != null && !b.getCustomerPhone().isBlank()) {
                    targetPhones.add(b.getCustomerPhone().replaceAll("[^0-9]", ""));
                    targetPhones.add(b.getCustomerPhone().trim());
                }
                if (b.getAccount() != null) {
                    targetAccountIds.add(b.getAccount().getId());
                    if (b.getAccount().getEmail() != null && !b.getAccount().getEmail().isBlank()) {
                        targetEmails.add(b.getAccount().getEmail().toLowerCase());
                    }
                    if (b.getAccount().getPhone() != null && !b.getAccount().getPhone().isBlank()) {
                        targetPhones.add(b.getAccount().getPhone().replaceAll("[^0-9]", ""));
                        targetPhones.add(b.getAccount().getPhone().trim());
                    }
                }
            }
        }

        // 4. Quét bổ sung Account: Nếu có SĐT hoặc Email nào thu được từ Booking mà thuộc về 1 Account, thêm Account đó vào targetAccountIds
        for (Account acc : allAccounts) {
            boolean secondaryMatch = false;
            if (acc.getEmail() != null && targetEmails.contains(acc.getEmail().toLowerCase())) {
                secondaryMatch = true;
            }
            if (acc.getPhone() != null && !acc.getPhone().isBlank()) {
                String accDigits = acc.getPhone().replaceAll("[^0-9]", "");
                if (targetPhones.contains(accDigits) || targetPhones.contains(acc.getPhone().trim())) {
                    secondaryMatch = true;
                }
            }
            if (secondaryMatch) {
                targetAccountIds.add(acc.getId());
                if (acc.getEmail() != null && !acc.getEmail().isBlank()) {
                    targetEmails.add(acc.getEmail().toLowerCase());
                }
                if (acc.getPhone() != null && !acc.getPhone().isBlank()) {
                    targetPhones.add(acc.getPhone().replaceAll("[^0-9]", ""));
                    targetPhones.add(acc.getPhone().trim());
                }
            }
        }

        // 5. Quét lần 2: Thu thập toàn bộ Booking khớp với bất kỳ Email, SĐT hoặc Account ID đã tổng hợp
        List<Booking> results = new java.util.ArrayList<>();
        java.util.Set<Long> addedBookingIds = new java.util.HashSet<>();

        for (Booking b : allBookings) {
            boolean include = false;

            if (b.getAccount() != null && targetAccountIds.contains(b.getAccount().getId())) {
                include = true;
            }
            if (b.getCustomerEmail() != null && targetEmails.contains(b.getCustomerEmail().toLowerCase())) {
                include = true;
            }
            if (b.getAccount() != null && b.getAccount().getEmail() != null && targetEmails.contains(b.getAccount().getEmail().toLowerCase())) {
                include = true;
            }
            if (b.getCustomerPhone() != null && !b.getCustomerPhone().isBlank()) {
                String bPhoneDigits = b.getCustomerPhone().replaceAll("[^0-9]", "");
                if (targetPhones.contains(bPhoneDigits) || targetPhones.contains(b.getCustomerPhone().trim())) {
                    include = true;
                }
            }
            if (b.getAccount() != null && b.getAccount().getPhone() != null && !b.getAccount().getPhone().isBlank()) {
                String accPhoneDigits = b.getAccount().getPhone().replaceAll("[^0-9]", "");
                if (targetPhones.contains(accPhoneDigits) || targetPhones.contains(b.getAccount().getPhone().trim())) {
                    include = true;
                }
            }

            if (include && !addedBookingIds.contains(b.getId())) {
                addedBookingIds.add(b.getId());

                // Điền email hiển thị nếu bị khuyết
                if (b.getCustomerEmail() == null || b.getCustomerEmail().isBlank()) {
                    if (b.getAccount() != null && b.getAccount().getEmail() != null && !b.getAccount().getEmail().isBlank()) {
                        b.setCustomerEmail(b.getAccount().getEmail());
                    } else if (b.getCustomerPhone() != null && !b.getCustomerPhone().isBlank()) {
                        var accOpt = accountRepository.findFirstByPhone(b.getCustomerPhone());
                        if (accOpt.isPresent() && accOpt.get().getEmail() != null && !accOpt.get().getEmail().isBlank()) {
                            b.setCustomerEmail(accOpt.get().getEmail());
                        }
                    }
                }

                results.add(b);
            }
        }

        // Sắp xếp giảm dần theo thời gian tạo mới nhất
        results.sort((b1, b2) -> {
            if (b1.getCreatedAt() == null || b2.getCreatedAt() == null) return 0;
            return b2.getCreatedAt().compareTo(b1.getCreatedAt());
        });

        return results;
    }

    @Override
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn đặt bàn", id));
    }

    @Override
    public List<BookingPreorder> getPreordersByBookingId(Long bookingId) {
        return bookingPreorderRepository.findByBookingId(bookingId);
    }

    @Override
    @Transactional
    public void cancelBooking(Long id, String username) {
        Booking booking = getBookingById(id);
        if (username != null && booking.getAccount() != null && !booking.getAccount().getUsername().equals(username)) {
            throw new IllegalArgumentException("Bạn không có quyền hủy đơn đặt bàn này.");
        }
        if (booking.getStatus() != Booking.Status.PENDING) {
            throw new IllegalStateException("Chỉ có thể hủy đơn đặt bàn khi đang ở trạng thái Chờ duyệt.");
        }
        booking.setStatus(Booking.Status.CANCELLED);
        bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public void confirmBooking(Long id) {
        Booking booking = getBookingById(id);
        if (booking.getStatus() != Booking.Status.PENDING) {
            throw new IllegalStateException("Chỉ có thể xác nhận đơn đang ở trạng thái Chờ duyệt (PENDING).");
        }
        booking.setStatus(Booking.Status.CONFIRMED);
        bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public void receiveBooking(Long id) {
        Booking booking = getBookingById(id);
        if (booking.getStatus() != Booking.Status.CONFIRMED && booking.getStatus() != Booking.Status.PENDING) {
            throw new IllegalStateException("Chỉ có thể nhận bàn cho đơn ở trạng thái Đã xác nhận hoặc Chờ duyệt.");
        }

        if (booking.getAssignedTable() == null) {
            List<RestaurantTable> tables = restaurantTableRepository.findByTableTypeId(booking.getTableType().getId());
            RestaurantTable availableTable = tables.stream()
                    .filter(t -> t.getStatus() == RestaurantTable.Status.EMPTY || t.getStatus() == RestaurantTable.Status.RESERVED)
                    .findFirst()
                    .orElseGet(() -> restaurantTableRepository.findAll().stream()
                            .filter(t -> t.getStatus() == RestaurantTable.Status.EMPTY || t.getStatus() == RestaurantTable.Status.RESERVED)
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("Hiện không còn bàn ăn nào trống để gán cho đơn này. Vui lòng dọn bàn trước.")));
            booking.setAssignedTable(availableTable);
        }

        booking.setStatus(Booking.Status.SEATED);
        RestaurantTable table = booking.getAssignedTable();
        table.setStatus(RestaurantTable.Status.SERVING);
        restaurantTableRepository.save(table);
        bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public void seatBooking(Long id, Long tableId) {
        Booking booking = getBookingById(id);
        if (booking.getStatus() != Booking.Status.CONFIRMED && booking.getStatus() != Booking.Status.PENDING) {
            throw new IllegalStateException("Đơn đặt bàn không thể xếp bàn ở trạng thái hiện tại.");
        }

        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Bàn ăn", tableId));

        if (table.getStatus() != RestaurantTable.Status.EMPTY && table.getStatus() != RestaurantTable.Status.RESERVED) {
            throw new IllegalStateException("Bàn " + table.getTableNumber() + " hiện tại đang không trống (" + table.getStatus() + ").");
        }

        booking.setStatus(Booking.Status.SEATED);
        booking.setAssignedTable(table);
        table.setStatus(RestaurantTable.Status.SERVING);

        restaurantTableRepository.save(table);
        bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public void cancelBookingByStaff(Long id) {
        Booking booking = getBookingById(id);
        if (booking.getStatus() == Booking.Status.SEATED) {
            throw new IllegalStateException("Đơn đã nhận bàn (SEATED) không thể hủy.");
        }

        // Tự động thêm ghi chú "Đơn quá hạn" nếu đơn bị quá hạn 30 phút nhưng chưa nhận bàn
        if (booking.isOverdue()) {
            String currentNotes = booking.getSpecialNotes();
            if (currentNotes == null || currentNotes.isBlank()) {
                booking.setSpecialNotes("Đơn quá hạn");
            } else if (!currentNotes.contains("Đơn quá hạn")) {
                booking.setSpecialNotes(currentNotes + " (Đơn quá hạn)");
            }
        }

        booking.setStatus(Booking.Status.CANCELLED);
        
        // Nếu đã gán bàn, giải phóng bàn về EMPTY
        if (booking.getAssignedTable() != null && booking.getAssignedTable().getStatus() == RestaurantTable.Status.SERVING) {
            RestaurantTable table = booking.getAssignedTable();
            table.setStatus(RestaurantTable.Status.EMPTY);
            restaurantTableRepository.save(table);
        }

        bookingRepository.save(booking);
    }

    @Override
    public Booking getActiveSeatedBooking(String username) {
        Account account = accountRepository.findByUsername(username).orElse(null);
        if (account == null) return null;
        return bookingRepository.findTopByAccountIdAndStatusOrderByCreatedAtDesc(account.getId(), Booking.Status.SEATED)
                .orElse(null);
    }

    @Override
    public com.lautuquy.management.dto.response.BookingDetailDto getBookingDetail(Long id) {
        Booking b = getBookingById(id);
        List<BookingPreorder> preorders = getPreordersByBookingId(id);

        com.lautuquy.management.dto.response.BookingDetailDto dto = new com.lautuquy.management.dto.response.BookingDetailDto();
        dto.setId(b.getId());
        dto.setCustomerName(b.getCustomerName());
        dto.setCustomerPhone(b.getCustomerPhone());
        String customerEmail = "—";
        if (b.getCustomerEmail() != null && !b.getCustomerEmail().isBlank()) {
            customerEmail = b.getCustomerEmail();
        } else if (b.getAccount() != null && b.getAccount().getEmail() != null && !b.getAccount().getEmail().isBlank()) {
            customerEmail = b.getAccount().getEmail();
        } else if (b.getCustomerPhone() != null && !b.getCustomerPhone().isBlank()) {
            java.util.Optional<Account> accOpt = accountRepository.findFirstByPhone(b.getCustomerPhone());
            if (accOpt.isPresent() && accOpt.get().getEmail() != null && !accOpt.get().getEmail().isBlank()) {
                customerEmail = accOpt.get().getEmail();
            }
        }
        dto.setCustomerEmail(customerEmail);
        dto.setBookingDate(b.getBookingDate() != null ? b.getBookingDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
        dto.setBookingTime(b.getBookingTime() != null ? b.getBookingTime().toString() : "");

        if (b.getTableType() != null) {
            dto.setTableTypeName("Bàn " + b.getTableType().getCapacity() + " người (" + b.getTableType().getTableClass() + ")");
        } else {
            dto.setTableTypeName("—");
        }

        if (b.getAssignedTable() != null) {
            dto.setTableName("Bàn " + b.getAssignedTable().getTableNumber());
        } else {
            dto.setTableName("Chưa xếp bàn");
        }

        dto.setSpecialNotes(b.getSpecialNotes() != null && !b.getSpecialNotes().isBlank() ? b.getSpecialNotes() : "—");
        dto.setStatus(b.getStatus().name());

        String statusDisplay = switch (b.getStatus()) {
            case PENDING -> "Chờ duyệt";
            case CONFIRMED -> "Đã xác nhận";
            case SEATED -> "Đã nhận bàn";
            case COMPLETED -> "Hoàn tất";
            case CANCELLED -> "Đã hủy";
        };
        dto.setStatusDisplayName(statusDisplay);
        dto.setCreatedAt(b.getCreatedAt() != null ? b.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "");
        dto.setOverdue(b.isOverdue());

        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        List<com.lautuquy.management.dto.response.BookingDetailDto.PreorderItemDto> itemDtos = new java.util.ArrayList<>();
        for (BookingPreorder po : preorders) {
            Dish dish = po.getDish();
            java.math.BigDecimal price = (dish != null && dish.getPrice() != null) ? dish.getPrice() : java.math.BigDecimal.ZERO;
            int qty = po.getQuantity() != null ? po.getQuantity() : 0;
            java.math.BigDecimal itemTotal = price.multiply(java.math.BigDecimal.valueOf(qty));
            total = total.add(itemTotal);

            com.lautuquy.management.dto.response.BookingDetailDto.PreorderItemDto itemDto = new com.lautuquy.management.dto.response.BookingDetailDto.PreorderItemDto(
                    dish != null ? dish.getId() : null,
                    dish != null ? dish.getName() : "Món ăn",
                    dish != null ? dish.getImageUrl() : "",
                    price,
                    qty,
                    itemTotal
            );
            itemDtos.add(itemDto);
        }

        dto.setTotalAmount(total);
        dto.setDepositAmount(total.multiply(new java.math.BigDecimal("0.50"))); // Tiền cọc (50%)
        dto.setPreorders(itemDtos);

        return dto;
    }
}
