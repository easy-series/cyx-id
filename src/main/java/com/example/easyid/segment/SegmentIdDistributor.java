package com.example.easyid.segment;

/**
 * 号段分发器接口
 * 用于管理和分配ID号段
 */
public interface SegmentIdDistributor {
    
    /**
     * 获取分发器的名称
     *
     * @return 分发器名称
     */
    String getName();
    
    /**
     * 获取下一个可用的ID号段
     *
     * @return ID号段
     */
    IdSegment nextSegment();
    
    /**
     * 获取号段的起始偏移量
     *
     * @return 偏移量
     */
    long getOffset();
    
    /**
     * 获取号段步长
     *
     * @return 步长
     */
    int getStep();
} 