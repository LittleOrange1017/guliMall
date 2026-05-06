package to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
@Data
public class SkuEsModel {
    private Long skuId;
    private Long spuId;
    private String skuTitle;
    private BigDecimal skuPrice; // 注意这里用 BigDecimal 接收，如果 ES 里是 double，序列化也能兼容
    private String skuImg;
    private Long saleCount;
    private Boolean hasStock; // 是否有库存
    private Long hotScore; // 热度评分（默认为0）
    private Long brandId;
    private Long catalogId;
    private String brandName;
    private String brandImg;
    private String catalogName;
    private List<Attrs> attrs; // 可检索属性集合
    @Data
    public static class Attrs {
        private Long attrId;
        private String attrName;
        private String attrValue;
    }
}
