package com.baidu.shop.mapper;

import com.baidu.shop.entity.BrandEntity;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<BrandEntity> {
    @Select(value = "SELECT b.* from tb_brand b,tb_category_brand b2 where b.id = b2.brand_id and b2.category_id =#{id}")
    List<BrandEntity>getBrandInfoByCategoryById(Integer cid);
}
