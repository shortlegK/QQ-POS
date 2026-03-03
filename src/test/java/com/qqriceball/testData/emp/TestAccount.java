package com.qqriceball.testData.emp;

import java.time.LocalDate;

public record TestAccount (Integer id, String username, String password,String name, Integer role, Integer status, LocalDate entryDate) {

}
