package com.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.UserHolder;

import java.time.LocalDateTime;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
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
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Autowired
    private ISeckillVoucherService seckillVoucherService;

    @Autowired
    private RedisIdWorker redisIdWorker;

    @Autowired
    private RedissonClient redissonClient;

    // @Override
    // public Long seckillVoucher(Long voucherId) {
    //     SeckillVoucher voucher = seckillVoucherService.getById(voucherId);

    //     if(voucher.getBeginTime().isAfter(LocalDateTime.now())) {
    //         return null;
    //     }
    //     if(voucher.getEndTime().isBefore(LocalDateTime.now())) {
    //         return null;
    //     }
    //     if(voucher.getStock() < 1) {
    //         return null;
    //     }

    //     // Pessimistic Lock
    //     // There is a problem that transacitonal
    //     // will fail.
    //     // See episode 55
    //     Long userId = UserHolder.getUser().getId();
    //     synchronized(userId.toString().intern()) {
    //         return createVoucherOrder(voucherId);
    //     }
    // }

    @Transactional
    public synchronized Long createVoucherOrder(Long voucherId) {
        // Only one order for one user
        Long userId = UserHolder.getUser().getId();
        Long count = query().eq("user_id", userId).eq("voucher_id", voucherId)
                            .count();
        // prohibit.
        if(count > 0) {
            return null;
        }

        // optimistic locking
        boolean success = seckillVoucherService.update()
                        .setSql("stock = stock - 1")
                        .eq("voucher_id", voucherId)
                        .gt("stock", 0)
                        .update();
        if (success == false) {
            return null;
        }
        VoucherOrder voucherOrder = new VoucherOrder();
        long orderId = redisIdWorker.nextId("order");
        voucherOrder.setId(orderId);

        // Long userId = UserHolder.getUser().getId();
        voucherOrder.setUserId(userId);

        voucherOrder.setVoucherId(voucherId);

        // store in mysql
        save(voucherOrder);

        return orderId;
    }


    public Long seckillVoucher(Long voucherId) {
        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);

        if(voucher.getBeginTime().isAfter(LocalDateTime.now())) {
            return null;
        }
        if(voucher.getEndTime().isBefore(LocalDateTime.now())) {
            return null;
        }
        if(voucher.getStock() < 1) {
            return null;
        }

        Long userId = UserHolder.getUser().getId();

        // Use redis as lock
        RLock lock = redissonClient.getLock("lock:order:" + userId);

        boolean isLockAcquired = lock.tryLock();

        if (isLockAcquired == false) {
            return null;
        }

        try {

            // Since transactional will be failed.
            // Use this to fix it.
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder(voucherId);
        } finally {
            lock.unlock();
        }
    }
}
