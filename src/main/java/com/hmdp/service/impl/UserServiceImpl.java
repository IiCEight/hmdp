package com.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
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

    // Injection by name first
    // however @Autowired injects by type first.
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sendCode(String phone, HttpSession session) {
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("phone wrong");
        }
        String code = RandomUtil.randomNumbers(6);

        // session.setAttribute("code", code);

        // store SM code into redis rather than session
        // and get by phone number
        stringRedisTemplate.opsForValue().set("login:code:" + phone, code, 
                    2,  TimeUnit.MINUTES);

        log.debug("SM code {}",code);

        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        String phone =loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("phone wrong");
        }

        // Object trueCode = session.getAttribute("code");
        // From redis get SM code by phone phone
        String trueCode = stringRedisTemplate.opsForValue().get("login:code:"+phone);
        String code = loginForm.getCode();
        if (trueCode == null || !trueCode.toString().equals(code)) {
            return Result.fail("Wrong code");
        }

        User user = query().eq("phone", phone).one();
        if(user == null) {
            user = createUserWithPhone(phone);
        }

        // session.setAttribute("user", user);

        // save user info into redis rather than session
        // use a token to get it.

        // generate a random string as token
        String token = UUID.randomUUID().toString(true);

        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        // convert object to map
        // Map<String, Object> userMap = BeanUtil.beanToMap(userDTO);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
            CopyOptions.create()
                .setIgnoreNullValue(true)
                .setFieldValueEditor((fieldName, fieldValue) -> fieldValue == null ? null : fieldValue.toString()));


        String key = "login:token:" + token;
        stringRedisTemplate.opsForHash().putAll(key, userMap);

        // Set expiration
        stringRedisTemplate.expire(key, 30, TimeUnit.MINUTES);

        return Result.ok(token);
    } 

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName("user_"+ RandomUtil.randomString(10));

        save(user);
        log.info("User id {}", user.getId());
        return user;
    }

}