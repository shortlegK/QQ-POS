package com.qqriceball.testData.product;

import com.qqriceball.enumeration.ProductTypeEnum;
import com.qqriceball.enumeration.StatusEnum;

public class SeedProductData {

    public static final TestProduct MEAT_PRODUCT =
            new TestProduct(1, "meat" , ProductTypeEnum.MEAT.getCode(),
                    50, StatusEnum.ACTIVE.getCode());

    public static final TestProduct MEAT_INACTIVE =
            new TestProduct(4, "meat_inactive" , ProductTypeEnum.MEAT.getCode(),
                    50, StatusEnum.INACTIVE.getCode());

    public static final TestProduct VEG_PRODUCT =
            new TestProduct(2, "veg", ProductTypeEnum.VEGAN.getCode(),
                    40, StatusEnum.ACTIVE.getCode());

    public static final TestProduct VEG_INACTIVE =
            new TestProduct(5, "veg_inactive", ProductTypeEnum.VEGAN.getCode(),
                    40, StatusEnum.INACTIVE.getCode());

    public static final TestProduct DRINK_PRODUCT =
            new TestProduct(3, "drink", ProductTypeEnum.DRINKS.getCode(),
                    30, StatusEnum.ACTIVE.getCode());

    public static final TestProduct DRINK_INACTIVE =
            new TestProduct(6, "drink_inactive", ProductTypeEnum.DRINKS.getCode(),
                    30, StatusEnum.INACTIVE.getCode());
}
