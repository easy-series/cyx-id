package com.caoyixin.cyxid.spring.boot.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * CyxId配置属性
 */
@Data
@ConfigurationProperties(prefix = "cyx-id")
public class CyxIdProperties {
    
    /**
     * 是否启用
     */
    private boolean enabled = true;
    
    /**
     * 提供者类型 (snowflake, segment, segment-chain)
     */
    private String type = "snowflake";
    
    /**
     * 雪花算法配置
     */
    private final SnowflakeProperties snowflake = new SnowflakeProperties();
    
    /**
     * 分段ID配置
     */
    private final SegmentProperties segment = new SegmentProperties();
    
    /**
     * 链式分段ID配置
     */
    private final SegmentChainProperties segmentChain = new SegmentChainProperties();
    
    /**
     * Redis配置
     */
    private final RedisProperties redis = new RedisProperties();
    
    /**
     * 雪花算法配置属性
     */
    @Data
    public static class SnowflakeProperties {
        
        /**
         * 是否启用
         */
        private boolean enabled = true;
        
        /**
         * 基准时间戳（2024-01-01 00:00:00）
         */
        private long epoch = 1704038400000L;
        
        /**
         * 工作节点ID配置
         */
        private final WorkerProperties worker = new WorkerProperties();
        
        /**
         * 序列号配置
         */
        private final SequenceProperties sequence = new SequenceProperties();
        
        /**
         * 时钟回拨配置
         */
        private final ClockBackwardsProperties clockBackwards = new ClockBackwardsProperties();
        
        /**
         * 工作节点ID配置属性
         */
        @Data
        public static class WorkerProperties {
            
            /**
             * 分配器类型 (static, redis)
             */
            private String allocator = "static";
            
            /**
             * 静态工作节点ID
             */
            private long id = 0;
            
            /**
             * 工作节点ID位数
             */
            private int bits = 10;
        }
        
        /**
         * 序列号配置属性
         */
        @Data
        public static class SequenceProperties {
            
            /**
             * 序列号位数
             */
            private int bits = 12;
        }
        
        /**
         * 时钟回拨配置属性
         */
        @Data
        public static class ClockBackwardsProperties {
            
            /**
             * 自旋等待阈值（毫秒）
             */
            private long spinThreshold = 10;
            
            /**
             * 最大允许的时钟回拨阈值（毫秒）
             */
            private long brokenThreshold = 2000;
        }
    }
    
    /**
     * 分段ID配置属性
     */
    @Data
    public static class SegmentProperties {
        
        /**
         * 是否启用
         */
        private boolean enabled = false;
        
        /**
         * 分配器类型 (redis)
         */
        private String allocator = "redis";
        
        /**
         * 最大步长
         */
        private int step = 1000;
        
        /**
         * 安全距离百分比（1-100）
         */
        private int safeDistancePercent = 50;
    }
    
    /**
     * 链式分段ID配置属性
     */
    @Data
    public static class SegmentChainProperties {
        
        /**
         * 是否启用
         */
        private boolean enabled = false;
        
        /**
         * 分配器类型 (redis)
         */
        private String allocator = "redis";
        
        /**
         * 最大步长
         */
        private int step = 1000;
        
        /**
         * 安全距离百分比（1-100）
         */
        private int safeDistancePercent = 20;
        
        /**
         * 最大链长
         */
        private int maxChainLength = 3;
        
        /**
         * 预取周期（毫秒）
         */
        private long prefetchPeriod = 1000;
    }
    
    /**
     * Redis配置属性
     */
    @Data
    public static class RedisProperties {
        
        /**
         * Redis主机地址
         */
        private String host = "localhost";
        
        /**
         * Redis端口
         */
        private int port = 6379;
        
        /**
         * Redis密码
         */
        private String password;
        
        /**
         * Redis数据库索引
         */
        private int database = 0;
        
        /**
         * 连接超时时间（毫秒）
         */
        private int timeout = 2000;
        
        /**
         * 最大连接数
         */
        private int maxTotal = 20;
        
        /**
         * 最大空闲连接数
         */
        private int maxIdle = 10;
        
        /**
         * 最小空闲连接数
         */
        private int minIdle = 2;
        
        /**
         * 各业务键前缀
         */
        private final KeyPrefixProperties keyPrefix = new KeyPrefixProperties();
        
        /**
         * 工作节点ID配置
         */
        private final WorkerIdProperties workerId = new WorkerIdProperties();
        
        /**
         * 键前缀配置属性
         */
        @Data
        public static class KeyPrefixProperties {
            
            /**
             * 分段ID键前缀
             */
            private String segment = "cyx-id:segment:";
            
            /**
             * 工作节点ID键前缀
             */
            private String workerId = "cyx-id:worker:";
            
            /**
             * 机器状态键前缀
             */
            private String machineState = "cyx-id:machine:";
        }
        
        /**
         * 工作节点ID配置属性
         */
        @Data
        public static class WorkerIdProperties {
            
            /**
             * 工作节点ID最大值
             */
            private int maxId = 1023;
            
            /**
             * 过期时间（秒）
             */
            private int expireSeconds = 60;
        }
    }
} 