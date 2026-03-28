package com.clientservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 客户文件上传请求DTO
 */
@Data
public class ClientFileUploadRequest {

    /** 项目ID */
    @NotBlank(message = "项目ID不能为空")
    private String matterId;

    /** 客户ID */
    @NotNull(message = "客户ID不能为空")
    private Long clientId;

    /** 文件 */
    @NotNull(message = "文件不能为空")
    private MultipartFile file;

    /** 文件类别：EVIDENCE、CONTRACT、ID_CARD、OTHER */
    private String fileCategory;

    /** 文件描述 */
    private String description;
}
