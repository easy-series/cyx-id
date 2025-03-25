package com.caoyixin.cyxid.spring.boot.config;

import com.caoyixin.cyxid.spring.boot.properties.CyxIdProperties;
import com.caoyixin.cyxid.storage.redis.RedisMachineStateStorage;
import com.caoyixin.cyxid.storage.redis.RedisSegmentAllocator;
import com.caoyixin.cyxid.storage.redis.RedisWorkerIdAllocator;
import com.caoyixin.cyxid.storage.redis.RedisConnectionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Redis自动配置类
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(CyxIdProperties.class)
@ConditionalOnProperty(prefix = "cyx-id", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CyxIdRedisAutoConfiguration {
    
    /**
     * 创建Redis连接管理器
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisConnectionManager redisConnectionManager(CyxIdProperties properties) {
        CyxIdProperties.RedisProperties redis = properties.getRedis();
        log.info("创建Redis连接管理器：{}:{}/{}", redis.getHost(), redis.getPort(), redis.getDatabase());
        
        return new RedisConnectionManager(
                redis.getHost(),
                redis.getPort(),
                redis.getPassword(),
                redis.getDatabase(),
                redis.getTimeout(),
                redis.getMaxTotal(),
                redis.getMaxIdle(),
                redis.getMinIdle()
        );
    }
    
    /**
     * 创建Redis段分配器
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisSegmentAllocator redisSegmentAllocator(RedisConnectionManager redisConnectionManager, 
                                                      CyxIdProperties properties) {
        CyxIdProperties.RedisProperties redis = properties.getRedis();
        int step;
        
        // 根据配置的类型选择步长
        if ("segment-chain".equals(properties.getType()) && properties.getSegmentChain().isEnabled()) {
            step = properties.getSegmentChain().getStep();
        } else {
            step = properties.getSegment().getStep();
        }
        
        log.info("创建Redis段分配器：步长={}, 键前缀={}", step, redis.getKeyPrefix().getSegment());
        
        return new RedisSegmentAllocator(
                redisConnectionManager,
                step,
                redis.getKeyPrefix().getSegment()
        );
    }
    
    /**
     * 创建Redis工作节点ID分配器
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "cyx-id.snowflake.worker", name = "allocator", havingValue = "redis")
    public RedisWorkerIdAllocator redisWorkerIdAllocator(RedisConnectionManager redisConnectionManager, 
                                                        CyxIdProperties properties) {
        CyxIdProperties.RedisProperties redis = properties.getRedis();
        CyxIdProperties.RedisProperties.WorkerIdProperties workerId = redis.getWorkerId();
        String keyPrefix = redis.getKeyPrefix().getWorkerId();
        
        log.info("创建Redis工作节点ID分配器：最大值={}, 过期时间={}秒, 键前缀={}", 
                workerId.getMaxId(), workerId.getExpireSeconds(), keyPrefix);
        
        return new RedisWorkerIdAllocator(
                redisConnectionManager,
                workerId.getMaxId(),
                keyPrefix + "nodeId",
                keyPrefix + "nodeIdSet",
                workerId.getExpireSeconds()
        );
    }
    
    /**
     * 创建Redis机器状态存储
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisMachineStateStorage redisMachineStateStorage(RedisConnectionManager redisConnectionManager, 
                                                           CyxIdProperties properties) {
        CyxIdProperties.RedisProperties redis = properties.getRedis();
        String keyPrefix = redis.getKeyPrefix().getMachineState();
        
        log.info("创建Redis机器状态存储：键前缀={}", keyPrefix);
        
        return new RedisMachineStateStorage(
                redisConnectionManager,
                keyPrefix,
                null // 使用默认节点标识
        );
    }
} 