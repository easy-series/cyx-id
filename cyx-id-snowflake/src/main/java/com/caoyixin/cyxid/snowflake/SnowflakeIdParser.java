package com.caoyixin.cyxid.snowflake;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 雪花算法ID解析器
 * 用于解析雪花算法生成的ID，提取其中的时间戳、工作节点ID和序列号
 */
public class SnowflakeIdParser {
    
    // 默认时间戳位移
    private static final int DEFAULT_TIMESTAMP_SHIFT = 22;
    
    // 默认工作节点ID位移
    private static final int DEFAULT_WORKER_ID_SHIFT = 12;
    
    // 默认序列号掩码
    private static final int DEFAULT_SEQUENCE_MASK = 0xFFF;
    
    // 默认工作节点ID掩码
    private static final int DEFAULT_WORKER_ID_MASK = 0x3FF;
    
    // 默认基准时间戳（2024-01-01 00:00:00）
    private static final long DEFAULT_EPOCH_TIMESTAMP = 1704038400000L;
    
    // 日期格式化器
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    /**
     * 解析雪花算法ID
     *
     * @param id 雪花算法ID
     * @return 解析结果
     */
    public static SnowflakeIdInfo parse(long id) {
        return parse(id, DEFAULT_EPOCH_TIMESTAMP, DEFAULT_TIMESTAMP_SHIFT, 
                DEFAULT_WORKER_ID_SHIFT, DEFAULT_WORKER_ID_MASK, DEFAULT_SEQUENCE_MASK);
    }
    
    /**
     * 解析雪花算法ID
     *
     * @param id 雪花算法ID
     * @param epochTimestamp 基准时间戳
     * @param timestampShift 时间戳位移
     * @param workerIdShift 工作节点ID位移
     * @param workerIdMask 工作节点ID掩码
     * @param sequenceMask 序列号掩码
     * @return 解析结果
     */
    public static SnowflakeIdInfo parse(long id, long epochTimestamp, int timestampShift, 
                                       int workerIdShift, int workerIdMask, int sequenceMask) {
        // 提取时间戳部分
        long timestamp = (id >> timestampShift) + epochTimestamp;
        
        // 提取工作节点ID部分
        long workerId = (id >> workerIdShift) & workerIdMask;
        
        // 提取序列号部分
        long sequence = id & sequenceMask;
        
        // 转换时间戳为可读格式
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        String formattedTime = dateTime.format(DATE_FORMATTER);
        
        return SnowflakeIdInfo.builder()
                .id(id)
                .timestamp(timestamp)
                .workerId(workerId)
                .sequence(sequence)
                .formattedTime(formattedTime)
                .build();
    }
    
    /**
     * 雪花算法ID解析结果
     */
    @Data
    @Builder
    public static class SnowflakeIdInfo {
        // 原始ID
        private long id;
        
        // 时间戳
        private long timestamp;
        
        // 工作节点ID
        private long workerId;
        
        // 序列号
        private long sequence;
        
        // 格式化后的时间
        private String formattedTime;
        
        @Override
        public String toString() {
            return String.format("ID: %d%n时间戳: %d%n工作节点ID: %d%n序列号: %d%n生成时间: %s", 
                    id, timestamp, workerId, sequence, formattedTime);
        }
    }
} 