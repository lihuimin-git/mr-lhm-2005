package com.baidu.shop.controller;

import com.baidu.shop.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

//@Controller
//@RequestMapping(value = "item")
public class PageController {

//    @Autowired
    private PageService pageService;


//    @GetMapping(value = "html")
    public String test1(){
        return "123";
    }

//    @GetMapping(value = "{spuId}.html")
    public String test(@PathVariable(value = "spuId") Integer spuId, ModelMap map){
        Map<String, Object> goodsInfo = pageService.getGoodsInfo(spuId);
        map.putAll(goodsInfo);
        return "item";
    }
}
