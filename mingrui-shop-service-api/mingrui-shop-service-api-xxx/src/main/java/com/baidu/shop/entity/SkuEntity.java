package com.baidu.shop.entity;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "tb_sku")
@Data
public class SkuEntity {
    @Id//此处必须写long类型,因为现在新增的id已经超过int的范围了
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer spuId;

    private String title;//商品表头

    private String images;//商品图片

    private Integer price;//销售价格

    private String indexes;//特有规格属性在spu属性模板中的对应下标组合

    private String ownSpec;//sku特有规格参数键值对

    private Integer enable;//是否有效

    private Date createTime;//添加时间

    private Date lastUpdateTime;//最后修改的时间
}
