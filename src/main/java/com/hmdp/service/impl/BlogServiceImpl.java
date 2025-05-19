package com.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
import com.hmdp.entity.User;
import com.hmdp.mapper.BlogMapper;
import com.hmdp.service.IBlogService;
import com.hmdp.service.IUserInfoService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.UserHolder;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    @Autowired
    private IUserService userService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryBlogById(Long id) {
        Blog blog = getById(id);
        if (blog == null) {
            return null;
        }
        queryBlogUser(blog);
        return Result.ok(blog);
    }

    private void queryBlogUser(Blog blog) {
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
        isBlogLiked(blog);
    }

    @Override
    public void likeBlog(Long blogId) {
        Long userId = UserHolder.getUser().getId();

        String key = "blog:like:"+ blogId;
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        if(score == null) {
            // This means this use didn't like such blog yet.
            // Thus he/she can like it.
            Boolean isSuccessful = update().setSql("liked = liked + 1")
                            .eq("id", blogId).update();
            if (isSuccessful) {
                // store into redis
                // stringRedisTemplate.opsForSet().add(key, userId.toString());

                // Use sorted-set for sort.
                // Note score need to be set.
                stringRedisTemplate.opsForZSet().add(key, userId.toString(), 
                    System.currentTimeMillis());
            }

        } else {
            // Otherwise cancle liked.
            Boolean isSuccessful = update().setSql("liked = liked - 1")
                            .eq("id", blogId).update();
            if (isSuccessful) {
                stringRedisTemplate.opsForZSet().remove(key, userId.toString());
            }
        }
    }

    public void isBlogLiked(Blog blog) {
        Long userId = UserHolder.getUser().getId();
        if (userId == null) 
            return;
        String key = "blog:like:"+ blog.getId();
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        blog.setIsLike(score != null);
    }

    @Override
    public List<UserDTO> queryBlogLikes(Long id) {
        String key = "blog:like:" + id;

        // from lower to higher
        Set<String> top5 = stringRedisTemplate.opsForZSet().range(key, 0, 4);
        if (top5 == null || top5.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> ids = top5.stream().map(Long::valueOf).toList();
        String idString = StrUtil.join(",", ids);

        // We need set the correct order when querying mysql.
        return userService.query().in("id", ids)
            .last("order by field (id, "+ idString +  ")").list()
            .stream()
            .map(user ->BeanUtil.copyProperties(user, UserDTO.class)).toList();
    }

}
