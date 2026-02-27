package com.qqriceball.integration.testData;

import com.qqriceball.mapper.EmpMapper;
import com.qqriceball.model.entity.Emp;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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
        createUser(SeedUserData.MANAGER);
        createUser(SeedUserData.STAFF);
        createUser(SeedUserData.INACTIVE);
        createUser(SeedUserData.TESTER);
    }

    private void createUser(TestAccount testAccount) {

        Emp existing = empMapper.getByUsername(testAccount.username());
        String encoded = passwordEncoder.encode(testAccount.password());

        if (existing == null) {
            Emp emp = new Emp();
            emp.setUsername(testAccount.username());
            emp.setPassword(encoded);
            emp.setName(testAccount.username());
            emp.setRole(testAccount.role());
            emp.setStatus(testAccount.status());
            emp.setEntryDate(testAccount.entryDate());
            emp.setCreateId(1);
            emp.setCreateTime(LocalDateTime.now());
            emp.setUpdateId(1);
            emp.setUpdateTime(LocalDateTime.now());

            empMapper.insert(emp);
        } else {
            existing.setPassword(encoded);
            existing.setRole(testAccount.role());
            existing.setStatus(testAccount.status());
            empMapper.updateById(existing);
        }
    }
}