package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.document.GoodsDoc;
import com.baidu.shop.dto.SkuDto;
import com.baidu.shop.dto.SpecParamDto;
import com.baidu.shop.dto.SpuDto;
import com.baidu.shop.entity.SpecParamEntity;
import com.baidu.shop.entity.SpuDetailEntity;
import com.baidu.shop.feign.GoodsFeign;
import com.baidu.shop.feign.SpecParamFeign;
import com.baidu.shop.service.ShopElasticearchService;
import com.baidu.shop.utils.HighlightUtil;
import com.baidu.shop.utils.JSONUtil;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.ibatis.scripting.xmltags.ChooseSqlNode;
import org.apache.lucene.queryparser.surround.query.SrndTermQuery;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import sun.util.resources.cldr.nyn.CalendarData_nyn_UG;
import tk.mybatis.mapper.entity.Example;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class ShopElasticearchServiceImpl extends BaseApiService implements ShopElasticearchService {

    @Autowired
    private SpecParamFeign specParamFeign;

    @Autowired
    private GoodsFeign goodsFeign;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public Result<List<GoodsDoc>> search(String search) {
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //多字段查查询
        queryBuilder.withQuery(
                QueryBuilders.multiMatchQuery(search,"title","brandName","categoryName")
        );
        //设置分页
        queryBuilder.withPageable(PageRequest.of(0,10));
        //设置高亮
        queryBuilder.withHighlightBuilder(HighlightUtil.getHighlightBuilder("title"));
        SearchHits<GoodsDoc> searchHits = elasticsearchRestTemplate.search(queryBuilder.build(), GoodsDoc.class);

        List<GoodsDoc> goodsDocsList = HighlightUtil.getHighlightList(searchHits.getSearchHits());

        return this.setResultSuccess(goodsDocsList);
    }

    //ES商品数据初始化-->索引创建,映射创建,mysql数据同步
    @Override
    public Result<JSONObject> initGoodsEsData() {
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(GoodsDoc.class);
        if (!indexOperations.exists()){
            indexOperations.create();
            indexOperations.createMapping();
        }

        //查询mysql中的数据
        List<GoodsDoc> goodsDocs = this.esGoodsInfo();
        elasticsearchRestTemplate.save(goodsDocs);
        return this.setResultSuccess("成功");
    }

    //清空ES中的商品数据
    @Override
    public Result<JSONObject> clearGoodsEsData() {
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(GoodsDoc.class);
      indexOperations.delete();
        return this.setResultSuccess();
    }


    private List<GoodsDoc> esGoodsInfo() {
        SpuDto spuDto = new SpuDto();
//        spuDto.setPage(1);
//        spuDto.setRows(5);
        Result<List<SpuDto>> spuInfo = goodsFeign.getSpuInfo(spuDto);

        if (spuInfo.isSuccess()) {
            List<SpuDto> spuList = spuInfo.getData();
            List<GoodsDoc> goodsDocList = spuList.stream().map(spu -> {
                //获取spu
                GoodsDoc goodsDoc = new GoodsDoc();
                goodsDoc.setId(spu.getId().longValue());
                goodsDoc.setTitle(spu.getTitle());
                goodsDoc.setBrandName(spu.getBrandName());
                goodsDoc.setCategoryName(spu.getCategoryName());
                goodsDoc.setCreateTime(spu.getCreateTime());
                goodsDoc.setBrandId(spu.getBrandId().longValue());
                goodsDoc.setCid1(spu.getCid1().longValue());
                goodsDoc.setCid2(spu.getCid2().longValue());
                goodsDoc.setCid3(spu.getCid3().longValue());


                //sku数据 , 通过spuid查询skus
                Map<List<Long>, List<Map<String, Object>>> skuAndPriceList = this.getSkuAndPriceList(spu.getId());
                skuAndPriceList.forEach((Key, value) -> {
                    goodsDoc.setPrice(Key);
                    goodsDoc.setSkus(JSONUtil.toJsonString(value));
                });

                //设置规格参数
                Map<String, Object> specMap = this.getSpecMap(spu);
                goodsDoc.setSpecs(specMap);
                return goodsDoc;
            }).collect(Collectors.toList());
            return goodsDocList;
        }
        return null;
    }

    //获取规格参数map
    private Map<String, Object> getSpecMap(SpuDto spu) {

        //通过cid3查询规格参数, searching为true
        SpecParamDto specParamDto = new SpecParamDto();
        specParamDto.setCid(spu.getCid3());
        specParamDto.setSearching(true);
        Result<List<SpecParamEntity>> specParamInfo = specParamFeign.getSpecParamInfo(specParamDto);
        if (specParamInfo.isSuccess()) {

            List<SpecParamEntity> specParamList = specParamInfo.getData();
            Result<SpuDetailEntity> spuDetailBySpuId = goodsFeign.getSpuDetailBySpuId(spu.getId());

            if (spuDetailBySpuId.isSuccess()) {

                SpuDetailEntity spuDetailEntity = spuDetailBySpuId.getData();
                Map<String, Object> specMap = this.getSpecMap(specParamList, spuDetailEntity);
                return specMap;
            }
        }
        return null;
    }

    private Map<String, Object> getSpecMap(List<SpecParamEntity> specParamEntityList, SpuDetailEntity spuDetailEntity) {

        Map<String, Object> specMap = new HashMap<>();
        //将json字符串转换成map集合
        Map<String, String> genericSpec = JSONUtil.toMapValueString(spuDetailEntity.getGenericSpec());
        Map<String, List<String>> specialSpec = JSONUtil.toMapValueStrList(spuDetailEntity.getSpecialSpec());

        //需要查询两张表的数据 spec_param(规格参数名) spu_detail(规格参数值) --> 规格参数名 : 规格参数值
        specParamEntityList.stream().forEach(specParam -> {
            if (specParam.getGeneric()) {//判断从那个map集合中获取数据
                if (specParam.getNumeric() && !StringUtils.isEmpty(specParam.getSegments())) {
                    specMap.put(specParam.getName()
                            , chooseSegment(genericSpec.get(specParam.getId() + ""), specParam.getSegments(), specParam.getUnit()));
                } else {
                    specMap.put(specParam.getName(), genericSpec.get(specParam.getId() + ""));
                }
            } else {
                specMap.put(specParam.getName(), specialSpec.get(specParam.getId() + ""));
            }
        });

        return specMap;
    }

    //sku数据 , 通过spuid查询skus
    private Map<List<Long>, List<Map<String, Object>>> getSkuAndPriceList(Integer spuId) {

        Map<List<Long>, List<Map<String, Object>>> hashMap = new HashMap<>();
        Result<List<SkuDto>> skuBySpuId = goodsFeign.getSkuBySpuId(spuId);
        if (skuBySpuId.isSuccess()) {

            List<SkuDto> skuList = skuBySpuId.getData();
            List<Long> priceList = new ArrayList<>();//一个spu的所有商品价格集合
            List<Map<String, Object>> skuMapList = skuList.stream().map(sku -> {

                Map<String, Object> map = new HashMap<>();
                map.put("id", sku.getId());
                map.put("title", sku.getTitle());
                map.put("image", sku.getImages());
                map.put("price", sku.getPrice());

                priceList.add(sku.getPrice().longValue());
                return map;
            }).collect(Collectors.toList());
            hashMap.put(priceList, skuMapList);
        }
        return hashMap;
    }

    //查询
//    @Override
//    public List<GoodsDoc> esGoodsInfo() {
//        SpuDto spuDto = new SpuDto();
////        spuDto.setPage(1);
////        spuDto.setRows(5);
//        Result<List<SpuDto>> spuInfo = goodsFeign.getSpuInfo(spuDto);
//
//        if (spuInfo.isSuccess()){
//            List<SpuDto> spuList = spuInfo.getData();
//            List<GoodsDoc> goodsDocList = spuList.stream().map(spu -> {
//                //获取spu
//                GoodsDoc goodsDoc = new GoodsDoc();
//                goodsDoc.setId(spu.getId().longValue());
//                goodsDoc.setTitle(spu.getTitle());
//                goodsDoc.setBrandName(spu.getBrandName());
//                goodsDoc.setCategoryName(spu.getCategoryName());
//                goodsDoc.setCreateTime(spu.getCreateTime());
//                goodsDoc.setBrandId(spu.getBrandId().longValue());
//                goodsDoc.setCid1(spu.getCid1().longValue());
//                goodsDoc.setCid2(spu.getCid2().longValue());
//                goodsDoc.setCid3(spu.getCid3().longValue());
//
//
//                //sku数据 , 通过spuid查询skus
//                Result<List<SkuDto>> skuBySpuId = goodsFeign.getSkuBySpuId(spu.getId());
//
//                if (skuBySpuId.isSuccess()){
//
//                    List<SkuDto> skuList = skuBySpuId.getData();
//                    List<Long> priceList = new ArrayList<>();//一个spu的所有商品价格集合
//                    List<Map<String,Object>> skuMapList = skuList.stream().map(sku -> {
//
//                      Map<String, Object> map = new HashMap<>();
//                      map.put("id",sku.getId());
//                      map.put("title",sku.getTitle());
//                      map.put("image",sku.getImages());
//                      map.put("price",sku.getPrice());
//
//                      priceList.add(sku.getPrice().longValue());
//                      return map;
//                    }).collect(Collectors.toList());
//                    goodsDoc.setPrice(priceList);
//                    goodsDoc.setSkus(JSONUtil.toJsonString(skuMapList));
//                }
//
//
//                //通过cid3查询规格参数, searching为true
//                SpecParamDto specParamDto = new SpecParamDto();
//                specParamDto.setCid(spu.getCid3());
//                specParamDto.setSearching(true);
//                Result<List<SpecParamEntity>> specParamInfo = specParamFeign.getSpecParamInfo(specParamDto);
//
//                if (specParamInfo.isSuccess()) {
//
//                    List<SpecParamEntity> specParamList = specParamInfo.getData();
//                    Result<SpuDetailEntity> spuDetailBySpuId = goodsFeign.getSpuDetailBySpuId(spu.getId());
//
//                    if (spuDetailBySpuId.isSuccess()) {
//
//                        SpuDetailEntity spuDetailData = spuDetailBySpuId.getData();
//
//                        //将json字符串转换成map集合
//                        Map<String, String> genericSpec = JSONUtil.toMapValueString(spuDetailData.getGenericSpec());
//                        Map<String, List<String>> specialSpec = JSONUtil.toMapValueStrList(spuDetailData.getSpecialSpec());
//
//                        //需要查询两张表的数据 spec_param(规格参数名) spu_detail(规格参数值) --> 规格参数名 : 规格参数值
//                        Map<String, Object> specMap = new HashMap<>();
//                        specParamList.stream().forEach(specParam -> {
//                            if (specParam.getGeneric()) {//判断从那个map集合中获取数据
//                                if (specParam.getNumeric() && !StringUtils.isEmpty(specParam.getSegments())) {
//                                    specMap.put(specParam.getName()
//                                            , chooseSegment(genericSpec.get(specParam.getId() + ""), specParam.getSegments(), specParam.getUnit()));
//                                } else {
//                                    specMap.put(specParam.getName(), genericSpec.get(specParam.getId() + ""));
//                                }
//                            } else {
//                                specMap.put(specParam.getName(), specialSpec.get(specParam.getId() + ""));
//                            }
//                        });
//                        goodsDoc.setSpecs(specMap);
//                    }
//                }
//                return goodsDoc;
//            }).collect(Collectors.toList());
//            return  goodsDocList;
//
//        }
//
//        return null;
//    }
    private String chooseSegment(String value, String segments, String unit) {//800 -> 5000-1000
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : segments.split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + unit + "以上";
                }else if(begin == 0){
                    result = segs[1] + unit + "以下";
                }else{
                    result = segment + unit;
                }
                break;
            }
        }
        return result;
    }
}
