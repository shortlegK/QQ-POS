package com.qqriceball.testData.option;

import com.qqriceball.enumeration.OptionTypeEnum;
import com.qqriceball.enumeration.StatusEnum;

public class SeedOptionData {

    public static final TestOption WHITE_RICE =
            new TestOption(1, "白米" , OptionTypeEnum.RICE_TYPE.getCode(),
                    0, StatusEnum.ACTIVE.getCode());
    public static final TestOption PURPLE_RICE =
            new TestOption(2, "紫米", OptionTypeEnum.RICE_TYPE.getCode(),
                    5, StatusEnum.ACTIVE.getCode());

    public static final TestOption SMALL_SIZE =
            new TestOption(3, "飯量少", OptionTypeEnum.RICE_SIZE.getCode(),
                    0, StatusEnum.ACTIVE.getCode());
    public static final TestOption NORMAL_SIZE =
            new TestOption(4,"飯量正常",OptionTypeEnum.RICE_SIZE.getCode(),
                    0, StatusEnum.ACTIVE.getCode());
    public static final TestOption LARGE_SIZE =
            new TestOption(5,"飯量多",OptionTypeEnum.RICE_SIZE.getCode(),
                    10, StatusEnum.ACTIVE.getCode());

    public static final TestOption NO_SPICY =
            new TestOption(6,"不辣",OptionTypeEnum.SPICE_LEVEL.getCode(),
                    0, StatusEnum.ACTIVE.getCode());
    public static final TestOption MILD_SPICY =
            new TestOption(7,"微辣",OptionTypeEnum.SPICE_LEVEL.getCode(),
                    0,StatusEnum.ACTIVE.getCode());
    public static final TestOption MEDIUM_SPICY =
            new TestOption(8,"中辣",OptionTypeEnum.SPICE_LEVEL.getCode(),
                    0, StatusEnum.ACTIVE.getCode());
    public static final TestOption HOT_SPICY =
            new TestOption(9,"大辣",OptionTypeEnum.SPICE_LEVEL.getCode(),
                    0, StatusEnum.ACTIVE.getCode());

    public static final TestOption HOT =
            new TestOption(10,"冷",OptionTypeEnum.DRINK_TEMPERATURE.getCode(),
                    0, StatusEnum.ACTIVE.getCode());
    public static final TestOption COLD =
            new TestOption(11,"熱",OptionTypeEnum.DRINK_TEMPERATURE.getCode(),
                    0, StatusEnum.ACTIVE.getCode());

    public static final TestOption EGG =
            new TestOption(12,"雞蛋",OptionTypeEnum.ADD_ON.getCode(),
                    10, StatusEnum.ACTIVE.getCode());

}
