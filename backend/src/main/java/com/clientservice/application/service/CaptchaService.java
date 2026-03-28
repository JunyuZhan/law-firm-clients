package com.clientservice.application.service;

import com.clientservice.common.exception.BusinessException;
import com.clientservice.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务
 * 
 * <p>生成和验证图形验证码，防止自动化攻击和DDoS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaService {

    private final StringRedisTemplate redisTemplate;

    /** 验证码Redis Key前缀 */
    private static final String CAPTCHA_KEY_PREFIX = "captcha:";

    /** 验证码有效期（分钟，可配置） */
    @Value("${client-service.security.captcha.expire-minutes:5}")
    private int captchaExpireMinutes;

    /** 验证码字符集（排除易混淆字符：0, O, I, l） */
    private static final String CAPTCHA_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    /** 验证码长度（可配置） */
    @Value("${client-service.security.captcha.length:4}")
    private int captchaLength;

    /** 图片宽度 */
    private static final int IMAGE_WIDTH = 120;

    /** 图片高度 */
    private static final int IMAGE_HEIGHT = 40;

    /** 安全随机数生成器 */
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 生成验证码图片（Base64编码）
     *
     * @param captchaId 验证码ID（由客户端生成，如UUID）
     * @return Base64编码的图片数据（data:image/png;base64,...）
     */
    public String generateCaptchaImage(String captchaId) {
        // 生成验证码文本
        String captchaText = generateCaptchaText();

        // 将验证码存储到Redis（可配置过期时间）
        String captchaKey = CAPTCHA_KEY_PREFIX + captchaId;
        redisTemplate.opsForValue().set(captchaKey, captchaText.toLowerCase(), 
            captchaExpireMinutes, TimeUnit.MINUTES);

        // 生成验证码图片
        BufferedImage image = createCaptchaImage(captchaText);

        // 转换为Base64
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            return "data:image/png;base64," + base64Image;
        } catch (IOException e) {
            log.error("生成验证码图片失败", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "生成验证码失败");
        }
    }

    /**
     * 验证验证码
     *
     * @param captchaId 验证码ID
     * @param captchaText 用户输入的验证码
     * @return 是否验证通过
     */
    public boolean validateCaptcha(String captchaId, String captchaText) {
        if (captchaId == null || captchaId.isEmpty() || captchaText == null || captchaText.isEmpty()) {
            return false;
        }

        String captchaKey = CAPTCHA_KEY_PREFIX + captchaId;
        String storedCaptcha = redisTemplate.opsForValue().get(captchaKey);

        if (storedCaptcha == null) {
            log.debug("验证码不存在或已过期: captchaId={}", captchaId);
            return false;
        }

        // 验证码不区分大小写
        boolean isValid = storedCaptcha.equalsIgnoreCase(captchaText.trim());

        // 验证后立即删除验证码（一次性使用）
        if (isValid) {
            redisTemplate.delete(captchaKey);
        }

        return isValid;
    }

    /**
     * 生成验证码文本
     */
    private String generateCaptchaText() {
        StringBuilder sb = new StringBuilder(captchaLength);
        for (int i = 0; i < captchaLength; i++) {
            int index = RANDOM.nextInt(CAPTCHA_CHARS.length());
            sb.append(CAPTCHA_CHARS.charAt(index));
        }
        return sb.toString();
    }

    /**
     * 创建验证码图片
     */
    private BufferedImage createCaptchaImage(String text) {
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 设置抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 填充背景（浅灰色）
        g.setColor(new Color(245, 245, 245));
        g.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

        // 绘制干扰线
        g.setColor(new Color(200, 200, 200));
        for (int i = 0; i < 5; i++) {
            int x1 = RANDOM.nextInt(IMAGE_WIDTH);
            int y1 = RANDOM.nextInt(IMAGE_HEIGHT);
            int x2 = RANDOM.nextInt(IMAGE_WIDTH);
            int y2 = RANDOM.nextInt(IMAGE_HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }

        // 绘制验证码文字
        Font font = new Font("Arial", Font.BOLD, 28);
        g.setFont(font);
        int x = 20;
        for (char c : text.toCharArray()) {
            // 随机颜色（深色系）
            int r = RANDOM.nextInt(100) + 50;
            int green = RANDOM.nextInt(100) + 50;
            int b = RANDOM.nextInt(100) + 50;
            g.setColor(new Color(r, green, b));

            // 随机旋转角度（-15到15度）
            double angle = (RANDOM.nextDouble() - 0.5) * 0.3;
            g.rotate(angle, x, IMAGE_HEIGHT / 2);

            // 绘制字符
            g.drawString(String.valueOf(c), x, IMAGE_HEIGHT / 2 + 8);
            g.rotate(-angle, x, IMAGE_HEIGHT / 2);

            x += 25;
        }

        // 绘制干扰点
        g.setColor(new Color(180, 180, 180));
        for (int i = 0; i < 30; i++) {
            int dotX = RANDOM.nextInt(IMAGE_WIDTH);
            int dotY = RANDOM.nextInt(IMAGE_HEIGHT);
            g.fillOval(dotX, dotY, 2, 2);
        }

        g.dispose();
        return image;
    }
}
