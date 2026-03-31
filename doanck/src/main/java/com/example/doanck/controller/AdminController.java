package com.example.doanck.controller;

import com.example.doanck.model.Category;
import com.example.doanck.model.Combo;
import com.example.doanck.model.Food;
import com.example.doanck.model.Movie;
import com.example.doanck.model.Promotion;
import com.example.doanck.model.Room;
import com.example.doanck.model.Showtime;
import com.example.doanck.model.Ticket;
import com.example.doanck.service.BookingService;
import com.example.doanck.service.ComboService;
import com.example.doanck.service.FoodService;
import com.example.doanck.service.MovieService;
import com.example.doanck.service.OrderService;
import com.example.doanck.service.PaymentService;
import com.example.doanck.service.PromotionService;
import com.example.doanck.service.RoomService;
import com.example.doanck.service.ShowtimeService;
import com.example.doanck.service.TicketService;
import com.example.doanck.service.UserService;
import com.example.doanck.util.QRCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private MovieService movieService;
    @Autowired private FoodService foodService;
    @Autowired private ShowtimeService showtimeService;
    @Autowired private RoomService roomService;
    @Autowired private UserService userService;
    @Autowired private ComboService comboService;
    @Autowired private PromotionService promotionService;
    @Autowired private OrderService orderService;
    @Autowired private TicketService ticketService;
    @Autowired private PaymentService paymentService;
    @Autowired private BookingService bookingService;
    @Autowired private QRCodeService qrCodeService;

    @GetMapping
    public String adminHome() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("movieCount", movieService.getAllMovies().size());
        model.addAttribute("foodCount", foodService.getAllFoods().size());
        model.addAttribute("userCount", userService.getAllUsers().size());
        model.addAttribute("comboCount", comboService.getAllCombos().size());
        model.addAttribute("promotionCount", promotionService.getAllPromotions().size());
        model.addAttribute("orderCount", orderService.getAllOrders().size());
        model.addAttribute("ticketCount", ticketService.getAllTickets().size());
        model.addAttribute("paymentCount", paymentService.getAllPayments().size());
        model.addAttribute("bookingCount", bookingService.getAllBookings().size());
        return "admin/dashboard";
    }

    @GetMapping("/movies")
    public String listAll(Model model) {
        model.addAttribute("movies", movieService.getAllMovies());
        model.addAttribute("foods", foodService.getAllFoods());
        model.addAttribute("categories", foodService.getAllCategories());
        return "admin/movies";
    }


    @GetMapping("/foods")
    public String foods(Model model) {
        model.addAttribute("foods", foodService.getAllFoods());
        model.addAttribute("categories", foodService.getAllCategories());
        return "admin/foods";
    }
    @GetMapping("/movies/create")
    public String showCreateMovieForm(Model model) {
        model.addAttribute("movie", new Movie());
        model.addAttribute("rooms", roomService.getAllRooms());
        return "admin/create-movie";
    }

    @GetMapping("/movies/view/{id}")
    public String viewMovie(@PathVariable Long id, Model model) {
        Movie movie = movieService.getMovieById(id);
        if (movie == null) {
            return "redirect:/admin/movies";
        }

        model.addAttribute("movie", movie);
        model.addAttribute("showtimes", movie.getShowtimes());
        return "admin/view-movie";
    }

    @GetMapping("/movies/edit/{id}")
    public String editMovie(@PathVariable Long id, Model model) {
        Movie movie = movieService.getMovieById(id);
        if (movie == null) {
            return "redirect:/admin/movies";
        }

        model.addAttribute("movie", movie);
        model.addAttribute("rooms", roomService.getAllRooms());

        Showtime showtime = (movie.getShowtimes() != null && !movie.getShowtimes().isEmpty())
                ? movie.getShowtimes().get(0)
                : null;

        model.addAttribute("showtime", showtime);

        return "admin/edit-movie";
    }

    @PostMapping("/movies/save")
    public String saveMovie(@ModelAttribute("movie") Movie movie,
                            @RequestParam(required = false) String showDate,
                            @RequestParam(required = false) String showTime,
                            @RequestParam(required = false) Long roomId,
                            RedirectAttributes redirectAttributes) {

        boolean isCreate = movie.getId() == null;

        MultipartFile imageFile = movie.getPosterFile();

        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String fileName = imageFile.getOriginalFilename();
                Path uploadPath = Paths.get("src/main/resources/static/uploads/");

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                Path filePath = uploadPath.resolve(fileName);
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                movie.setPoster(fileName);
            } else if (movie.getId() != null) {
                Movie oldMovie = movieService.getMovieById(movie.getId());
                if (oldMovie != null) {
                    movie.setPoster(oldMovie.getPoster());
                }
            }

            Movie savedMovie = movieService.saveMovie(movie);

            boolean hasScheduleInput = showDate != null && !showDate.isBlank()
                    && showTime != null && !showTime.isBlank()
                    && roomId != null;

            if (hasScheduleInput) {
                String dateTime = showDate + "T" + showTime;

                Showtime showtime;
                if (savedMovie.getShowtimes() != null && !savedMovie.getShowtimes().isEmpty()) {
                    showtime = savedMovie.getShowtimes().get(0);
                } else {
                    showtime = new Showtime();
                }

                Room room = roomService.getAllRooms()
                        .stream()
                        .filter(r -> r.getId().equals(roomId))
                        .findFirst()
                        .orElse(null);

                if (room == null) {
                    throw new IllegalArgumentException("Khong tim thay phong chieu da chon.");
                }

                showtime.setStartTime(LocalDateTime.parse(dateTime));
                showtime.setMovie(savedMovie);
                showtime.setRoom(room);
                showtimeService.save(showtime);
            }

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    isCreate ? "Them phim thanh cong." : "Cap nhat phim thanh cong.");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Luu phim that bai: " + e.getMessage());
            return isCreate
                    ? "redirect:/admin/movies/create"
                    : "redirect:/admin/movies/edit/" + movie.getId();
        }

        return "redirect:/admin/movies";
    }

    @GetMapping("/movies/delete/{id}")
    public String deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return "redirect:/admin/movies";
    }

    @GetMapping("/foods/create")
    public String showCreateFoodForm(Model model) {
        model.addAttribute("food", new Food());
        model.addAttribute("categories", foodService.getAllCategories());
        return "admin/create-food";
    }

    @GetMapping("/foods/edit/{id}")
    public String editFood(@PathVariable Long id, Model model) {
        Food food = foodService.getFoodById(id);
        if (food == null) return "redirect:/admin/foods";

        model.addAttribute("food", food);
        model.addAttribute("categories", foodService.getAllCategories());
        return "admin/edit-food";
    }

    @PostMapping("/foods/save")
    public String saveFood(@ModelAttribute("food") Food food,
                           @RequestParam("categoryId") Long categoryId,
                           @RequestParam("imageFile") MultipartFile imageFile) {

        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String fileName = imageFile.getOriginalFilename();
                Path uploadPath = Paths.get("src/main/resources/static/uploads/");

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                Path filePath = uploadPath.resolve(fileName);
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                food.setImage(fileName);
            } else if (food.getId() != null) {
                Food oldFood = foodService.getFoodById(food.getId());
                if (oldFood != null) {
                    food.setImage(oldFood.getImage());
                }
            }

            Category category = foodService.getCategoryById(categoryId);
            if (category != null) {
                food.setCategory(category);
            }

            foodService.saveFood(food);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/admin/foods";
    }

    @GetMapping("/foods/delete/{id}")
    public String deleteFood(@PathVariable Long id) {
        foodService.deleteFood(id);
        return "redirect:/admin/foods";
    }

    @GetMapping("/foods/categories/create")
    public String showCreateCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        return "admin/create-category";
    }

    @GetMapping("/foods/categories/edit/{id}")
    public String editCategory(@PathVariable Long id, Model model) {
        Category category = foodService.getCategoryById(id);
        if (category == null) {
            return "redirect:/admin/foods";
        }
        model.addAttribute("category", category);
        return "admin/edit-category";
    }

    @PostMapping("/foods/categories/save")
    public String saveCategory(@ModelAttribute Category category) {
        foodService.createCategory(category);
        return "redirect:/admin/foods";
    }

    @GetMapping("/foods/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {
        foodService.deleteCategory(id);
        return "redirect:/admin/foods";
    }

    @GetMapping("/rooms")
    public String rooms(Model model) {
        model.addAttribute("rooms", roomService.getAllRooms());
        return "admin/rooms";
    }

    @GetMapping("/showtimes")
    public String showtimes(Model model) {
        model.addAttribute("showtimes", showtimeService.getAllShowtimes());
        return "admin/showtimes-manager";
    }

    @GetMapping("/showtimes/create")
    public String createShowtimeForm(Model model) {
        model.addAttribute("showtime", new Showtime());
        model.addAttribute("movies", movieService.getAllMovies());
        model.addAttribute("rooms", roomService.getAllRooms());
        return "admin/create-showtime";
    }

    @GetMapping("/showtimes/edit/{id}")
    public String editShowtime(@PathVariable Long id, Model model) {
        Showtime showtime = showtimeService.getShowtimeById(id);
        if (showtime == null) {
            return "redirect:/admin/showtimes";
        }

        model.addAttribute("showtime", showtime);
        model.addAttribute("movies", movieService.getAllMovies());
        model.addAttribute("rooms", roomService.getAllRooms());
        return "admin/edit-showtime";
    }

    @PostMapping("/showtimes/save")
    public String saveShowtime(@RequestParam(required = false) Long id,
                               @RequestParam Long movieId,
                               @RequestParam Long roomId,
                               @RequestParam String startTime) {
        Showtime showtime = id != null ? showtimeService.getShowtimeById(id) : new Showtime();
        if (showtime == null) {
            showtime = new Showtime();
        }

        Movie movie = movieService.getMovieById(movieId);
        Room room = roomService.getAllRooms()
                .stream()
                .filter(r -> r.getId().equals(roomId))
                .findFirst()
                .orElse(null);

        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(LocalDateTime.parse(startTime));
        showtimeService.save(showtime);

        return "redirect:/admin/showtimes";
    }

    @GetMapping("/showtimes/delete/{id}")
    public String deleteShowtime(@PathVariable Long id) {
        showtimeService.delete(id);
        return "redirect:/admin/showtimes";
    }

    @GetMapping("/rooms/create")
    public String createRoomForm(Model model) {
        model.addAttribute("room", new Room());
        return "admin/create-room";
    }

    @PostMapping("/rooms/save")
    public String saveRoom(@ModelAttribute("room") Room room) {
        roomService.saveRoom(room);
        return "redirect:/admin/rooms";
    }

    @GetMapping("/rooms/edit/{id}")
    public String editRoom(@PathVariable Long id, Model model) {
        Room room = roomService.getAllRooms()
                .stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (room == null) return "redirect:/admin/rooms";

        model.addAttribute("room", room);
        return "admin/edit-room";
    }

    @GetMapping("/rooms/delete/{id}")
    public String deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return "redirect:/admin/rooms";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    @GetMapping("/combos")
    public String combos(Model model) {
        model.addAttribute("combos", comboService.getAllCombos());
        return "admin/combos";
    }

    @GetMapping("/combos/create")
    public String createComboForm(Model model) {
        model.addAttribute("combo", new Combo());
        return "admin/create-combo";
    }

    @GetMapping("/combos/edit/{id}")
    public String editCombo(@PathVariable Long id, Model model) {
        Optional<Combo> combo = comboService.getComboById(id);
        if (combo.isEmpty()) {
            return "redirect:/admin/combos";
        }
        model.addAttribute("combo", combo.get());
        return "admin/edit-combo";
    }

    @PostMapping("/combos/save")
    public String saveCombo(@ModelAttribute Combo combo) {
        MultipartFile imageFile = combo.getImageFile();

        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String fileName = imageFile.getOriginalFilename();
                Path uploadPath = Paths.get("uploads/");

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                Path filePath = uploadPath.resolve(fileName);
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                combo.setImage(fileName);
            } else if (combo.getId() != null) {
                Optional<Combo> oldCombo = comboService.getComboById(combo.getId());
                if (oldCombo.isPresent()) {
                    combo.setImage(oldCombo.get().getImage());
                }
            }

            if (combo.getId() == null) {
                comboService.createCombo(combo);
            } else {
                comboService.updateCombo(combo.getId(), combo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin/combos/create";
        }

        return "redirect:/admin/combos";
    }

    @GetMapping("/combos/delete/{id}")
    public String deleteCombo(@PathVariable Long id) {
        comboService.deleteCombo(id);
        return "redirect:/admin/combos";
    }

    @GetMapping("/promotions")
    public String promotions(Model model) {
        model.addAttribute("promotions", promotionService.getAllPromotions());
        return "admin/promotions";
    }

    @GetMapping("/promotions/create")
    public String createPromotionForm(Model model) {
        model.addAttribute("promotion", new Promotion());
        return "admin/create-promotion";
    }

    @GetMapping("/promotions/edit/{id}")
    public String editPromotion(@PathVariable Long id, Model model) {
        Optional<Promotion> promotion = promotionService.getPromotionById(id);
        if (promotion.isEmpty()) {
            return "redirect:/admin/promotions";
        }
        model.addAttribute("promotion", promotion.get());
        return "admin/edit-promotion";
    }

    @PostMapping("/promotions/save")
    public String savePromotion(@ModelAttribute Promotion promotion) {
        if (promotion.getId() == null) {
            promotionService.createPromotion(promotion);
        } else {
            promotionService.updatePromotion(promotion.getId(), promotion);
        }
        return "redirect:/admin/promotions";
    }

    @GetMapping("/promotions/delete/{id}")
    public String deletePromotion(@PathVariable Long id) {
        promotionService.deletePromotion(id);
        return "redirect:/admin/promotions";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        model.addAttribute("tickets", ticketService.getAdminTicketHistory());
        model.addAttribute("bookingCount", bookingService.getAllBookings().size());
        model.addAttribute("checkedInTicketCount", ticketService.countCheckedInTickets());
        model.addAttribute("soldTicketCount", ticketService.countSoldTickets());
        model.addAttribute("soldTicketRevenue", ticketService.getSoldTicketRevenue());
        return "admin/orders";
    }

    @GetMapping("/orders/{id}/paid")
    public String markOrderPaid(@PathVariable Long id) {
        orderService.confirmAndPayOrder(id);
        return "redirect:/admin/orders";
    }

    @GetMapping("/orders/{id}/complete")
    public String completeOrder(@PathVariable Long id) {
        orderService.completeOrder(id);
        return "redirect:/admin/orders";
    }

    @GetMapping("/orders/{id}/cancel")
    public String cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return "redirect:/admin/orders";
    }

    @GetMapping("/tickets")
    public String tickets(Model model) {
        model.addAttribute("tickets", ticketService.getAdminTicketHistory());
        model.addAttribute("bookingCount", bookingService.getAllBookings().size());
        model.addAttribute("checkedInTicketCount", ticketService.countCheckedInTickets());
        model.addAttribute("bookedTicketCount", ticketService.countBookedTickets());
        return "admin/tickets-manager";
    }

    @GetMapping("/tickets/check-in")
    public String ticketCheckInPage(@RequestParam(required = false) String ticketCode, Model model) {
        model.addAttribute("scanValue", ticketCode != null ? ticketCode : "");
        return "admin/ticket-checkin";
    }

    @PostMapping("/tickets/check-in")
    public String checkInTicket(@RequestParam(required = false) String scanValue,
                                @RequestParam(required = false) MultipartFile qrFile,
                                Model model) {
        String resolvedScanValue = scanValue;

        if ((resolvedScanValue == null || resolvedScanValue.isBlank())
                && qrFile != null
                && !qrFile.isEmpty()) {
            resolvedScanValue = qrCodeService.decodeQRCode(qrFile);
        }

        if (resolvedScanValue == null || resolvedScanValue.isBlank()) {
            model.addAttribute("resultType", "error");
            model.addAttribute("resultMessage", "H??y nh???p m?? v?? ho???c t???i l??n ???nh QR ????? check-in.");
            model.addAttribute("scanValue", "");
            return "admin/ticket-checkin";
        }

        TicketService.CheckInResult result = ticketService.checkInByScanValue(resolvedScanValue);
        Ticket ticket = result.ticket();

        model.addAttribute("scanValue", resolvedScanValue);
        model.addAttribute("resultType", result.success() ? "success" : "error");
        model.addAttribute("resultMessage", result.message());
        model.addAttribute("checkedTicket", ticket);

        return "admin/ticket-checkin";
    }

    @GetMapping("/payments")
    public String payments(Model model) {
        model.addAttribute("payments", paymentService.getAllPayments());
        return "admin/payments";
    }

    @GetMapping("/bookings")
    public String bookings(Model model) {
        model.addAttribute("bookings", bookingService.getAllBookings());
        return "admin/bookings";
    }
}



