package com.qqriceball.testData.product;

import com.qqriceball.enumeration.ProductTypeEnum;
import com.qqriceball.enumeration.StatusEnum;

public class SeedProductData {

    public static final TestProduct MEAT_PRODUCT =
            new TestProduct(1, "meat" , ProductTypeEnum.MEAT.getCode(),
                    50, StatusEnum.ACTIVE.getCode());

    public static final TestProduct VEG_PRODUCT =
            new TestProduct(2, "veg", ProductTypeEnum.VEGAN.getCode(),
                    40, StatusEnum.ACTIVE.getCode());

    public static final TestProduct DRINK_PRODUCT =
            new TestProduct(3, "drink", ProductTypeEnum.DRINKS.getCode(),
                    30, StatusEnum.ACTIVE.getCode());
}
