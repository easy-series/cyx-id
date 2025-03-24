package com.example.easyid.core;

/**
 * 安全时钟装饰器，处理时钟回拨问题
 */
public class SafeClock implements Clock {
    
    private final Clock delegateClock;
    private final int spinThreshold;
    private final int brokenThreshold;
    private volatile long lastTimestamp;
    
    /**
     * 创建安全时钟
     *
     * @param delegateClock 被装饰的时钟
     * @param spinThreshold 自旋阈值（毫秒）：当检测到时钟回拨且回拨时间小于该阈值时，通过自旋等待解决
     * @param brokenThreshold 熔断阈值（毫秒）：当检测到时钟回拨且回拨时间大于该阈值时，抛出异常
     */
    public SafeClock(Clock delegateClock, int spinThreshold, int brokenThreshold) {
        this.delegateClock = delegateClock;
        this.spinThreshold = spinThreshold;
        this.brokenThreshold = brokenThreshold;
        this.lastTimestamp = 0;
    }
    
    @Override
    public long currentTimeMillis() {
        long currentTimestamp = delegateClock.currentTimeMillis();
        // 检测时钟回拨
        if (currentTimestamp < lastTimestamp) {
            long backwardsDelta = lastTimestamp - currentTimestamp;
            
            // 如果回拨时间超过熔断阈值，直接抛出异常
            if (backwardsDelta > brokenThreshold) {
                throw new ClockBackwardsException(lastTimestamp, currentTimestamp);
            }
            
            // 如果回拨时间小于自旋阈值，通过自旋等待解决
            if (backwardsDelta <= spinThreshold) {
                while (currentTimestamp <= lastTimestamp) {
                    currentTimestamp = delegateClock.currentTimeMillis();
                }
            } else {
                // 回拨时间在自旋阈值和熔断阈值之间，使用最后的时间戳加1
                currentTimestamp = lastTimestamp + 1;
            }
        }
        
        // 更新最后的时间戳
        lastTimestamp = Math.max(lastTimestamp, currentTimestamp);
        return currentTimestamp;
    }
    
    @Override
    public long getEpoch() {
        return delegateClock.getEpoch();
    }
} 