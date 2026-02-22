package com.example.shardedsagawallet.repositories;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import com.example.shardedsagawallet.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User,Long>{

    List<User> findByNameContainingIgnoreCase(String name);
} 
