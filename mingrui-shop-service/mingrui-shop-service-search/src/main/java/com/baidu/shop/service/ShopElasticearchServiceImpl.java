package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.document.GoodsDoc;
import com.baidu.shop.dto.SkuDto;
import com.baidu.shop.dto.SpecParamDto;
import com.baidu.shop.dto.SpuDto;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.entity.SpecParamEntity;
import com.baidu.shop.entity.SpuDetailEntity;
import com.baidu.shop.feign.BrandFeign;
import com.baidu.shop.feign.CategoryFeign;
import com.baidu.shop.feign.GoodsFeign;
import com.baidu.shop.feign.SpecParamFeign;
import com.baidu.shop.response.GoodsResponse;
import com.baidu.shop.service.ShopElasticearchService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.HighlightUtil;
import com.baidu.shop.utils.JSONUtil;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.ibatis.scripting.xmltags.ChooseSqlNode;

import org.apache.lucene.queryparser.surround.query.SrndTermQuery;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.repository.cdi.CdiRepositoryBean;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import sun.util.resources.cldr.nyn.CalendarData_nyn_UG;
import tk.mybatis.mapper.entity.Example;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class ShopElasticearchServiceImpl extends BaseApiService implements ShopElasticearchService {

    @Autowired
    private SpecParamFeign specParamFeign;

    @Autowired
    private GoodsFeign goodsFeign;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private BrandFeign brandFeign;

    @Autowired
    private CategoryFeign categoryFeign;

    @Override
    public GoodsResponse search(String search, Integer page,String filter) {
        //查询es库
        SearchHits<GoodsDoc> searchHits = elasticsearchRestTemplate.search(this.getNativeSearchQueryBuider(search, page,filter).build(), GoodsDoc.class);

        List<GoodsDoc> goodsDocs = HighlightUtil.getHighlightList(searchHits.getSearchHits());

        //得到总条数和计算总条数
        long total = searchHits.getTotalHits();
        long totalPage= Double.valueOf(Math.ceil(Double.valueOf(total) / 10)).longValue();

        Map<Integer, List<CategoryEntity>> map = this.getCategoryByBucket(searchHits.getAggregations());

        Integer hotCid = 0;
        List<CategoryEntity> categoryList = null;

        for (Map.Entry<Integer,List<CategoryEntity>> entry : map.entrySet()){
            hotCid = entry.getKey();
            categoryList = entry.getValue();
        }
        //返回
        return new GoodsResponse(total,totalPage,categoryList,
                                this.getBrandListByBucket(searchHits.getAggregations()),goodsDocs,
                                this.getSpecMap(hotCid,search));
    }

    private Map<String, List<String>> getSpecMap(Integer hotCid,String search) {

        //通过cid3查询规格参数, searching为true
        SpecParamDto specParamDto = new SpecParamDto();
        specParamDto.setCid(hotCid);
        specParamDto.setSearching(true);
        Result<List<SpecParamEntity>> specParamInfo = specParamFeign.getSpecParamInfo(specParamDto);
        Map<String, List<String>> specMap = new HashMap<>();
        if (specParamInfo.isSuccess()) {

            List<SpecParamEntity> specParamList = specParamInfo.getData();

            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
            queryBuilder.withQuery(QueryBuilders.multiMatchQuery(search,"title","brandName","categoryName"));
            queryBuilder.withPageable(PageRequest.of(0,1));
            specParamList.stream().forEach(specParam->{
                queryBuilder.addAggregation(AggregationBuilders.terms(specParam.getName())
                        .field("specs." + specParam.getName() + ".keyword"));
            });

            SearchHits<GoodsDoc> searchHits = elasticsearchRestTemplate.search(queryBuilder.build(), GoodsDoc.class);
            Aggregations aggregations = searchHits.getAggregations();

            specParamList.stream().forEach(specParam ->{
                Terms aggregation = aggregations.get(specParam.getName());
                List<? extends Terms.Bucket> buckets = aggregation.getBuckets();
                List<String> valueList = buckets.stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());

                specMap.put(specParam.getName(),valueList);
            });
        }
        return specMap;
    }



    //得到NativeSearchQueryBuilder
    private NativeSearchQueryBuilder getNativeSearchQueryBuider(String search,Integer page,String filter){

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //多字段查查询
        queryBuilder.withQuery(
                QueryBuilders.multiMatchQuery(search,"title","brandName","categoryName")
        );

        //搜索过滤
        if (!StringUtils.isEmpty(filter) && filter.length() > 2){
            //将字符串转为map集合
            Map<String, String> filterMap = JSONUtil.toMapValueString(filter);
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            filterMap.forEach((key,value)->{
                MatchQueryBuilder matchQueryBuilder =null;
                //判断是否是cid3或brandId
                if (key.equals("brandId") || key.equals("cid3")){
                    matchQueryBuilder = QueryBuilders.matchQuery(key,value);
                }else {
                    matchQueryBuilder = QueryBuilders.matchQuery("specs." + key + ".keyword",value);
                }
                boolQueryBuilder.must(matchQueryBuilder);
            });
            queryBuilder.withFilter(boolQueryBuilder);
              /*nativeSearchQueryBuilder.withFilter(
                    QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("spec.分辨率.keyword","分辨率"))
                            .must(QueryBuilders.matchQuery("brandId","8557"))
            );*/
        }

        //结果过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","title","skus"},null));
        //设置分页
        queryBuilder.withPageable(PageRequest.of(page-1,10));
        //设置高亮
        queryBuilder.withHighlightBuilder(HighlightUtil.getHighlightBuilder("title"));
        //聚合-品牌,分类聚合
        queryBuilder.addAggregation(AggregationBuilders.terms("agg_category").field("cid3"));
        queryBuilder.addAggregation(AggregationBuilders.terms("agg_brand").field("brandId"));

        return queryBuilder;
    }


    //通过聚合得到分类List
    private Map<Integer,List<CategoryEntity>> getCategoryByBucket(Aggregations aggregations){

        Terms agg_category = aggregations.get("agg_category");

        List<? extends Terms.Bucket> categoryBuckets = agg_category.getBuckets();

        List<Long> doCount = Arrays.asList(0L);
        List<Integer> hotCid = Arrays.asList(0);

        List<String> catehoryIdList = categoryBuckets.stream().map(categoryBucket-> {

            if (categoryBucket.getDocCount() > doCount.get(0)){
                doCount.set(0,categoryBucket.getDocCount());
                hotCid.set(0,categoryBucket.getKeyAsNumber().intValue());
            }
           return categoryBucket.getKeyAsNumber().longValue()+"";

        }).collect(Collectors.toList());

        Result<List<CategoryEntity>> categoryIds = categoryFeign.getCategoryIds(String.join(",", catehoryIdList));


        List<CategoryEntity> categoryList = null;
        if (categoryIds.isSuccess()){
            categoryList = categoryIds.getData();
        }

        HashMap<Integer, List<CategoryEntity>> categoryMap = new HashMap<>();
        categoryMap.put(hotCid.get(0),categoryList);

        return categoryMap;
    }



    //通过聚合得到品牌List
    private List<BrandEntity> getBrandListByBucket(Aggregations aggregations){
        Terms agg_brand = aggregations.get("agg_brand");
        List<? extends Terms.Bucket> brandBuckets = agg_brand.getBuckets();

        List<String> brandIdList = brandBuckets.stream().map(brangBucket ->
                brangBucket.getKeyAsNumber().longValue()+""
        ).collect(Collectors.toList());

        //要将List<Long>转换成 String类型的字符串并且用,拼接
        Result<List<BrandEntity>> brandByIds = brandFeign.getBrandByIds(String.join(",", brandIdList));

        List<BrandEntity> brandList = null;
        if(brandByIds.isSuccess()){
            brandList = brandByIds.getData();
        }

        return brandList;
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


    //查询
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
    private Map<String,Object> getSpecMap(SpuDto spu){
        SpecParamDto specParamDto = new SpecParamDto();
        specParamDto.setCid(spu.getCid3());
        specParamDto.setSearching(true);
        Result<List<SpecParamEntity>> specParamInfo = specParamFeign.getSpecParamInfo(specParamDto);
        if (specParamInfo.isSuccess()){


            List<SpecParamEntity> specParamList = specParamInfo.getData();
            Result<SpuDetailEntity> spuDetailBySpuId = goodsFeign.getSpuDetailBySpuId(spu.getId());

            if (spuDetailBySpuId.isSuccess()){

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
