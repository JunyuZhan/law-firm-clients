-- =====================================================
-- 客户服务系统 - 来源归属加固迁移
-- =====================================================
-- 版本: 1.1.2
-- 日期: 2026-04-12
-- 描述:
--   1. 为 client_matter 增加 source_api_key_id 显式列，并将唯一约束升级为 (source_api_key_id, law_firm_matter_id)
--   2. 为 letter_verification 增加 source_api_key_id 显式列，并建立来源维度唯一索引
-- 说明:
--   1. client_matter 历史数据优先从 matter_data._sourceApiKeyId 回填
--   2. 如存在跨来源重号或历史脏数据，需要在执行前人工确认
-- =====================================================

ALTER TABLE public.client_matter
    ADD COLUMN IF NOT EXISTS source_api_key_id BIGINT;

UPDATE public.client_matter
SET source_api_key_id = NULLIF(matter_data ->> '_sourceApiKeyId', '')::BIGINT
WHERE source_api_key_id IS NULL
  AND matter_data IS NOT NULL
  AND matter_data ? '_sourceApiKeyId';

CREATE INDEX IF NOT EXISTS idx_client_matter_source_api_key_id
    ON public.client_matter(source_api_key_id);

ALTER TABLE public.client_matter
    DROP CONSTRAINT IF EXISTS uk_client_matter_law_firm_id;

ALTER TABLE public.client_matter
    DROP CONSTRAINT IF EXISTS uk_client_matter_source_law_firm_id;

ALTER TABLE public.client_matter
    ADD CONSTRAINT uk_client_matter_source_law_firm_id
    UNIQUE (source_api_key_id, law_firm_matter_id);

COMMENT ON COLUMN public.client_matter.source_api_key_id IS '来源 API Key ID，用于多来源隔离';
COMMENT ON CONSTRAINT uk_client_matter_source_law_firm_id ON public.client_matter
    IS '唯一约束：同一来源下律所系统项目ID必须唯一';

ALTER TABLE public.letter_verification
    ADD COLUMN IF NOT EXISTS source_api_key_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_letter_verification_source_api_key_id
    ON public.letter_verification(source_api_key_id);

DROP INDEX IF EXISTS uk_letter_verification_source_application_no;
CREATE UNIQUE INDEX IF NOT EXISTS uk_letter_verification_source_application_no
    ON public.letter_verification(source_api_key_id, application_no)
    WHERE deleted = false;

COMMENT ON COLUMN public.letter_verification.source_api_key_id IS '来源 API Key ID，用于多来源隔离';
