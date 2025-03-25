package com.caoyixin.cyxid.storage.api;

/**
 * ID段分配器接口
 * 用于分配ID段
 */
public interface SegmentAllocator {
    
    /**
     * 获取下一个ID段
     *
     * @param name ID生成器名称
     * @return 分配的ID段
     */
    IdSegment nextSegment(String name);
} 