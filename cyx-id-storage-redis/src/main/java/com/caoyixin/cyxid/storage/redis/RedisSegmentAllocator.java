package com.caoyixin.cyxid.storage.redis;

import com.caoyixin.cyxid.core.exception.CyxIdException;
import com.caoyixin.cyxid.storage.api.IdSegment;
import com.caoyixin.cyxid.storage.api.SegmentAllocator;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

/**
 * Redis段分配器
 * 基于Redis实现的ID段分配器
 */
@Slf4j
public class RedisSegmentAllocator implements SegmentAllocator {
    
    /**
     * Redis连接管理器
     */
    private final RedisConnectionManager connectionManager;
    
    /**
     * ID段步长，即每次分配的ID数量
     */
    private final int step;
    
    /**
     * Redis中存储各段当前值的键前缀
     */
    private final String keyPrefix;
    
    /**
     * 创建Redis段分配器
     *
     * @param connectionManager Redis连接管理器
     * @param step ID段步长
     * @param keyPrefix Redis键前缀
     */
    public RedisSegmentAllocator(RedisConnectionManager connectionManager, int step, String keyPrefix) {
        this.connectionManager = connectionManager;
        this.step = step;
        this.keyPrefix = keyPrefix;
        
        log.info("初始化Redis段分配器：步长={}，键前缀={}", step, keyPrefix);
    }
    
    /**
     * 创建Redis段分配器（使用默认配置）
     *
     * @param connectionManager Redis连接管理器
     */
    public RedisSegmentAllocator(RedisConnectionManager connectionManager) {
        this(connectionManager, 1000, "cyx-id:segment:");
    }
    
    @Override
    public IdSegment nextSegment(String name) {
        String key = keyPrefix + name;
        
        try {
            // 使用Redis的INCRBY原子操作获取一段ID范围
            long maxId = connectionManager.execute(jedis -> {
                return jedis.incrBy(key, step);
            });
            
            // 计算最小ID
            long minId = maxId - step + 1;
            
            IdSegment segment = new IdSegment(minId, maxId);
            log.debug("分配ID段[{}]：{}", name, segment);
            
            return segment;
            
        } catch (Exception e) {
            log.error("从Redis分配ID段失败：{}", name, e);
            throw new CyxIdException("从Redis分配ID段失败：" + name, e);
        }
    }
    
    /**
     * 重置ID段
     *
     * @param name ID生成器名称
     * @param value 重置的值
     */
    public void resetSegment(String name, long value) {
        String key = keyPrefix + name;
        
        try {
            connectionManager.execute(jedis -> {
                jedis.set(key, String.valueOf(value));
                return null;
            });
            
            log.info("重置ID段[{}]值为：{}", name, value);
            
        } catch (Exception e) {
            log.error("重置ID段失败：{}", name, e);
            throw new CyxIdException("重置ID段失败：" + name, e);
        }
    }
} 