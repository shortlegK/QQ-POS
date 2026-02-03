package com.qqriceball.integration.testData;

import com.qqriceball.enumeration.RoleEnum;
import com.qqriceball.enumeration.StatusEnum;
import com.qqriceball.mapper.EmpMapper;
import com.qqriceball.model.entity.Emp;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Profile("test")
@Component
public class TestDataSeeder implements ApplicationRunner {

    private final PasswordEncoder passwordEncoder;
    private final EmpMapper empMapper;

    public TestDataSeeder(PasswordEncoder passwordEncoder, EmpMapper empMapper) {
        this.passwordEncoder = passwordEncoder;
        this.empMapper = empMapper;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        createUser(SeedUserData.MANAGER.username(),SeedUserData.MANAGER.password(),
                RoleEnum.MANAGER.getValue(), StatusEnum.ACTIVE.getValue());

        createUser(SeedUserData.STAFF.username(),SeedUserData.STAFF.password(),
                RoleEnum.STAFF.getValue(), StatusEnum.ACTIVE.getValue());

        createUser(SeedUserData.INACTIVE.username(),SeedUserData.INACTIVE.password(),
                RoleEnum.STAFF.getValue(), StatusEnum.INACTIVE.getValue());

        createUser(SeedUserData.TESTER.username(), SeedUserData.TESTER.password(),
                RoleEnum.STAFF.getValue(), StatusEnum.ACTIVE.getValue());
    }

    private void createUser(String username, String rawPassword, int role, int status) {

        Emp existing = empMapper.getByUsername(username);
        String encoded = passwordEncoder.encode(rawPassword);

        if (existing == null) {
            existing.setUsername(username);
            existing.setPassword(encoded);
            existing.setName(username);
            existing.setRole(role);
            existing.setStatus(status);
            existing.setEntryDate(LocalDate.now());
            existing.setCreateId(1);
            existing.setCreateTime(LocalDateTime.now());
            existing.setUpdateId(1);
            existing.setUpdateTime(LocalDateTime.now());

            empMapper.insert(existing);
        } else {
            existing.setPassword(encoded);
            existing.setRole(role);
            existing.setStatus(status);
            empMapper.updateById(existing);
        }
    }
}