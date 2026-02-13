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
                RoleEnum.MANAGER.getCode(), StatusEnum.ACTIVE.getCode());

        createUser(SeedUserData.STAFF.username(),SeedUserData.STAFF.password(),
                RoleEnum.STAFF.getCode(), StatusEnum.ACTIVE.getCode());

        createUser(SeedUserData.INACTIVE.username(),SeedUserData.INACTIVE.password(),
                RoleEnum.STAFF.getCode(), StatusEnum.INACTIVE.getCode());

        createUser(SeedUserData.TESTER.username(), SeedUserData.TESTER.password(),
                RoleEnum.STAFF.getCode(), StatusEnum.ACTIVE.getCode());
    }

    private void createUser(String username, String rawPassword, int role, int status) {

        Emp existing = empMapper.getByUsername(username);
        String encoded = passwordEncoder.encode(rawPassword);

        if (existing == null) {
            Emp emp = new Emp();
            emp.setUsername(username);
            emp.setPassword(encoded);
            emp.setName(username);
            emp.setRole(role);
            emp.setStatus(status);
            emp.setEntryDate(LocalDate.now());
            emp.setCreateId(1);
            emp.setCreateTime(LocalDateTime.now());
            emp.setUpdateId(1);
            emp.setUpdateTime(LocalDateTime.now());

            empMapper.insert(emp);
        } else {
            existing.setPassword(encoded);
            existing.setRole(role);
            existing.setStatus(status);
            empMapper.updateById(existing);
        }
    }
}