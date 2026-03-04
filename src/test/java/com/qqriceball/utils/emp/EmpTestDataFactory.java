package com.qqriceball.utils.emp;

import com.qqriceball.enumeration.StatusEnum;
import com.qqriceball.model.dto.*;
import com.qqriceball.model.entity.Emp;
import com.qqriceball.model.vo.EmpVO;
import com.qqriceball.testData.emp.TestAccount;
import org.springframework.beans.BeanUtils;

import java.time.LocalDate;

public class EmpTestDataFactory {

    public static EmpLoginDTO getEmpLoginDTO(String username, String password) {
        EmpLoginDTO empLoginDTO = new EmpLoginDTO();
        empLoginDTO.setUsername(username);
        empLoginDTO.setPassword(password);

        return empLoginDTO;
    }

    public static EmpCreateDTO getEmpCreateDTO(String username, String password, int role) {
        EmpCreateDTO empCreateDTO = new EmpCreateDTO();
        empCreateDTO.setUsername(username);
        empCreateDTO.setPassword(password);
        empCreateDTO.setName(username);
        empCreateDTO.setRole(role);
        empCreateDTO.setEntryDate(LocalDate.now());

        return empCreateDTO;
    }

    public static EmpCreateDTO getEmpCreateDTO(TestAccount account) {

        EmpCreateDTO empCreateDTO = new EmpCreateDTO();
        BeanUtils.copyProperties(account, empCreateDTO);
        return empCreateDTO;
    }

    public static Emp getEmp(TestAccount account){
        Emp emp = new Emp();
        BeanUtils.copyProperties(account,emp);
        return emp;
    }

    public static EmpEditDTO getEmpEditDTO(TestAccount account){
        EmpEditDTO empEditDTO = new EmpEditDTO();
        BeanUtils.copyProperties(account,empEditDTO);
        return empEditDTO;
    }

    public static EmpVO getEmpVO(TestAccount account){
        EmpVO empVO = new EmpVO();
        BeanUtils.copyProperties(account,empVO);
        return empVO;
    }

    public static EmpPageQueryDTO getEmpPageQueryDTO(Integer page, Integer pageSize , String name){
        EmpPageQueryDTO empPageQueryDTO = new EmpPageQueryDTO();
        empPageQueryDTO.setPage(page);
        empPageQueryDTO.setPageSize(pageSize);
        empPageQueryDTO.setName(name);
        return empPageQueryDTO;
    }

    public static EmpStatusDTO getEmpStatusDTO(StatusEnum status) {
        EmpStatusDTO empStatusDTO = new EmpStatusDTO();
        empStatusDTO.setStatus(status.getCode());
        return empStatusDTO;
    }

}
