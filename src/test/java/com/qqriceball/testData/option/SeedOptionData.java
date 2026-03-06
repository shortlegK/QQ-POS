package com.qqriceball.testData.option;

import com.qqriceball.enumeration.DefaultEnum;
import com.qqriceball.enumeration.OptionTypeEnum;
import com.qqriceball.enumeration.StatusEnum;

public class SeedOptionData {

    public static final TestOption WHITE_RICE =
            new TestOption(1, "白米" , OptionTypeEnum.RICE_TYPE.getCode(),
                    DefaultEnum.YES.getCode(),0, StatusEnum.ACTIVE.getCode());
    public static final TestOption PURPLE_RICE =
            new TestOption(2, "紫米", OptionTypeEnum.RICE_TYPE.getCode(),
                    DefaultEnum.NO.getCode(), 5, StatusEnum.ACTIVE.getCode());

    public static final TestOption RICE_TYPE_INACTIVE =
            new TestOption(3, "白麵", OptionTypeEnum.RICE_TYPE.getCode(),
                    DefaultEnum.NO.getCode(), 0, StatusEnum.INACTIVE.getCode());

    public static final TestOption SMALL_SIZE =
            new TestOption(3, "飯量少", OptionTypeEnum.RICE_SIZE.getCode(),
                    DefaultEnum.NO.getCode(), 0, StatusEnum.ACTIVE.getCode());
    public static final TestOption NORMAL_SIZE =
            new TestOption(4,"飯量正常",OptionTypeEnum.RICE_SIZE.getCode(),
                    DefaultEnum.YES.getCode(), 0, StatusEnum.ACTIVE.getCode());
    public static final TestOption LARGE_SIZE =
            new TestOption(5,"飯量多",OptionTypeEnum.RICE_SIZE.getCode(),
                    DefaultEnum.NO.getCode(),10, StatusEnum.ACTIVE.getCode());

    public static final TestOption SIZE_INACTIVE =
            new TestOption(5,"飯量多多",OptionTypeEnum.RICE_SIZE.getCode(),
                    DefaultEnum.NO.getCode(),10, StatusEnum.INACTIVE.getCode());

    public static final TestOption NO_SPICY =
            new TestOption(6,"不辣",OptionTypeEnum.SPICE_LEVEL.getCode(),
                    DefaultEnum.YES.getCode(),0, StatusEnum.ACTIVE.getCode());
    public static final TestOption MILD_SPICY =
            new TestOption(7,"微辣",OptionTypeEnum.SPICE_LEVEL.getCode(),
                    DefaultEnum.NO.getCode(),0,StatusEnum.ACTIVE.getCode());
    public static final TestOption MEDIUM_SPICY =
            new TestOption(8,"中辣",OptionTypeEnum.SPICE_LEVEL.getCode(),
                    DefaultEnum.NO.getCode(),0, StatusEnum.ACTIVE.getCode());
    public static final TestOption HOT_SPICY =
            new TestOption(9,"大辣",OptionTypeEnum.SPICE_LEVEL.getCode(),
                    DefaultEnum.NO.getCode(),0, StatusEnum.ACTIVE.getCode());

    public static final TestOption SPICY_INACTIVE =
            new TestOption(9,"超辣",OptionTypeEnum.SPICE_LEVEL.getCode(),
                    DefaultEnum.NO.getCode(),0, StatusEnum.INACTIVE.getCode());

    public static final TestOption HOT =
            new TestOption(10,"冷",OptionTypeEnum.DRINK_TEMPERATURE.getCode(),
                    DefaultEnum.YES.getCode(), 0, StatusEnum.ACTIVE.getCode());
    public static final TestOption COLD =
            new TestOption(11,"熱",OptionTypeEnum.DRINK_TEMPERATURE.getCode(),
                    DefaultEnum.NO.getCode(),0, StatusEnum.ACTIVE.getCode());

    public static final TestOption TEMP_INACTIVE =
            new TestOption(11,"溫",OptionTypeEnum.DRINK_TEMPERATURE.getCode(),
                    DefaultEnum.NO.getCode(),0, StatusEnum.INACTIVE.getCode());

    public static final TestOption EGG =
            new TestOption(12,"雞蛋",OptionTypeEnum.ADD_ON.getCode(),
                    DefaultEnum.NO.getCode(),10, StatusEnum.ACTIVE.getCode());

        public static final TestOption ADD_ON_INACTIVE =
            new TestOption(13,"起司",OptionTypeEnum.ADD_ON.getCode(),
                    DefaultEnum.NO.getCode(),10, StatusEnum.INACTIVE.getCode());

}
