package com.xjz.gulimall.member.service.impl;

import com.xjz.gulimall.member.exception.PhoneExistException;
import com.xjz.gulimall.member.exception.UsernameExistException;
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
        BCryptPasswordEncoder passwordEncoder=new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(vo.getPassword());
        member.setPassword(encode);
        Date date=new Date();
        member.setCreateTime(date);
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