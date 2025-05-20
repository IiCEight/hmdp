package com.hmdp.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.dto.Result;
import com.hmdp.dto.ScrollResult;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IBlogService extends IService<Blog> {

    Result queryBlogById(Long id);

    void likeBlog(Long id);

    void isBlogLiked(Blog blog);

    List<UserDTO> queryBlogLikes(Long id);

    Long saveBlog(Blog blog);

    ScrollResult queryBlogOfFollow(Long max, Long offset);

}
