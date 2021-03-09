package com.baidu.shop.service.impl;

import com.baidu.shop.base.Result;
import com.baidu.shop.dto.*;
import com.baidu.shop.entity.*;
import com.baidu.shop.feign.*;
import com.baidu.shop.service.PageService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//@Service
public class PageServiceImpl implements PageService {

//    @Autowired
    private BrandFeign brandFeign;

//    @Autowired
    private CategoryFeign categoryFeign;

//    @Autowired
    private GoodsFeign goodsFeign;

//    @Autowired
    private SpecGroupFeign specGroupFeign;

//    @Autowired
    private SpecParamFeign specParamFeign;

    @Override
    public Map<String, Object> getGoodsInfo(Integer spuId) {
        Map<String, Object> goodsInfoMap = new HashMap<>();

        SpuDto spuDto = new SpuDto();
        spuDto.setId(spuId);
        Result<List<SpuDto>> spuInfo = goodsFeign.getSpuInfo(spuDto);
        SpuDto spuResultData = null;
        if(spuInfo.isSuccess()){
            spuResultData = spuInfo.getData().get(0);
            goodsInfoMap.put("spuInfo",spuResultData);
        }

        //spuDetail
        Result<SpuDetailEntity> spuDetailBySpuId = goodsFeign.getSpuDetailBySpuId(spuId);
        if(spuDetailBySpuId.isSuccess()){
            goodsInfoMap.put("spuDateil",spuDetailBySpuId.getData());
        }

        //分类信息
        Result<List<CategoryEntity>> categoryResult = categoryFeign.getCategoryIds(
                String.join(
                        ","
                        , Arrays.asList(spuResultData.getCid1() + "", spuResultData.getCid2() + ""
                                , spuResultData.getCid3() + "")
                )
        );
        if (categoryResult.isSuccess()){
            goodsInfoMap.put("categoryInfo",categoryResult.getData());
        }

        //品牌信息
        BrandDto brandDto = new BrandDto();
        brandDto.setId(spuResultData.getBrandId());
        Result<PageInfo<BrandEntity>> brandResult = brandFeign.getBrandInfo(brandDto);
        if (brandResult.isSuccess()){
            goodsInfoMap.put("brandInfo",brandResult.getData().getList().get(0));
        }
        //sku
        Result<List<SkuDto>> skusResult = goodsFeign.getSkuBySpuId(spuId);
        if (skusResult.isSuccess()){
            goodsInfoMap.put("skus",skusResult.getData());
        }

        //规格参数,规格组(通用)
        SpecGroupDto specGroupDto = new SpecGroupDto();
        specGroupDto.setCid(spuResultData.getCid3());
        Result<List<SpecGroupEntity>> specGroupResult = specGroupFeign.getSpecGroupInfo(specGroupDto);
        if (specGroupResult.isSuccess()){

            List<SpecGroupEntity> specGroupList = specGroupResult.getData();
            List<SpecGroupDto> specGroupAndParam = specGroupList.stream().map(specGruop -> {
                SpecGroupDto specGroupDto1 = BaiduBeanUtil.copyProperties(specGruop, SpecGroupDto.class);

                SpecParamDto specParamDto = new SpecParamDto();
                specParamDto.setGroupId(specGroupDto1.getId());
                specParamDto.setGeneric(true);
                //specParamInfo
                Result<List<SpecParamEntity>> specParamResult = specParamFeign.getSpecParamInfo(specParamDto);
                if(specParamResult.isSuccess()){
                    specGroupDto1.setSpecList(specParamResult.getData());
                }
                return specGroupDto1;
            }).collect(Collectors.toList());
            goodsInfoMap.put("specGruopAndParam",specGroupAndParam);
        }

        //特殊规格
        SpecParamDto specParamDto = new SpecParamDto();
        specParamDto.setCid(spuResultData.getCid3());
        specParamDto.setGeneric(true);
        Result<List<SpecParamEntity>> specParamResult = specParamFeign.getSpecParamInfo(specParamDto);
        if (specParamResult.isSuccess()){
            List<SpecParamEntity> specParamEntityList = specParamResult.getData();
            Map<Integer, String> specParamMap = new HashMap<>();
            specParamEntityList.stream().forEach(specParam->
                    specParamMap.put(specParam.getId(),specParam.getName()));
                    goodsInfoMap.put("specParamMap",specParamMap);
        }
        return goodsInfoMap;
    }
}
