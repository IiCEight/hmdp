package com.hmdp.interceptor;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.UserHolder;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RefreshInterceptor implements HandlerInterceptor {
    
    private StringRedisTemplate stringRedisTemplate;

    public RefreshInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    // premit everything, it just refresh redis expiration
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) throws Exception {
        log.info("Refresh Intercepor ......");
        log.info("Url {}", request.getRequestURI());

        // HttpSession session = request.getSession();
        // Get token
        String token = request.getHeader("authorization");
        
        log.info("token {}", token);
        
        if (StrUtil.isBlank(token)) {
            return true;
        }

        // Get user info by token from redis
        String key = RedisConstants.LOGIN_USER_KEY + token;
        
        Map <Object, Object> userMap = stringRedisTemplate.opsForHash().entries(key);
        log.info("userMap {}", userMap);
        if (userMap.isEmpty()) {
            return true;
        }

        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);

        UserHolder.saveUser(userDTO);

        stringRedisTemplate.expire(key, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);

        return true;
    }
}
