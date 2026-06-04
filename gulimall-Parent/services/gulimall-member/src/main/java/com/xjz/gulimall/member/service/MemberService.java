package com.xjz.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xjz.gulimall.member.vo.LoginFeginVo;
import com.xjz.gulimall.member.vo.LoginOrRegistFeignVo;
import com.xjz.gulimall.member.vo.RegFeignVo;
import utils.PageUtils;
import com.xjz.gulimall.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-03-12 10:59:14
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(RegFeignVo regFeignVo);

    MemberEntity login(LoginFeginVo vo);

    void loginOrRegist(LoginOrRegistFeignVo vo);
}

