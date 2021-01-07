package com.baidu.shop.dto;

import com.baidu.shop.base.BaseDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Api(value = "库存数据传输类")
@Data
public class StockDto extends BaseDTO {
    @ApiModelProperty(value = "sku主键",example = "1")
    private Long skuId;//库存对应的商品skuId;

    @ApiModelProperty(value = "可秒杀库存",example = "1")
    private Integer seckillStock;//可秒杀库存

    @ApiModelProperty(value = "秒杀总数量",example = "1")
    private Integer seckillTotal;//秒杀总数量

    @ApiModelProperty(value = "库存数量",example = "1")
    private Integer stock;//库存数量
}
