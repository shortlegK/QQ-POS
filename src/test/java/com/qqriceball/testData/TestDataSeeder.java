package com.qqriceball.testData;

import com.qqriceball.mapper.OptionMapper;
import com.qqriceball.model.entity.Option;
import com.qqriceball.testData.emp.SeedUserData;
import com.qqriceball.testData.emp.TestAccount;
import com.qqriceball.testData.option.SeedOptionData;
import com.qqriceball.testData.option.TestOption;
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
import java.time.LocalDateTime;

@Profile("test")
@Component
public class TestDataSeeder implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final EmpMapper empMapper;
    private final ProductMapper productMapper;
    private final OptionMapper optionMapper;


    public TestDataSeeder(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder, EmpMapper empMapper , ProductMapper productMapper, OptionMapper optionMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.empMapper = empMapper;
        this.productMapper = productMapper;
        this.optionMapper = optionMapper;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        // 清空測試資料
        jdbcTemplate.execute("TRUNCATE TABLE products");
        jdbcTemplate.execute("TRUNCATE TABLE emps");
        jdbcTemplate.execute("TRUNCATE TABLE options");


        createUser(SeedUserData.MANAGER);
        createUser(SeedUserData.STAFF);
        createUser(SeedUserData.INACTIVE);
        createUser(SeedUserData.TESTER);

        createProduct(SeedProductData.MEAT_PRODUCT);
        createProduct(SeedProductData.VEG_PRODUCT);
        createProduct(SeedProductData.DRINK_PRODUCT);

        createOption(SeedOptionData.WHITE_RICE);
        createOption(SeedOptionData.PURPLE_RICE);
        createOption(SeedOptionData.SMALL_SIZE);
        createOption(SeedOptionData.NORMAL_SIZE);
        createOption(SeedOptionData.LARGE_SIZE);
        createOption(SeedOptionData.NO_SPICY);
        createOption(SeedOptionData.MILD_SPICY);
        createOption(SeedOptionData.MEDIUM_SPICY);
        createOption(SeedOptionData.HOT_SPICY);
        createOption(SeedOptionData.HOT);
        createOption(SeedOptionData.COLD);
        createOption(SeedOptionData.EGG);

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

    private  void createOption(TestOption testOption){
        Option option = new Option();
        BeanUtils.copyProperties(testOption, option);
        option.setCreateId(1);
        option.setCreateTime(LocalDateTime.now());
        option.setUpdateId(1);
        option.setUpdateTime(LocalDateTime.now());
        optionMapper.insert(option);

    }
}