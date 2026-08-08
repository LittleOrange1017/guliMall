package to;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillSkuRedisTo {
    /** 场次 id */
    private Long promotionSessionId;
    /** 商品 skuId */
    private Long skuId;
    /** 秒杀价格 */
    private BigDecimal seckillPrice;
    /** 秒杀总量 */
    private BigDecimal seckillCount;
    /** 每人限购数量 */
    private BigDecimal seckillLimit;
    /** 排序 */
    private Integer seckillSort;

    // --- 秒杀增强字段 ---
    /** 场次开始时间 (毫秒时间戳) */
    private Long startTime;
    /** 场次结束时间 (毫秒时间戳) */
    private Long endTime;
    /** 秒杀随机码（防刷 Token） */
    private String randomCode;
    /** SKU 冗余详细信息 (标题、图片、属性等) */
    private SkuInfoTo skuInfo;
}
