package com.baidu.shop.service;

import com.baidu.shop.base.Result;
import com.google.gson.JsonObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;

@Api(tags = "模板接口")
public interface TemplateService {
    @GetMapping(value = "template/createStaticTemplate")
    @ApiOperation(value ="通过spuId创建html文件")
    Result<JsonObject> createStaticTemplate(Integer spuId);

    @GetMapping(value = "template/initStaticTemplate")
    @ApiOperation(value = "初始化html文件")
    Result<JsonObject> initStaticTemplate();

    @GetMapping(value = "template/clearStaticTemplate")
    @ApiOperation(value = "清空html文件")
    Result<JsonObject> clearStaticTemplate();

    @GetMapping(value = "template/deleteStaticTemplate")
    @ApiOperation(value = "通过spuId删除html文件")
    Result<JsonObject> deleteStaticTemplate(Integer spuId);
}
