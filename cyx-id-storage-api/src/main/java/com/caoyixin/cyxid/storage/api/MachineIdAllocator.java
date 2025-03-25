package com.caoyixin.cyxid.storage.api;

/**
 * 机器ID分配器接口
 * 用于分配和释放机器ID
 */
public interface MachineIdAllocator {
    
    /**
     * 分配机器ID
     *
     * @return 分配的机器ID
     */
    int allocate();
    
    /**
     * 释放机器ID
     *
     * @param machineId 要释放的机器ID
     * @return 释放结果，成功返回true，失败返回false
     */
    boolean release(int machineId);
} 