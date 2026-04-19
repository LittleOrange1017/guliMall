package to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * ClassName: SpuBoundTo
 * Package:to
 * Description:
 *
 * @Author 小橘子神灯
 * @Create 2026/4/19 11:09
 * @Version 1.0
 */
@Data
public class SpuBoundTo {
    private Long spuId;
    private BigDecimal buyBounds; // 购物积分
    private BigDecimal growBounds; // 成长积分
}
