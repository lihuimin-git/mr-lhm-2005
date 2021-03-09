package com.baidu.shop.service.impl;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.*;
import com.baidu.shop.entity.*;
import com.baidu.shop.feign.*;
import com.baidu.shop.service.TemplateService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.ObjectUtil;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import org.aspectj.weaver.ast.Var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;
import java.text.BreakIterator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class TemplateServiceImpl extends BaseApiService implements TemplateService {

    private final Integer CREATE_STATIC_HTML = 1;
    private final Integer DELETE_STATIC_HTML = 2;

    @Autowired
    private BrandFeign brandFeign;

    @Autowired
    private CategoryFeign categoryFeign;

    @Autowired
    private GoodsFeign goodsFeign;

    @Autowired
    private SpecGroupFeign specGroupFeign;

    @Autowired
    private SpecParamFeign specParamFeign;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private TemplateEngine templateEngine;

    @Value(value = "${mrshop.static.html.path}")
    private String htmlPath;

    @Override
    public Result<JsonObject> createStaticTemplate(Integer spuId) {
        //得到要渲染的数据
        Map<String, Object> goodsInfo = this.getGoodsInfo(spuId);

        Context context = new Context();
        context.setVariables(goodsInfo);

        File file = new File(htmlPath, spuId + ".html");
        if (!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        PrintWriter writer = null;
        try {
            writer= new PrintWriter(file, "UTF-8");
            templateEngine.process("item",context,writer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }finally {
            if (ObjectUtil.isNotNull(writer))
                writer.close();
        }
        return this.setResultSuccess();
    }


    public Map<String, Object> getGoodsInfo(Integer spuId) {
        Map<String, Object> goodsInfoMap = new HashMap<>();
        //spu
        SpuDto spuResultData  = this.getSpuInfo(spuId);
        goodsInfoMap.put("spuInfo",spuResultData );
        //spuDetail
        goodsInfoMap.put("spuDetail",this.getSpuDetail(spuId));
        //分类信息
        goodsInfoMap.put("category",this.getCategoryInfo(spuResultData.getCid1() + "",spuResultData.getCid2() + "",spuResultData.getCid3() + ""));
        //品牌信息
        goodsInfoMap.put("brandInfo",this.getBrandInfo(spuResultData.getBrandId()));
        //sku
        goodsInfoMap.put("skus",this.getSkus(spuId));
        //规格组,规格参数(通用)
        goodsInfoMap.put("specGroupAndParam",this.getSpecGroupAndParam(spuResultData.getCid3()));
        //特殊规格
        goodsInfoMap.put("specParamMap",this.getSpecParam(spuResultData.getCid3()));
        return goodsInfoMap;
    }


    //SpuDto
    private SpuDto getSpuInfo(Integer spuId){
        SpuDto spuDto = new SpuDto();
        spuDto.setId(spuId);
        Result<List<SpuDto>> spuResult = goodsFeign.getSpuInfo(spuDto);
        SpuDto spuResultData = null;
        if(spuResult.isSuccess()){
            spuResultData = spuResult.getData().get(0);
        }
        return spuResultData;
    }

    //SpuDetailEntity
    private SpuDetailEntity getSpuDetail(Integer spuId){
        //spuDetail
        SpuDetailEntity spuDetailEntity = null;
        Result<SpuDetailEntity> spuDetailResult = goodsFeign.getSpuDetailBySpuId(spuId);
        if(spuDetailResult.isSuccess()){
                spuDetailEntity = spuDetailResult.getData();
        }
        return spuDetailEntity;
    }

    //CategoryEntity
    private List<CategoryEntity> getCategoryInfo(String cid1,String cid2,String cid3){

        List<CategoryEntity> categoryEntityList = null;
        //分类信息
        Result<List<CategoryEntity>> categoryResult = categoryFeign.getCategoryIds(
                String.join(
                        ","
                        , Arrays.asList(cid1,cid2,cid3)
                )
        );
        if (categoryResult.isSuccess()){
            categoryEntityList = categoryResult.getData();
        }
        return categoryEntityList;
    }

    //BrandEntity
    private BrandEntity getBrandInfo(Integer brandId){
        BrandEntity brandEntity = null;
        //品牌信息
        BrandDto brandDto = new BrandDto();
        brandDto.setId(brandId);
        Result<PageInfo<BrandEntity>> brandResult = brandFeign.getBrandInfo(brandDto);
        if (brandResult.isSuccess()){
            brandEntity = brandResult.getData().getList().get(0);
        }
        return brandEntity;
    }

    //sku
    private List<SkuDto> getSkus(Integer spuId){
        List<SkuDto> skuDtoList = null;

        Result<List<SkuDto>> skusResult = goodsFeign.getSkuBySpuId(spuId);
        if (skusResult.isSuccess()){
            skuDtoList = skusResult.getData();
        }

        return skuDtoList;
    }

   //规格参数,规格组(通用)
    private List<SpecGroupDto> getSpecGroupAndParam (Integer cid3){
        List<SpecGroupDto> specGroupAndParam = null;

        SpecGroupDto specGroupDto = new SpecGroupDto();
        specGroupDto.setCid(cid3);
        Result<List<SpecGroupEntity>> specGroupResult = specGroupFeign.getSpecGroupInfo(specGroupDto);
        if (specGroupResult.isSuccess()){

            List<SpecGroupEntity> specGroupList = specGroupResult.getData();
            specGroupAndParam = specGroupList.stream().map(specGruop -> {
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
        }
        return specGroupAndParam;
    }

    //特殊规格
    private Map<Integer,String> getSpecParam(Integer cid3){
        HashMap<Integer, String> specParamMap = new HashMap<>();


        SpecParamDto specParamDto = new SpecParamDto();
        specParamDto.setCid(cid3);
        specParamDto.setGeneric(true);
        Result<List<SpecParamEntity>> specParamResult = specParamFeign.getSpecParamInfo(specParamDto);
        if (specParamResult.isSuccess()){
            List<SpecParamEntity> specParamEntityList = specParamResult.getData();
            specParamEntityList.stream().forEach(specParam->
                    specParamMap.put(specParam.getId(),specParam.getName()));
        }
        return specParamMap;
    }

    @Override
    public Result<JsonObject> initStaticTemplate() {
     this.operationStaticHTML(CREATE_STATIC_HTML);

        return this.setResultSuccess();
    }

    //清除
    @Override
    public Result<JsonObject> clearStaticTemplate() {
        this.operationStaticHTML(DELETE_STATIC_HTML);

        return this.setResultSuccess();
    }


    private Boolean operationStaticHTML(Integer operation){

        try {
            Result<List<SpuDto>> spuInfo = goodsFeign.getSpuInfo(new SpuDto());
            if(spuInfo.isSuccess()){
                spuInfo.getData().stream().forEach(spuDTO -> {
                    if(operation == 1){
                        this.createStaticTemplate(spuDTO.getId());
                    }else{
                        this.deleteStaticTemplate(spuDTO.getId());
                    }
                });
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //删除
    @Override
    public Result<JsonObject> deleteStaticTemplate(Integer spuId) {
        File file = new File(htmlPath, spuId + ".html");
        if (file.exists()){
            file.delete();
        }
        return this.setResultSuccess();
    }
}
