package com.caoyixin.cyxid.storage.api;

/**
 * 机器ID存储接口
 * 用于持久化和加载机器状态
 */
public interface MachineIdStorage {
    
    /**
     * 加载机器状态
     *
     * @return 机器状态
     */
    MachineState load();
    
    /**
     * 保存机器状态
     *
     * @param state 机器状态
     */
    void save(MachineState state);
} 