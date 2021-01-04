package com.baidu.shop.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;

@Data
@Table(name = "tb_category_brand")
@NoArgsConstructor
@AllArgsConstructor
public class CategoryBrandEntity {
    private Integer categoryId;

    private Integer brandId;
}
