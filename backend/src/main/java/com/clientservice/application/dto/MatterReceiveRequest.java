package com.clientservice.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * 接收项目数据请求DTO
 */
@Data
public class MatterReceiveRequest {

    /** 客户ID */
    @NotNull(message = "客户ID不能为空")
    @JsonProperty("clientId")
    private Long clientId;

    /** 客户名称 */
    @JsonProperty("clientName")
    private String clientName;

    /** 项目数据 */
    @NotNull(message = "项目数据不能为空")
    @JsonProperty("matterData")
    private Map<String, Object> matterData;

    /** 数据有效期（天数） */
    @JsonProperty("validDays")
    private Integer validDays;

    /** 数据范围列表 */
    @NotNull(message = "数据范围不能为空")
    @JsonProperty("scopes")
    private List<String> scopes;

    /** 律所管理系统回调基础地址 */
    @JsonProperty("callbackUrl")
    private String callbackUrl;
}
