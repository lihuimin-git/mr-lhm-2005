package com.baidu.shop.entity;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "tb_spu_detail")
@Data
public class SpuDetailEntity {
    @Id
    private Integer spuId;

    private String description;//商品描述

    private String genericSpec;//通用规格参数数据

    private String specialSpec;//特有规格参数及可选值信息

    private String packingList;//包装清单

    private String afterService;//售后服务

}
