package com.xjz.gulimall.order.feign;

import com.xjz.gulimall.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient("service-member")
public interface MemberFeign {
    @RequestMapping("/member/memberreceiveaddress/addressList/{id}")
    public List<MemberAddressVo> addressList(@PathVariable("id") Long id);

}
