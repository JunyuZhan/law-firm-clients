-- =====================================================
-- 客户服务系统 - 数据库迁移脚本
-- =====================================================
-- 版本: 1.1.2
-- 日期: 2026-02-15
-- 描述: 为 client_file 表添加 file_hash 字段
-- 目的: 支持文件上传幂等性校验，避免重复上传相同文件
-- =====================================================

-- 1. 添加 file_hash 字段
ALTER TABLE public.client_file ADD COLUMN IF NOT EXISTS file_hash VARCHAR(64);
COMMENT ON COLUMN public.client_file.file_hash IS '文件哈希值（SHA-256），用于幂等性校验';

-- 2. 添加索引以加速查询
CREATE INDEX IF NOT EXISTS idx_client_file_matter_hash ON public.client_file(matter_id, file_hash);
