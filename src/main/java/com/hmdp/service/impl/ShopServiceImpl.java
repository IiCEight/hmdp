package com.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RedisData;

import cn.hutool.core.util.StrUtil;
import cn.hutool.db.nosql.redis.RedisDS;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Override
    public Shop getById(Serializable id) {
        
        // Cache penetration
        Shop shop = queryWithPassThrough(id);

        // HotSpot key is invalid
        // Shop shop = queryWithMutex(id);

        // Use logic expiration to solve that HotSpot key is invalid
        // Shop shop = queryWithLogicExpiration(id);


        return shop;
    }

    // Use setnx to implement lock in redis.
    // Use a key value to represent a lock
    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        if (flag == null)
            return false;
        return flag;
    }

    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }


    // Use logic expiration
    private void saveShop2Redis(Serializable id, Long expireSeconds) {
        // find in mysql
        Shop shop = super.getById(id);
        RedisData redisData = new RedisData();
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));

        // store into redis
        stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY,
             JSONUtil.toJsonStr(redisData));
    }

    @Override
    @Transactional
    public boolean updateById(Shop entity) {
        // First update mysql
        updateById(entity);

        // Then delete cache in redis
        stringRedisTemplate.delete(RedisConstants.CACHE_SHOP_KEY + entity.getId());
        return true;
    }

    
    public Shop queryWithPassThrough(Serializable id) {
        String key = "cache:shop:" + id;
        log.info("key {}", key);
        String shopJson = stringRedisTemplate.opsForValue().get(key);


        if (StrUtil.isNotBlank(shopJson)) {
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return shop;
        }

        // this means shopJson may be "" or something similar.
        // So it may be our stored "" for data which no exists.
        if (shopJson != null) {
            return null;
        }

        // find in mysql
        Shop shop = super.getById(id);

        log.info("id = {}", id);

        if (shop == null) {
            // Store null into redis to prevent cache penetration problem
            stringRedisTemplate.opsForValue().set(key,"", 2, TimeUnit.MINUTES);

            return null;
        }

        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop)
            ,30,TimeUnit.MINUTES);

        return shop;
    }

    // HotSpot key is invalid
    // Use lock to reconstruct expired HotSpot key.
    public Shop queryWithMutex(Serializable id) {
        String key = "cache:shop:" + id;
        log.info("key {}", key);  
        String shopJson = stringRedisTemplate.opsForValue().get(key);

        if (StrUtil.isNotBlank(shopJson)) {
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return shop;
        }
        if (shopJson != null) {
            // this means shopJson may be "" or something similar.
            return null;
        }

        // Now here is no such key
        String lockKey= "lock:shop:" + id;
        Shop shop = null;
        try {
            if (tryLock(lockKey) == false) {
                // sleep some time then goto begin
                Thread.sleep(50);
                queryWithMutex(id);
            }
            
            // Get the Lock, check if key exists again
            String shopJsonAgain = stringRedisTemplate.opsForValue().get(key);
            
            if (StrUtil.isNotBlank(shopJsonAgain)) {
                shop = JSONUtil.toBean(shopJsonAgain, Shop.class);
                return shop;
            }
            if (shopJsonAgain != null) {
                // this means shopJson may be "" or something similar.
                return null;
            }
            
            // pretended delay 
            Thread.sleep(200);

            // find in mysql
            shop = super.getById(id);
            
            log.info("id = {}", id);
            
            if (shop == null) {
                // Store null into redis to prevent cache penetration problem
                stringRedisTemplate.opsForValue().set(key,"", 2, TimeUnit.MINUTES);
                
                return null;
            }
            
            // Reconstruct key.
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop)
            ,30,TimeUnit.MINUTES);
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Release lock.
            unlock(lockKey);
        }
        return shop;
    }


    private static final ExecutorService CACHE_REBUILD_SERVICE = Executors.newFixedThreadPool(10);

    public Shop queryWithLogicExpiration(Serializable id) {
        String key = "cache:shop:" + id;
        String shopJson = stringRedisTemplate.opsForValue().get(key);

        // If nothing return null
        // Don't search in mysql.
        // This is a big difference.
        if (StrUtil.isBlank(shopJson)) {
            return null;
        }

        // Get data from redis
        RedisData redisData = JSONUtil.toBean(shopJson, RedisData.class);
        Shop shop = JSONUtil.toBean((JSONObject) redisData.getData(), Shop.class);
        LocalDateTime expiration = redisData.getExpireTime();

        // if it is not expired.
        if (expiration.isAfter(LocalDateTime.now())) {
            return shop;
        }

        // If get lock then reconstruct cache, 
        // or return expired data.

        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
        // Get lock successfully.
        if (tryLock(lockKey ) == true) {
            // double check if expired.

            key = "cache:shop:" + id;
            shopJson = stringRedisTemplate.opsForValue().get(key);

            if (StrUtil.isBlank(shopJson)) {
                return null;
            }
            redisData = JSONUtil.toBean(shopJson, RedisData.class);
            shop = JSONUtil.toBean((JSONObject) redisData.getData(), Shop.class);
            expiration = redisData.getExpireTime();

            if (expiration.isAfter(LocalDateTime.now())) {
                return shop;
            }

            // use anthor thread to reconstruct.
            CACHE_REBUILD_SERVICE.submit(()-> {
                try {
                    this.saveShop2Redis(id, 20L);
                } catch(Exception e) {
                    e.getStackTrace();
                } finally {
                    unlock(lockKey);
                }
            });

        }
        // return expired data.
        return shop;
    }
}