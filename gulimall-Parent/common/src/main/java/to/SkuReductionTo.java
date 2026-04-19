package to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * ClassName: SkuReductionTo
 * Package:to
 * Description:
 *
 * @Author 小橘子神灯
 * @Create 2026/4/19 15:06
 * @Version 1.0
 */
@Data
public class SkuReductionTo {
    private Long skuId;
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;

    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;
    private List<MemberPrice> memberPrice;
    @Data
    public static class MemberPrice {
        private Long id;
        private String name;
        private BigDecimal price;
    }
}
