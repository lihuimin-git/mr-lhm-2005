package com.baidu.shop.service.impl;

import com.baidu.shop.entity.CategoryBrandEntity;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.mapper.CategoryBrandMapper;
import com.baidu.shop.mapper.CategoryMapper;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.service.CategoryService;
import com.google.gson.JsonObject;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class CategoryServiceImpl extends BaseApiService implements CategoryService {

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private CategoryBrandMapper categoryBrandMapper;

    //通过id集合查询分类信息


    @Override
    public Result<List<CategoryEntity>> getCategoryIds(String categoryIds) {
        List<Integer> categoryList = Arrays.asList(categoryIds.split(",")).stream().map(ids -> Integer.valueOf(ids)).collect(Collectors.toList());
        List<CategoryEntity> categoryEntities = categoryMapper.selectByIdList(categoryList);
        return this.setResultSuccess(categoryEntities);
    }

    //商品查询
    @Override
    public Result<List<CategoryEntity>> getCategoryByPid(Integer pid) {
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setParentId(pid);
        List<CategoryEntity> list = categoryMapper.select(categoryEntity);
        return this.setResultSuccess(list);
    }

    //商品删除
    @Transactional//事务 增删改都需要
    @Override
    public Result<JsonObject> delCategory(Integer id) {

        String msg = "";

        System.out.println(id);
        //判断页面传递过来的id是否合法
        if(null != id && id >0){
            //通过id查询当前节点信息
            CategoryEntity categoryEntity = categoryMapper.selectByPrimaryKey(id);

            //判断当前节点是否为父节点(安全!)
            if (categoryEntity.getParentId() == 1)return this.setResultError("当前是父节点不能删除");//return之后的代码不会执行

            //如果当前分类被品牌绑定的话不能被删除 --> 通过分类id查询中间表是否有数据 true : 当前分类不能被删除 false:继续执行
            Example example1 = new Example(CategoryBrandEntity.class);
            example1.createCriteria().andEqualTo("categoryId",id);
            List<CategoryBrandEntity> categoryBrandEntities = categoryBrandMapper.selectByExample(example1);
            if (categoryBrandEntities.size() > 0){
                return this.setResultError("当前分类被品牌绑定不能被删除 ");
            }


            //通过当前节点的父节点id 查询 当前节点(将要被删除的节点)的父节点下是否还有其他子节点
            Example example = new Example(CategoryEntity.class);
            example.createCriteria().andEqualTo("parentId",categoryEntity.getParentId());
            List<CategoryEntity> categoryList = categoryMapper.selectByExample(example);


            //如果size <= 1 --> 如果当前节点被删除的话 当前节点的父节点下没有节点了 --> 将当前节点的父节点状态改为叶子节点
            if (categoryList.size() <= 1){
                CategoryEntity updateCategoryEntity= new CategoryEntity();
                updateCategoryEntity.setParentId(0);
                updateCategoryEntity.setId(categoryEntity.getParentId());

                categoryMapper.updateByPrimaryKeySelective(updateCategoryEntity);
            }
            categoryMapper.deleteByPrimaryKey(id);
            return this.setResultSuccess();
        }
        return this.setResultError("id不合理");
    }

    //修改商品分类
    @Transactional
    @Override
    public Result<JsonObject> editCategory(CategoryEntity entity) {
        try {
            categoryMapper.updateByPrimaryKeySelective(entity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.setResultSuccess();
    }

    //新增商品分类
    @Transactional
    @Override
    public Result<JsonObject> addCategory(CategoryEntity entity) {

        CategoryEntity parentCategoryEntity = new CategoryEntity();
        parentCategoryEntity.setId(entity.getParentId());
        parentCategoryEntity.setIsParent(1);
        categoryMapper.updateByPrimaryKeySelective(parentCategoryEntity);

        categoryMapper.insertSelective(entity);

        return this.setResultSuccess();
    }

    //通过品牌查询商品id
    @Override
    public Result<List<CategoryEntity>> getByBrand(Integer brandId) {
        List<CategoryEntity> byBrandId = categoryMapper.getByBrandId(brandId);
        return this.setResultSuccess(byBrandId);
    }
}
