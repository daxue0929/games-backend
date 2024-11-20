package org.daxue.games.utils;

import org.daxue.games.entity.common.TableIdPrefix;

import java.util.UUID;

public class IDUtil {

    public static final String FORMAT = "%s%s";

    /**
     * 生成user表业务主键
     * @return user表的userId
     */
    public static String genUserId() {
        return String.format(FORMAT, TableIdPrefix.USER, genUUID());
    }
    /**
     * 获取UUID 32位
     * @return
     */
    public static String genUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
