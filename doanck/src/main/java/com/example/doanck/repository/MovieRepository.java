package com.example.doanck.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.doanck.model.Movie;

public interface MovieRepository extends JpaRepository<Movie,Long>{

}