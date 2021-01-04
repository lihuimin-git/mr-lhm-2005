package com.baidu.shop.service.impl;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SpecGroupDto;
import com.baidu.shop.entity.SpecGroupEntity;
import com.baidu.shop.mapper.SpecGroupMapper;
import com.baidu.shop.service.SpecGroupService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.ObjectUtil;
import com.google.gson.JsonObject;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class SpecGroupServiceImpl extends BaseApiService implements SpecGroupService {

    @Resource
    private SpecGroupMapper specGroupMapper;

    //规格组查询
    @Override
    public Result<List<SpecGroupEntity>> getSpecGroupInfo(SpecGroupDto specGroupDto) {
        Example example = new Example(SpecGroupEntity.class);

        if (ObjectUtil.isNotNull(specGroupDto.getCid()))
                example
                .createCriteria()
                .andEqualTo("cid", BaiduBeanUtil.copyProperties(specGroupDto,SpecGroupEntity.class).getCid());

        List<SpecGroupEntity> specGroupExample = specGroupMapper.selectByExample(example);
        return this.setResultSuccess(specGroupExample);
    }

    //规格组修改
    @Transactional
    @Override
    public Result<JsonObject> saveSpecGroup(SpecGroupDto specGroupDto) {
        specGroupMapper.insertSelective(BaiduBeanUtil.copyProperties(specGroupDto,SpecGroupEntity.class));
        return this.setResultSuccess();
    }

    //规格组修改
    @Override
    public Result<JsonObject> editSpecGroup(SpecGroupDto specGroupDto) {
        specGroupMapper.updateByPrimaryKeySelective(BaiduBeanUtil.copyProperties(specGroupDto,SpecGroupEntity.class));
        return this.setResultSuccess();
    }

    //规格组删除

    @Override
    public Result<JsonObject> delSpecRoupById(Integer id) {
        specGroupMapper.deleteByPrimaryKey(id);
        return this.setResultSuccess();
    }
}
