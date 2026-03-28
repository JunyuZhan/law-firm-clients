-- =====================================================
-- 初始化数据脚本（生产环境）
-- =====================================================
-- 用途：生产环境初始化必需数据
-- 注意：此脚本不包含测试数据，仅包含系统运行必需的基础数据
-- =====================================================
--
-- ██╗    ██╗ █████╗ ██████╗ ███╗   ██╗██╗███╗   ██╗ ██████╗ 
-- ██║    ██║██╔══██╗██╔══██╗████╗  ██║██║████╗  ██║██╔════╝ 
-- ██║ █╗ ██║███████║██████╔╝██╔██╗ ██║██║██╔██╗ ██║██║  ███╗
-- ██║███╗██║██╔══██║██╔══██╗██║╚██╗██║██║██║╚██╗██║██║   ██║
-- ╚███╔███╔╝██║  ██║██║  ██║██║ ╚████║██║██║ ╚████║╚██████╔╝
--  ╚══╝╚══╝ ╚═╝  ╚═╝╚═╝  ╚═╝╚═╝  ╚═══╝╚═╝╚═╝  ╚═══╝ ╚═════╝ 
--
-- 安全警告：
-- 1. 默认管理员密码为 admin123，部署后必须立即修改！
-- 2. 请确保已配置强 JWT 密钥（环境变量 CLIENT_SERVICE_JWT_SECRET）
-- 3. 请确保已修改数据库默认密码
--
-- =====================================================

-- =====================================================
-- 1. 管理员用户初始化数据
-- =====================================================

-- 插入默认管理员账号
-- ┌─────────────────────────────────────────────────────┐
-- │  用户名：admin                                       │
-- │  密码：admin123                                      │
-- │  ⚠️  生产环境部署后，请立即登录并修改密码！          │
-- └─────────────────────────────────────────────────────┘
INSERT INTO public.admin_user (
    username, password_hash, real_name, email, enabled, 
    failed_login_count, locked_until, deleted
)
VALUES 
    (
        'admin', 
        '$2a$10$ku0ryzybvQ8h8G.jiNDsL.qgiJebObwN/olqNfNSBCMSX9ANwaJfW',  -- admin123的BCrypt加密
        '系统管理员', 
        'admin@example.com', 
        true, 
        0, 
        NULL, 
        false
    )
ON CONFLICT (username) DO NOTHING;

-- =====================================================
-- 2. 系统配置初始化数据
-- =====================================================

-- 插入系统配置（使用 ON CONFLICT DO NOTHING 避免覆盖已有配置）
INSERT INTO public.sys_config (config_key, config_value, config_type, description)
VALUES 
    -- 系统基本信息
    ('system.name', '客户服务系统', 'STRING', '系统名称'),
    ('system.version', '1.0.0', 'STRING', '系统版本'),
    ('system.base-url', 'http://localhost', 'STRING', '系统基础URL（用于生成客户访问链接）'),
    -- 通知配置
    ('notification.email.enabled', 'false', 'BOOLEAN', '邮件通知是否启用'),
    ('notification.sms.enabled', 'false', 'BOOLEAN', '短信通知是否启用'),
    ('notification.wechat.enabled', 'false', 'BOOLEAN', '微信通知是否启用'),
    ('notification.wechat.template-id', '', 'STRING', '微信模板ID'),
    -- 文件配置
    ('file.max-size', '10485760', 'NUMBER', '最大文件大小（字节，默认10MB）'),
    ('file.storage.type', 'local', 'STRING', '文件存储类型：local、minio、oss'),
    -- Token 配置
    ('api.token-expire-days', '30', 'NUMBER', 'Token默认有效期（天）'),
    ('api.token-length', '32', 'NUMBER', 'Token长度'),
    -- 律所系统回调配置
    ('callback.enabled', 'true', 'BOOLEAN', '是否启用律所系统回调'),
    ('callback.law-firm-url', '', 'STRING', '律所管理系统回调地址'),
    ('callback.api-key', '', 'STRING', '回调API密钥')
ON CONFLICT (config_key) DO NOTHING;

-- =====================================================
-- 3. 验证数据
-- =====================================================

-- 验证管理员用户
SELECT '管理员用户数量: ' || COUNT(*) FROM public.admin_user WHERE enabled = true;

-- 验证系统配置
SELECT '系统配置数量: ' || COUNT(*) FROM public.sys_config;
