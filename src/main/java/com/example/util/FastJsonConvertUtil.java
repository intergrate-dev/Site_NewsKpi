
/*
 * Copyright @ 2018 Springboot4RabbitMQ 下午7:00:11 All right reserved.
 */

package com.example.util;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;


/**
 * @desc: Springboot4RabbitMQ
 * @author: 吴晓
 * @createTime: 2018年9月12日 下午7:00:11
 * @history:
 * @version: v1.0
 */

public class FastJsonConvertUtil<T> {

    /**
     * @param object
     * @return String
     * @author: 吴晓
     * @createTime: 2018年9月12日 下午7:04:44
     * @history:
     */

    public static String convertObjectToJSON(Object object) {
        return JSON.toJSONString(object);

    }

    /**
     * @param message
     * @param clazz
     * @return Order
     * @author: 吴晓
     * @createTime: 2018年9月12日 下午7:15:33
     * @history:
     */

    public static Object convertJSONToObject(String message, Class<Object> clazz) {
        JSONObject json = JSONObject.parseObject(message);
        return json.toJavaObject(clazz);
    }

    public static JSONObject toJsonObject(Object javaBean) {
        return JSONObject.parseObject(JSONObject.toJSON(javaBean).toString());
    }

}
