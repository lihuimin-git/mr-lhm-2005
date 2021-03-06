package com.baidu.shop.response;

import com.baidu.shop.base.Result;
import com.baidu.shop.document.GoodsDoc;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.status.HTTPStatus;
import com.netflix.ribbon.proxy.annotation.Http;
import com.sun.scenario.effect.impl.prism.PrTexture;
import javafx.beans.property.ObjectProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class GoodsResponse extends Result<List<GoodsDoc>> {

    private Long total;

    private Long totalPage;

    private List<BrandEntity> brandList;

    private List<CategoryEntity> categoryList;

    private Map<String,List<String>> specMap;

    public GoodsResponse(Long total,Long totalPage, List<CategoryEntity> categoryList,
                         List<BrandEntity> brandList, List<GoodsDoc> goodsDocList
                        ,Map<String,List<String>> specMap){
        //父类的构造函数
        super(HTTPStatus.OK,"",goodsDocList);

        this.total = total;
        this.totalPage = totalPage;
        this.categoryList = categoryList;
        this.brandList = brandList;
        this.specMap = specMap;
    }
}
