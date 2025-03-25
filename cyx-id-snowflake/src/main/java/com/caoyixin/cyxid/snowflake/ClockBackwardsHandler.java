package com.caoyixin.cyxid.snowflake;

/**
 * 时钟回拨处理器接口
 * 用于处理时钟回拨问题
 */
public interface ClockBackwardsHandler {
    
    /**
     * 处理时钟回拨
     *
     * @param lastTimestamp 上次生成ID的时间戳
     * @param currentTimestamp 当前时间戳
     * @return 修正后的时间戳
     */
    long handleBackwards(long lastTimestamp, long currentTimestamp);
} 