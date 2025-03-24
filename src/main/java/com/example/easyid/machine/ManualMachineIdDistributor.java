package com.example.easyid.machine;

/**
 * 手动指定机器号的分配器
 * 适用于固定部署环境，手动配置机器号的场景
 */
public class ManualMachineIdDistributor implements MachineIdDistributor {
    
    private final String namespace;
    private final int machineId;
    
    /**
     * 创建手动机器号分配器
     *
     * @param namespace 命名空间
     * @param machineId 手动指定的机器号
     */
    public ManualMachineIdDistributor(String namespace, int machineId) {
        this.namespace = namespace;
        this.machineId = machineId;
    }
    
    @Override
    public String getNamespace() {
        return namespace;
    }
    
    @Override
    public int distribute(int maxMachineId) {
        if (machineId >= maxMachineId) {
            throw new IllegalArgumentException(
                    "指定的机器号[" + machineId + "]超出最大值[" + maxMachineId + "]");
        }
        return machineId;
    }
    
    @Override
    public boolean release(int machineId) {
        // 手动分配的机器号无需释放
        return true;
    }
} 