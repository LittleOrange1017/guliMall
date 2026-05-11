package com.xjz.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Catelog2Vo {
    private String catelog1Id;//1级父分类Id
    private List<Catelog3Vo> catelog3VoList;
    private String id;
    private String name;
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Catelog3Vo{
        private String catelog2Id;//2级父分类Id
        private String id;
        private String name;
    }
}
