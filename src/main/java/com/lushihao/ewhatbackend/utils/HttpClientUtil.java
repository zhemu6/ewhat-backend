package com.lushihao.ewhatbackend.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.ContentType;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Hutool Http工具类
 */
public class HttpClientUtil {

    // 超时时间 5000ms
    private static final int TIMEOUT_MSEC = 5000;

    /**
     * GET 请求
     *
     * @param url      请求地址
     * @param paramMap 参数
     * @return 响应内容
     */
    public static String doGet(String url, Map<String, String> paramMap) {
        // Hutool HttpUtil 自动拼接参数
        return HttpUtil.get(url, new HashMap<>(paramMap), TIMEOUT_MSEC);
    }

    /**
     * POST 表单请求
     *
     * @param url      请求地址
     * @param paramMap 表单参数
     * @return 响应内容
     */
    public static String doPost(String url, Map<String, String> paramMap) {
        return HttpRequest.post(url)
                .form(new HashMap<>(paramMap))
                .timeout(TIMEOUT_MSEC)
                .execute()
                .body();
    }


    /**
     * POST JSON 请求
     *
     * @param url      请求地址
     * @param paramMap JSON 参数
     * @return 响应内容
     */
    public static String doPost4Json(String url, Map<String, String> paramMap) {
        String json = "{}";
        if (paramMap != null && !paramMap.isEmpty()) {
            // 使用静态方法生成 JSON
            json = JSONObject.toJSONString(paramMap);
        }
        HttpResponse response = HttpRequest.post(url)
                .body(json, "application/json")
                .timeout(TIMEOUT_MSEC)
                .execute();
        return response.body();
    }

}
