package to;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillOrderTo {
    /** 订单号 (全局唯一，由 IdWorker 生成) */
    private String orderSn;

    /** 活动场次 ID */
    private Long promotionSessionId;

    /** 商品 SKU ID */
    private Long skuId;

    /** 秒杀价格 */
    private BigDecimal seckillPrice;

    /** 购买数量 */
    private Integer num;

    /** 会员/买家 ID */
    private Long memberId;
}
