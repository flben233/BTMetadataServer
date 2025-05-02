package org.shirakawatyu.btmetadataserver.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExtraData {
    @TableId
    String infoHash;
    String title;
    String publish;
    String category;
}
