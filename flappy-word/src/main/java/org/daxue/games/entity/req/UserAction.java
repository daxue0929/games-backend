package org.daxue.games.entity.req;

import lombok.Data;

/**
 * 用户行为
 *
 * @author XieYT
 * @since 2024/11/22 11:29
 */
@Data
public class UserAction {

    /**
     * 行为
     */
    private String action;

    /**
     * 字母
     */
    private String letter;

    /**
     * 时间戳
     */
    private Long time;
}
