package com.qqriceball.server.service;


import com.qqriceball.common.exception.AccountInactiveException;
import com.qqriceball.common.exception.AccountNotExistException;
import com.qqriceball.common.exception.PasswordErrorException;
import com.qqriceball.constant.MessageEnum;
import com.qqriceball.constant.StatusEnum;
import com.qqriceball.pojo.dto.EmpLoginDTO;
import com.qqriceball.pojo.entity.Emp;
import com.qqriceball.server.mapper.EmpMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpServiceTest {

    @Mock
    private EmpMapper empMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmpService empService;


    @Test
    @DisplayName("登入帳號不存在，應回傳 AccountNotExistException")
    void testLoginAccountNotExist() {

        EmpLoginDTO empLoginDTO = new EmpLoginDTO();
        empLoginDTO.setUsername("NotExist");

       lenient().when(empMapper.getByUsername(empLoginDTO.getUsername())).thenReturn(null);

        AccountNotExistException ex = assertThrows(AccountNotExistException.class,
                () -> empService.login(empLoginDTO));
        assertEquals(MessageEnum.ACCOUNT_NOT_FOUND.getMessage(), ex.getMessage());

        verify(empMapper, times(1)).getByUsername("NotExist");


    }

    @Test
    @DisplayName("登入密碼錯誤，應回傳 PasswordErrorException")
    void testLoginPasswordError() {
        EmpLoginDTO empLoginDTO = new EmpLoginDTO();
        empLoginDTO.setUsername("admin");
        empLoginDTO.setPassword("wrongPassword");

        Emp fakeEmp = new Emp();
        fakeEmp.setUsername("admin");
        fakeEmp.setPassword("encodedPassword");

        when(empMapper.getByUsername(empLoginDTO.getUsername())).thenReturn(fakeEmp);
        when(passwordEncoder.matches(empLoginDTO.getPassword(), fakeEmp.getPassword())).thenReturn(false);

        PasswordErrorException ex = assertThrows(PasswordErrorException.class,
                () -> empService.login(empLoginDTO));
        assertEquals(MessageEnum.PASSWORD_ERROR.getMessage(), ex.getMessage());

        verify(empMapper, times(1)).getByUsername(empLoginDTO.getUsername());
        verify(passwordEncoder, times(1)).matches(empLoginDTO.getPassword(), fakeEmp.getPassword());
    }

    @Test
    @DisplayName("登入帳號已停用，應回傳 AccountInactiveException")
    void testLoginAccountInactive() {
        EmpLoginDTO empLoginDTO = new EmpLoginDTO();
        empLoginDTO.setUsername("admin");
        empLoginDTO.setPassword("Password");

        Emp fakeEmp = new Emp();
        fakeEmp.setUsername("admin");
        fakeEmp.setPassword("Password");
        fakeEmp.setStatus(StatusEnum.INACTIVE.getValue());

        when(empMapper.getByUsername(empLoginDTO.getUsername())).thenReturn(fakeEmp);
        when(passwordEncoder.matches(empLoginDTO.getPassword(), fakeEmp.getPassword())).thenReturn(true);

        AccountInactiveException ex = assertThrows(AccountInactiveException.class,
                () -> empService.login(empLoginDTO));
        assertEquals(MessageEnum.ACCOUNT_INACTIVE.getMessage(), ex.getMessage());

        verify(empMapper, times(1)).getByUsername(empLoginDTO.getUsername());
        verify(passwordEncoder, times(1)).matches(empLoginDTO.getPassword(), fakeEmp.getPassword());
    }


    @Test
    @DisplayName("登入帳號啓用中，應可正常登入回傳 token")
    void testLoginAccountActive() {
        EmpLoginDTO empLoginDTO = new EmpLoginDTO();
        empLoginDTO.setUsername("admin");
        empLoginDTO.setPassword("Password");

        Emp fakeEmp = new Emp();
        fakeEmp.setUsername("admin");
        fakeEmp.setPassword("Password");
        fakeEmp.setStatus(StatusEnum.ACTIVE.getValue());

        when(empMapper.getByUsername(empLoginDTO.getUsername())).thenReturn(fakeEmp);
        when(passwordEncoder.matches(empLoginDTO.getPassword(), fakeEmp.getPassword())).thenReturn(true);

        Emp result = empService.login(empLoginDTO);

        assertAll(
                () -> assertEquals(empLoginDTO.getUsername(), result.getUsername()),
                () -> assertEquals(StatusEnum.ACTIVE.getValue(), result.getStatus())
        );

        verify(empMapper, times(1)).getByUsername(empLoginDTO.getUsername());
        verify(passwordEncoder, times(1)).matches(empLoginDTO.getPassword(), fakeEmp.getPassword());
    }


}