package com.baidu.shop.dto;

import com.baidu.shop.base.BaseDTO;
import com.baidu.shop.validate.group.MingruiOperation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Api(value = "Stock库存数据传输DTO")
@Data
public class StockDto extends BaseDTO {
    @ApiModelProperty(value = "skuId主键",example = "1")
    @NotNull(message = "skuId主键不能为空",groups ={MingruiOperation.Update.class})
    private Long skuId;//库存对应的商品skuId;

    @ApiModelProperty(value = "可秒杀库存",example = "1")
    private Integer seckillStock;//可秒杀库存

    @ApiModelProperty(value = "秒杀总数量",example = "1")
    private Integer seckillTotal;//秒杀总数量

    @ApiModelProperty(value = "库存数量",example = "1")
    @NotNull(message = "库存数量不能为空",groups ={MingruiOperation.Update.class,MingruiOperation.Add.class})
    private Integer stock;//库存数量
}
