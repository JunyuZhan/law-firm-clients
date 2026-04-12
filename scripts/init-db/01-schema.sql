-- =====================================================
-- 客户服务系统 - 数据库初始化脚本
-- =====================================================
-- 版本: 1.1.0
-- 日期: 2026-02-06
-- 描述: 客户服务系统核心表结构
-- 说明: 新增通知失败回调机制（law_firm_matter_id、client_name、error_code字段）
-- =====================================================

-- =====================================================
-- 1. 项目数据表 - 存储从律所管理系统推送的项目数据
-- =====================================================
CREATE TABLE IF NOT EXISTS public.client_matter (
    id VARCHAR(50) PRIMARY KEY,                    -- 主键，外部系统ID
    law_firm_matter_id BIGINT NOT NULL,            -- 律所系统项目ID
    client_id BIGINT NOT NULL,                      -- 客户ID
    client_name VARCHAR(200),                       -- 客户名称
    source_api_key_id BIGINT,                       -- 来源 API Key ID
    matter_data JSONB NOT NULL,                     -- 项目数据（完整JSON）
    scopes VARCHAR(500),                             -- 数据范围（逗号分隔）
    valid_days INTEGER DEFAULT 30,                  -- 有效期（天）
    expires_at TIMESTAMP,                           -- 过期时间
    access_token VARCHAR(100) NOT NULL UNIQUE,       -- 访问令牌（用于生成访问链接）
    access_url VARCHAR(500),                        -- 客户访问链接
    status VARCHAR(20) DEFAULT 'ACTIVE',            -- 状态：ACTIVE（有效）、EXPIRED（已过期）、REVOKED（已撤销）
    deleted BOOLEAN DEFAULT false,                  -- 逻辑删除标记
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_client_matter_law_firm_id ON public.client_matter(law_firm_matter_id);
CREATE INDEX IF NOT EXISTS idx_client_matter_source_api_key_id ON public.client_matter(source_api_key_id);
ALTER TABLE public.client_matter DROP CONSTRAINT IF EXISTS uk_client_matter_law_firm_id;
ALTER TABLE public.client_matter ADD CONSTRAINT uk_client_matter_source_law_firm_id
    UNIQUE (source_api_key_id, law_firm_matter_id);

CREATE INDEX IF NOT EXISTS idx_client_matter_client_id ON public.client_matter(client_id);
CREATE INDEX IF NOT EXISTS idx_client_matter_access_token ON public.client_matter(access_token);
CREATE INDEX IF NOT EXISTS idx_client_matter_status ON public.client_matter(status);
CREATE INDEX IF NOT EXISTS idx_client_matter_expires_at ON public.client_matter(expires_at);

COMMENT ON TABLE public.client_matter IS '项目数据表 - 存储从律所管理系统推送的项目数据';
COMMENT ON COLUMN public.client_matter.id IS '主键，外部系统ID，由客户服务系统生成';
COMMENT ON COLUMN public.client_matter.law_firm_matter_id IS '律所系统项目ID，用于关联';
COMMENT ON COLUMN public.client_matter.source_api_key_id IS '来源 API Key ID，用于多来源隔离';
COMMENT ON COLUMN public.client_matter.access_token IS '访问令牌，用于生成客户访问链接';
COMMENT ON COLUMN public.client_matter.matter_data IS '项目数据，JSON格式，包含完整的项目信息';

-- =====================================================
-- 2. 通知记录表 - 记录通知发送历史
-- =====================================================
CREATE TABLE IF NOT EXISTS public.notification_record (
    id BIGSERIAL PRIMARY KEY,
    matter_id VARCHAR(50) NOT NULL,                  -- 项目ID
    client_id BIGINT NOT NULL,                      -- 客户ID
    law_firm_matter_id BIGINT,                     -- 律所系统项目ID（用于回调）
    client_name VARCHAR(100),                        -- 客户名称（用于回调）
    notification_type VARCHAR(20) NOT NULL,         -- 通知类型：SMS、WECHAT、EMAIL
    recipient VARCHAR(200) NOT NULL,                -- 接收人（手机号/微信号/邮箱）
    content TEXT NOT NULL,                          -- 通知内容
    status VARCHAR(20) DEFAULT 'PENDING',           -- 状态：PENDING、SUCCESS、FAILED
    error_code VARCHAR(50),                          -- 错误分类：DATA_NOT_FOUND、DATA_EMPTY、SEND_FAILED、CHANNEL_DISABLED
    error_message TEXT,                             -- 错误信息
    sent_at TIMESTAMP,                              -- 发送时间
    -- 重试相关字段
    retry_count INTEGER DEFAULT 0,                  -- 重试次数
    max_retries INTEGER DEFAULT 3,                  -- 最大重试次数
    next_retry_at TIMESTAMP,                        -- 下次重试时间
    last_retry_at TIMESTAMP,                        -- 最后重试时间
    deleted BOOLEAN DEFAULT false,                  -- 逻辑删除标记
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notification_matter_id ON public.notification_record(matter_id);
CREATE INDEX IF NOT EXISTS idx_notification_client_id ON public.notification_record(client_id);
CREATE INDEX IF NOT EXISTS idx_notification_status ON public.notification_record(status);
CREATE INDEX IF NOT EXISTS idx_notification_type ON public.notification_record(notification_type);
CREATE INDEX IF NOT EXISTS idx_notification_created_at ON public.notification_record(created_at DESC);
-- 重试查询优化索引
CREATE INDEX IF NOT EXISTS idx_notification_next_retry_at
    ON public.notification_record(next_retry_at)
    WHERE status = 'FAILED' AND next_retry_at IS NOT NULL;

COMMENT ON TABLE public.notification_record IS '通知记录表 - 记录通知发送历史';
COMMENT ON COLUMN public.notification_record.notification_type IS '通知类型：SMS（短信）、WECHAT（微信）、EMAIL（邮件）';
COMMENT ON COLUMN public.notification_record.law_firm_matter_id IS '律所系统项目ID，用于回调';
COMMENT ON COLUMN public.notification_record.client_name IS '客户名称，用于回调';
COMMENT ON COLUMN public.notification_record.error_code IS '错误分类：DATA_NOT_FOUND（联系方式未找到）、DATA_EMPTY（联系方式为空）、SEND_FAILED（发送失败）、CHANNEL_DISABLED（渠道未启用）';
COMMENT ON COLUMN public.notification_record.retry_count IS '当前重试次数';
COMMENT ON COLUMN public.notification_record.next_retry_at IS '下次重试时间（指数退避计算）';

-- =====================================================
-- 3. 客户文件表 - 存储客户上传的文件
-- =====================================================
CREATE TABLE IF NOT EXISTS public.client_file (
    id VARCHAR(50) PRIMARY KEY,                     -- 主键，外部文件ID
    matter_id VARCHAR(50) NOT NULL,                 -- 项目ID
    client_id BIGINT NOT NULL,                      -- 客户ID
    file_name VARCHAR(255) NOT NULL,                -- 文件名
    file_size BIGINT,                               -- 文件大小（字节）
    file_type VARCHAR(100),                         -- 文件类型（MIME类型）
    file_category VARCHAR(50) DEFAULT 'OTHER',       -- 文件类别：EVIDENCE（证据材料）、CONTRACT（合同文件）、ID_CARD（身份证件）、OTHER（其他）
    description VARCHAR(500),                       -- 文件描述
    storage_path VARCHAR(500) NOT NULL,             -- 存储路径
    file_source VARCHAR(20) DEFAULT 'CLIENT_UPLOAD', -- 文件来源：PUSHED（系统推送）、CLIENT_UPLOAD（客户上传）
    file_hash VARCHAR(64),                          -- 文件哈希值（SHA-256）
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 上传时间
    status VARCHAR(20) DEFAULT 'ACTIVE',            -- 状态：ACTIVE（有效）、DELETED（已删除）
    deleted BOOLEAN DEFAULT false,                  -- 逻辑删除标记
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_client_file_matter_id ON public.client_file(matter_id);
CREATE INDEX IF NOT EXISTS idx_client_file_client_id ON public.client_file(client_id);
CREATE INDEX IF NOT EXISTS idx_client_file_matter_hash ON public.client_file(matter_id, file_hash);
CREATE INDEX IF NOT EXISTS idx_client_file_status ON public.client_file(status);
CREATE INDEX IF NOT EXISTS idx_client_file_uploaded_at ON public.client_file(uploaded_at DESC);

COMMENT ON TABLE public.client_file IS '客户文件表 - 存储客户上传的文件和系统推送的文件';
COMMENT ON COLUMN public.client_file.id IS '主键，外部文件ID，由客户服务系统生成';
COMMENT ON COLUMN public.client_file.file_source IS '文件来源：PUSHED（系统推送）、CLIENT_UPLOAD（客户上传）';

-- =====================================================
-- 4. 访问日志表 - 记录客户访问日志
-- =====================================================
CREATE TABLE IF NOT EXISTS public.access_log (
    id BIGSERIAL PRIMARY KEY,
    matter_id VARCHAR(50) NOT NULL,                 -- 项目ID
    client_id BIGINT NOT NULL,                      -- 客户ID
    access_token VARCHAR(100) NOT NULL,              -- 访问令牌
    ip_address VARCHAR(50),                         -- IP地址
    user_agent VARCHAR(500),                        -- 用户代理
    access_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 访问时间
    deleted BOOLEAN DEFAULT false,                  -- 逻辑删除标记
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_access_log_matter_id ON public.access_log(matter_id);
CREATE INDEX IF NOT EXISTS idx_access_log_access_token ON public.access_log(access_token);
CREATE INDEX IF NOT EXISTS idx_access_log_access_time ON public.access_log(access_time DESC);

COMMENT ON TABLE public.access_log IS '访问日志表 - 记录客户访问日志';

-- =====================================================
-- 5. 系统配置表 - 存储系统配置信息
-- =====================================================
CREATE TABLE IF NOT EXISTS public.sys_config (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,         -- 配置键
    config_value TEXT,                              -- 配置值
    config_type VARCHAR(50) DEFAULT 'STRING',      -- 配置类型：STRING、NUMBER、BOOLEAN、JSON
    description VARCHAR(500),                      -- 描述
    deleted BOOLEAN DEFAULT false,                  -- 逻辑删除标记
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_sys_config_key ON public.sys_config(config_key);

COMMENT ON TABLE public.sys_config IS '系统配置表 - 存储系统配置信息';
-- 注意：初始配置数据在 02-init-data.sql 中插入

-- =====================================================
-- 6. API密钥表 - 存储API密钥（用于与律所系统对接）
-- =====================================================
CREATE TABLE IF NOT EXISTS public.api_key (
    id BIGSERIAL PRIMARY KEY,
    key_name VARCHAR(100) NOT NULL,                 -- 密钥名称
    api_key VARCHAR(200) NOT NULL UNIQUE,           -- API密钥
    api_secret VARCHAR(200),                        -- API密钥Secret（可选）
    law_firm_name VARCHAR(200),                     -- 律所名称
    enabled BOOLEAN DEFAULT true,                   -- 是否启用
    expires_at TIMESTAMP,                           -- 过期时间（NULL表示永不过期）
    last_used_at TIMESTAMP,                         -- 最后使用时间
    deleted BOOLEAN DEFAULT false,                  -- 逻辑删除标记
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_api_key_key ON public.api_key(api_key);
CREATE INDEX IF NOT EXISTS idx_api_key_enabled ON public.api_key(enabled);

COMMENT ON TABLE public.api_key IS 'API密钥表 - 存储API密钥（用于与律所系统对接）';

-- =====================================================
-- 7. 管理员用户表 - 存储管理后台登录用户
-- =====================================================
CREATE TABLE IF NOT EXISTS public.admin_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,           -- 登录用户名
    password_hash VARCHAR(200) NOT NULL,            -- 密码哈希（BCrypt）
    real_name VARCHAR(100),                         -- 真实姓名
    email VARCHAR(200),                             -- 邮箱
    phone VARCHAR(20),                              -- 手机号
    enabled BOOLEAN DEFAULT true,                   -- 是否启用
    failed_login_count INTEGER DEFAULT 0,           -- 登录失败次数
    locked_until TIMESTAMP,                         -- 锁定截止时间
    last_login_at TIMESTAMP,                        -- 最后登录时间
    last_login_ip VARCHAR(50),                      -- 最后登录IP
    deleted BOOLEAN DEFAULT false,                  -- 逻辑删除标记
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_admin_user_username ON public.admin_user(username);
CREATE INDEX IF NOT EXISTS idx_admin_user_enabled ON public.admin_user(enabled);

COMMENT ON TABLE public.admin_user IS '管理员用户表 - 存储管理后台登录用户';
COMMENT ON COLUMN public.admin_user.password_hash IS '密码哈希，使用 BCrypt 加密';
COMMENT ON COLUMN public.admin_user.failed_login_count IS '连续登录失败次数，超过阈值则锁定账户';

-- =====================================================
-- 8. 通知模板表 - 存储通知模板
-- =====================================================
CREATE TABLE IF NOT EXISTS public.notification_template (
    id BIGSERIAL PRIMARY KEY,
    template_code VARCHAR(50) NOT NULL UNIQUE,      -- 模板编码
    template_name VARCHAR(100) NOT NULL,            -- 模板名称
    template_type VARCHAR(20) NOT NULL,             -- 类型：SMS、WECHAT、EMAIL
    subject VARCHAR(200),                           -- 邮件主题（仅 EMAIL 类型）
    template_content TEXT NOT NULL,                 -- 模板内容（支持变量：{{变量名}}）
    template_variables VARCHAR(500),                -- 变量列表（JSON格式，描述变量含义）
    provider VARCHAR(50),                           -- 提供商：aliyun、tencent、wechat等
    sign_name VARCHAR(100),                         -- 签名名称（短信需要）
    enabled BOOLEAN DEFAULT true,                   -- 是否启用
    description VARCHAR(500),                       -- 描述
    deleted BOOLEAN DEFAULT false,                  -- 逻辑删除标记
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notification_template_code ON public.notification_template(template_code);
CREATE INDEX IF NOT EXISTS idx_notification_template_type ON public.notification_template(template_type);

COMMENT ON TABLE public.notification_template IS '通知模板表 - 存储短信、微信、邮件通知模板';
COMMENT ON COLUMN public.notification_template.template_content IS '模板内容，支持变量替换，格式：{{变量名}}';

-- =====================================================
-- 9. 管理员登录日志表 - 记录管理后台登录历史
-- =====================================================
CREATE TABLE IF NOT EXISTS public.admin_login_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,                                   -- 用户ID
    username VARCHAR(50),                             -- 用户名
    ip_address VARCHAR(50),                           -- IP地址
    user_agent VARCHAR(500),                          -- 用户代理
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,   -- 登录时间
    success BOOLEAN DEFAULT true,                     -- 是否成功
    failure_reason VARCHAR(200),                      -- 失败原因
    deleted BOOLEAN DEFAULT false,                    -- 逻辑删除标记
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_admin_login_log_user_id ON public.admin_login_log(user_id);
CREATE INDEX IF NOT EXISTS idx_admin_login_log_login_time ON public.admin_login_log(login_time DESC);

COMMENT ON TABLE public.admin_login_log IS '管理员登录日志表 - 记录管理后台登录历史';

-- =====================================================
-- 10. 下载日志表 - 记录文件下载历史
-- =====================================================
CREATE TABLE IF NOT EXISTS public.download_log (
    id BIGSERIAL PRIMARY KEY,
    matter_id VARCHAR(50),                            -- 项目ID
    file_id VARCHAR(50),                              -- 文件ID
    file_name VARCHAR(255),                           -- 文件名
    client_id BIGINT,                                 -- 客户ID
    access_token VARCHAR(100),                        -- 访问令牌
    ip_address VARCHAR(50),                           -- IP地址
    user_agent VARCHAR(500),                          -- 用户代理
    download_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 下载时间
    deleted BOOLEAN DEFAULT false,                    -- 逻辑删除标记
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_download_log_matter_id ON public.download_log(matter_id);
CREATE INDEX IF NOT EXISTS idx_download_log_file_id ON public.download_log(file_id);
CREATE INDEX IF NOT EXISTS idx_download_log_download_time ON public.download_log(download_time DESC);

COMMENT ON TABLE public.download_log IS '下载日志表 - 记录文件下载历史';
