package com.qqriceball.testData;

import com.qqriceball.testData.emp.SeedUserData;
import com.qqriceball.testData.emp.TestAccount;
import com.qqriceball.testData.product.SeedProductData;
import com.qqriceball.testData.product.TestProduct;
import com.qqriceball.mapper.EmpMapper;
import com.qqriceball.mapper.ProductMapper;
import com.qqriceball.model.entity.Emp;
import com.qqriceball.model.entity.Product;
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

    public TestDataSeeder(JdbcTemplate jdbcTemplate,PasswordEncoder passwordEncoder, EmpMapper empMapper , ProductMapper productMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.empMapper = empMapper;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        // 清空測試資料
        jdbcTemplate.execute("TRUNCATE TABLE products");
        jdbcTemplate.execute("TRUNCATE TABLE emps");

        createUser(SeedUserData.MANAGER);
        createUser(SeedUserData.STAFF);
        createUser(SeedUserData.INACTIVE);
        createUser(SeedUserData.TESTER);

        createProduct(SeedProductData.MEAT_PRODUCT);
        createProduct(SeedProductData.VEG_PRODUCT);
        createProduct(SeedProductData.DRINK_PRODUCT);
    }

    private void createUser(TestAccount testAccount) {

        Emp emp = new Emp();
        emp.setUsername(testAccount.username());
        emp.setPassword(passwordEncoder.encode(testAccount.password()));
        emp.setName(testAccount.name());
        emp.setRole(testAccount.role());
        emp.setEntryDate(testAccount.entryDate());
        emp.setStatus(testAccount.status());
        emp.setCreateId(1);
        emp.setCreateTime(LocalDateTime.now());
        emp.setUpdateId(1);
        emp.setUpdateTime(LocalDateTime.now());

        empMapper.insert(emp);
    }

    private void createProduct(TestProduct testProduct){
        Product product = new Product();
        product.setTitle(testProduct.title());
        product.setProductType(testProduct.productType());
        product.setPrice(testProduct.price());
        product.setStatus(testProduct.status());
        product.setCreateId(1);
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateId(1);
        product.setUpdateTime(LocalDateTime.now());

        productMapper.insert(product);
    }
}