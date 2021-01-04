package com.baidu.shop.service.impl;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SpecParamDto;
import com.baidu.shop.entity.SpecParamEntity;
import com.baidu.shop.mapper.SpecParamMapper;
import com.baidu.shop.service.SpecParamService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.google.gson.JsonObject;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class SpecParamServiceImpl extends BaseApiService implements SpecParamService {

    @Resource
    private SpecParamMapper specParamMapper;

    //查询规格参数
    @Override
    public Result<List<SpecParamEntity>> getSpecParamInfo(SpecParamDto specParamDto) {
        Example example = new Example(SpecParamEntity.class);
        example.createCriteria().andEqualTo("groupId",specParamDto.getGroupId());
        List<SpecParamEntity> specParamEntities = specParamMapper.selectByExample(example);
        return this.setResultSuccess(specParamEntities);
    }

    //新增规格参数
    @Transactional
    @Override
    public Result<JsonObject> saveSpecParam(SpecParamDto specParamDto) {
        specParamMapper.insertSelective(BaiduBeanUtil.copyProperties(specParamDto,SpecParamEntity.class));
        return this.setResultSuccess();
    }

    //新增规格参数
    @Transactional
    @Override
    public Result<JsonObject> editSpecParam(SpecParamDto specParamDto) {
        specParamMapper.updateByPrimaryKeySelective(BaiduBeanUtil.copyProperties(specParamDto,SpecParamEntity.class));
        return this.setResultSuccess();
    }

    //删除规格参数
    @Transactional
    @Override
    public Result<JsonObject> delSpecParam(Integer id) {
        specParamMapper.deleteByPrimaryKey(id);
        return this.setResultSuccess();
    }
}
