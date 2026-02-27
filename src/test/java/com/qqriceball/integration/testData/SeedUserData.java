package com.qqriceball.integration.testData;

import com.qqriceball.enumeration.RoleEnum;
import com.qqriceball.enumeration.StatusEnum;

import java.time.LocalDate;

public class SeedUserData {

    public static final TestAccount MANAGER =
            new TestAccount(1, "managerrole","(Qqpos1357",
                    "Manager", RoleEnum.MANAGER.getCode(),
                    StatusEnum.ACTIVE.getCode(), LocalDate.of(2025,1,1));

    public static final TestAccount STAFF =
            new TestAccount(2, "staffrole","(Qqpos1357",
                    "Staff", RoleEnum.STAFF.getCode(),
                    StatusEnum.ACTIVE.getCode(), LocalDate.of(2025,2,1));

    public static final TestAccount INACTIVE =
            new TestAccount(3, "inactive","(Qqpos1357",
                    "Incative",RoleEnum.STAFF.getCode(),
                    StatusEnum.INACTIVE.getCode(), LocalDate.of(2025,3,1));

    public static final TestAccount TESTER =
            new TestAccount(4, "tester","(Qqpos1357",
                    "Tester",RoleEnum.STAFF.getCode(),
                    StatusEnum.ACTIVE.getCode(), LocalDate.of(2025,4,1));

}
