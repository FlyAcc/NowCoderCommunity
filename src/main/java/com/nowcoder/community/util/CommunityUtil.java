package com.nowcoder.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

public class CommunityUtil {
    // 生成随机字符串
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /*
    MD5加密（用于加密密码）：加密后无法解密，但是同样输入每次加密得到输出都一样，因此可以根据输出猜出输入
    为了提高安全性，可以加salt（原密码加一段字符串（后缀））
     */
    public static String md5(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }

        return DigestUtils.md5DigestAsHex(key.getBytes());
    }
}
