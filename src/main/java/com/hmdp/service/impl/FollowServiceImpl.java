package com.hmdp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.entity.Follow;
import com.hmdp.mapper.FollowMapper;
import com.hmdp.service.IFollowService;
import com.hmdp.utils.UserHolder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void follow(Long followUserId, Boolean isFollow) {
        Long userId = UserHolder.getUser().getId();
        
        String key = "follows:" + userId;
        if (isFollow) {
            // follow
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            save(follow);
            // add to redis
            stringRedisTemplate.opsForSet().add(key, followUserId.toString());
        } else {
            // cancel
            remove(new QueryWrapper<Follow>()
                .eq("user_id", userId)
                .eq("follow_user_id", followUserId));
            // remove from redis
            stringRedisTemplate.opsForSet().remove(key, followUserId.toString());
        }
    }

    @Override
    public Boolean isFollow(Long followUserId) {
        Long userId = UserHolder.getUser().getId();
        Long count = query()
            .eq("user_id", userId)
            .eq("follow_user_id", followUserId).count();
        
        return count > 0;
    }

}