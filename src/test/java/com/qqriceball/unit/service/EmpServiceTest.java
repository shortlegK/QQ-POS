package com.qqriceball.unit.service;


import com.github.pagehelper.Page;
import com.qqriceball.common.exception.*;
import com.qqriceball.common.result.PageResult;
import com.qqriceball.enumeration.MessageEnum;
import com.qqriceball.enumeration.RoleEnum;
import com.qqriceball.enumeration.StatusEnum;
import com.qqriceball.model.dto.emp.*;
import com.qqriceball.model.entity.Emp;
import com.qqriceball.model.vo.emp.EmpVO;
import com.qqriceball.mapper.EmpMapper;
import com.qqriceball.service.EmpService;
import com.qqriceball.testData.emp.SeedUserData;
import com.qqriceball.utils.emp.EmpTestDataFactory;
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
    @DisplayName("[Unit] EmpService.login() - 登入帳號不存在，應拋出 ResourceNotFoundException")
    void testLoginAccountNotExist() {

        EmpLoginDTO empLoginDTO = EmpTestDataFactory.getEmpLoginDTO(SeedUserData.TESTER.username(), SeedUserData.TESTER.password());

        lenient().when(empMapper.getByUsername(empLoginDTO.getUsername())).thenReturn(null);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> empService.login(empLoginDTO));
        assertEquals(MessageEnum.ACCOUNT_NOT_EXISTS.getMessage(), ex.getMessage());

        verify(empMapper).getByUsername(SeedUserData.TESTER.username());

    }

    @Test
    @DisplayName("[Unit] EmpService.login() - 登入密碼錯誤，應拋出 PasswordErrorException")
    void testLoginPasswordError() {

        EmpLoginDTO empLoginDTO = EmpTestDataFactory.getEmpLoginDTO(SeedUserData.TESTER.username(), SeedUserData.TESTER.password());

        Emp fakeEmp = EmpTestDataFactory.getEmp(SeedUserData.TESTER);

        when(empMapper.getByUsername(any(String.class))).thenReturn(fakeEmp);
        when(passwordEncoder.matches(empLoginDTO.getPassword(), fakeEmp.getPassword())).thenReturn(false);

        PasswordErrorException ex = assertThrows(PasswordErrorException.class,
                () -> empService.login(empLoginDTO));
        assertEquals(MessageEnum.PASSWORD_ERROR.getMessage(), ex.getMessage());

        verify(empMapper).getByUsername(empLoginDTO.getUsername());
        verify(passwordEncoder).matches(empLoginDTO.getPassword(), fakeEmp.getPassword());
    }

    @Test
    @DisplayName("[Unit] EmpService.login() - 登入帳號已停用，應拋出 AccountInactiveException")
    void testLoginAccountInactive() {

        EmpLoginDTO empLoginDTO = EmpTestDataFactory.getEmpLoginDTO(SeedUserData.INACTIVE.username(), SeedUserData.TESTER.password());

        Emp fakeEmp = EmpTestDataFactory.getEmp(SeedUserData.INACTIVE);

        when(empMapper.getByUsername(any(String.class))).thenReturn(fakeEmp);
        when(passwordEncoder.matches(empLoginDTO.getPassword(), fakeEmp.getPassword())).thenReturn(true);

        AccountInactiveException ex = assertThrows(AccountInactiveException.class,
                () -> empService.login(empLoginDTO));
        assertEquals(MessageEnum.ACCOUNT_INACTIVE.getMessage(), ex.getMessage());

        verify(empMapper).getByUsername(empLoginDTO.getUsername());
        verify(passwordEncoder).matches(empLoginDTO.getPassword(), fakeEmp.getPassword());
    }


    @Test
    @DisplayName("[Unit] EmpService.login() - 登入帳號啟用中且密碼正確，應回傳 Emp 資料")
    void testLoginAccountActive() {

        EmpLoginDTO empLoginDTO = EmpTestDataFactory.getEmpLoginDTO(SeedUserData.TESTER.username(), SeedUserData.TESTER.password());

        Emp fakeEmp = EmpTestDataFactory.getEmp(SeedUserData.TESTER);

        when(empMapper.getByUsername(any(String.class))).thenReturn(fakeEmp);
        when(passwordEncoder.matches(empLoginDTO.getPassword(), fakeEmp.getPassword())).thenReturn(true);

        Emp result = empService.login(empLoginDTO);

        assertAll(
                () -> assertEquals(empLoginDTO.getUsername(), result.getUsername()),
                () -> assertEquals(StatusEnum.ACTIVE.getCode(), result.getStatus())
        );

        verify(empMapper).getByUsername(empLoginDTO.getUsername());
        verify(passwordEncoder).matches(empLoginDTO.getPassword(), fakeEmp.getPassword());
    }

    @Test
    @DisplayName("[Unit] EmpService.create() - 建立重複帳號，應拋出 AlreadyExistsException")
    void testCreateEmpUsernameDuplicate() {

        EmpCreateDTO empCreateDTO = EmpTestDataFactory.getEmpCreateDTO(SeedUserData.TESTER);

        when(passwordEncoder.encode(any(String.class))).thenReturn(SeedUserData.TESTER.password());

        doThrow(new DuplicateKeyException("duplicate"))
                .when(empMapper)
                .insert(any(Emp.class));

        AlreadyExistsException ex = assertThrows(AlreadyExistsException.class,
                () -> empService.create(empCreateDTO));
        assertEquals(MessageEnum.USERNAME_ALREADY_EXISTS.getMessage(), ex.getMessage());

        verify(empMapper).insert(any(Emp.class));
    }

    @Test
    @DisplayName("[Unit] EmpService.create() - 建立員工，應加密密碼並呼叫 insert 帶入 Emp 資料")
    void testCreateEmpSuccess() {

        EmpCreateDTO empCreateDTO = EmpTestDataFactory.getEmpCreateDTO(SeedUserData.TESTER);

        when(passwordEncoder.encode(any(String.class))).thenReturn(SeedUserData.TESTER.password());

        empService.create(empCreateDTO);

        ArgumentCaptor<Emp> empArgumentCaptor = ArgumentCaptor.forClass(Emp.class);
        verify(empMapper).insert(empArgumentCaptor.capture());
        verify(passwordEncoder).encode(SeedUserData.TESTER.password());

        Emp capturedEmp = empArgumentCaptor.getValue();

        assertAll(
                () -> assertEquals(empCreateDTO.getUsername(), capturedEmp.getUsername(), "username 應與傳入參數相同"),
                () -> assertEquals(empCreateDTO.getPassword(), capturedEmp.getPassword(), "password 應為加密後的密碼"),
                () -> assertEquals(empCreateDTO.getName(), capturedEmp.getName(), "name 應與傳入參數相同"),
                () -> assertEquals(empCreateDTO.getRole(), capturedEmp.getRole(), "role 應與傳入參數相同"),
                () -> assertEquals(empCreateDTO.getEntryDate(), capturedEmp.getEntryDate(), "entryDate 應與傳入參數相同")
        );

    }

    @Test
    @DisplayName("[Unit] EmpService.updateStatus() - 員工 id 不存在，應拋出 ResourceNotFoundException")
    void testUpdateStatusAccountNotExist() {

        EmpStatusDTO empStatusDTO = new EmpStatusDTO();
        Integer id = 1;

        when(empMapper.getById(id)).thenReturn(null);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> empService.updateStatus(empStatusDTO, id));
        assertEquals(MessageEnum.ACCOUNT_NOT_EXISTS.getMessage(), ex.getMessage());


        verify(empMapper).getById(id);
        verify(empMapper, never()).updateById(any(Emp.class));

    }

    @ParameterizedTest(name = "[Unit] EmpService.updateStatus() - 變更員工啟用狀態，應呼叫 empMapper.updateById")
    @EnumSource(value = StatusEnum.class, names = {"ACTIVE", "INACTIVE"})
    void testUpdateStatusSetInactive(StatusEnum statusEnum) {

        EmpStatusDTO empStatusDTO = new EmpStatusDTO();
        empStatusDTO.setStatus(statusEnum.getCode());
        Integer id = 1;

        EmpVO empVO = new EmpVO();
        when(empMapper.getById(id)).thenReturn(empVO);

        empService.updateStatus(empStatusDTO, id);

        ArgumentCaptor<Emp> empArgumentCaptor = ArgumentCaptor.forClass(Emp.class);
        verify(empMapper).updateById(empArgumentCaptor.capture());
        verify(empMapper).getById(id);

        Emp capturedEmp = empArgumentCaptor.getValue();
        assertAll(
                () -> assertEquals(id, capturedEmp.getId(), "id 應與傳入參數相同"),
                () -> assertEquals(statusEnum.getCode(), capturedEmp.getStatus(), "status 應與傳入參數相同")
        );

    }


    @Test
    @DisplayName("[Unit] EmpService.getById() - 員工 id 不存在，應拋出 ResourceNotFoundException")
    void testGetByIdAccountNotExist() {

        Integer id = Integer.MAX_VALUE;

        when(empMapper.getById(id)).thenReturn(null);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> empService.getById(id));
        assertEquals(MessageEnum.ACCOUNT_NOT_EXISTS.getMessage(), ex.getMessage());

        verify(empMapper).getById(id);

    }

    @Test
    @DisplayName("[Unit] EmpService.getById() - 員工 id 存在，應呼叫 empMapper.getById 回傳 EmpVO 資料")
    void testGetByIdAccountExist() {

        Integer id = 1;

        EmpVO empVO = new EmpVO();
        empVO.setId(id);
        empVO.setUsername("getById");
        empVO.setName("getById");
        empVO.setRole(RoleEnum.STAFF.getCode());
        empVO.setStatus(StatusEnum.ACTIVE.getCode());

        when(empMapper.getById(id)).thenReturn(empVO);

        EmpVO result = empService.getById(id);

        assertAll(
                () -> assertEquals(id, result.getId(), "id 應與傳入參數相同"),
                () -> assertEquals("getById", result.getUsername(), "username 應與傳入參數相同"),
                () -> assertEquals("getById", result.getName(), "name 應與傳入參數相同")
        );

        verify(empMapper).getById(id);

    }


    @Test
    @DisplayName("[Unit] EmpService.updateById() - 員工 id 不存在，應拋出 ResourceNotFoundException")
    void testUpdateByIdAccountNotExist() {

        EmpEditDTO empEditDTO = new EmpEditDTO();
        empEditDTO.setId(Integer.MAX_VALUE);

        when(empMapper.getById(Integer.MAX_VALUE)).thenReturn(null);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> empService.updateById(empEditDTO));
        assertEquals(MessageEnum.ACCOUNT_NOT_EXISTS.getMessage(), ex.getMessage());

        verify(empMapper).getById(Integer.MAX_VALUE);
        verify(empMapper, never()).updateById(any(Emp.class));

    }

    @Test
    @DisplayName("[Unit] EmpService.updateById() - 變更員工資料，應呼叫 empMapper.updateById")
    void testUpdateByIdSuccess() {

        EmpEditDTO empEditDTO = EmpTestDataFactory.getEmpEditDTO(SeedUserData.TESTER);

        EmpVO empVO = EmpTestDataFactory.getEmpVO(SeedUserData.TESTER);
        when(empMapper.getById(any(Integer.class))).thenReturn(empVO);

        empService.updateById(empEditDTO);

        ArgumentCaptor<Emp> empArgumentCaptor = ArgumentCaptor.forClass(Emp.class);
        verify(empMapper).updateById(empArgumentCaptor.capture());
        verify(empMapper, times(2)).getById(empEditDTO.getId());

        Emp capturedEmp = empArgumentCaptor.getValue();
        assertAll(
                () -> assertEquals(empEditDTO.getId(), capturedEmp.getId(), "id 應與傳入參數相同"),
                () -> assertEquals(empEditDTO.getName(), capturedEmp.getName(), "name 應與傳入參數相同")
        );
    }

    @Test
    @DisplayName("[Unit] EmpService.pageQuery() - 分頁查詢成功，應回傳 PageResult 資料")
    void testPageQuerySuccess() {
        Integer page = 1;
        Integer pageSize = 5;
        String name = "emp";

        EmpPageQueryDTO empPageQueryDTO = EmpTestDataFactory.getEmpPageQueryDTO(page, pageSize, name);

        EmpVO data1 = EmpTestDataFactory.getEmpVO(SeedUserData.TESTER);
        EmpVO data2 = EmpTestDataFactory.getEmpVO(SeedUserData.STAFF);

        Page<EmpVO> mockPage = new Page<>(page, pageSize);
        mockPage.setTotal(2L);
        mockPage.add(data1);
        mockPage.add(data2);

        when(empMapper.pageQuery(any(EmpPageQueryDTO.class))).thenReturn(mockPage);

        PageResult result = empService.pageQuery(empPageQueryDTO);

        assertAll(
                () -> assertEquals(page, result.getPage()),
                () -> assertEquals(pageSize, result.getPageSize()),
                () -> assertEquals(2L, result.getTotal()),
                () -> assertEquals(mockPage.getResult(), result.getRecords())
        );

        verify(empMapper).pageQuery(any(EmpPageQueryDTO.class));
    }

    @Test
    @DisplayName("[Unit] EmpService.updatePassword() - 更新密碼，舊密碼錯誤，應拋出 PasswordErrorException")
    void testUpdatePasswordOldPasswordError() {
        String currentUserName = SeedUserData.TESTER.username();
        String oldPassword = "wrongOldPassword";
        String newPassword = "newPassword";

        EmpUpdatePasswordDTO empUpdatePasswordDTO = EmpTestDataFactory.getEmpUpdatePasswordDTO(oldPassword, newPassword);
        Emp emp = EmpTestDataFactory.getEmp(SeedUserData.TESTER);

        when(empMapper.getByUsername(currentUserName)).thenReturn(emp);
        when(passwordEncoder.matches(oldPassword, emp.getPassword())).thenReturn(false);

        BadRequestArgsException ex = assertThrows(BadRequestArgsException.class,
                () -> empService.updatePassword(currentUserName, empUpdatePasswordDTO));
        assertEquals(MessageEnum.OLD_PASSWORD_ERROR.getMessage(), ex.getMessage());

        verify(empMapper).getByUsername(currentUserName);
        verify(passwordEncoder).matches(oldPassword, emp.getPassword());
    }

    @Test
    @DisplayName("[Unit] EmpService.updatePassword() - 更新密碼成功，應加密新密碼並呼叫 empMapper.updateById")
    void testUpdatePasswordSuccess() {
        String currentUserName = SeedUserData.TESTER.username();
        String oldPassword = SeedUserData.TESTER.password();
        String newPassword = "newPassword";

        EmpUpdatePasswordDTO empUpdatePasswordDTO = EmpTestDataFactory.getEmpUpdatePasswordDTO(oldPassword, newPassword);
        Emp emp = EmpTestDataFactory.getEmp(SeedUserData.TESTER);

        when(empMapper.getByUsername(currentUserName)).thenReturn(emp);
        when(passwordEncoder.matches(oldPassword, SeedUserData.TESTER.password())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");

        empService.updatePassword(currentUserName, empUpdatePasswordDTO);

        ArgumentCaptor<Emp> empArgumentCaptor = ArgumentCaptor.forClass(Emp.class);
        verify(empMapper).updateById(empArgumentCaptor.capture());
        verify(empMapper).getByUsername(currentUserName);
        verify(passwordEncoder).matches(oldPassword, SeedUserData.TESTER.password());
        verify(passwordEncoder).encode(newPassword);

        Emp capturedEmp = empArgumentCaptor.getValue();
        assertAll(
                () -> assertEquals(emp.getId(), capturedEmp.getId(), "id 應與當前使用者相同"),
                () -> assertEquals("encodedNewPassword", capturedEmp.getPassword(), "password 應為加密後的新密碼")
        );
    }

}