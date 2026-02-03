package com.qqriceball;

import com.qqriceball.enumeration.RoleEnum;
import com.qqriceball.enumeration.StatusEnum;
import com.qqriceball.mapper.EmpMapper;
import com.qqriceball.model.entity.Emp;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
        seed("demomanager", "Qq!pos3426", RoleEnum.MANAGER.getValue());
        seed("demostaff", "Qq!pos3426", RoleEnum.STAFF.getValue());
    }

    private void seed(String username, String rawPassword, int role) {
        String encoded = passwordEncoder.encode(rawPassword);
        Emp existing = empMapper.getByUsername(username);

        if (existing == null) {
            existing.setUsername(username);
            existing.setPassword(encoded);
            existing.setName(username);
            existing.setRole(role);
            existing.setStatus(StatusEnum.ACTIVE.getValue());
            existing.setEntryDate(LocalDate.now());
            existing.setCreateId(1);
            existing.setCreateTime(LocalDateTime.now());
            existing.setUpdateId(1);
            existing.setUpdateTime(LocalDateTime.now());

            empMapper.insert(existing);
        } else {
            existing.setPassword(encoded);
            existing.setRole(role);
            existing.setStatus(StatusEnum.ACTIVE.getValue());

            empMapper.updateById(existing);
        }
    }
}
