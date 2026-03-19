package com.qqriceball.testData;

import com.qqriceball.mapper.OptionMapper;
import com.qqriceball.mapper.order.OrderItemMapper;
import com.qqriceball.mapper.order.OrderItemOptionMapper;
import com.qqriceball.mapper.order.OrderMapper;
import com.qqriceball.model.entity.Option;
import com.qqriceball.model.entity.order.Order;
import com.qqriceball.model.entity.order.OrderItem;
import com.qqriceball.model.entity.order.OrderItemOption;
import com.qqriceball.testData.emp.SeedUserData;
import com.qqriceball.testData.emp.TestAccount;
import com.qqriceball.testData.option.SeedOptionData;
import com.qqriceball.testData.option.TestOption;
import com.qqriceball.testData.order.SeedOrderData;
import com.qqriceball.testData.order.TestOrder;
import com.qqriceball.testData.product.SeedProductData;
import com.qqriceball.testData.product.TestProduct;
import com.qqriceball.mapper.EmpMapper;
import com.qqriceball.mapper.ProductMapper;
import com.qqriceball.model.entity.Emp;
import com.qqriceball.model.entity.Product;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Profile("test")
@Component
public class TestDataSeeder implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final EmpMapper empMapper;
    private final ProductMapper productMapper;
    private final OptionMapper optionMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderItemOptionMapper orderItemOptionMapper;


    public TestDataSeeder(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder,
                          EmpMapper empMapper , ProductMapper productMapper, OptionMapper optionMapper,
                          OrderMapper orderMapper, OrderItemMapper orderItemMapper, OrderItemOptionMapper orderItemOptionMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.empMapper = empMapper;
        this.productMapper = productMapper;
        this.optionMapper = optionMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.orderItemOptionMapper = orderItemOptionMapper;
    }


    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        log.info("開始執行 TestDataSeeder，為測試環境建立測試資料");

        // 清空測試資料
        jdbcTemplate.execute("TRUNCATE TABLE order_item_options");
        jdbcTemplate.execute("TRUNCATE TABLE order_items");
        jdbcTemplate.execute("TRUNCATE TABLE orders");
        jdbcTemplate.execute("TRUNCATE TABLE options");
        jdbcTemplate.execute("TRUNCATE TABLE products");
        jdbcTemplate.execute("TRUNCATE TABLE emps");

        createUser(SeedUserData.MANAGER);
        createUser(SeedUserData.STAFF);
        createUser(SeedUserData.INACTIVE);
        createUser(SeedUserData.TESTER);

        createProduct(SeedProductData.MEAT_PRODUCT);
        createProduct(SeedProductData.MEAT_INACTIVE);
        createProduct(SeedProductData.VEG_PRODUCT);
        createProduct(SeedProductData.VEG_INACTIVE);
        createProduct(SeedProductData.DRINK_PRODUCT);
        createProduct(SeedProductData.DRINK_INACTIVE);

        createOption(SeedOptionData.WHITE_RICE);
        createOption(SeedOptionData.PURPLE_RICE);
        createOption(SeedOptionData.NORMAL_SIZE);
        createOption(SeedOptionData.LARGE_SIZE);
        createOption(SeedOptionData.SIZE_INACTIVE);
        createOption(SeedOptionData.MILD_SPICY);
        createOption(SeedOptionData.MEDIUM_SPICY);
        createOption(SeedOptionData.HOT_SPICY);
        createOption(SeedOptionData.HOT);
        createOption(SeedOptionData.COLD);
        createOption(SeedOptionData.TEMP_INACTIVE);
        createOption(SeedOptionData.EGG);
        createOption(SeedOptionData.ADD_ON_INACTIVE);

        createOrder(SeedOrderData.orderMaking,SeedProductData.MEAT_PRODUCT,SeedOptionData.WHITE_RICE);
        createOrder(SeedOrderData.orderMakingNextWeek,SeedProductData.VEG_PRODUCT,SeedOptionData.NORMAL_SIZE);
        createOrder(SeedOrderData.orderReady,SeedProductData.DRINK_PRODUCT,SeedOptionData.COLD);
        createOrder(SeedOrderData.orderPickedUp,SeedProductData.MEAT_PRODUCT,SeedOptionData.LARGE_SIZE);
        createOrder(SeedOrderData.orderCancel,SeedProductData.VEG_PRODUCT,SeedOptionData.MILD_SPICY);

        log.info("完成 TestDataSeeder，測試資料已建立完成");
    }

    private void createUser(TestAccount testAccount) {
        Emp emp = new Emp();
        BeanUtils.copyProperties(testAccount, emp);
        emp.setPassword(passwordEncoder.encode(emp.getPassword()));
        emp.setCreateId(1);
        emp.setCreateTime(LocalDateTime.now());
        emp.setUpdateId(1);
        emp.setUpdateTime(LocalDateTime.now());
        empMapper.insert(emp);
    }

    private void createProduct(TestProduct testProduct){
        Product product = new Product();
        BeanUtils.copyProperties(testProduct, product);
        product.setCreateId(1);
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateId(1);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.insert(product);
    }

    private void createOption(TestOption testOption){
        Option option = new Option();
        BeanUtils.copyProperties(testOption, option);
        option.setCreateId(1);
        option.setCreateTime(LocalDateTime.now());
        option.setUpdateId(1);
        option.setUpdateTime(LocalDateTime.now());
        optionMapper.insert(option);

    }

    private void createOrder(TestOrder testOrder,TestProduct testProduct,TestOption testOption){

        Order order = new Order();
        BeanUtils.copyProperties(testOrder, order);
        order.setCreateId(1);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateId(1);
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.insert(order);

        this.buildOrderItem(order.getId(),testProduct,testOption);
    }

    private void buildOrderItem(Integer orderId,TestProduct testProduct,TestOption testOption){

        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(orderId);
        orderItem.setProductId(testProduct.id());
        orderItem.setProductTitle(testProduct.title());
        orderItem.setProductType(testProduct.productType());
        orderItem.setProductPrice(testProduct.price());
        orderItem.setQuantity(1);
        orderItem.setLineTotal(10);
        orderItemMapper.insert(orderItem);
        this.buildOrderItemOption(orderItem.getId(),testOption);
    }

    private void buildOrderItemOption(Integer orderItemId,TestOption testOption){
        OrderItemOption orderItemOption = new OrderItemOption();
        orderItemOption.setOrderItemId(orderItemId);
        orderItemOption.setOptionId(testOption.id());
        orderItemOption.setOptionType(testOption.optionType());
        orderItemOption.setOptionTitle(testOption.title());
        orderItemOption.setOptionPrice(testOption.price());
        orderItemOption.setQuantity(2);
        orderItemOptionMapper.insert(orderItemOption);
    }

}