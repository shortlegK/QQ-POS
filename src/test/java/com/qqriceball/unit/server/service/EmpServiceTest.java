package com.qqriceball.unit.server.service;


import com.qqriceball.common.exception.AccountInactiveException;
import com.qqriceball.common.exception.AccountNotExistException;
import com.qqriceball.common.exception.AlreadyExistsException;
import com.qqriceball.common.exception.PasswordErrorException;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.enumeration.StatusEnum;
import com.qqriceball.pojo.dto.EmpCreateDTO;
import com.qqriceball.pojo.dto.EmpLoginDTO;
import com.qqriceball.pojo.dto.EmpStatusDTO;
import com.qqriceball.pojo.entity.Emp;
import com.qqriceball.pojo.vo.EmpVO;
import com.qqriceball.server.mapper.EmpMapper;
import com.qqriceball.server.service.EmpService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

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
    @DisplayName("[Unit] EmpService.login - 登入帳號不存在，應拋出 AccountNotExistException")
    void testLoginAccountNotExist() {

        String username = "NotExist";

        EmpLoginDTO empLoginDTO = new EmpLoginDTO();
        empLoginDTO.setUsername(username);

       lenient().when(empMapper.getByUsername(empLoginDTO.getUsername())).thenReturn(null);

        AccountNotExistException ex = assertThrows(AccountNotExistException.class,
                () -> empService.login(empLoginDTO));
        assertEquals(MessageEnum.ACCOUNT_NOT_EXIST.getMessage(), ex.getMessage());

        verify(empMapper).getByUsername(username);

    }

    @Test
    @DisplayName("[Unit] EmpService.login - 登入密碼錯誤，應拋出 PasswordErrorException")
    void testLoginPasswordError() {

        String username = "admin";
        String wrongPassword = "wrongPassword";
        String encodedPassword = "encoded_password";


        EmpLoginDTO empLoginDTO = new EmpLoginDTO();
        empLoginDTO.setUsername(username);
        empLoginDTO.setPassword(wrongPassword);

        Emp fakeEmp = new Emp();
        fakeEmp.setUsername(username);
        fakeEmp.setPassword(encodedPassword);

        when(empMapper.getByUsername(username)).thenReturn(fakeEmp);
        when(passwordEncoder.matches(wrongPassword, fakeEmp.getPassword())).thenReturn(false);

        PasswordErrorException ex = assertThrows(PasswordErrorException.class,
                () -> empService.login(empLoginDTO));
        assertEquals(MessageEnum.PASSWORD_ERROR.getMessage(), ex.getMessage());

        verify(empMapper).getByUsername(empLoginDTO.getUsername());
        verify(passwordEncoder).matches(empLoginDTO.getPassword(), fakeEmp.getPassword());
    }

    @Test
    @DisplayName("[Unit] EmpService.login - 登入帳號已停用，應拋出 AccountInactiveException")
    void testLoginAccountInactive() {

        String username = "admin";
        String rawPassword = "Password";

        EmpLoginDTO empLoginDTO = new EmpLoginDTO();
        empLoginDTO.setUsername(username);
        empLoginDTO.setPassword(rawPassword);

        Emp fakeEmp = new Emp();
        fakeEmp.setUsername(username);
        fakeEmp.setPassword(rawPassword);
        fakeEmp.setStatus(StatusEnum.INACTIVE.getValue());

        when(empMapper.getByUsername(username)).thenReturn(fakeEmp);
        when(passwordEncoder.matches(rawPassword, fakeEmp.getPassword())).thenReturn(true);

        AccountInactiveException ex = assertThrows(AccountInactiveException.class,
                () -> empService.login(empLoginDTO));
        assertEquals(MessageEnum.ACCOUNT_INACTIVE.getMessage(), ex.getMessage());

        verify(empMapper).getByUsername(empLoginDTO.getUsername());
        verify(passwordEncoder).matches(empLoginDTO.getPassword(), fakeEmp.getPassword());
    }


    @Test
    @DisplayName("[Unit] EmpService.login - 登入帳號啟用中且密碼正確，應回傳 Emp 資料")
    void testLoginAccountActive() {

        String username = "admin";
        String rawPassword = "Password";

        EmpLoginDTO empLoginDTO = new EmpLoginDTO();
        empLoginDTO.setUsername(username);
        empLoginDTO.setPassword(rawPassword);

        Emp fakeEmp = new Emp();
        fakeEmp.setUsername(username);
        fakeEmp.setPassword(rawPassword);
        fakeEmp.setStatus(StatusEnum.ACTIVE.getValue());

        when(empMapper.getByUsername(username)).thenReturn(fakeEmp);
        when(passwordEncoder.matches(rawPassword, fakeEmp.getPassword())).thenReturn(true);

        Emp result = empService.login(empLoginDTO);

        assertAll(
                () -> assertEquals(empLoginDTO.getUsername(), result.getUsername()),
                () -> assertEquals(StatusEnum.ACTIVE.getValue(), result.getStatus())
        );

        verify(empMapper).getByUsername(empLoginDTO.getUsername());
        verify(passwordEncoder).matches(empLoginDTO.getPassword(), fakeEmp.getPassword());
    }

    @Test
    @DisplayName("[Unit] EmpService.create - 建立重複帳號，應拋出 AlreadyExistsException")
    void testCreateEmpUsernameDuplicate(){

        String username = "admin";
        String rawPassword = "Password";
        String encodedPassword = "encoded_password";
        EmpCreateDTO empCreateDTO = new EmpCreateDTO();
        empCreateDTO.setUsername(username);
        empCreateDTO.setPassword(rawPassword);

        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        doThrow(new DuplicateKeyException("duplicate"))
                .when(empMapper)
                .insert(any(Emp.class));

        AlreadyExistsException ex = assertThrows(AlreadyExistsException.class,
                () -> empService.create(empCreateDTO));
        assertEquals(MessageEnum.USERNAME_ALREADY_EXIST.getMessage(), ex.getMessage());

        verify(empMapper).insert(any(Emp.class));


    }

    @Test
    @DisplayName("[Unit] EmpService.create - 建立員工，應加密密碼並呼叫 insert 帶入 Emp 資料")
    void testCreateEmpSuccess(){

        String username = "testSuccess";
        String rawPassword = "Password";
        String encodedPassword = "encoded_password";
        String name = "tester";
        LocalDate entryDate = LocalDate.of(2026, 1, 1);
        int role = 1;

        EmpCreateDTO empCreateDTO = new EmpCreateDTO();
        empCreateDTO.setUsername(username);
        empCreateDTO.setPassword(rawPassword);
        empCreateDTO.setName(name);
        empCreateDTO.setRole(role);
        empCreateDTO.setEntryDate(entryDate);

        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        empService.create(empCreateDTO);

        ArgumentCaptor<Emp> empArgumentCaptor = ArgumentCaptor.forClass(Emp.class);
        verify(empMapper).insert(empArgumentCaptor.capture());
        verify(passwordEncoder).encode(rawPassword);

        Emp capturedEmp = empArgumentCaptor.getValue();

        assertAll(
                () -> assertEquals(username,capturedEmp.getUsername(),"username 應與傳入參數相同"),
                () -> assertEquals(encodedPassword,capturedEmp.getPassword(),"password 應為加密後的密碼"),
                () -> assertEquals(name,capturedEmp.getName(),"name 應與傳入參數相同"),
                () -> assertEquals(role,capturedEmp.getRole(),"role 應與傳入參數相同"),
                () -> assertEquals(entryDate,capturedEmp.getEntryDate(),"entryDate 應與傳入參數相同")
        );

    }

    @Test
    @DisplayName("[Unit] EmpService.updateStatus - 員工 id 不存在，應拋出 AccountNotExistException")
    void testUpdateStatusAccountNotExist(){

        EmpStatusDTO empStatusDTO = new EmpStatusDTO();
        Integer id = 1;

        when(empMapper.getById(id)).thenReturn(null);

        AccountNotExistException ex = assertThrows(AccountNotExistException.class,
                () -> empService.updateStatus(empStatusDTO,id));
        assertEquals(MessageEnum.ACCOUNT_NOT_EXIST.getMessage(), ex.getMessage());


        verify(empMapper).getById(id);
        verify(empMapper, never()).updateById(any(Emp.class));

    }

    @ParameterizedTest(name = "[Unit] EmpService.updateStatus - 變更員工啟用狀態，應呼叫 empMapper.updateById")
    @EnumSource(value = StatusEnum.class, names = {"ACTIVE", "INACTIVE"})
    void testUpdateStatusSetInactive(StatusEnum statusEnum){

        EmpStatusDTO empStatusDTO = new EmpStatusDTO();
        empStatusDTO.setStatus(statusEnum.getValue());
        Integer id = 1;

        EmpVO empVO = new EmpVO();
        when(empMapper.getById(id)).thenReturn(empVO);

        empService.updateStatus(empStatusDTO,id);

        ArgumentCaptor<Emp> empArgumentCaptor = ArgumentCaptor.forClass(Emp.class);
        verify(empMapper).updateById(empArgumentCaptor.capture());
        verify(empMapper).getById(id);

        Emp capturedEmp = empArgumentCaptor.getValue();
        assertAll(
                () -> assertEquals(id,capturedEmp.getId(),"id 應與傳入參數相同"),
                () -> assertEquals(statusEnum.getValue(),capturedEmp.getStatus(),"status 應與傳入參數相同")
        );

    }



}