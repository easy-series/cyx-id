package com.caoyixin.cyxid.snowflake;

import lombok.Getter;

/**
 * 静态节点ID分配器
 * 使用固定配置的方式分配节点ID
 */
public class StaticWorkerIdAssigner implements WorkerIdAssigner {
    
    /**
     * 工作节点ID
     */
    @Getter
    private final long workerId;
    
    /**
     * 创建静态节点ID分配器
     *
     * @param workerId 工作节点ID
     */
    public StaticWorkerIdAssigner(long workerId) {
        this.workerId = workerId;
    }
    
    @Override
    public long assignWorkerId() {
        return workerId;
    }
} 