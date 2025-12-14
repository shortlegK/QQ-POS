package com.qqriceball.server.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qqriceball.common.exception.*;
import com.qqriceball.common.result.PageResult;
import com.qqriceball.constant.MessageConstant;
import com.qqriceball.constant.StatusConstant;
import com.qqriceball.pojo.dto.*;
import com.qqriceball.pojo.entity.Emp;
import com.qqriceball.pojo.vo.EmpPageQueryVO;
import com.qqriceball.pojo.vo.EmpVO;
import com.qqriceball.server.mapper.EmpMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
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

        if (emp == null) {
            throw new AccountNotExistException(MessageConstant.ACCOUNT_NOT_EXIST);
        }

        if (!passwordEncoder.matches(password, emp.getPassword())) {
            log.error("登入密碼錯誤,username: {}",username);
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (emp.getStatus().equals(StatusConstant.INACTIVE.getValue())) {
            log.error("登入帳號未啟用,username: {}",username);
            throw new AccountInactiveException(MessageConstant.ACCOUNT_INACTIVE);
        }

        return emp;

    }

    public void create(EmpCreateDTO empCreateDTO, Integer currentUserId) {
        Emp emp = new Emp();

        // 將 empDTO 內容 copy 至 emp
        BeanUtils.copyProperties(empCreateDTO, emp);

        emp.setPassword(passwordEncoder.encode(emp.getPassword()));
        emp.setCreateId(currentUserId);
        emp.setCreateTime(LocalDateTime.now());
        emp.setUpdateId(currentUserId);
        emp.setUpdateTime(LocalDateTime.now());

        try{
            empMapper.insert(emp);
        } catch (DuplicateKeyException e){
            log.error("建立員工,帳號重複,username: {}",empCreateDTO.getUsername(),e);
            throw new AlreadyExistsException(MessageConstant.USERNAME_ALREADY_EXIST);
        }

    }

    public PageResult pageQuery(EmpPageQueryDTO empPageQueryDTO) {
        try(Page<EmpPageQueryVO> pageQuery =
                    PageHelper.startPage(empPageQueryDTO.getPage(), empPageQueryDTO.getPageSize())){

            empMapper.pageQuery(empPageQueryDTO);

            Long total = pageQuery.getTotal();
            List<EmpPageQueryVO> records = pageQuery.getResult();
            return new PageResult(total, empPageQueryDTO.getPage(), empPageQueryDTO.getPageSize(), records);
        }
    }

    public void updateStatus(EmpStatusDTO empStatusDTO, Integer id, Integer currentUserId) {

        this.getById(id);

        Emp emp = new Emp();
        emp.setId(id);
        emp.setStatus(empStatusDTO.getStatus());
        emp.setUpdateId(currentUserId);
        emp.setUpdateTime(LocalDateTime.now());

        empMapper.updateById(emp);
    }

    public EmpVO getById(Integer id){

        EmpVO empVO = empMapper.getById(id);

        if (empVO == null){
            log.error("查無資料,ID: {}", id);
            throw new AccountNotExistException(MessageConstant.ACCOUNT_NOT_EXIST);
        }else{
            return  empVO;
        }
    }

    public void updateById(EmpEditDTO empEditDTO,Integer currentUserId) {

        this.getById(empEditDTO.getId());

        Emp emp = new Emp();
        BeanUtils.copyProperties(empEditDTO, emp);
        emp.setUpdateId(currentUserId);
        emp.setUpdateTime(LocalDateTime.now());

        empMapper.updateById(emp);

    }

    // 確認 Emp 啟用狀態
    public EmpVO checkActiveEmpById(Integer id){
        EmpVO empVO = this.getById(id);

        if (empVO.equals(StatusConstant.INACTIVE.getValue())) {
            log.error("未啟用,ID: {}", id);
            throw new AccountInactiveException(MessageConstant.ACCOUNT_INACTIVE);
        }
        return empVO;
    }

}
