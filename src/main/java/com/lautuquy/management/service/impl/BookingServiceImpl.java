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

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản", 0L));

        TableType tableType = tableTypeRepository.findById(request.getTableTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Loại bàn", request.getTableTypeId()));

        Booking booking = new Booking();
        booking.setAccount(account);
        booking.setCustomerName(request.getCustomerName());
        booking.setCustomerPhone(request.getCustomerPhone());
        booking.setBookingDate(request.getBookingDate());
        booking.setBookingTime(request.getBookingTime());
        booking.setTableType(tableType);
        booking.setSpecialNotes(request.getSpecialNotes());
        booking.setStatus(Booking.Status.PENDING);

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
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản", 0L));
        return bookingRepository.findByAccountIdOrderByCreatedAtDesc(account.getId());
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
        if (!booking.getAccount().getUsername().equals(username)) {
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
}
