package com.example.easyid.segment;

/**
 * ID号段模型
 * 表示一个ID号段的范围和状态
 */
public class IdSegment {
    /**
     * 号段最小值（包含）
     */
    private final long minId;
    
    /**
     * 号段最大值（包含）
     */
    private final long maxId;
    
    /**
     * 号段步长
     */
    private final int step;
    
    /**
     * 当前ID值
     */
    private volatile long currentId;
    
    /**
     * 创建号段
     *
     * @param minId 号段最小值
     * @param step 号段步长
     */
    public IdSegment(long minId, int step) {
        this.minId = minId;
        this.step = step;
        this.maxId = minId + step - 1;
        this.currentId = minId - 1; // 初始值设置为最小值-1，第一次获取时会自增为最小值
    }
    
    /**
     * 判断号段是否可用（未耗尽）
     *
     * @return 如果号段未耗尽则返回true
     */
    public boolean isAvailable() {
        return currentId < maxId;
    }
    
    /**
     * 获取号段下一个可用ID
     *
     * @return 下一个ID
     * @throws IllegalStateException 如果号段已耗尽则抛出异常
     */
    public synchronized long nextId() {
        if (!isAvailable()) {
            throw new IllegalStateException("号段已耗尽");
        }
        return ++currentId;
    }
    
    /**
     * 获取号段的剩余可用数量
     *
     * @return 剩余可用数量
     */
    public long getRemaining() {
        return Math.max(0, maxId - currentId);
    }
    
    /**
     * 获取号段的使用率
     *
     * @return 使用率，0.0-1.0之间的浮点数
     */
    public double getUsageRate() {
        return 1.0 - (double) getRemaining() / step;
    }
    
    /**
     * 获取号段最小值
     *
     * @return 号段最小值
     */
    public long getMinId() {
        return minId;
    }
    
    /**
     * 获取号段最大值
     *
     * @return 号段最大值
     */
    public long getMaxId() {
        return maxId;
    }
    
    /**
     * 获取号段步长
     *
     * @return 号段步长
     */
    public int getStep() {
        return step;
    }
    
    /**
     * 获取当前ID值
     *
     * @return 当前ID值
     */
    public long getCurrentId() {
        return currentId;
    }
    
    @Override
    public String toString() {
        return "IdSegment{" +
                "minId=" + minId +
                ", maxId=" + maxId +
                ", step=" + step +
                ", currentId=" + currentId +
                ", remaining=" + getRemaining() +
                ", usageRate=" + getUsageRate() +
                '}';
    }
} 