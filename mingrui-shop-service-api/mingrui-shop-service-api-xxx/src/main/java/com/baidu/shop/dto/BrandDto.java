package com.baidu.shop.dto;

import com.baidu.shop.base.BaseDTO;
import com.baidu.shop.validate.group.MingruiOperation;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(value = "品牌DTO")
public class BrandDto extends BaseDTO {

    @ApiModelProperty(value = "品牌ID",example = "1")
    @NotNull(message = "主键不能为空",groups = {MingruiOperation.Add.class,MingruiOperation.Update.class})
    private Integer id;

    @ApiModelProperty(value = "品牌名称",example = "1")
    @NotEmpty(message = "品牌名称不能为空",groups = {MingruiOperation.Add.class, MingruiOperation.Update.class})
    private String name;

    @ApiModelProperty(value = "品牌图片")
    private String image;

    @ApiModelProperty(value = "品牌首字母")
    private Character letter;

    @ApiModelProperty(value = "品牌分类信息")
    @NotEmpty(message = "品牌分类信息不能为空",groups = {MingruiOperation.Add.class})
    private String categories;
}
