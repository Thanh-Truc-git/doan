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

    public User register(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        return userRepository.save(user);
    }

    public User addUser(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User updateUser(Long id, User newUser){
        User user = userRepository.findById(id).orElse(null);

        if(user != null){
            user.setUsername(newUser.getUsername());
            user.setEmail(newUser.getEmail());
            user.setRole(newUser.getRole());

            if(newUser.getPassword() != null && !newUser.getPassword().isEmpty()){
                user.setPassword(passwordEncoder.encode(newUser.getPassword()));
            }

            return userRepository.save(user);
        }

        return null;
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public User findByUsername(String username){
        return userRepository.findByUsername(username);
    }

    public boolean changePassword(String username, String currentPassword, String newPassword){
        User user = userRepository.findByUsername(username);
        if(user == null){
            return false;
        }
        if(!passwordEncoder.matches(currentPassword, user.getPassword())){
            return false;
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }

    public User getUserById(Long id){
        return userRepository.findById(id).orElse(null);
    }

    public void deleteUser(Long id){
        userRepository.deleteById(id);
    }
}
