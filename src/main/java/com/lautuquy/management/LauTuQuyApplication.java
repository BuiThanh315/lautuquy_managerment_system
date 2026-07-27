package com.lautuquy.management;

import com.lautuquy.management.entity.*;
import com.lautuquy.management.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Điểm khởi động của ứng dụng Quản lý Nhà hàng Lẩu Tứ Quý.
 * Tự động seed dữ liệu mẫu ban đầu cho CSDL (Accounts, TableTypes, Tables, Categories, Dishes, Bookings).
 */
@SpringBootApplication
public class LauTuQuyApplication {

    public static void main(String[] args) {
        SpringApplication.run(LauTuQuyApplication.class, args);
    }

    @Bean
    CommandLineRunner seedSampleData(AccountRepository accountRepository,
                                     TableTypeRepository tableTypeRepository,
                                     RestaurantTableRepository tableRepository,
                                     CategoryRepository categoryRepository,
                                     DishRepository dishRepository,
                                     BookingRepository bookingRepository,
                                     PasswordEncoder passwordEncoder) {
        return args -> {
            String rawPassword = "123456";

            // 1. Seed & Đồng bộ Mật khẩu Accounts
            accountRepository.findAll().forEach(account -> {
                account.setPassword(passwordEncoder.encode(rawPassword));
                accountRepository.save(account);
            });

            // 2. Seed TableTypes nếu chưa có
            if (tableTypeRepository.count() == 0) {
                TableType t1 = tableTypeRepository.save(new TableType(null, 4, TableType.TableClass.REGULAR));
                TableType t2 = tableTypeRepository.save(new TableType(null, 6, TableType.TableClass.REGULAR));
                TableType t3 = tableTypeRepository.save(new TableType(null, 8, TableType.TableClass.VIP));

                // Seed RestaurantTables
                tableRepository.save(new RestaurantTable(null, "T01", t1, RestaurantTable.Status.EMPTY));
                tableRepository.save(new RestaurantTable(null, "T02", t1, RestaurantTable.Status.EMPTY));
                tableRepository.save(new RestaurantTable(null, "T03", t2, RestaurantTable.Status.EMPTY));
                tableRepository.save(new RestaurantTable(null, "V01", t3, RestaurantTable.Status.EMPTY));
            }

            // 3. Seed Categories & Dishes nếu chưa có
            if (categoryRepository.count() == 0) {
                Category c1 = categoryRepository.save(new Category(null, "Nước lẩu", "Các loại nước lẩu đặc trưng"));
                Category c2 = categoryRepository.save(new Category(null, "Đồ nhúng bò", "Thịt bò tươi nhúng lẩu"));
                Category c3 = categoryRepository.save(new Category(null, "Rau nấm", "Rau xanh và nấm tươi"));

                dishRepository.save(new Dish(null, c1, "Lẩu Thái Chua Cay", "/images/dishes/lau-thai.jpg", new BigDecimal("189000"), "Nước lẩu Thái chua cay chuẩn vị", Dish.Status.AVAILABLE));
                dishRepository.save(new Dish(null, c1, "Lẩu Nấm Thượng Hạng", "/images/dishes/lau-nam.jpg", new BigDecimal("169000"), "Nước lẩu nấm thanh ngọt dưỡng sinh", Dish.Status.AVAILABLE));
                dishRepository.save(new Dish(null, c2, "Ba Chỉ Bỏ Mỹ", "/images/dishes/bo-my.jpg", new BigDecimal("129000"), "Ba chỉ bò Mỹ nhúng lẩu mềm ngon", Dish.Status.AVAILABLE));
                dishRepository.save(new Dish(null, c2, "Bắp Bỏ Hoa", "/images/dishes/bap-bo.jpg", new BigDecimal("149000"), "Bắp bò giòn ngon ngọt thịt", Dish.Status.AVAILABLE));
                dishRepository.save(new Dish(null, c3, "Nấm Kim Chi", "/images/dishes/nam-kim-chi.jpg", new BigDecimal("39000"), "Nấm tươi ngon", Dish.Status.AVAILABLE));
            }

            // 4. Seed Bookings mẫu nếu chưa có
            if (bookingRepository.count() == 0) {
                var khachA = accountRepository.findByUsername("khachA").orElse(null);
                var type1 = tableTypeRepository.findAll().stream().findFirst().orElse(null);

                if (khachA != null && type1 != null) {
                    Booking b = new Booking();
                    b.setAccount(khachA);
                    b.setCustomerName("Phạm Thu Hà");
                    b.setCustomerPhone("0911111111");
                    b.setBookingDate(LocalDate.now().plusDays(1));
                    b.setBookingTime(LocalTime.of(18, 30));
                    b.setTableType(type1);
                    b.setSpecialNotes("Cần bàn gần cửa sổ");
                    b.setStatus(Booking.Status.PENDING);
                    bookingRepository.save(b);
                }
            }

            System.out.println("✅ [LauTuQuy] Seed dữ liệu hoàn tất! Mật khẩu tất cả tài khoản mẫu: 123456");
        };
    }
}
