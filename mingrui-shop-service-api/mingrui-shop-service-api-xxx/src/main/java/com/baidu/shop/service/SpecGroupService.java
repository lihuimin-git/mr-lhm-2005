package com.baidu.shop.service;

import com.baidu.shop.entity.SpecGroupEntity;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SpecGroupDto;
import com.baidu.shop.validate.group.MingruiOperation;
import com.google.gson.JsonObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value ="规格组接口")
public interface SpecGroupService {

    @ApiModelProperty(value = "规格组查询")
    @GetMapping(value = "specGroup/getSpecGroupInfo")
    Result<List<SpecGroupEntity>> getSpecGroupInfo(@SpringQueryMap SpecGroupDto specGroupDto);

    @ApiModelProperty(value = "新增规格组")
    @PostMapping(value = "specGroup/save")
    Result<JsonObject> saveSpecGroup(@Validated({MingruiOperation.Add.class})@RequestBody SpecGroupDto specGroupDto);

    @ApiModelProperty(value = "修改规格组")
    @PutMapping(value = "specGroup/save")
    Result<JsonObject> editSpecGroup(@Validated({MingruiOperation.Update.class})@RequestBody SpecGroupDto specGroupDto);

    @ApiModelProperty(value = "删除规格组")
    @DeleteMapping("specGroup/delete/{id}")
    Result<JsonObject> delSpecRoupById(@PathVariable Integer id);
}
