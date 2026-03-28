package com.clientservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 客户文件DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientFileDTO {

    /** 文件ID */
    private String id;

    /** 项目ID */
    private String matterId;

    /** 客户ID */
    private Long clientId;

    /** 文件名 */
    private String fileName;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 文件类型（MIME类型） */
    private String fileType;

    /** 文件类别 */
    private String fileCategory;

    /** 文件描述 */
    private String description;

    /** 存储路径 */
    private String storagePath;

    /** 文件来源：PUSHED（系统推送）、CLIENT_UPLOAD（客户上传） */
    private String fileSource;

    /** 文件哈希值（SHA-256） */
    private String fileHash;

    /** 上传时间 */
    private LocalDateTime uploadedAt;

    /** 状态 */
    private String status;
}
