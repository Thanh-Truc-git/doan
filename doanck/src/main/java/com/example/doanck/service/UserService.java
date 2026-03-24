package com.example.doanck.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.doanck.model.User;
import com.example.doanck.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // =====================
    // REGISTER USER
    // =====================
    public User register(User user){

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");

        return userRepository.save(user);
    }

    // =====================
    // ADMIN ADD USER
    // =====================
    public User addUser(User user){

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if(user.getRole() == null){
            user.setRole("ROLE_USER");
        }

        return userRepository.save(user);
    }

    // =====================
    // GET ALL USERS
    // =====================
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    // =====================
    // FIND BY USERNAME
    // =====================
    public User findByUsername(String username){
        return userRepository.findByUsername(username);
    }

    // =====================
    // GET USER BY ID
    // =====================
    public User getUserById(Long id){
        return userRepository.findById(id).orElse(null);
    }

    // =====================
    // UPDATE USER
    // =====================
    public void updateUser(Long id, User newUser){

        User user = userRepository.findById(id).orElse(null);

        if(user != null){

            user.setUsername(newUser.getUsername());

            if(newUser.getPassword() != null && !newUser.getPassword().isEmpty()){
                user.setPassword(passwordEncoder.encode(newUser.getPassword()));
            }

            user.setRole(newUser.getRole());

            userRepository.save(user);
        }
    }

    // =====================
    // DELETE USER
    // =====================
    public void deleteUser(Long id){
        userRepository.deleteById(id);
    }

}