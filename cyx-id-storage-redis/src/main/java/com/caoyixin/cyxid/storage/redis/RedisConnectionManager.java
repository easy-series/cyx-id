package com.caoyixin.cyxid.storage.redis;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Redis连接管理器
 * 负责管理Redis连接池
 */
@Slf4j
public class RedisConnectionManager {
    
    /**
     * Redis连接池
     */
    private final JedisPool jedisPool;
    
    /**
     * 创建Redis连接管理器
     *
     * @param host Redis主机地址
     * @param port Redis端口
     * @param password Redis密码，可为null
     * @param database Redis数据库索引
     * @param timeout 连接超时时间（毫秒）
     * @param maxTotal 最大连接数
     * @param maxIdle 最大空闲连接数
     * @param minIdle 最小空闲连接数
     */
    public RedisConnectionManager(String host, int port, String password, int database,
                                int timeout, int maxTotal, int maxIdle, int minIdle) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);
        
        if (password != null && password.trim().isEmpty()) {
            password = null;
        }
        
        jedisPool = new JedisPool(config, host, port, timeout, password, database);
        
        log.info("初始化Redis连接池：{}:{}/{}", host, port, database);
    }
    
    /**
     * 创建Redis连接管理器（使用默认配置）
     *
     * @param host Redis主机地址
     * @param port Redis端口
     */
    public RedisConnectionManager(String host, int port) {
        this(host, port, null, 0, 2000, 20, 5, 2);
    }
    
    /**
     * 获取Jedis连接
     *
     * @return Jedis连接
     */
    public Jedis getConnection() {
        return jedisPool.getResource();
    }
    
    /**
     * 关闭连接池
     */
    public void shutdown() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
            log.info("关闭Redis连接池");
        }
    }
    
    /**
     * 执行Redis操作
     *
     * @param callback Redis操作回调
     * @param <T> 返回值类型
     * @return 操作结果
     */
    public <T> T execute(RedisCallback<T> callback) {
        try (Jedis jedis = getConnection()) {
            return callback.doInRedis(jedis);
        } catch (Exception e) {
            log.error("执行Redis操作异常", e);
            throw e;
        }
    }

    /**
     * Redis操作回调接口
     *
     * @param <T> 返回值类型
     */
    @FunctionalInterface
    public interface RedisCallback<T> {
        
        /**
         * 在Redis中执行操作
         *
         * @param jedis Jedis连接
         * @return 操作结果
         */
        T doInRedis(Jedis jedis);
    }
} 