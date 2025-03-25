package com.caoyixin.cyxid.core.exception;

/**
 * 时钟回拨异常
 */
public class ClockBackwardsException extends CyxIdException {
    
    private static final long serialVersionUID = 1L;
    
    private final long lastTimestamp;
    private final long currentTimestamp;
    
    public ClockBackwardsException(String message, long lastTimestamp, long currentTimestamp) {
        super(message);
        this.lastTimestamp = lastTimestamp;
        this.currentTimestamp = currentTimestamp;
    }
    
    public long getLastTimestamp() {
        return lastTimestamp;
    }
    
    public long getCurrentTimestamp() {
        return currentTimestamp;
    }
    
    public long getBackwardsDistance() {
        return lastTimestamp - currentTimestamp;
    }
} 