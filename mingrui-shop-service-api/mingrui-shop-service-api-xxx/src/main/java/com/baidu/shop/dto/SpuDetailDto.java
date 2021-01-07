package com.baidu.shop.dto;

import com.baidu.shop.base.BaseDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Api(value = "spu大字段数据传输类")
@Data
public class SpuDetailDto extends BaseDTO {
    @ApiModelProperty(value = "spu主键",example = "1")
    private Integer spuId;

    @ApiModelProperty(value = "商品描述信息",example = "1")
    private String description;//商品描述

    @ApiModelProperty(value = "通用规格参数数据",example = "1")
    private String genericSpec;//通用规格参数数据

    @ApiModelProperty(value = "特有规格参数及可选值信息，json格式",example = "1")
    private String specialSpec;//特有规格参数及可选值信息

    @ApiModelProperty(value = "包装清单",example = "1")
    private String packingList;//包装清单

    @ApiModelProperty(value = "售后服务",example = "1")
    private String afterService;//售后服务
}
