package com.example.easyid.core;

/**
 * 系统时钟实现
 * 使用系统时间作为时间源
 */
public class SystemClock implements Clock {
    /**
     * 默认的基准时间（2020-01-01 00:00:00.000）
     */
    public static final long DEFAULT_EPOCH = 1577808000000L;
    
    private final long epoch;
    
    /**
     * 使用默认基准时间创建系统时钟
     */
    public SystemClock() {
        this(DEFAULT_EPOCH);
    }
    
    /**
     * 指定基准时间创建系统时钟
     *
     * @param epoch 基准时间
     */
    public SystemClock(long epoch) {
        this.epoch = epoch;
    }
    
    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }
    
    @Override
    public long getEpoch() {
        return epoch;
    }
} 