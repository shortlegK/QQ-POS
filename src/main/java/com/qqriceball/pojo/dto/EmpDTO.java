package com.qqriceball.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class EmpDTO implements Serializable {

    private Integer id;
    private String username;
    private String password;
    private String name;
    private Integer role;
    private Integer status;
    private LocalDate entrydate;




}
