package com.example.easyid.core;

/**
 * 时钟回拨异常
 */
public class ClockBackwardsException extends RuntimeException {
    
    private final long lastTimestamp;
    private final long currentTimestamp;
    
    public ClockBackwardsException(long lastTimestamp, long currentTimestamp) {
        super("Clock moved backwards. Last timestamp: " + lastTimestamp + ", current timestamp: " + currentTimestamp);
        this.lastTimestamp = lastTimestamp;
        this.currentTimestamp = currentTimestamp;
    }
    
    public long getLastTimestamp() {
        return lastTimestamp;
    }
    
    public long getCurrentTimestamp() {
        return currentTimestamp;
    }
    
    /**
     * 获取时间差值（回拨了多少时间）
     *
     * @return 时间差值
     */
    public long getBackwardsDelta() {
        return lastTimestamp - currentTimestamp;
    }
} 