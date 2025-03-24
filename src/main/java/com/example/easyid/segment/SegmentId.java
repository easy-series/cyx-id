package com.example.easyid.segment;

import com.example.easyid.core.IdGenerator;

/**
 * 号段ID生成器接口
 */
public interface SegmentId extends IdGenerator {
    
    /**
     * 获取号段分发器
     *
     * @return 号段分发器
     */
    SegmentIdDistributor getDistributor();
    
    /**
     * 获取当前可用的ID号段
     *
     * @return 当前可用的ID号段
     */
    IdSegment getCurrentSegment();
} 