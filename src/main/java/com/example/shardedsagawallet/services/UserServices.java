package com.example.shardedsagawallet.services;

import java.util.List;

import org.springframework.stereotype.Service;
import com.example.shardedsagawallet.repositories.UserRepository;
import com.example.shardedsagawallet.entities.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServices {
    
    private final UserRepository userRepository;

    public User createUser(User user){
        System.out.println("=== DEBUG: createUser called ===");
        log.info("Createing user: {}",user.getEmail());
        User newUser = userRepository.save(user);
        log.info("User created with id {} in database shardwallet" , newUser.getId() , (newUser.getId()%2+1));
        return newUser;
    }

    public User getUserId(Long id){
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<User> getUserByName(String name){
        return userRepository.findByNameContainingIgnoreCase(name);
    }
}
