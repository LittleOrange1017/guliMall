package vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class MemberVo implements Serializable {
    private String username;
    private Long userId;
    private String avatar_url;
}
