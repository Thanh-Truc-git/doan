package com.example.doanck.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 2000)
    private String description;

    private String poster;

    private String trailerUrl;

    private Integer duration;

}
