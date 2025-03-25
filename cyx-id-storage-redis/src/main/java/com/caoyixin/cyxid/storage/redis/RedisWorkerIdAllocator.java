package com.caoyixin.cyxid.storage.redis;

import com.caoyixin.cyxid.core.exception.MachineIdAllocationException;
import com.caoyixin.cyxid.storage.api.MachineIdAllocator;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Redis工作节点ID分配器
 * 基于Redis实现的工作节点ID分配器
 */
@Slf4j
public class RedisWorkerIdAllocator implements MachineIdAllocator {
    
    /**
     * Redis连接管理器
     */
    private final RedisConnectionManager connectionManager;
    
    /**
     * 工作节点ID的最大值
     */
    private final int maxWorkerId;
    
    /**
     * Redis中存储工作节点ID分配信息的键
     */
    private final String nodeIdKey;
    
    /**
     * Redis中存储工作节点ID集合的键
     */
    private final String nodeIdSetKey;
    
    /**
     * 当前节点的工作节点ID
     */
    private volatile int workerId = -1;
    
    /**
     * 节点标识，用于唯一标识当前节点
     */
    private final String nodeIdentity;
    
    /**
     * 工作节点ID的过期时间（秒）
     */
    private final int expireSeconds;
    
    /**
     * 创建Redis工作节点ID分配器
     *
     * @param connectionManager Redis连接管理器
     * @param maxWorkerId 工作节点ID的最大值
     * @param nodeIdKey Redis中存储工作节点ID分配信息的键
     * @param nodeIdSetKey Redis中存储工作节点ID集合的键
     * @param expireSeconds 工作节点ID的过期时间（秒）
     */
    public RedisWorkerIdAllocator(RedisConnectionManager connectionManager, int maxWorkerId,
                               String nodeIdKey, String nodeIdSetKey, int expireSeconds) {
        this.connectionManager = connectionManager;
        this.maxWorkerId = maxWorkerId;
        this.nodeIdKey = nodeIdKey;
        this.nodeIdSetKey = nodeIdSetKey;
        this.expireSeconds = expireSeconds;
        this.nodeIdentity = generateNodeIdentity();
        
        log.info("初始化Redis工作节点ID分配器：最大值={}，节点标识={}", maxWorkerId, nodeIdentity);
    }
    
    /**
     * 创建Redis工作节点ID分配器（使用默认配置）
     *
     * @param connectionManager Redis连接管理器
     */
    public RedisWorkerIdAllocator(RedisConnectionManager connectionManager) {
        this(connectionManager, 1023, "cyx-id:worker:nodeId", "cyx-id:worker:nodeIdSet", 60);
    }
    
    @Override
    public int allocate() {
        if (workerId >= 0) {
            return workerId;
        }
        
        synchronized (this) {
            if (workerId >= 0) {
                return workerId;
            }
            
            try {
                // 1. 尝试从Redis中获取已分配的工作节点ID
                Integer existingWorkerId = findExistingWorkerId();
                if (existingWorkerId != null) {
                    workerId = existingWorkerId;
                    log.info("从Redis获取已分配的工作节点ID：{}", workerId);
                    return workerId;
                }
                
                // 2. 尝试分配新的工作节点ID
                workerId = allocateNewWorkerId();
                log.info("分配新的工作节点ID：{}", workerId);
                
                // 3. 启动心跳线程，防止工作节点ID过期
                startHeartbeatThread();
                
                return workerId;
                
            } catch (Exception e) {
                log.error("分配工作节点ID失败", e);
                throw new MachineIdAllocationException("分配工作节点ID失败", e);
            }
        }
    }
    
    @Override
    public boolean release(int machineId) {
        if (machineId != workerId) {
            log.warn("尝试释放的工作节点ID与当前分配的不一致：{} != {}", machineId, workerId);
            return false;
        }
        
        try {
            // 从Redis中删除工作节点ID映射和集合中的记录
            boolean result = connectionManager.execute(jedis -> {
                // 删除节点映射
                long deletedKeys = jedis.del(nodeIdKey + ":" + nodeIdentity);
                
                // 从集合中移除
                jedis.srem(nodeIdSetKey, String.valueOf(workerId));
                
                return deletedKeys > 0;
            });
            
            if (result) {
                log.info("释放工作节点ID成功：{}", workerId);
                workerId = -1;
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("释放工作节点ID失败：{}", workerId, e);
            return false;
        }
    }
    
    /**
     * 从Redis中查找已分配的工作节点ID
     *
     * @return 已分配的工作节点ID，如果未分配则返回null
     */
    private Integer findExistingWorkerId() {
        return connectionManager.execute(jedis -> {
            String nodeKey = nodeIdKey + ":" + nodeIdentity;
            String workerIdStr = jedis.get(nodeKey);
            
            if (workerIdStr != null) {
                // 刷新过期时间
                jedis.expire(nodeKey, expireSeconds);
                return Integer.parseInt(workerIdStr);
            }
            
            return null;
        });
    }
    
    /**
     * 分配新的工作节点ID
     *
     * @return 新分配的工作节点ID
     * @throws MachineIdAllocationException 如果无法分配工作节点ID
     */
    private int allocateNewWorkerId() {
        return connectionManager.execute(jedis -> {
            // 获取已分配的所有工作节点ID
            Set<String> allocatedIds = jedis.smembers(nodeIdSetKey);
            
            // 找到一个未分配的ID
            int newWorkerId = findAvailableWorkerId(allocatedIds);
            
            // 使用Lua脚本保证原子性
            String script = "if redis.call('sadd', KEYS[1], ARGV[1]) == 1 then " +
                    "redis.call('setex', KEYS[2], ARGV[2], ARGV[1]) " +
                    "return 1 " +
                    "else " +
                    "return 0 " +
                    "end";
            
            String nodeKey = nodeIdKey + ":" + nodeIdentity;
            Object result = jedis.eval(
                    script,
                    2,
                    nodeIdSetKey, nodeKey,
                    String.valueOf(newWorkerId), String.valueOf(expireSeconds)
            );
            
            if (!"1".equals(result.toString())) {
                throw new MachineIdAllocationException("无法分配工作节点ID：并发冲突");
            }
            
            return newWorkerId;
        });
    }
    
    /**
     * 在已分配的ID集合中找一个可用的ID
     *
     * @param allocatedIds 已分配的ID集合
     * @return 可用的ID
     * @throws MachineIdAllocationException 如果没有可用的ID
     */
    private int findAvailableWorkerId(Set<String> allocatedIds) {
        Set<Integer> allocated = new HashSet<>();
        for (String idStr : allocatedIds) {
            try {
                allocated.add(Integer.parseInt(idStr));
            } catch (NumberFormatException e) {
                log.warn("忽略无效的工作节点ID：{}", idStr);
            }
        }
        
        // 寻找第一个未分配的ID
        for (int id = 0; id <= maxWorkerId; id++) {
            if (!allocated.contains(id)) {
                return id;
            }
        }
        
        throw new MachineIdAllocationException("无法分配工作节点ID：已达到最大数量 " + maxWorkerId);
    }
    
    /**
     * 生成节点标识
     * 使用主机名和MAC地址的组合
     *
     * @return 节点标识
     */
    private String generateNodeIdentity() {
        try {
            InetAddress address = InetAddress.getLocalHost();
            String hostname = address.getHostName();
            NetworkInterface ni = NetworkInterface.getByInetAddress(address);
            
            StringBuilder sb = new StringBuilder();
            sb.append(hostname);
            
            if (ni != null) {
                byte[] mac = ni.getHardwareAddress();
                if (mac != null) {
                    sb.append(":");
                    for (byte b : mac) {
                        sb.append(String.format("%02X", b));
                    }
                }
            }
            
            return sb.toString();
            
        } catch (UnknownHostException | SocketException e) {
            log.warn("生成节点标识失败，使用随机值", e);
            return "node-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);
        }
    }
    
    /**
     * 启动心跳线程，定期刷新Redis中的过期时间
     */
    private void startHeartbeatThread() {
        Thread heartbeatThread = new Thread(() -> {
            String nodeKey = nodeIdKey + ":" + nodeIdentity;
            
            while (!Thread.currentThread().isInterrupted() && workerId >= 0) {
                try {
                    // 刷新过期时间
                    boolean success = connectionManager.execute(jedis -> {
                        return jedis.expire(nodeKey, expireSeconds) == 1L;
                    });
                    
                    if (!success) {
                        log.warn("刷新工作节点ID过期时间失败，尝试重新注册");
                        connectionManager.execute(jedis -> {
                            return jedis.setex(nodeKey, expireSeconds, String.valueOf(workerId));
                        });
                    }
                    
                    // 休眠一段时间，比过期时间短，确保不会过期
                    Thread.sleep(expireSeconds * 1000 / 3);
                    
                } catch (InterruptedException e) {
                    log.info("心跳线程被中断");
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("心跳线程执行异常", e);
                    // 继续尝试
                }
            }
            
            log.info("心跳线程退出");
        });
        
        heartbeatThread.setName("worker-id-heartbeat");
        heartbeatThread.setDaemon(true);
        heartbeatThread.start();
        
        log.info("启动工作节点ID心跳线程，过期时间：{}秒", expireSeconds);
    }
} 