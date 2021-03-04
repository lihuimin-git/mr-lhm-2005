package com.baidu.shop.service;

import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.BrandDto;
import com.baidu.shop.validate.group.MingruiOperation;
import com.google.gson.JsonObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "品牌接口")
public interface BrandService {
    @GetMapping(value = "brand/list")
    @ApiOperation(value = "查询品牌列表")
    Result<List<BrandEntity>> getBrandInfo(BrandDto brandDto);

    @PostMapping(value = "brand/save")
    @ApiOperation(value = "新增品牌")
    Result<JsonObject> save(@Validated({MingruiOperation.Add.class})@RequestBody BrandDto brandDto);

    @PutMapping(value = "brand/save")
    @ApiOperation(value = "修改品牌")
    Result<JsonObject> eidt(@Validated({MingruiOperation.Update.class})@RequestBody BrandDto brandDto);

    @DeleteMapping(value = "brand/del")
    @ApiOperation(value = "删除品牌")
    Result<JsonObject> del( Integer id);

    @GetMapping(value = "brand/getBrandInfoByCategoryById")
    @ApiOperation(value = "通过分类id获取品牌")
    Result<List<BrandEntity>> getBrandInfoByCategoryById(Integer cid);
}
