package com.clientservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件删除请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileDeleteRequest {

    /** 文件ID */
    private String fileId;

    /** 操作类型：DELETE（删除）、CLEANUP（清理） */
    private String action;
}
