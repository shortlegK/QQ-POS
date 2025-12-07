package com.qqriceball.server.service;

import com.qqriceball.common.exception.AccountInactiveException;
import com.qqriceball.common.exception.AccountNotExistException;
import com.qqriceball.common.exception.PasswordErrorException; 
import com.qqriceball.constant.MessageEnum; 
import com.qqriceball.constant.StatusEnum;
import com.qqriceball.pojo.dto.EmpLoginDTO;
import com.qqriceball.pojo.entity.Emp;
import com.qqriceball.server.mapper.EmpMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class EmpService {

    private final EmpMapper empMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public EmpService(EmpMapper empMapper, PasswordEncoder passwordEncoder) {
        this.empMapper = empMapper;
        this.passwordEncoder = passwordEncoder;
    }


    public Emp login(EmpLoginDTO empLoginDTO) {
        String username = empLoginDTO.getUsername();
        String password = empLoginDTO.getPassword();

        Emp emp = empMapper.getByUsername(username);

        if (emp == null){
            throw new AccountNotExistException(MessageEnum.ACCOUNT_NOT_EXIST);
        }

        if (!passwordEncoder.matches(password,emp.getPassword())){
            throw new PasswordErrorException(MessageEnum.PASSWORD_ERROR);
        }

        if (emp.getStatus().equals(StatusEnum.INACTIVE.getValue())){
            throw new AccountInactiveException(MessageEnum.ACCOUNT_INACTIVE);
        }

        return emp;

    }
}
