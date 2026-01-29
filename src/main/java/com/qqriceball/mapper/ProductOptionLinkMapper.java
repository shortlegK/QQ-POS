package com.qqriceball.mapper;

import com.qqriceball.model.entity.ProductOptionLink;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductOptionLinkMapper {

    void insertBatch(List<ProductOptionLink> allowedOptionIds);
}
