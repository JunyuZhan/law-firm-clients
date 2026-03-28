package com.clientservice.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码哈希生成工具（临时工具类，用于生成BCrypt哈希）
 * 使用方法：运行此类的main方法，传入密码作为参数
 */
public class PasswordHashGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String password = args.length > 0 ? args[0] : "admin123";
        String hash = encoder.encode(password);
        
        System.out.println("==========================================");
        System.out.println("密码: " + password);
        System.out.println("BCrypt哈希: " + hash);
        System.out.println("==========================================");
        
        // 验证哈希是否正确
        boolean matches = encoder.matches(password, hash);
        System.out.println("验证结果: " + (matches ? "✓ 正确" : "✗ 错误"));
        
        // 验证数据库中的哈希
        String dbHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        boolean dbMatches = encoder.matches(password, dbHash);
        System.out.println("数据库哈希验证: " + (dbMatches ? "✓ 匹配" : "✗ 不匹配"));
        
        if (!dbMatches) {
            System.out.println("\n警告：数据库中的哈希值不匹配！");
            System.out.println("请使用以下SQL更新密码：");
            System.out.println("UPDATE admin_user SET password_hash = '" + hash + "' WHERE username = 'admin';");
        }
    }
}
