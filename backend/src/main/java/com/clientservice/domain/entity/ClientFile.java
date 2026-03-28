package com.clientservice.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.clientservice.common.base.BaseEntity;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 客户文件实体 - 存储客户上传的文件
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("client_file")
public class ClientFile extends BaseEntity {

    /** 主键，外部文件ID */
    @TableId(type = IdType.INPUT)
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

    /** 文件类别：EVIDENCE（证据材料）、CONTRACT（合同文件）、ID_CARD（身份证件）、OTHER（其他） */
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

    /** 状态：ACTIVE（有效）、DELETED（已删除） */
    private String status;

    // ========== 文件类别常量 ==========
    /** 文件类别：证据材料 */
    public static final String CATEGORY_EVIDENCE = "EVIDENCE";

    /** 文件类别：合同文件 */
    public static final String CATEGORY_CONTRACT = "CONTRACT";

    /** 文件类别：身份证件 */
    public static final String CATEGORY_ID_CARD = "ID_CARD";

    /** 文件类别：其他 */
    public static final String CATEGORY_OTHER = "OTHER";

    // ========== 状态常量 ==========
    /** 状态：有效 */
    public static final String STATUS_ACTIVE = "ACTIVE";

    /** 状态：已删除 */
    public static final String STATUS_DELETED = "DELETED";

    // ========== 文件来源常量 ==========
    /** 文件来源：系统推送 */
    public static final String SOURCE_PUSHED = "PUSHED";

    /** 文件来源：客户上传 */
    public static final String SOURCE_CLIENT_UPLOAD = "CLIENT_UPLOAD";
}
