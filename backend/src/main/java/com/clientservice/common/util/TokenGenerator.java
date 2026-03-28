package com.clientservice.common.util;

import java.security.SecureRandom;

/**
 * 令牌生成器
 */
public class TokenGenerator {

    /** 令牌字符集 */
    private static final String TOKEN_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /** 数字字符集 */
    private static final String NUMBER_CHARS = "0123456789";

    /** 安全随机数生成器 */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 生成访问令牌
     *
     * @param length 令牌长度
     * @return 访问令牌
     */
    public static String generateToken(final int length) {
        StringBuilder token = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = SECURE_RANDOM.nextInt(TOKEN_CHARS.length());
            token.append(TOKEN_CHARS.charAt(index));
        }
        return token.toString();
    }

    /**
     * 生成32位访问令牌（默认）
     *
     * @return 访问令牌
     */
    public static String generateToken() {
        return generateToken(32);
    }

    /**
     * 生成随机数字字符串
     *
     * @param length 长度
     * @return 随机数字字符串
     */
    private static String generateRandomNumbers(final int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = SECURE_RANDOM.nextInt(NUMBER_CHARS.length());
            sb.append(NUMBER_CHARS.charAt(index));
        }
        return sb.toString();
    }

    /**
     * 生成ID（用于client_matter表的id字段）
     *
     * @return ID字符串
     */
    public static String generateId() {
        // 格式：CS + 时间戳（13位） + 随机数（6位）
        return "CS" + System.currentTimeMillis() + generateRandomNumbers(6);
    }
}
