package com.baidu.shop.dto;

import com.baidu.shop.base.BaseDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Api(value = "SKU属性数据传输类")
@Data
public class SkuDto extends BaseDTO {

    @ApiModelProperty(value = "主键",example = "1")
    private Long id;

    @ApiModelProperty(value = "spu主键",example = "1")
    private Integer spuId;

    @ApiModelProperty(value = "商品表头",example = "1")
    private String title;//商品表头

    @ApiModelProperty(value = "商品的图片，多个图片以‘,’分割",example = "1")
    private String images;//商品图片

    @ApiModelProperty(value = "销售价格，单位为分",example = "1")
    private Integer price;//销售价格

    @ApiModelProperty(value = "特有规格属性在spu属性模板中的对应下标组合",example = "1")
    private String indexes;//特有规格属性在spu属性模板中的对应下标组合

    @ApiModelProperty(value = "sku的特有规格参数键值对，json格式，反序列化时请使用linkedHashMap，保证有序",example = "1")
    private String ownSpec;//sku特有规格参数键值对

    @ApiModelProperty(value = "是否有效，0无效，1有效",example = "1")
    private Boolean enable;//是否有效

    @ApiModelProperty(value = "添加时间",example = "1")
    private Date createTime;//添加时间

    @ApiModelProperty(value = "最后修改的时间",example = "1")
    private Date lastUpdateTime;//最后修改的时间

    @ApiModelProperty(value = "商品库存",example = "1")
    private Integer stock;
}
