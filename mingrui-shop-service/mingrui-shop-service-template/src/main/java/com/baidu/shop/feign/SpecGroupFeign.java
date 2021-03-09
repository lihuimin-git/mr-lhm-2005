package com.baidu.shop.feign;

import com.baidu.shop.service.SpecGroupService;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "xxx-server",contextId = "SpecGroupService")
public interface SpecGroupFeign extends SpecGroupService {
}
