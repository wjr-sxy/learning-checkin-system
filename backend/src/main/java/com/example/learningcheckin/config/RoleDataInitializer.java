package com.example.learningcheckin.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
public class RoleDataInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Checking role data...");
        try {
            String encodedPassword = passwordEncoder.encode("123456");
            
            // Check if teacher exists
            Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM sys_user WHERE username = 'teacher'", Integer.class);
            
            if (count != null && count == 0) {
                 jdbcTemplate.update("INSERT INTO `sys_user` (`username`, `password`, `role`, `points`) VALUES (?, ?, ?, ?)",
                         "teacher", encodedPassword, "TEACHER", 0);
                 System.out.println("Created test teacher user.");
            } else {
                // Update password to ensure it matches 123456
                jdbcTemplate.update("UPDATE `sys_user` SET `password` = ? WHERE `username` = 'teacher'", encodedPassword);
                System.out.println("Updated teacher user password.");
            }
        } catch (Exception e) {
             e.printStackTrace();
        }
    }
}
