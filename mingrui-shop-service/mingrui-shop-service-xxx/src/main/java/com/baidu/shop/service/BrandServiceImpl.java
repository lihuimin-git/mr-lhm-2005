package com.baidu.shop.service;

import com.baidu.shop.dto.SpuDto;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.entity.CategoryBrandEntity;
import com.baidu.shop.mapper.BrandMapper;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.BrandDto;
import com.baidu.shop.mapper.CategoryBrandMapper;
import com.baidu.shop.service.BrandService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.ObjectUtil;
import com.baidu.shop.utils.PinyinUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class BrandServiceImpl extends BaseApiService implements BrandService{

    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private CategoryBrandMapper categoryBrandMapper;

    //通过品牌id集合获取品牌


    @Override
    public Result<List<BrandEntity>> getBrandByIds(String brandIds) {
        List<Integer> brandIdsList = Arrays.asList(brandIds.split(",")).stream().map(ids -> Integer.valueOf(ids)).collect(Collectors.toList());
        List<BrandEntity> brandEntities = brandMapper.selectByIdList(brandIdsList);
        return this.setResultSuccess(brandEntities);
    }

    //查询品牌
    @Override
    public Result<PageInfo<BrandEntity>> getBrandInfo(BrandDto brandDto) {
        //mybatis如何自定义分页插件 --》 mybatis执行器
//        PageHelper.startPage(brandDto.getPage(),brandDto.getRows());
        if (ObjectUtil.isNotNull(brandDto.getPage()) && ObjectUtil.isNotNull(brandDto.getRows()))
            PageHelper.startPage(brandDto.getPage(),brandDto.getRows());

        if (!StringUtils.isEmpty(brandDto.getSort()))PageHelper.orderBy(brandDto.getOrder());

        BrandEntity brandEntity = BaiduBeanUtil.copyProperties(brandDto, BrandEntity.class);

        Example example = new Example(BrandEntity.class);

        Example.Criteria criteria = example.createCriteria();
//        example.createCriteria().andLike("name","%" + brandEntity.getName() + "%");
        if(ObjectUtil.isNotNull(brandEntity.getName())){
            criteria.andLike("name","%" + brandEntity.getName() + "%");
        }
        if (ObjectUtil.isNotNull(brandDto.getId()))criteria.andEqualTo("id",brandDto.getId());

        List<BrandEntity> brandEntities = brandMapper.selectByExample(example);
        PageInfo<BrandEntity> pageInfo = new PageInfo<>(brandEntities);
        return this.setResultSuccess(pageInfo);
    }

    //新增品牌
    @Transactional
    @Override
    public Result<JsonObject> save(BrandDto brandDto) {

        //新增返回主键?
        //两种方式实现 select-key insert加两个属性
        BrandEntity brandEntity = BaiduBeanUtil.copyProperties(brandDto,BrandEntity.class);
        //处理品牌首字母
        brandDto.setLetter(PinyinUtil.getUpperCase(String.valueOf(brandDto.getName().charAt(0)),PinyinUtil.TO_FUUL_PINYIN).charAt(0));

        brandMapper.insertSelective(brandEntity);
        //维护中间表数据
        this.insetrCategoryBrandList(brandDto.getCategories(),brandEntity.getId());
        return this.setResultSuccess();
    }

    //修改品牌
    @Transactional
    @Override
    public Result<JsonObject> eidt(BrandDto brandDto) {


        BrandEntity brandEntity = BaiduBeanUtil.copyProperties(brandDto, BrandEntity.class);

        brandEntity.setLetter(PinyinUtil.getUpperCase(String.valueOf(brandEntity.getName().toCharArray()[0]),false).toCharArray()[0]);
        brandMapper.updateByPrimaryKeySelective(brandEntity);
        //先通过brandId删除中间表的数据
        this.delteCategoryByBrandId(brandEntity.getId());
        //批量新增 / 新增
        this.insetrCategoryBrandList(brandDto.getCategories(),brandEntity.getId());
        return this.setResultSuccess();
    }

    //删除品牌
    @Override
    public Result<JsonObject> del(Integer id) {
        //删除品牌信息
        brandMapper.deleteByPrimaryKey(id);
        this.delteCategoryByBrandId(id);
        return this.setResultSuccess();
    }

    private void  delteCategoryByBrandId (Integer brandId){
        Example example = new Example(CategoryBrandEntity.class);
        example.createCriteria().andEqualTo("brandId",brandId);
        categoryBrandMapper.deleteByExample(example);
    }

    //新增修改整合
    private void insetrCategoryBrandList (String categories,Integer brandId){
        if (StringUtils.isEmpty(categories))throw new RuntimeException("分类信息不能为空");

        //判断分类集合字符串中是否包含,
        if(categories.contains(",")){
            categoryBrandMapper.insertList(
                    //数组转list
                    Arrays.asList(categories.split(","))
                            //获取stream流，流：对一次数据进行操作
                            .stream()
                            //遍历list中所有数据
                            .map(categoryById->new CategoryBrandEntity(Integer.valueOf(categoryById),brandId))
                            //最终转换成list
                            .collect(Collectors.toList())
            );
        }else{
            CategoryBrandEntity categoryBrandEntity = new CategoryBrandEntity();
            categoryBrandEntity.setCategoryId(Integer.valueOf(categories));
            categoryBrandEntity.setBrandId(brandId);

            categoryBrandMapper.insertSelective(categoryBrandEntity);
        }
    }

    //通过分类id获取品牌
    @Override
    public Result<List<BrandEntity>> getBrandInfoByCategoryById(Integer cid) {
        List<BrandEntity> byId = brandMapper.getBrandInfoByCategoryById(cid);
        return this.setResultSuccess(byId);
    }
}