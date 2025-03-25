package com.caoyixin.cyxid.snowflake;

/**
 * 节点ID分配器
 * 用于为雪花算法分配工作节点ID
 */
public interface WorkerIdAssigner {
    
    /**
     * 分配工作节点ID
     *
     * @return 工作节点ID
     */
    long assignWorkerId();
    
    /**
     * 释放工作节点ID
     * 主要用于服务下线时释放占用的ID
     * 
     * @param workerId 要释放的工作节点ID
     */
    default void releaseWorkerId(long workerId) {
        // 默认不做任何操作
    }
} 