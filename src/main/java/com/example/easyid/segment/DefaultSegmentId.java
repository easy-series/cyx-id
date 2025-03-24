package com.example.easyid.segment;

/**
 * 默认的号段ID生成器实现
 */
public class DefaultSegmentId implements SegmentId {
    
    private final SegmentIdDistributor distributor;
    private volatile IdSegment currentSegment;
    
    /**
     * 使用指定的号段分发器创建号段ID生成器
     *
     * @param distributor 号段分发器
     */
    public DefaultSegmentId(SegmentIdDistributor distributor) {
        this.distributor = distributor;
        // 初始化时不分配号段，延迟到首次使用时
        this.currentSegment = null;
    }
    
    @Override
    public synchronized long nextId() {
        // 如果当前号段为空或已耗尽，则获取新号段
        if (currentSegment == null || !currentSegment.isAvailable()) {
            currentSegment = distributor.nextSegment();
        }
        
        return currentSegment.nextId();
    }
    
    @Override
    public String getName() {
        return distributor.getName();
    }
    
    @Override
    public SegmentIdDistributor getDistributor() {
        return distributor;
    }
    
    @Override
    public IdSegment getCurrentSegment() {
        return currentSegment;
    }
} 