package com.baidu.shop.dto;

import com.baidu.shop.base.BaseDTO;
import com.baidu.shop.validate.group.MingruiOperation;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@ApiModel(value = "规格分组")
@Data
public class SpecGroupDto extends BaseDTO {

    @ApiModelProperty(value = "规格分组id主键",example = "1")
    @NotNull(message = "规格分组id主键不能为空",groups = {MingruiOperation.Update.class})
    private Integer id;

    @ApiModelProperty(value = "类型id",example = "1")
    @NotNull(message = "类型id不能为空",groups = {MingruiOperation.Add.class,MingruiOperation.Update.class})
    private Integer cid;

    @ApiModelProperty(value = "规格组名称",example = "1")
    @NotEmpty(message = "规格组名称不能为空",groups = {MingruiOperation.Add.class,MingruiOperation.Update.class})
    private String name;
}
