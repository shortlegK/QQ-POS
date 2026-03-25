package com.qqriceball;

import com.qqriceball.enumeration.RoleEnum;
import com.qqriceball.enumeration.StatusEnum;
import com.qqriceball.mapper.EmpMapper;
import com.qqriceball.model.entity.Emp;
import org.springframework.beans.factory.annotation.Value;
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
@Profile("prod")
@Component
@ConditionalOnProperty(prefix = "app", name = "init-admin", havingValue = "true")
public class ProdInitSeeder implements ApplicationRunner {

    @Value("${app.init-admin-username}")
    private String adminUsername;

    @Value("${app.init-admin-password}")
    private String adminPassword;

    private final PasswordEncoder passwordEncoder;
    private final EmpMapper empMapper;

    public ProdInitSeeder(PasswordEncoder passwordEncoder, EmpMapper empMapper) {
        this.passwordEncoder = passwordEncoder;
        this.empMapper = empMapper;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("開始執行 ProdInitSeeder，建立初始管理員帳號");
        seed(adminUsername, adminPassword, RoleEnum.MANAGER.getCode());
        log.info("完成 ProdInitSeeder");
    }

    private void seed(String username, String rawPassword, int role) {
        Emp existing = empMapper.getByUsername(username);
        if (existing == null) {
            Emp emp = new Emp();
            emp.setUsername(username);
            emp.setPassword(passwordEncoder.encode(rawPassword));
            emp.setName("Admin");
            emp.setRole(role);
            emp.setStatus(StatusEnum.ACTIVE.getCode());
            emp.setEntryDate(LocalDate.now());
            emp.setCreateId(1);
            emp.setCreateTime(LocalDateTime.now());
            emp.setUpdateId(1);
            emp.setUpdateTime(LocalDateTime.now());
            empMapper.insert(emp);
            log.info("初始管理員帳號建立完成");
        } else {
            log.info("管理員帳號已存在，略過建立");
        }
    }

}
