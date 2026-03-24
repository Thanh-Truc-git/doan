package com.example.doanck.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.doanck.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

}