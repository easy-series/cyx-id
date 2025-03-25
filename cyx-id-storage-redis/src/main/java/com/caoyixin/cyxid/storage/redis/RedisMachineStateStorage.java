package com.caoyixin.cyxid.storage.redis;

import com.caoyixin.cyxid.core.exception.CyxIdException;
import com.caoyixin.cyxid.storage.api.MachineState;
import com.caoyixin.cyxid.storage.api.MachineIdStorage;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

/**
 * Redis机器状态存储
 * 基于Redis实现的机器状态存储
 */
@Slf4j
public class RedisMachineStateStorage implements MachineIdStorage {
    
    /**
     * Redis连接管理器
     */
    private final RedisConnectionManager connectionManager;
    
    /**
     * Redis中存储机器状态的键前缀
     */
    private final String keyPrefix;
    
    /**
     * 节点标识
     */
    private final String nodeIdentity;
    
    /**
     * 创建Redis机器状态存储
     *
     * @param connectionManager Redis连接管理器
     * @param keyPrefix Redis键前缀
     * @param nodeIdentity 节点标识
     */
    public RedisMachineStateStorage(RedisConnectionManager connectionManager, String keyPrefix, String nodeIdentity) {
        this.connectionManager = connectionManager;
        this.keyPrefix = keyPrefix;
        this.nodeIdentity = nodeIdentity;
        
        log.info("初始化Redis机器状态存储：键前缀={}，节点标识={}", keyPrefix, nodeIdentity);
    }
    
    /**
     * 创建Redis机器状态存储（使用默认配置）
     *
     * @param connectionManager Redis连接管理器
     */
    public RedisMachineStateStorage(RedisConnectionManager connectionManager) {
        this(connectionManager, "cyx-id:machine:", generateNodeIdentity());
    }
    
    @Override
    public MachineState load() {
        String key = keyPrefix + nodeIdentity;
        
        try {
            return connectionManager.execute(jedis -> {
                String value = jedis.get(key);
                if (value == null || value.isEmpty()) {
                    log.warn("未找到机器状态：{}", key);
                    return MachineState.NOT_FOUND;
                }
                
                String[] parts = value.split(":");
                if (parts.length != 2) {
                    log.warn("机器状态格式无效：{}", value);
                    return MachineState.NOT_FOUND;
                }
                
                try {
                    int machineId = Integer.parseInt(parts[0]);
                    long lastTimeStamp = Long.parseLong(parts[1]);
                    log.info("加载机器状态：machineId={}, lastTimeStamp={}", machineId, lastTimeStamp);
                    return new MachineState(machineId, lastTimeStamp);
                } catch (NumberFormatException e) {
                    log.warn("机器状态数据格式无效：{}", value, e);
                    return MachineState.NOT_FOUND;
                }
            });
            
        } catch (Exception e) {
            log.error("加载机器状态失败", e);
            throw new CyxIdException("加载机器状态失败", e);
        }
    }
    
    @Override
    public void save(MachineState state) {
        String key = keyPrefix + nodeIdentity;
        String value = state.getMachineId() + ":" + state.getLastTimeStamp();
        
        try {
            connectionManager.execute(jedis -> {
                jedis.set(key, value);
                log.info("保存机器状态：{}={}", key, value);
                return null;
            });
            
        } catch (Exception e) {
            log.error("保存机器状态失败", e);
            throw new CyxIdException("保存机器状态失败", e);
        }
    }
    
    /**
     * 生成节点标识
     *
     * @return 节点标识
     */
    private static String generateNodeIdentity() {
        try {
            String hostname = java.net.InetAddress.getLocalHost().getHostName();
            return hostname + "-" + System.currentTimeMillis();
        } catch (Exception e) {
            log.warn("获取主机名失败，使用随机值", e);
            return "node-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);
        }
    }
} 