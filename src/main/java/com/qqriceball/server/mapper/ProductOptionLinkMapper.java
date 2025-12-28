package com.qqriceball.server.mapper;

import com.qqriceball.pojo.entity.ProductOptionLink;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductOptionLinkMapper {

    void insertBatch(List<ProductOptionLink> allowedOptionIds);
}
