package com.hmdp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.hmdp.interceptor.LoginInterceptor;
import com.hmdp.interceptor.RefreshInterceptor;

@Configuration
public class MvcConfiguration implements WebMvcConfigurer{
    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Note: Order is important if .order is not setted.
        // the smaller Order number has higher priority.

        registry.addInterceptor(new RefreshInterceptor(stringRedisTemplate))
                .addPathPatterns("/**")        
                .order(0);

        registry.addInterceptor(new LoginInterceptor())
                .excludePathPatterns("/user/code", "/user/login",
                    "/blog/hot", "/shop/**", "/shop-type/**", "/upload/**",
                    "/voucher/**").order(1);
    }
}
