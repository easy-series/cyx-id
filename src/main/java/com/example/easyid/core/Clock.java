package com.example.easyid.core;

/**
 * 时钟接口，用于获取时间戳
 */
public interface Clock {
    
    /**
     * 获取当前时间戳
     *
     * @return 时间戳
     */
    long currentTimeMillis();
    
    /**
     * 获取基准时间
     *
     * @return 基准时间
     */
    long getEpoch();
    
    /**
     * 获取当前时间相对于基准时间的偏移量
     *
     * @return 时间偏移量
     */
    default long getTimestamp() {
        return currentTimeMillis() - getEpoch();
    }
} 