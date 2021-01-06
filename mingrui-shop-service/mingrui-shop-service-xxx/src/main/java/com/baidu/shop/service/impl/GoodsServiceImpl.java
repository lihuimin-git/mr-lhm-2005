package com.baidu.shop.service.impl;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SpuDto;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.entity.SpuEntity;
import com.baidu.shop.mapper.BrandMapper;
import com.baidu.shop.mapper.CategoryMapper;
import com.baidu.shop.mapper.SpuMapper;
import com.baidu.shop.service.GoodsService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.aspectj.weaver.ast.Var;
import org.bouncycastle.LICENSE;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
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

    @Override
    public Result<PageInfo<SpuEntity>> getSpuInfo(SpuDto spuDto) {
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
}
