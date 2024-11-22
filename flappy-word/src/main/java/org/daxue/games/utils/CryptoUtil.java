package org.daxue.games.utils;

import lombok.extern.slf4j.Slf4j;
import java.nio.charset.StandardCharsets;
import java.net.URLDecoder;
import java.util.Base64;

@Slf4j
public class CryptoUtil {
    private static final String SECRET_KEY = "3ab6c89d1e#$@";
    private static final String SEPARATOR = "#";

    public static String decrypt(String encryptedStr) {
        if (encryptedStr == null || encryptedStr.trim().isEmpty()) {
            return encryptedStr;
        }

        try {
            // 1. Base64解码
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedStr.trim());
            String decoded = new String(decodedBytes, StandardCharsets.UTF_8);

            // 2. XOR解密
            StringBuilder decrypted = new StringBuilder();
            for (int i = 0; i < decoded.length(); i++) {
                char charCode = (char) (decoded.charAt(i) ^ SECRET_KEY.charAt(i % SECRET_KEY.length()));
                decrypted.append(charCode);
            }

            // 3. 提取实际内容
            String result = decrypted.toString();
            int separatorIndex = result.indexOf(SEPARATOR);
            if (separatorIndex >= 0 && separatorIndex < result.length() - 1) {
                result = result.substring(separatorIndex + 1);
            } else {
                log.warn("解密数据格式异常，未找到分隔符: {}", encryptedStr);
                return encryptedStr;
            }

            // 4. URL解码
            return URLDecoder.decode(result, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("解密失败: {}", encryptedStr, e);
            return encryptedStr;
        }
    }

    // 用于测试的主方法
    public static void main(String[] args) {
        try {
            // 测试加密字符串
            String encryptedStr = "ClJXWAVTTQcSQBZmZQQjRwRRWVoQWApNAXIBRFF3RgoLDkQIUwFyAURQdUYKCwhUEVdBMhZTUBNQeRxWAzAGFnIWUyETUQpNDVwABhZyFlIjB1QLC1YEUBcTdgJWVhNUfBxWckAUZmUBUwNVF1FWChRXEQFzckRQBAlNVBQUVxEBcnBEUAQPXU0QVBcGFnIWUiMTUQpjQQNXBhYDFlNQQgpVXEEDVwYXAQJWUQRRDQxQBlITFXMWViYTUXscU3NAERYhUBULWQ0dC1YUVmIBcgELF1sTHQtWFFdgAXIBDQdCF11LQQNXBhcBFlNQdEYKC0EDJgYWckcID1NGCgtBAiQSE3MBU1cDVw8BVABQBhMEFlQm";

            // 打印每一步的结果，用于调试
            String decrypted = decrypt(encryptedStr);
            System.out.println("最终解密结果: " + decrypted);

            // 测试一个简单的字符串
            String testEncrypted = encrypt("Hello World");
            String testDecrypted = decrypt(testEncrypted);
            System.out.println("测试字符串解密结果: " + testDecrypted);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 用于测试的加密方法
    public static String encrypt(String str) {
        try {
            if (str == null || str.isEmpty()) return str;

            // URL编码
            String encoded = java.net.URLEncoder.encode(str, StandardCharsets.UTF_8.name());

            // 添加前缀
            String withPrefix = "test" + SEPARATOR + encoded;

            // XOR加密
            StringBuilder encrypted = new StringBuilder();
            for (int i = 0; i < withPrefix.length(); i++) {
                char charCode = (char) (withPrefix.charAt(i) ^ SECRET_KEY.charAt(i % SECRET_KEY.length()));
                encrypted.append(charCode);
            }

            // Base64编码
            return Base64.getEncoder().encodeToString(encrypted.toString().getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            e.printStackTrace();
            return str;
        }
    }
}