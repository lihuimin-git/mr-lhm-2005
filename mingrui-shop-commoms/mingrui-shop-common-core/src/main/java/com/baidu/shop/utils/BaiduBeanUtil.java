package com.baidu.shop.utils;

import org.springframework.beans.BeanUtils;

public class BaiduBeanUtil<T>{
    public static <T> T copyProperties(Object source,Class<T> tClass) {
        T t=null;
        try {
            t = tClass.newInstance();//创建实例
            BeanUtils.copyProperties(source, t);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return t;
    }
}
