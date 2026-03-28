package com.clientservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 联系方式提取结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactInfoExtractResult {

    /** 提取的值 */
    private String value;

    /** 已尝试查找的字段名列表 */
    private String[] searchedFieldNames;

    /** 是否找到 */
    public boolean isFound() {
        return value != null && !value.isEmpty();
    }
}
