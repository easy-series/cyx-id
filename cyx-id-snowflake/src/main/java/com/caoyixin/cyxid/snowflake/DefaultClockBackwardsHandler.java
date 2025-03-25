package com.caoyixin.cyxid.snowflake;

import com.caoyixin.cyxid.core.exception.ClockBackwardsException;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认的时钟回拨处理器
 * 采用自旋等待的方式处理小幅度的时钟回拨，超过阈值则抛出异常
 */
@Slf4j
public class DefaultClockBackwardsHandler implements ClockBackwardsHandler {
    
    /**
     * 自旋等待的时钟回拨阈值（毫秒），超过这个值使用休眠等待
     */
    private final long spinThreshold;
    
    /**
     * 最大允许的时钟回拨阈值（毫秒），超过这个值直接抛异常
     */
    private final long brokenThreshold;
    
    /**
     * 创建默认的时钟回拨处理器
     *
     * @param spinThreshold 自旋等待阈值（毫秒）
     * @param brokenThreshold 最大允许的时钟回拨阈值（毫秒）
     */
    public DefaultClockBackwardsHandler(long spinThreshold, long brokenThreshold) {
        this.spinThreshold = spinThreshold;
        this.brokenThreshold = brokenThreshold;
    }
    
    /**
     * 创建默认的时钟回拨处理器，使用默认阈值
     */
    public DefaultClockBackwardsHandler() {
        // 默认自旋阈值10毫秒
        this.spinThreshold = 10L;
        // 默认最大阈值2秒
        this.brokenThreshold = 2000L;
    }
    
    @Override
    public long handleBackwards(long lastTimestamp, long currentTimestamp) {
        // 计算时钟回拨差值
        long offset = lastTimestamp - currentTimestamp;
        
        if (offset <= 0) {
            // 没有发生时钟回拨，直接返回当前时间戳
            return currentTimestamp;
        }
        
        // 如果回拨时间超过阈值，直接抛出异常
        if (offset > brokenThreshold) {
            log.error("时钟回拨超过阈值：{}ms > {}ms", offset, brokenThreshold);
            throw new ClockBackwardsException("时钟回拨超过阈值", lastTimestamp, currentTimestamp);
        }
        
        // 回拨时间在可接受范围内，等待时钟赶上
        log.warn("检测到时钟回拨：{}ms，等待时钟同步", offset);
        
        // 是否需要自旋等待
        boolean shouldSpin = offset <= spinThreshold;
        
        // 自旋或休眠等待
        long timestamp = waitForClockBackwards(lastTimestamp, shouldSpin);
        
        log.info("时钟已恢复同步，等待时长：{}ms", timestamp - currentTimestamp);
        return timestamp;
    }
    
    /**
     * 等待时钟回拨恢复
     *
     * @param lastTimestamp 上次时间戳
     * @param shouldSpin 是否使用自旋等待
     * @return 恢复后的时间戳
     */
    private long waitForClockBackwards(long lastTimestamp, boolean shouldSpin) {
        long timestamp;
        if (shouldSpin) {
            // 自旋等待
            timestamp = spinWait(lastTimestamp);
        } else {
            // 休眠等待
            timestamp = sleepWait(lastTimestamp);
        }
        return timestamp;
    }
    
    /**
     * 自旋等待
     *
     * @param lastTimestamp 上次时间戳
     * @return 恢复后的时间戳
     */
    private long spinWait(long lastTimestamp) {
        long timestamp;
        do {
            timestamp = System.currentTimeMillis();
        } while (timestamp <= lastTimestamp);
        return timestamp;
    }
    
    /**
     * 休眠等待
     *
     * @param lastTimestamp 上次时间戳
     * @return 恢复后的时间戳
     */
    private long sleepWait(long lastTimestamp) {
        long timestamp;
        do {
            try {
                long offset = lastTimestamp - System.currentTimeMillis();
                if (offset > 0) {
                    // 休眠等待
                    Thread.sleep(offset);
                }
            } catch (InterruptedException e) {
                log.warn("等待时钟回拨时被中断", e);
                Thread.currentThread().interrupt();
            }
            timestamp = System.currentTimeMillis();
        } while (timestamp <= lastTimestamp);
        return timestamp;
    }
} 