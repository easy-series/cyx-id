package com.example.easyid.machine;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 基于Redis的机器号分配器
 * 使用Redis Set实现机器号的分配和释放
 */
public class RedisMachineIdDistributor implements MachineIdDistributor {
    
    private static final String MACHINE_ID_KEY_PREFIX = "easy-id:machine:";
    private static final String MACHINE_ID_LOCK_PREFIX = "easy-id:machine:lock:";
    private static final Duration LOCK_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration LEASE_TIME = Duration.ofMinutes(5);
    
    private final StringRedisTemplate redisTemplate;
    private final String namespace;
    private final String instanceId;
    
    /**
     * 创建Redis机器号分配器
     *
     * @param redisTemplate Redis模板
     * @param namespace 命名空间
     * @param instanceId 实例ID，通常是主机名或容器ID
     */
    public RedisMachineIdDistributor(StringRedisTemplate redisTemplate, String namespace, String instanceId) {
        this.redisTemplate = redisTemplate;
        this.namespace = namespace;
        this.instanceId = instanceId;
    }
    
    @Override
    public String getNamespace() {
        return namespace;
    }
    
    @Override
    public int distribute(int maxMachineId) {
        String machineIdKey = getMachineIdKey();
        String lockKey = getLockKey();
        
        // 尝试获取分布式锁
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, instanceId, LOCK_TIMEOUT);
        
        if (Boolean.TRUE.equals(acquired)) {
            try {
                // 查询是否已经分配了机器号
                String machineIdStr = redisTemplate.opsForValue().get(machineIdKey);
                if (machineIdStr != null) {
                    try {
                        int machineId = Integer.parseInt(machineIdStr);
                        // 刷新过期时间
                        redisTemplate.expire(machineIdKey, LEASE_TIME);
                        return machineId;
                    } catch (NumberFormatException e) {
                        // 如果存储的不是有效的机器号，则清除并重新分配
                        redisTemplate.delete(machineIdKey);
                    }
                }
                
                // 查找未使用的机器号
                int machineId = findAvailableMachineId(maxMachineId);
                
                // 保存分配结果
                redisTemplate.opsForValue().set(machineIdKey, String.valueOf(machineId), LEASE_TIME);
                return machineId;
            } finally {
                // 释放锁
                redisTemplate.delete(lockKey);
            }
        } else {
            // 如果无法获取锁，等待并重试
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return distribute(maxMachineId);
        }
    }
    
    @Override
    public boolean release(int machineId) {
        String machineIdKey = getMachineIdKey();
        return Boolean.TRUE.equals(redisTemplate.delete(machineIdKey));
    }
    
    private int findAvailableMachineId(int maxMachineId) {
        // 获取当前已分配的所有机器号
        String machineSetKey = MACHINE_ID_KEY_PREFIX + namespace + ":set";
        List<String> allMachineIds = new ArrayList<>(redisTemplate.opsForSet().members(machineSetKey));
        
        // 找到未使用的最小机器号
        for (int i = 0; i < maxMachineId; i++) {
            String machineIdStr = String.valueOf(i);
            if (!allMachineIds.contains(machineIdStr)) {
                // 找到未使用的机器号，将其加入集合
                redisTemplate.opsForSet().add(machineSetKey, machineIdStr);
                return i;
            }
        }
        
        throw new IllegalStateException("没有可用的机器号，已达到最大上限: " + maxMachineId);
    }
    
    private String getMachineIdKey() {
        return MACHINE_ID_KEY_PREFIX + namespace + ":" + instanceId;
    }
    
    private String getLockKey() {
        return MACHINE_ID_LOCK_PREFIX + namespace;
    }
} 