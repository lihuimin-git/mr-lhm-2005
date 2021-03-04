package com.baidu.shop.service;

import com.baidu.shop.entity.SpuDetailEntity;
import com.baidu.shop.entity.SpuEntity;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SkuDto;
import com.baidu.shop.dto.SpuDto;
import com.baidu.shop.validate.group.MingruiOperation;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Api(tags = "商品接口")
public interface GoodsService {
    @ApiOperation(value = "查询商品信息")
    @GetMapping(value = "goods/getSpuInfo")
    Result<List<SpuEntity>> getSpuInfo(@SpringQueryMap  SpuDto spuDto);

    @ApiOperation(value = "新增商品")
    @PostMapping(value = "goods/save")
    Result<JsonObject> saveGoods(@Validated({MingruiOperation.Add.class}) @RequestBody SpuDto spuDto);

    @ApiOperation(value = "修改商品")
    @PutMapping(value = "goods/save")
    Result<JsonObject> editGoods(@Validated({MingruiOperation.Update.class})@RequestBody SpuDto spuDto);

    @ApiOperation(value ="删除商品")
    @DeleteMapping(value = "goods/delGoods")
    Result<JsonObject> delGoods(Integer spuId);

    @ApiOperation(value = "上下架商品")
    @PutMapping(value = "goods/sxjGoods")
    Result<JsonObject> sxjGoods(@RequestBody SpuDto spuDto);

    @ApiOperation(value = "通过spuId查询SpuDetail信息")
    @GetMapping(value = "goods/getSpuDetailBySpuId")
    Result<SpuDetailEntity> getSpuDetailBySpuId(Integer spuId);

    @ApiOperation(value = "通过spuId查询Sku信息")
    @GetMapping(value = "goods/getSkuBySpuId")
    Result<List<SkuDto>> getSkuBySpuId(Integer spuId);


}
