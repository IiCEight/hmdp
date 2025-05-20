package com.hmdp.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Follow;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IFollowService extends IService<Follow> {

    void follow(Long followUserId, Boolean isFollow);

    Boolean isFollow(Long followUserId);

    List<UserDTO> followCommons(Long id);
}
