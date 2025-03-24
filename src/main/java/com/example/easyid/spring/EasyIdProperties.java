package com.example.easyid.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Easy-ID框架的配置属性
 */
@ConfigurationProperties(prefix = "easy-id")
public class EasyIdProperties {
    
    /**
     * 应用命名空间，用于隔离不同应用的ID生成器
     */
    private String namespace = "default";
    
    /**
     * 机器号配置
     */
    private final Machine machine = new Machine();
    
    /**
     * 雪花算法配置
     */
    private final Snowflake snowflake = new Snowflake();
    
    /**
     * 号段模式配置
     */
    private final Segment segment = new Segment();
    
    public String getNamespace() {
        return namespace;
    }
    
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    public Machine getMachine() {
        return machine;
    }
    
    public Snowflake getSnowflake() {
        return snowflake;
    }
    
    public Segment getSegment() {
        return segment;
    }
    
    /**
     * 机器号配置
     */
    public static class Machine {
        /**
         * 是否启用机器号分配
         */
        private boolean enabled = true;
        
        /**
         * 机器号位数
         */
        private int machineBit = 10;
        
        /**
         * 实例ID，通常是主机名或容器ID
         */
        private String instanceId = "default";
        
        /**
         * 机器号分配器配置
         */
        private final Distributor distributor = new Distributor();
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public int getMachineBit() {
            return machineBit;
        }
        
        public void setMachineBit(int machineBit) {
            this.machineBit = machineBit;
        }
        
        public String getInstanceId() {
            return instanceId;
        }
        
        public void setInstanceId(String instanceId) {
            this.instanceId = instanceId;
        }
        
        public Distributor getDistributor() {
            return distributor;
        }
        
        /**
         * 机器号分配器配置
         */
        public static class Distributor {
            /**
             * 分配器类型：redis, manual
             */
            private String type = "manual";
            
            /**
             * 手动配置的机器号
             */
            private int machineId = 0;
            
            public String getType() {
                return type;
            }
            
            public void setType(String type) {
                this.type = type;
            }
            
            public int getMachineId() {
                return machineId;
            }
            
            public void setMachineId(int machineId) {
                this.machineId = machineId;
            }
        }
    }
    
    /**
     * 雪花算法配置
     */
    public static class Snowflake {
        /**
         * 是否启用雪花算法
         */
        private boolean enabled = true;
        
        /**
         * 基准时间戳
         */
        private long epoch = 1577808000000L; // 2020-01-01 00:00:00.000
        
        /**
         * 时钟回拨配置
         */
        private final ClockBackwards clockBackwards = new ClockBackwards();
        
        /**
         * 雪花算法提供者配置
         */
        private final Map<String, ProviderConfig> provider = new HashMap<>();
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public long getEpoch() {
            return epoch;
        }
        
        public void setEpoch(long epoch) {
            this.epoch = epoch;
        }
        
        public ClockBackwards getClockBackwards() {
            return clockBackwards;
        }
        
        public Map<String, ProviderConfig> getProvider() {
            return provider;
        }
        
        /**
         * 时钟回拨配置
         */
        public static class ClockBackwards {
            /**
             * 自旋阈值（毫秒）
             */
            private int spinThreshold = 10;
            
            /**
             * 熔断阈值（毫秒）
             */
            private int brokenThreshold = 2000;
            
            public int getSpinThreshold() {
                return spinThreshold;
            }
            
            public void setSpinThreshold(int spinThreshold) {
                this.spinThreshold = spinThreshold;
            }
            
            public int getBrokenThreshold() {
                return brokenThreshold;
            }
            
            public void setBrokenThreshold(int brokenThreshold) {
                this.brokenThreshold = brokenThreshold;
            }
        }
        
        /**
         * 雪花算法提供者配置
         */
        public static class ProviderConfig {
            /**
             * 时间戳位数
             */
            private int timestampBit = 41;
            
            /**
             * 机器号位数
             */
            private int machineBit = 10;
            
            /**
             * 序列号位数
             */
            private int sequenceBit = 12;
            
            public int getTimestampBit() {
                return timestampBit;
            }
            
            public void setTimestampBit(int timestampBit) {
                this.timestampBit = timestampBit;
            }
            
            public int getMachineBit() {
                return machineBit;
            }
            
            public void setMachineBit(int machineBit) {
                this.machineBit = machineBit;
            }
            
            public int getSequenceBit() {
                return sequenceBit;
            }
            
            public void setSequenceBit(int sequenceBit) {
                this.sequenceBit = sequenceBit;
            }
        }
    }
    
    /**
     * 号段模式配置
     */
    public static class Segment {
        /**
         * 是否启用号段模式
         */
        private boolean enabled = true;
        
        /**
         * 号段模式：normal, chain
         */
        private String mode = "normal";
        
        /**
         * 号段分发器配置
         */
        private final Distributor distributor = new Distributor();
        
        /**
         * 链式号段配置
         */
        private final Chain chain = new Chain();
        
        /**
         * 号段提供者配置
         */
        private final Map<String, ProviderConfig> provider = new HashMap<>();
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public String getMode() {
            return mode;
        }
        
        public void setMode(String mode) {
            this.mode = mode;
        }
        
        public Distributor getDistributor() {
            return distributor;
        }
        
        public Chain getChain() {
            return chain;
        }
        
        public Map<String, ProviderConfig> getProvider() {
            return provider;
        }
        
        /**
         * 号段分发器配置
         */
        public static class Distributor {
            /**
             * 分发器类型：redis, jdbc
             */
            private String type = "redis";
            
            public String getType() {
                return type;
            }
            
            public void setType(String type) {
                this.type = type;
            }
        }
        
        /**
         * 链式号段配置
         */
        public static class Chain {
            /**
             * 安全距离比例
             */
            private double safeDistance = 0.7;
            
            /**
             * 预取线程配置
             */
            private final PrefetchWorker prefetchWorker = new PrefetchWorker();
            
            public double getSafeDistance() {
                return safeDistance;
            }
            
            public void setSafeDistance(double safeDistance) {
                this.safeDistance = safeDistance;
            }
            
            public PrefetchWorker getPrefetchWorker() {
                return prefetchWorker;
            }
            
            /**
             * 预取线程配置
             */
            public static class PrefetchWorker {
                /**
                 * 核心线程数
                 */
                private int corePoolSize = 2;
                
                /**
                 * 预取周期
                 */
                private String prefetchPeriod = "1s";
                
                public int getCorePoolSize() {
                    return corePoolSize;
                }
                
                public void setCorePoolSize(int corePoolSize) {
                    this.corePoolSize = corePoolSize;
                }
                
                public String getPrefetchPeriod() {
                    return prefetchPeriod;
                }
                
                public void setPrefetchPeriod(String prefetchPeriod) {
                    this.prefetchPeriod = prefetchPeriod;
                }
            }
        }
        
        /**
         * 号段提供者配置
         */
        public static class ProviderConfig {
            /**
             * 起始偏移量
             */
            private long offset = 0;
            
            /**
             * 号段步长
             */
            private int step = 100;
            
            public long getOffset() {
                return offset;
            }
            
            public void setOffset(long offset) {
                this.offset = offset;
            }
            
            public int getStep() {
                return step;
            }
            
            public void setStep(int step) {
                this.step = step;
            }
        }
    }
} 