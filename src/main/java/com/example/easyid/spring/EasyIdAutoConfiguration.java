package com.example.easyid.spring;

import com.example.easyid.core.Clock;
import com.example.easyid.core.SafeClock;
import com.example.easyid.core.SystemClock;
import com.example.easyid.machine.MachineIdDistributor;
import com.example.easyid.machine.ManualMachineIdDistributor;
import com.example.easyid.machine.RedisMachineIdDistributor;
import com.example.easyid.segment.DefaultSegmentId;
import com.example.easyid.segment.RedisSegmentIdDistributor;
import com.example.easyid.segment.SegmentChainId;
import com.example.easyid.segment.SegmentId;
import com.example.easyid.segment.SegmentIdDistributor;
import com.example.easyid.snowflake.DefaultSnowflakeId;
import com.example.easyid.snowflake.SnowflakeId;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Easy-ID自动配置类
 */
@AutoConfiguration
@EnableConfigurationProperties(EasyIdProperties.class)
public class EasyIdAutoConfiguration {
    
    /**
     * 机器号配置
     */
    @Configuration
    @ConditionalOnProperty(prefix = "easy-id.machine", name = "enabled", havingValue = "true", matchIfMissing = true)
    public static class MachineIdConfiguration {
        
        @Bean
        @ConditionalOnMissingBean
        public String instanceId() {
            try {
                return InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                return "unknown-" + System.currentTimeMillis();
            }
        }
        
        @Bean
        @ConditionalOnMissingBean
        public MachineIdDistributor machineIdDistributor(
                EasyIdProperties properties,
                StringRedisTemplate redisTemplate,
                String instanceId) {
            
            String type = properties.getMachine().getDistributor().getType();
            String namespace = properties.getNamespace();
            
            if ("redis".equalsIgnoreCase(type)) {
                return new RedisMachineIdDistributor(redisTemplate, namespace, instanceId);
            } else {
                // 默认使用手动配置
                int machineId = properties.getMachine().getDistributor().getMachineId();
                return new ManualMachineIdDistributor(namespace, machineId);
            }
        }
    }
    
    /**
     * 时钟配置
     */
    @Configuration
    public static class ClockConfiguration {
        
        @Bean
        @ConditionalOnMissingBean
        public Clock systemClock(EasyIdProperties properties) {
            return new SystemClock(properties.getSnowflake().getEpoch());
        }
        
        @Bean
        @ConditionalOnMissingBean
        public Clock safeClock(Clock systemClock, EasyIdProperties properties) {
            int spinThreshold = properties.getSnowflake().getClockBackwards().getSpinThreshold();
            int brokenThreshold = properties.getSnowflake().getClockBackwards().getBrokenThreshold();
            return new SafeClock(systemClock, spinThreshold, brokenThreshold);
        }
    }
    
    /**
     * 雪花算法配置
     */
    @Configuration
    @ConditionalOnProperty(prefix = "easy-id.snowflake", name = "enabled", havingValue = "true", matchIfMissing = true)
    public static class SnowflakeIdConfiguration {
        
        @Bean
        @ConditionalOnMissingBean(name = "snowflakeId")
        public SnowflakeId snowflakeId(
                Clock safeClock, 
                MachineIdDistributor machineIdDistributor, 
                EasyIdProperties properties) {
            
            int machineBit = properties.getMachine().getMachineBit();
            int maxMachineId = 1 << machineBit;
            int machineId = machineIdDistributor.distribute(maxMachineId);
            
            return new DefaultSnowflakeId(
                    "snowflake", 
                    safeClock, 
                    machineId, 
                    41, // 默认时间戳位数
                    machineBit,
                    12); // 默认序列号位数
        }
        
        @Bean
        @ConditionalOnMissingBean
        public Map<String, SnowflakeId> snowflakeIdMap(
                Clock safeClock,
                MachineIdDistributor machineIdDistributor,
                EasyIdProperties properties) {
            
            Map<String, SnowflakeId> idMap = new HashMap<>();
            Map<String, EasyIdProperties.Snowflake.ProviderConfig> providers = 
                    properties.getSnowflake().getProvider();
            
            // 为每个配置的提供者创建雪花算法ID生成器
            for (Map.Entry<String, EasyIdProperties.Snowflake.ProviderConfig> entry : providers.entrySet()) {
                String name = entry.getKey();
                EasyIdProperties.Snowflake.ProviderConfig config = entry.getValue();
                
                int machineBit = config.getMachineBit();
                int maxMachineId = 1 << machineBit;
                int machineId = machineIdDistributor.distribute(maxMachineId);
                
                SnowflakeId snowflakeId = new DefaultSnowflakeId(
                        name,
                        safeClock,
                        machineId,
                        config.getTimestampBit(),
                        machineBit,
                        config.getSequenceBit());
                
                idMap.put(name, snowflakeId);
            }
            
            return idMap;
        }
    }
    
    /**
     * 号段模式配置
     */
    @Configuration
    @ConditionalOnProperty(prefix = "easy-id.segment", name = "enabled", havingValue = "true", matchIfMissing = true)
    public static class SegmentIdConfiguration {
        
        @Bean
        @ConditionalOnClass(StringRedisTemplate.class)
        @ConditionalOnBean(StringRedisTemplate.class)
        @ConditionalOnMissingBean
        public Map<String, SegmentIdDistributor> segmentIdDistributorMap(
                StringRedisTemplate redisTemplate,
                EasyIdProperties properties) {
            
            Map<String, SegmentIdDistributor> distributorMap = new HashMap<>();
            Map<String, EasyIdProperties.Segment.ProviderConfig> providers = 
                    properties.getSegment().getProvider();
            
            // 为每个配置的提供者创建号段分发器
            for (Map.Entry<String, EasyIdProperties.Segment.ProviderConfig> entry : providers.entrySet()) {
                String name = entry.getKey();
                EasyIdProperties.Segment.ProviderConfig config = entry.getValue();
                
                SegmentIdDistributor distributor = new RedisSegmentIdDistributor(
                        redisTemplate,
                        name,
                        config.getOffset(),
                        config.getStep());
                
                distributorMap.put(name, distributor);
            }
            
            return distributorMap;
        }
        
        @Bean
        @ConditionalOnMissingBean
        public ScheduledExecutorService prefetchExecutor(EasyIdProperties properties) {
            int corePoolSize = properties.getSegment().getChain().getPrefetchWorker().getCorePoolSize();
            return Executors.newScheduledThreadPool(corePoolSize);
        }
        
        @Bean
        @ConditionalOnMissingBean
        public Map<String, SegmentId> segmentIdMap(
                Map<String, SegmentIdDistributor> segmentIdDistributorMap,
                ScheduledExecutorService prefetchExecutor,
                EasyIdProperties properties) {
            
            Map<String, SegmentId> idMap = new HashMap<>();
            String mode = properties.getSegment().getMode();
            double safeDistance = properties.getSegment().getChain().getSafeDistance();
            
            // 为每个分发器创建号段ID生成器
            for (Map.Entry<String, SegmentIdDistributor> entry : segmentIdDistributorMap.entrySet()) {
                String name = entry.getKey();
                SegmentIdDistributor distributor = entry.getValue();
                
                SegmentId segmentId;
                if ("chain".equalsIgnoreCase(mode)) {
                    // 链式号段模式
                    segmentId = new SegmentChainId(distributor, safeDistance, prefetchExecutor);
                } else {
                    // 普通号段模式
                    segmentId = new DefaultSegmentId(distributor);
                }
                
                idMap.put(name, segmentId);
            }
            
            return idMap;
        }
    }
} 