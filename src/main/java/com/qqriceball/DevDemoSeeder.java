package com.qqriceball;

import com.qqriceball.enumeration.RoleEnum;
import com.qqriceball.enumeration.StatusEnum;
import com.qqriceball.mapper.EmpMapper;
import com.qqriceball.model.entity.Emp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Profile("dev")
@Component
@ConditionalOnProperty(prefix = "app", name = "seed-demo", havingValue = "true")
public class DevDemoSeeder implements ApplicationRunner {

    private final PasswordEncoder passwordEncoder;
    private final EmpMapper empMapper;

    public DevDemoSeeder(PasswordEncoder passwordEncoder, EmpMapper empMapper) {
        this.passwordEncoder = passwordEncoder;
        this.empMapper = empMapper;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("開始執行 DevDemoSeeder，為開發環境建立基本使用帳號");
        seed("demomanager", "Qq!pos3426", RoleEnum.MANAGER.getCode());
        seed("demostaff", "Qq!pos3426", RoleEnum.STAFF.getCode());
        log.info("完成 DevDemoSeeder，帳號已建立完成");
    }

    private void seed(String username, String rawPassword, int role) {
        String encoded = passwordEncoder.encode(rawPassword);
        Emp existing = empMapper.getByUsername(username);

        if (existing == null) {
            Emp emp = new Emp();
            emp.setUsername(username);
            emp.setPassword(encoded);
            emp.setName(username);
            emp.setRole(role);
            emp.setStatus(StatusEnum.ACTIVE.getCode());
            emp.setEntryDate(LocalDate.now());
            emp.setCreateId(1);
            emp.setCreateTime(LocalDateTime.now());
            emp.setUpdateId(1);
            emp.setUpdateTime(LocalDateTime.now());

            empMapper.insert(emp);
        } else {
            existing.setPassword(encoded);
            existing.setRole(role);
            existing.setStatus(StatusEnum.ACTIVE.getCode());

            empMapper.updateById(existing);
        }
    }
}
