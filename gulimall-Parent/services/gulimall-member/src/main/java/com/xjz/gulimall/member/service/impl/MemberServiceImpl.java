package com.xjz.gulimall.member.service.impl;

import com.xjz.gulimall.member.exception.*;
import com.xjz.gulimall.member.vo.LoginFeginVo;
import com.xjz.gulimall.member.vo.LoginOrRegistFeignVo;
import com.xjz.gulimall.member.vo.RegFeignVo;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import utils.PageUtils;
import utils.Query;


import com.xjz.gulimall.member.dao.MemberDao;
import com.xjz.gulimall.member.entity.MemberEntity;
import com.xjz.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(RegFeignVo vo) {
        MemberEntity member=new MemberEntity();
        checkPhoneUnique(vo.getPhone());
        checkUsernameUnique(vo.getUserName());
        member.setUsername(vo.getUserName());
        member.setMobile(vo.getPhone());
        member.setNickname(vo.getUserName());
        member.setLevelId(1L);
        member.setHeader("https://gulimall-bylittleorange.oss-cn-beijing.aliyuncs.com/2026-05-15/nanjing-university-of-posts-and-telecommunications-logo-1024px.png");
        BCryptPasswordEncoder passwordEncoder=new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(vo.getPassword());
        String[] parts = encode.split("\\$");
        String salt = parts[3].substring(0, 22);
        member.setSalt(salt);
        member.setPassword(encode);
        member.setIntegration(0);
        Date date=new Date();
        member.setCreateTime(date);
        this.save(member);
    }

    @Override
    public MemberEntity login(LoginFeginVo vo) {
        String password = vo.getPassword();
        String loginacct = vo.getLoginacct();
        MemberEntity entity = this.getOne(new QueryWrapper<MemberEntity>()
                .eq("username", loginacct)
                .or().eq("mobile", loginacct)
                .or().eq("email", loginacct));
        if(entity==null)
        {
            throw new UsernameISNOTExistException();
        }
        String entityPassword = entity.getPassword();
        BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
        boolean matches = encoder.matches(password, entityPassword);
        if(!matches)
        {
            throw new PassWordIsNotCorrectException();
        }
        return entity;
    }

    @Override
    public void loginOrRegist(LoginOrRegistFeignVo vo) {
        Long socialUid = vo.getId();
        QueryWrapper<MemberEntity> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("social_uid",socialUid);
        long count = this.count(queryWrapper);
        if(count>0)
        {
            //说明这个社交用户已经绑定过现有的商城用户了,直接返回登录页
            throw new SocialUserISExistException();
        }
        MemberEntity member=new MemberEntity();
        member.setCreateTime(new Date());
        member.setUsername(vo.getLogin());
        member.setNickname(vo.getName());
        member.setHeader(vo.getAvatar_url());
        member.setSocialUid(String.valueOf(vo.getId()));
        member.setLevelId(1L);
        member.setIntegration(0);
        member.setGrowth(0);
        this.save(member);
    }

    private void checkUsernameUnique(String userName) {
        QueryWrapper<MemberEntity> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("username",userName);
        long count = this.count(queryWrapper);
        if(count>0)
        {
            throw new UsernameExistException();
        }
    }

    private void checkPhoneUnique(String phone) {
        QueryWrapper<MemberEntity> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("mobile",phone);
        long count = this.count(queryWrapper);
        if(count>0)
        {
            throw new PhoneExistException();
        }
    }

}