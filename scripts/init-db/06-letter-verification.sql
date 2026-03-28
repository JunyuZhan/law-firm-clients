-- 函件验证数据表
-- 用于存储律所系统推送的电子函件验证信息

CREATE TABLE IF NOT EXISTS public.letter_verification (
    id BIGSERIAL PRIMARY KEY,
    letter_id BIGINT NOT NULL,                       -- 律所系统函件ID
    application_no VARCHAR(50) NOT NULL UNIQUE,      -- 函件申请编号
    verification_code VARCHAR(100) NOT NULL,         -- 验证码
    letter_type VARCHAR(50),                         -- 函件类型编码
    letter_type_name VARCHAR(100),                   -- 函件类型名称
    target_unit VARCHAR(200),                        -- 接收单位
    lawyer_names VARCHAR(500),                       -- 出函律师姓名
    firm_name VARCHAR(200),                          -- 律师事务所名称
    matter_name VARCHAR(200),                        -- 关联项目名称（脱敏）
    approved_at TIMESTAMP,                           -- 审批时间
    printed_at TIMESTAMP,                            -- 打印/盖章时间
    valid_until TIMESTAMP,                           -- 有效期截止时间
    remark TEXT,                                     -- 备注
    verify_count INTEGER DEFAULT 0,                  -- 验证次数
    last_verify_at TIMESTAMP,                        -- 最后验证时间
    last_verify_ip VARCHAR(50),                      -- 最后验证IP
    status VARCHAR(20) DEFAULT 'ACTIVE',             -- 状态：ACTIVE/EXPIRED/REVOKED
    deleted BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE public.letter_verification IS '函件验证数据表';
COMMENT ON COLUMN public.letter_verification.letter_id IS '律所系统函件ID';
COMMENT ON COLUMN public.letter_verification.application_no IS '函件申请编号';
COMMENT ON COLUMN public.letter_verification.verification_code IS '验证码（HMAC-SHA256生成）';
COMMENT ON COLUMN public.letter_verification.letter_type IS '函件类型编码';
COMMENT ON COLUMN public.letter_verification.letter_type_name IS '函件类型名称';
COMMENT ON COLUMN public.letter_verification.target_unit IS '接收单位';
COMMENT ON COLUMN public.letter_verification.lawyer_names IS '出函律师姓名（多人逗号分隔）';
COMMENT ON COLUMN public.letter_verification.firm_name IS '律师事务所名称';
COMMENT ON COLUMN public.letter_verification.matter_name IS '关联项目名称（脱敏）';
COMMENT ON COLUMN public.letter_verification.approved_at IS '审批时间';
COMMENT ON COLUMN public.letter_verification.printed_at IS '打印/盖章时间';
COMMENT ON COLUMN public.letter_verification.valid_until IS '有效期截止时间';
COMMENT ON COLUMN public.letter_verification.remark IS '备注';
COMMENT ON COLUMN public.letter_verification.verify_count IS '验证次数';
COMMENT ON COLUMN public.letter_verification.last_verify_at IS '最后验证时间';
COMMENT ON COLUMN public.letter_verification.last_verify_ip IS '最后验证IP';
COMMENT ON COLUMN public.letter_verification.status IS '状态：ACTIVE-有效/EXPIRED-已过期/REVOKED-已撤销';

CREATE INDEX IF NOT EXISTS idx_letter_verification_code ON letter_verification(verification_code);
CREATE INDEX IF NOT EXISTS idx_letter_verification_letter_id ON letter_verification(letter_id);
CREATE INDEX IF NOT EXISTS idx_letter_verification_status ON letter_verification(status);
CREATE INDEX IF NOT EXISTS idx_letter_verification_application_no ON letter_verification(application_no);
