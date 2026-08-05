package to;

import lombok.Data;

import java.util.List;

@Data
public class OrderStockTo {
    private String orderSn;
    private Long orderId;
    private List<SkuStockLockedTo> locks;
}
