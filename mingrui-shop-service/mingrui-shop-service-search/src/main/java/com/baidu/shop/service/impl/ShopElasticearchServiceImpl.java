package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SpuDto;
import com.baidu.shop.entity.SpuEntity;
import com.baidu.shop.feign.GoodsFeign;
import com.baidu.shop.service.ShopElasticearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class ShopElasticearchServiceImpl extends BaseApiService implements ShopElasticearchService {

    @Autowired
    private GoodsFeign goodsFeign;

    @Override
    public Result<JSONObject> esGoodsInfo() {
        SpuDto spuDto = new SpuDto();
        spuDto.setPage(1);
        spuDto.setRows(5);
        Result<List<SpuEntity>> spuInfo = goodsFeign.getSpuInfo(spuDto);
        System.out.println(spuInfo);
        return null;
    }
}
