package com.example.jwtfood.services;


import com.example.jwtfood.entity.ERole;
import com.example.jwtfood.entity.Role;
import com.example.jwtfood.entity.User;
import com.example.jwtfood.repository.RoleRepository;
import com.example.jwtfood.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;


    @Transactional
    public User getByUsername(String username) throws Exception {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new Exception("User Not Found with username: " + username));
        return user;
    }

    public Boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public Role findByName(ERole roleUser) {
        Role role;
        try {
            role = roleRepository.findByName(roleUser).get();
        }catch (Exception e){
            e.printStackTrace();
            role = roleRepository.save(new Role(roleUser));
        }
        return role;
    }
}