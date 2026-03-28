-- =====================================================
-- 客户服务系统 - 数据库迁移脚本
-- =====================================================
-- 版本: 1.1.1
-- 日期: 2026-02-15
-- 描述: 为 client_matter 表的 law_firm_matter_id 添加唯一约束
-- 目的: 防止并发插入导致重复数据 (Check-Then-Act Race Condition)
-- 注意: 此约束可能已在 01-schema.sql 中定义，使用 IF NOT EXISTS 避免重复创建错误
-- =====================================================

-- 1. 首先删除可能已经存在的重复数据（保留 ID 最大的记录）
-- 注意：在生产环境执行此操作前请务必备份数据
DELETE FROM public.client_matter a USING public.client_matter b
WHERE a.id < b.id
  AND a.law_firm_matter_id = b.law_firm_matter_id;

-- 2. 添加唯一约束（如果不存在）
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'uk_client_matter_law_firm_id'
    ) THEN
        ALTER TABLE public.client_matter
            ADD CONSTRAINT uk_client_matter_law_firm_id UNIQUE (law_firm_matter_id);
        
        COMMENT ON CONSTRAINT uk_client_matter_law_firm_id ON public.client_matter 
            IS '唯一约束：律所系统项目ID必须唯一，防止并发重复插入';
    END IF;
END $$;
