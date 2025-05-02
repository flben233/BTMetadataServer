package org.shirakawatyu.btmetadataserver.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.ByteArrayTypeHandler;

import java.util.Map;


@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(autoResultMap = true)
public class Metadata {
    @TableId
    String infoHash;
    @TableField(typeHandler = JacksonTypeHandler.class)
    Map<String, Object> info;
    @TableField(typeHandler = ByteArrayTypeHandler.class)
    byte[] torrent;
}
