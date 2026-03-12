package com.xjz.gulimall.product;

import com.xjz.gulimall.product.entity.BrandEntity;
import com.xjz.gulimall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * ClassName: CrudTest
 * Package:com.xjz.gulimall.product
 * Description:
 *
 * @Author 小橘子神灯
 * @Create 2026/3/12 9:56
 * @Version 1.0
 */
@SpringBootTest
public class CrudTest {
    @Autowired
    BrandService brandService;
    @Test
    public void test(){
        boolean b = brandService.removeById(6);
        if(b)
        {
            System.out.println("删除成功");
        }
        else
        {
            System.out.println("删除失败");
        }
    }
}
