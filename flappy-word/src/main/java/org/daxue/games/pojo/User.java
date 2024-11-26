package org.daxue.games.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@TableName("user")
public class User {

    @TableId(value = "id", type = IdType.AUTO)
    Integer id;

    @TableField(value = "user_id")
    String userId;

    @TableField(value = "name")
    String name;

    @TableField(value = "code")
    String code;

    @TableField(value = "status")
    String status;

    @TableField(value = "create_time")
    LocalDateTime createTime;

    @TableField(value = "update_time")
    LocalDateTime updateTime;


}
