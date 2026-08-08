package to;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuInfoTo {
    /**
     * skuId
     */
    private Long skuId;

    /**
     * spuId
     */
    private Long spuId;

    /**
     * SKU 标题 / 名称
     */
    private String skuTitle;

    /**
     * 副标题
     */
    private String skuSubtitle;

    /**
     * 商品描述
     */
    private String skuDesc;

    /**
     * 所属分类 ID
     */
    private Long catalogId;

    /**
     * 所属品牌 ID
     */
    private Long brandId;

    /**
     * 默认展示主图 URL
     */
    private String skuDefaultImg;

    /**
     * 原价（市场价）
     * 注意：秒杀价在 SeckillSkuRedisTo 的 seckillPrice 字段中
     */
    private BigDecimal price;

    /**
     * 销量
     */
    private Long saleCount;

}
