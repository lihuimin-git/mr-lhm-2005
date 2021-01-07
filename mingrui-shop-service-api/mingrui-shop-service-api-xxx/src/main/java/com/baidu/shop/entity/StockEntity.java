package com.baidu.shop.entity;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name ="tb_stock")
public class StockEntity {

    @Id
    private Long skuId;//库存对应的商品skuId;

    private Integer seckillStock;//可秒杀库存

    private Integer seckillTotal;//秒杀总数量

    private Integer stock;//库存数量
}
