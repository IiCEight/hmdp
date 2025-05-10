package com.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;

import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public Result sendCode(String phone, HttpSession session) {
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("phone wrong");
        }
        String code = RandomUtil.randomNumbers(6);

        session.setAttribute("code", code);

        log.debug("SM code {}",code);

        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        String phone =loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("phone wrong");
        }

        Object trueCode = session.getAttribute("code");
        String code = loginForm.getCode();
        if (trueCode == null || !trueCode.toString().equals(code)) {
            return Result.fail("Wrong code");
        }

        User user = query().eq("phone", phone).one();
        if(user == null) {
            createUserWithPhone(phone);
        }

        session.setAttribute("user", user);

        return Result.ok();
    } 

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName("user_"+ RandomUtil.randomString(10));

        return user;
    }

}
