package com.baidu.shop.service;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SkuDto;
import com.baidu.shop.dto.SpuDetailDto;
import com.baidu.shop.dto.SpuDto;
import com.baidu.shop.entity.*;
import com.baidu.shop.mapper.*;
import com.baidu.shop.service.GoodsService;
import com.baidu.shop.utils.ObjectUtil;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class GoodsServiceImpl extends BaseApiService implements GoodsService {

    @Resource
    private SpuMapper spuMapper;

    @Resource
    private BrandMapper brandMapper;

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private SpuDetailMapper spuDetailMapper;

    @Resource
    private SkuMapper skuMapper;

    @Resource
    private StockMapper stockMapper;

    //查询商品
    @Override
    public Result<List<SpuDto>> getSpuInfo(SpuDto spuDto) {
        //倒序
        if (!StringUtils.isEmpty(spuDto.getSort()) && !StringUtils.isEmpty(spuDto.getOrder()))
            PageHelper.orderBy(spuDto.getOrder());
        //分页插件
        if (ObjectUtil.isNotNull(spuDto.getPage()) && ObjectUtil.isNotNull(spuDto.getRows()))
            PageHelper.startPage(spuDto.getPage(),spuDto.getRows());

        Example example = new Example(SpuEntity.class);
        Example.Criteria criteria = example.createCriteria();

        //上下架
        if (ObjectUtil.isNotNull(spuDto.getSaleable()) && spuDto.getSaleable() < 2)
            criteria.andEqualTo("saleable",spuDto.getSaleable());
        //条件查询
        if(!StringUtils.isEmpty(spuDto.getTitle()))
            criteria.andLike("title","%"+spuDto.getTitle()+"%");

        if (ObjectUtil.isNotNull(spuDto.getId()))criteria.andEqualTo("id",spuDto.getId());

        List<SpuEntity> spuEntities = spuMapper.selectByExample(example);

        //分页
        List<SpuDto> spuDtoList = spuEntities.stream().map(spuEntity -> {
            SpuDto spuDto1 = BaiduBeanUtil.copyProperties(spuEntity, SpuDto.class);

            //通过分类id集合查询数据(分类)
            List<CategoryEntity> categoryEntities = categoryMapper.selectByIdList(Arrays.asList(spuEntity.getCid1(), spuEntity.getCid2(), spuEntity.getCid3()));
            // 遍历集合并且将分类名称用 / 拼接
            String collect = categoryEntities.stream().map(categoryEntity -> categoryEntity.getName()).collect(Collectors.joining("/"));
            spuDto1.setCategoryName(collect);

            //品牌
            BrandEntity brandEntity = brandMapper.selectByPrimaryKey(spuEntity.getBrandId());
            spuDto1.setBrandName(brandEntity.getName());

            return spuDto1;
        }).collect(Collectors.toList());


        PageInfo<SpuEntity> spuEntityPageInfo = new PageInfo<>(spuEntities);

//        return this.setResultSuccess(spuEntityPageInfo);
        return this.setResult(HTTPStatus.OK,spuEntityPageInfo.getTotal() + "",spuDtoList);
    }

    //新增商品
    @Transactional
    @Override
    public Result<JsonObject> saveGoods(SpuDto spuDto) {
        System.out.println(spuDto);
        final Date date = new Date();
        //新增spu,新增返回主键, 给必要字段赋默认值
        //转spuEntity
        SpuEntity spuEntity = BaiduBeanUtil.copyProperties(spuDto, SpuEntity.class);
        spuEntity.setSaleable(1);
        spuEntity.setValid(1);
        spuEntity.setCreateTime(date);
        spuEntity.setLastUpdateTime(date);
        spuMapper.insertSelective(spuEntity);

        //新增spuDetail
        //从spuDto中获取SpuDetail
        SpuDetailDto spuDetail = spuDto.getSpuDetail();
        SpuDetailEntity spuDetailEntity = BaiduBeanUtil.copyProperties(spuDetail, SpuDetailEntity.class);
        spuDetailEntity.setSpuId(spuEntity.getId());
        spuDetailMapper.insertSelective(spuDetailEntity);

        //新增sku
        //从spuDto中获取sku
        this.saveSkuAndStockInfo(spuDto,spuEntity.getId(),date);
        return this.setResultSuccess();
    }

    //修改商品
    @Override
    @Transactional
    public Result<JsonObject> editGoods(SpuDto spuDto) {
        //修改spu
        Date date = new Date();
        SpuEntity spuEntity = BaiduBeanUtil.copyProperties(spuDto, SpuEntity.class);
        spuEntity.setLastUpdateTime(date);
        spuMapper.updateByPrimaryKeySelective(spuEntity);

        //修改spuDetail
        spuDetailMapper.updateByPrimaryKeySelective(BaiduBeanUtil.copyProperties(spuDto.getSpuDetail(),SpuDetailEntity.class));

        //修改sku
        //先通过spuid删除sku
        //然后新增数据
        this.delSkuAndStrock(spuEntity.getId());

        //修改stock
        //删除stock
        //但是sku在上面已经被删除掉了
        //所以应该先查询出被删除的skuid
        //新增stock
        this.saveSkuAndStockInfo(spuDto,spuEntity.getId(),date);


        return this.setResultSuccess();
    }

    //删除商品
    @Transactional
    @Override
    public Result<JsonObject> delGoods(Integer spuId) {
        //删除spu
        spuMapper.deleteByPrimaryKey(spuId);
        //删除spuDetail
        spuDetailMapper.deleteByPrimaryKey(spuId);

        //先通过spuid删除sku
        this.delSkuAndStrock(spuId);
        return this.setResultSuccess();
    }

    //上下架
    @Override
    public Result<JsonObject> sxjGoods(SpuDto spuDto) {
        SpuEntity spuEntity = BaiduBeanUtil.copyProperties(spuDto, SpuEntity.class);
        spuMapper.updateByPrimaryKeySelective(spuEntity);
        return this.setResultSuccess();
    }

    //通过spuId查询SpuDetail信息
    @Override
    public Result<SpuDetailEntity> getSpuDetailBySpuId(Integer spuId) {
        SpuDetailEntity spuDetailEntity = spuDetailMapper.selectByPrimaryKey(spuId);
        return this.setResultSuccess(spuDetailEntity);
    }

    //通过spuId查询sku信息
    @Override
    public Result<List<SkuDto>> getSkuBySpuId(Integer spuId) {
        List<SkuDto> skuEntity = skuMapper.getSkusAndStockBySpuId(spuId);
            return this.setResultSuccess(skuEntity);
    }

    //修改和新增的整合（sku增加）
    private void saveSkuAndStockInfo(SpuDto spuDto,Integer spuId,Date date){
        List<SkuDto> skus = spuDto.getSkus();
        skus.stream().forEach(skuDto -> {
            SkuEntity skuEntity = BaiduBeanUtil.copyProperties(skuDto, SkuEntity.class);
            skuEntity.setSpuId(spuId);
            skuEntity.setCreateTime(date);
            skuEntity.setLastUpdateTime(date);
            skuMapper.insertSelective(skuEntity);

            StockEntity stockEntity = new StockEntity();
            stockEntity.setSkuId(skuEntity.getId());
            stockEntity.setStock(skuDto.getStock());
            stockMapper.insertSelective(stockEntity);
        });
    }

    //修改和删除整合
    private void delSkuAndStrock(Integer spuId){
        Example example = new Example(SkuEntity.class);
        example.createCriteria().andEqualTo("spuId",spuId);
        List<SkuEntity> skuEntities = skuMapper.selectByExample(example);
        List<Long> skuCollect = skuEntities.stream().map(sku ->sku.getId()).collect(Collectors.toList());
        skuMapper.deleteByIdList(skuCollect);
    }
}
