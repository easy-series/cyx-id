package com.example.easyid.machine;

/**
 * 机器号分配器接口
 * 用于在分布式环境下为不同节点分配唯一的机器号
 */
public interface MachineIdDistributor {
    
    /**
     * 获取应用的命名空间
     *
     * @return 命名空间
     */
    String getNamespace();
    
    /**
     * 分配机器号
     *
     * @param maxMachineId 最大机器号（不包含）
     * @return 分配的机器号
     */
    int distribute(int maxMachineId);
    
    /**
     * 释放机器号
     *
     * @param machineId 要释放的机器号
     * @return 是否释放成功
     */
    boolean release(int machineId);
} 