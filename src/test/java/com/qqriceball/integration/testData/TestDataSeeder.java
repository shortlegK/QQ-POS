package com.qqriceball.integration.testData;

import com.qqriceball.integration.testData.emp.SeedUserData;
import com.qqriceball.integration.testData.emp.TestAccount;
import com.qqriceball.integration.testData.product.SeedProductData;
import com.qqriceball.integration.testData.product.TestProduct;
import com.qqriceball.mapper.EmpMapper;
import com.qqriceball.mapper.ProductMapper;
import com.qqriceball.model.entity.Emp;
import com.qqriceball.model.entity.Product;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Profile("test")
@Component
public class TestDataSeeder implements ApplicationRunner {

    private final PasswordEncoder passwordEncoder;
    private final EmpMapper empMapper;
    private final ProductMapper productMapper;

    public TestDataSeeder(PasswordEncoder passwordEncoder, EmpMapper empMapper , ProductMapper productMapper) {
        this.passwordEncoder = passwordEncoder;
        this.empMapper = empMapper;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        createUser(SeedUserData.MANAGER);
        createUser(SeedUserData.STAFF);
        createUser(SeedUserData.INACTIVE);
        createUser(SeedUserData.TESTER);

        createProduct(SeedProductData.MEAT_PRODUCT);
        createProduct(SeedProductData.VEG_PRODUCT);
        createProduct(SeedProductData.DRINK_PRODUCT);
    }

    private void createUser(TestAccount testAccount) {

        Emp existing = empMapper.getByUsername(testAccount.username());
        String encoded = passwordEncoder.encode(testAccount.password());

        if (existing == null) {
            Emp emp = new Emp();
            emp.setUsername(testAccount.username());
            emp.setPassword(encoded);
            emp.setName(testAccount.username());
            emp.setRole(testAccount.role());
            emp.setStatus(testAccount.status());
            emp.setEntryDate(testAccount.entryDate());
            emp.setCreateId(1);
            emp.setCreateTime(LocalDateTime.now());
            emp.setUpdateId(1);
            emp.setUpdateTime(LocalDateTime.now());

            empMapper.insert(emp);
        } else {
            existing.setPassword(encoded);
            existing.setRole(testAccount.role());
            existing.setStatus(testAccount.status());
            empMapper.updateById(existing);
        }
    }

    private void createProduct(TestProduct testProduct){

        Product existing = productMapper.getByTitle(testProduct.title());

        if(existing == null){
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
        }else{
            existing.setProductType(testProduct.productType());
            existing.setPrice(testProduct.price());
            existing.setStatus(testProduct.status());
            productMapper.updateById(existing);

        }


    }
}