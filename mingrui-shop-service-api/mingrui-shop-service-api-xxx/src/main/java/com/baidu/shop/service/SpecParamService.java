package com.baidu.shop.service;

import com.baidu.shop.entity.SpecParamEntity;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SpecParamDto;
import com.baidu.shop.validate.group.MingruiOperation;
import com.google.gson.JsonObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "规格参数接口")
public interface SpecParamService {
    @ApiOperation(value = "查询规格参数")
    @GetMapping(value = "specParam/getSpecParamInfo")
    Result<List<SpecParamEntity>> getSpecParamInfo(@SpringQueryMap  SpecParamDto specParamDto);

    @ApiOperation(value = "新增规格参数")
    @PostMapping(value = "specParam/save")
    Result<JsonObject> saveSpecParam(@Validated({MingruiOperation.Add.class}) @RequestBody SpecParamDto specParamDto);

    @ApiOperation(value = "修改规格参数")
    @PutMapping(value = "specParam/save")
    Result<JsonObject> editSpecParam(@Validated({MingruiOperation.Update.class})@RequestBody SpecParamDto specParamDto);

    @ApiOperation(value = "删除规格参数")
    @DeleteMapping(value = "specParam/delete")
    Result<JsonObject> delSpecParam(Integer id);
}
