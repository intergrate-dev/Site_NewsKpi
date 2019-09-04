
/*
 * Copyright @ 2018 Springboot4RabbitMQ 下午7:00:11 All right reserved.
 */

package com.example.util;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;


public class FastJsonConvertUtil<T> {

    public static String convertObjectToJSON(Object object) {
        return JSON.toJSONString(object);

    }

    public static Object convertJSONToObject(String message, Class<Object> clazz) {
        JSONObject json = JSONObject.parseObject(message);
        return json.toJavaObject(clazz);
    }

    public static JSONObject toJsonObject(Object javaBean) {
        return JSONObject.parseObject(JSONObject.toJSON(javaBean).toString());
    }

}
