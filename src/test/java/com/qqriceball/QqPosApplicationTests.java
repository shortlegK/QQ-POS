package com.qqriceball;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class QqPosApplicationTests {

    @Test
    void contextLoads() {
    }

//    @Autowired
//    private PasswordEncoder passwordEncoder;
//    @Test
//    void testGeneratePasswordHash() {
//        String rawPassword = "123456";
//        String encodedPassword = passwordEncoder.encode(rawPassword);
//
//        System.out.println("原始密碼: " + rawPassword);
//        System.out.println("BCrypt 雜湊值: " + encodedPassword);
//    }
}
