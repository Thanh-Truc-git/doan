package com.example.doanck.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.doanck.model.Movie;
import com.example.doanck.model.Room;
import com.example.doanck.model.Showtime;
import com.example.doanck.model.User;
import com.example.doanck.service.MovieService;
import com.example.doanck.service.RoomService;
import com.example.doanck.service.ShowtimeService;
import com.example.doanck.service.UserService;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private MovieService movieService;

    @Autowired
    private ShowtimeService showtimeService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoomService roomService;

    // ========================
    // DASHBOARD
    // ========================

    @GetMapping("/dashboard")
    public String dashboard(){
        return "admin/dashboard";
    }

    // ========================
    // MOVIE CRUD
    // ========================

    @GetMapping("/movies")
    public String movies(Model model){

        model.addAttribute("movies", movieService.getAllMovies());

        return "admin/movies";
    }

    @GetMapping("/movies/create")
    public String createMovie(Model model){

        model.addAttribute("movie", new Movie());

        return "admin/create-movie";
    }

    @PostMapping("/movies/save")
    public String saveMovie(@ModelAttribute Movie movie,
                            @RequestParam("posterFile") MultipartFile file)
            throws IOException {

        if(file != null && !file.isEmpty()){

            String uploadDir = System.getProperty("user.dir") + "/uploads/";

            File dir = new File(uploadDir);
            if(!dir.exists()){
                dir.mkdirs();
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            File saveFile = new File(uploadDir + fileName);

            file.transferTo(saveFile);

            movie.setPoster(fileName);
        }

        movieService.saveMovie(movie);

        return "redirect:/admin/movies";
    }

    @GetMapping("/movies/edit/{id}")
    public String editMovie(@PathVariable Long id, Model model){

        model.addAttribute("movie", movieService.getMovieById(id));

        return "admin/edit-movie";
    }

    @PostMapping("/movies/update/{id}")
    public String updateMovie(@PathVariable Long id,
                              @ModelAttribute Movie movie,
                              @RequestParam(value="posterFile", required=false) MultipartFile file)
            throws IOException {

        Movie oldMovie = movieService.getMovieById(id);

        if(oldMovie == null){
            return "redirect:/admin/movies";
        }

        if(file != null && !file.isEmpty()){

            String uploadDir = System.getProperty("user.dir") + "/uploads/";

            File dir = new File(uploadDir);
            if(!dir.exists()){
                dir.mkdirs();
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            File saveFile = new File(uploadDir + fileName);

            file.transferTo(saveFile);

            movie.setPoster(fileName);

        } else {

            movie.setPoster(oldMovie.getPoster());

        }

        movie.setId(id);

        movieService.saveMovie(movie);

        return "redirect:/admin/movies";
    }

    // ========================
    // SHOWTIME CRUD
    // ========================

    @GetMapping("/showtimes")
    public String showtimes(Model model){

        model.addAttribute("showtimes", showtimeService.getAllShowtimes());

        return "admin/showtimes";
    }

    @GetMapping("/showtimes/create")
    public String createShowtime(Model model){

        model.addAttribute("showtime", new Showtime());
        model.addAttribute("movies", movieService.getAllMovies());
        model.addAttribute("rooms", roomService.getAllRooms());

        return "admin/create-showtime";
    }

    @PostMapping("/showtimes/save")
    public String saveShowtime(@ModelAttribute Showtime showtime){

        showtimeService.save(showtime);

        return "redirect:/admin/showtimes";
    }

    @GetMapping("/showtimes/edit/{id}")
    public String editShowtime(@PathVariable Long id, Model model){

        model.addAttribute("showtime", showtimeService.getShowtimeById(id));
        model.addAttribute("movies", movieService.getAllMovies());
        model.addAttribute("rooms", roomService.getAllRooms());

        return "admin/edit-showtime";
    }

    @PostMapping("/showtimes/update/{id}")
    public String updateShowtime(@PathVariable Long id,
                                 @ModelAttribute Showtime showtime){

        showtime.setId(id);

        showtimeService.save(showtime);

        return "redirect:/admin/showtimes";
    }

    @GetMapping("/showtimes/delete/{id}")
    public String deleteShowtime(@PathVariable Long id){

        showtimeService.delete(id);

        return "redirect:/admin/showtimes";
    }

    // ========================
    // ROOM CRUD
    // ========================

    @GetMapping("/rooms")
    public String rooms(Model model){

        model.addAttribute("rooms", roomService.getAllRooms());

        return "admin/rooms";
    }

    @GetMapping("/rooms/create")
    public String createRoom(Model model){

        model.addAttribute("room", new Room());

        return "admin/create-room";
    }

    @PostMapping("/rooms/save")
    public String saveRoom(@ModelAttribute Room room){

        roomService.save(room);

        return "redirect:/admin/rooms";
    }

    @GetMapping("/rooms/edit/{id}")
    public String editRoom(@PathVariable Long id, Model model){

        model.addAttribute("room", roomService.getRoomById(id));

        return "admin/edit-room";
    }

    @PostMapping("/rooms/update/{id}")
    public String updateRoom(@PathVariable Long id,
                             @ModelAttribute Room room){

        room.setId(id);

        roomService.save(room);

        return "redirect:/admin/rooms";
    }

    @GetMapping("/rooms/delete/{id}")
    public String deleteRoom(@PathVariable Long id){

        roomService.delete(id);

        return "redirect:/admin/rooms";
    }

    // ========================
    // USER CRUD
    // ========================

    @GetMapping("/users")
    public String users(Model model){

        model.addAttribute("users", userService.getAllUsers());

        return "admin/users";
    }

    @GetMapping("/users/edit/{id}")
    public String editUser(@PathVariable Long id, Model model){

        model.addAttribute("user", userService.getUserById(id));

        return "admin/edit-user";
    }

    @PostMapping("/users/update/{id}")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute User user){

        userService.updateUser(id, user);

        return "redirect:/admin/users";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id){

        userService.deleteUser(id);

        return "redirect:/admin/users";
    }

}