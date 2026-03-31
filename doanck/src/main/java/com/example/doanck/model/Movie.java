package com.example.doanck.model;

import jakarta.persistence.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Entity
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String genre;
    private String description;
    private String poster;
    private String trailerUrl;
    private Integer duration;

    // ✅ FIX: Thêm cascade và orphanRemoval để xóa phim không bị lỗi 500
    @OneToMany(mappedBy = "movie", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Showtime> showtimes;

    @Transient
    private MultipartFile posterFile;

    // --- Getters & Setters giữ nguyên ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }
    public String getTrailerUrl() { return trailerUrl; }
    public void setTrailerUrl(String trailerUrl) { this.trailerUrl = trailerUrl; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public MultipartFile getPosterFile() { return posterFile; }
    public void setPosterFile(MultipartFile posterFile) { this.posterFile = posterFile; }
    public List<Showtime> getShowtimes() { return showtimes; }
    public void setShowtimes(List<Showtime> showtimes) { this.showtimes = showtimes; }
}