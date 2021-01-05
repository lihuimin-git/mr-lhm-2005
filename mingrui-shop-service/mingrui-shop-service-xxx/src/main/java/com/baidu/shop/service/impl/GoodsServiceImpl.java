package com.baidu.shop.service.impl;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SpuDto;
import com.baidu.shop.entity.SpuEntity;
import com.baidu.shop.mapper.SpuMapper;
import com.baidu.shop.service.GoodsService;
import com.baidu.shop.utils.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.aspectj.weaver.ast.Var;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;
@RestController
public class GoodsServiceImpl extends BaseApiService implements GoodsService {

    @Resource
    private SpuMapper spuMapper;

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
        PageInfo<SpuEntity> spuEntityPageInfo = new PageInfo<>(spuEntities);

        return this.setResultSuccess(spuEntityPageInfo);
    }
}
