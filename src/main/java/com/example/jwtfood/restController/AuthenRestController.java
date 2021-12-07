package com.example.jwtfood.restController;

import com.example.jwtfood.entity.ERole;
import com.example.jwtfood.entity.Role;
import com.example.jwtfood.entity.User;
import com.example.jwtfood.payload.request.LoginRequest;
import com.example.jwtfood.payload.request.SignupRequest;
import com.example.jwtfood.payload.response.JwtResponse;
import com.example.jwtfood.payload.response.MessageResponse;
import com.example.jwtfood.repository.RoleRepository;
import com.example.jwtfood.services.AccountService;
import com.example.jwtfood.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthenRestController {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AccountService accountService;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest login) {
        User user;
        try {
            user = accountService.getByUsername(login.getUsername());
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new ResponseEntity<>("Username not exist", HttpStatus.UNAUTHORIZED);
        }

        if (!encoder.matches(login.getPassword(), user.getPassword()))
            return new ResponseEntity<>("Password error", HttpStatus.UNAUTHORIZED);

        String token = jwtUtil.generateToken(login.getUsername());
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(token, user.getId(), user.getUsername(), roles));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody SignupRequest signup) {
        // Persist user to some persistent storage

        if (accountService.existsByUsername(signup.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }
        User user = new User(signup.getUsername(), encoder.encode(signup.getPassword()));
        Set<String> strRoles = signup.getRole();
        Set<Role> roles = new HashSet<>();
        if (strRoles == null) {
            Role userRole = accountService.findByName(ERole.ROLE_USER);
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role.toLowerCase()) {
                    case "admin":
                        Role adminRole = accountService.findByName(ERole.ROLE_ADMIN);
                        roles.add(adminRole);
                        break;
                    default:
                        Role userRole = accountService.findByName(ERole.ROLE_USER);
                        roles.add(userRole);
                }
            });
        }
        user.setRoles(roles);
        accountService.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PostMapping("/validateToken")
    public ResponseEntity<Boolean> validateToken(@RequestBody String token) {
        return new ResponseEntity<>(jwtUtil.validateJwtToken(token), HttpStatus.OK);
    }

    @PostMapping("/getClaims")
    public ResponseEntity<String> getClaims(@RequestBody String token) {
        return new ResponseEntity<>(jwtUtil.getClaims(token).getSubject(), HttpStatus.OK);
    }
}
