package com.clientservice.infrastructure.notification.wechat;

import com.clientservice.common.exception.BusinessException;
import com.clientservice.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 微信API客户端
 */
@Slf4j
@Component
public class WechatApiClient {

    private final RestTemplate restTemplate;

    public WechatApiClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);  // 5秒连接超时
        factory.setReadTimeout(10000);    // 10秒读取超时
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * GET请求
     *
     * @param url 请求URL
     * @return 响应内容
     */
    public String get(final String url) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, String.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("微信API GET请求失败: url={}", url, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "微信API请求失败: " + e.getMessage());
        }
    }

    /**
     * POST请求（JSON）
     *
     * @param url 请求URL
     * @param jsonBody JSON请求体
     * @return 响应内容
     */
    public String postJson(final String url, final String jsonBody) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("微信API POST请求失败: url={}, body={}", url, jsonBody, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "微信API请求失败: " + e.getMessage());
        }
    }
}
