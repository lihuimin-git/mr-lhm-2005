package com.baidu.shop.base;

import com.baidu.shop.status.HTTPStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * @ClassName Result
 * @Description: 统一返回
 * @Author shenyaqi
 * @Date 2020/8/17
 * @Version V1.0
 **/
@Data
@NoArgsConstructor
public class Result<T> {
    private Integer code;//返回码
    private String message;//返回消息
    private T data;//返回数据
    public Result(Integer code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = (T) data;
    }

    public Boolean isSuccess(){
        return code == HTTPStatus.OK;
    }
}

