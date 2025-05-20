package com.hmdp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Follow;
import com.hmdp.mapper.FollowMapper;
import com.hmdp.service.IFollowService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.UserHolder;

import cn.hutool.core.bean.BeanUtil;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private IUserService userService;

    @Override
    public void follow(Long followUserId, Boolean isFollow) {
        Long userId = UserHolder.getUser().getId();
        
        String key = "follows:" + userId;
        if (isFollow) {
            // follow
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            Boolean isSaved = save(follow);
            // add to redis
            if (isSaved) {
                stringRedisTemplate.opsForSet().add(key, followUserId.toString());
            }
        } else {
            // cancel
            Boolean isRemoved = remove(new QueryWrapper<Follow>()
                .eq("user_id", userId)
                .eq("follow_user_id", followUserId));
            // remove from redis
            if (isRemoved) {
                stringRedisTemplate.opsForSet().remove(key, followUserId.toString());
            }
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

    @Override
    public List<UserDTO> followCommons(Long id) {
        Long userId = UserHolder.getUser().getId();
        String key = "follows:" + userId;
        String key2 = "follows:" + id;
        // log.info("[DEBUG] Redis key1: {}, key2: {}", key, key2);
        Set<String> intersect = stringRedisTemplate.opsForSet().intersect(key, key2);
        // log.info("[DEBUG] Intersect result: {}", intersect);
        if(intersect == null || intersect.isEmpty()) {
            // log.info("[DEBUG] No common follows found.");
            return List.of();
        }
        List<Long> ids = intersect.stream()
            .map(Long::valueOf)
            .toList();
        // log.info("[DEBUG] Common follow IDs: {}", ids);
        List<UserDTO> userDTOs = userService.listByIds(ids)
            .stream()
            .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
            .toList();
        // log.info("[DEBUG] Common follow UserDTOs: {}", userDTOs);
        return userDTOs;
    }

}