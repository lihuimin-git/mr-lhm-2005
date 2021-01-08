package com.baidu.shop.dto;

import com.baidu.shop.base.BaseDTO;
import com.baidu.shop.validate.group.MingruiOperation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Api(value = "spu商品标题传输DTO")
@Data
public class SpuDetailDto extends BaseDTO {
    @ApiModelProperty(value = "spu主键商品id",example = "1")
    @NotNull(message = "spu主键商品id不能为空",groups = {MingruiOperation.Update.class})
    private Integer spuId;

    @ApiModelProperty(value = "商品描述信息",example = "1")
    @NotEmpty(message = "商品描述信息不能为空",groups = {MingruiOperation.Add.class})
    private String description;//商品描述

    @ApiModelProperty(value = "通用规格参数数据",example = "1")
    @NotEmpty(message = "通用规格参数数据不能为空",groups = {MingruiOperation.Add.class})
    private String genericSpec;//通用规格参数数据

    @ApiModelProperty(value = "特有规格参数及可选值信息，json格式",example = "1")
    @NotEmpty(message = "特有规格参数及可选值信息不能为空",groups = {MingruiOperation.Add.class})
    private String specialSpec;//特有规格参数及可选值信息

    @ApiModelProperty(value = "包装清单",example = "1")
    @NotEmpty(message = "包装清单不能为空",groups = {MingruiOperation.Add.class})
    private String packingList;//包装清单

    @ApiModelProperty(value = "售后服务",example = "1")
    @NotEmpty(message = "售后服务不能为空",groups = {MingruiOperation.Add.class})
    private String afterService;//售后服务
}
