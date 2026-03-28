package com.clientservice.application.util;

import com.clientservice.application.dto.NotificationTemplate;
import lombok.extern.slf4j.Slf4j;

/**
 * 模板变量替换工具类
 * 统一处理通知模板中的变量替换逻辑
 *
 * @author client-service
 * @since 2026-02-03
 */
@Slf4j
public class TemplateVariableReplacer {

    /**
     * 支持的变量名
     */
    public static final String VAR_MATTER_NAME = "${matterName}";
    public static final String VAR_ACCESS_URL = "${accessUrl}";
    public static final String VAR_CLIENT_NAME = "${clientName}";
    public static final String VAR_VALID_DAYS = "${validDays}";

    /**
     * 默认有效期天数
     */
    private static final int DEFAULT_VALID_DAYS = 30;

    /**
     * 替换模板内容中的变量
     *
     * @param templateContent 模板内容
     * @param template 通知模板（包含变量数据）
     * @return 替换后的内容
     */
    public static String replaceVariables(
            final String templateContent,
            final NotificationTemplate template) {
        if (templateContent == null || template == null) {
            return templateContent;
        }

        String result = templateContent;

        // 替换项目名称
        result = result.replace(
                VAR_MATTER_NAME,
                getSafeString(template.getMatterName()));

        // 替换访问链接
        result = result.replace(
                VAR_ACCESS_URL,
                getSafeString(template.getAccessUrl()));

        // 替换客户名称
        result = result.replace(
                VAR_CLIENT_NAME,
                getSafeString(template.getClientName()));

        // 替换有效期天数
        result = result.replace(
                VAR_VALID_DAYS,
                String.valueOf(getValidDays(template.getValidDays())));

        return result;
    }

    /**
     * 安全获取字符串值（空值返回空字符串）
     *
     * @param value 原始值
     * @return 非空字符串
     */
    private static String getSafeString(final String value) {
        return value != null ? value : "";
    }

    /**
     * 获取有效期天数（空值返回默认值）
     *
     * @param validDays 有效期天数
     * @return 有效期天数
     */
    private static int getValidDays(final Integer validDays) {
        return validDays != null ? validDays : DEFAULT_VALID_DAYS;
    }

    /**
     * 构建默认短信内容
     *
     * @param template 通知模板
     * @return 默认短信内容
     */
    public static String buildDefaultSmsContent(final NotificationTemplate template) {
        return String.format(
                "【客户服务系统】您好，%s的项目信息已更新，访问链接：%s，有效期%d天。",
                getSafeString(template.getClientName()),
                getSafeString(template.getAccessUrl()),
                getValidDays(template.getValidDays()));
    }

    /**
     * 构建默认邮件内容（HTML格式）
     *
     * @param template 通知模板
     * @return 默认邮件HTML内容
     */
    public static String buildDefaultEmailContent(final NotificationTemplate template) {
        return String.format(
                "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                "<h2 style='color: #2c3e50;'>项目信息已更新</h2>" +
                "<p>尊敬的%s，</p>" +
                "<p>您的项目信息已更新，请点击以下链接查看详情：</p>" +
                "<p style='text-align: center; margin: 30px 0;'>" +
                "<a href='%s' style='display: inline-block; padding: 12px 30px; background-color: #3498db; color: white; text-decoration: none; border-radius: 5px;'>查看项目详情</a>" +
                "</p>" +
                "<p>链接有效期：%d天</p>" +
                "<p style='color: #7f8c8d; font-size: 12px; margin-top: 30px;'>此邮件由系统自动发送，请勿回复。</p>" +
                "</div>" +
                "</body>" +
                "</html>",
                getSafeString(template.getClientName()),
                getSafeString(template.getAccessUrl()),
                getValidDays(template.getValidDays()));
    }
}
