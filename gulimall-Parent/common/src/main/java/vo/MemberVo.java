package vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class MemberVo implements Serializable {
    private String username;
    /**
     * 新增
     */
    private Long userId;
    private String avatar_url;
    private Integer integration;
}
