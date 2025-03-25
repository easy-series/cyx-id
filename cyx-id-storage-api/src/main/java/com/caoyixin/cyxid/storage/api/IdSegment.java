package com.caoyixin.cyxid.storage.api;

import lombok.Getter;

/**
 * ID段
 * 表示一段连续的ID范围
 */
@Getter
public class IdSegment {
    
    /**
     * 段的最小ID（包含）
     */
    private final long minId;
    
    /**
     * 段的最大ID（包含）
     */
    private final long maxId;
    
    /**
     * 创建ID段
     *
     * @param minId 最小ID（包含）
     * @param maxId 最大ID（包含）
     */
    public IdSegment(long minId, long maxId) {
        if (minId > maxId) {
            throw new IllegalArgumentException("最小ID不能大于最大ID");
        }
        this.minId = minId;
        this.maxId = maxId;
    }
    
    /**
     * 获取段的步长
     *
     * @return 段的步长
     */
    public long getStep() {
        return maxId - minId + 1;
    }
    
    /**
     * 是否为空段
     *
     * @return 如果为空段，返回true
     */
    public boolean isEmpty() {
        return minId > maxId;
    }
    
    @Override
    public String toString() {
        return "IdSegment{" +
                "minId=" + minId +
                ", maxId=" + maxId +
                ", step=" + getStep() +
                '}';
    }
} 