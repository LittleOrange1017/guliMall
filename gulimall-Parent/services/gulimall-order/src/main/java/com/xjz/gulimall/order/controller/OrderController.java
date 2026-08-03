package com.xjz.gulimall.order.controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xjz.gulimall.order.entity.OrderEntity;
import com.xjz.gulimall.order.service.OrderService;
import utils.PageUtils;
import utils.R;


/**
 * 订单
 *
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-03-12 11:09:40
 */
@RestController
@RequestMapping("order/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("order:order:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = orderService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("order:order:info")
    public R info(@PathVariable("id") Long id){
		OrderEntity order = orderService.getById(id);

        return R.ok().put("order", order);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("order:order:save")
    public R save(@RequestBody OrderEntity order){
		orderService.save(order);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("order:order:update")
    public R update(@RequestBody OrderEntity order){
		orderService.updateById(order);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("order:order:delete")
    public R delete(@RequestBody Long[] ids){
		orderService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    /**
     * 运费查询：根据收货地址ID计算当前订单的运费金额
     * 完整路径：GET /order/order/freight?addrId=xxx
     * TODO 后续接入真实业务：调用 ware 服务，根据商品总重量与收货地址距离计算运费，当前返回模拟固定值
     */
    @GetMapping("/freight")
    public R freight(@RequestParam("addrId") Long addrId){
        // 模拟运费金额，待后续替换为真实运费计算逻辑
        BigDecimal freightAmount=new BigDecimal(0.00);
        if(addrId.equals(3L))
        {
           freightAmount=new BigDecimal("10.00");
        }
        else if(addrId.equals(4L))
        {
            freightAmount=new BigDecimal("15.00");
        }

        return R.ok().put("freightAmount", freightAmount);
    }

}
