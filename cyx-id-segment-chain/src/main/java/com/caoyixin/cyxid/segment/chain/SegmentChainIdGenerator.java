package com.caoyixin.cyxid.segment.chain;

import com.caoyixin.cyxid.core.IdGenerator;
import com.caoyixin.cyxid.storage.api.SegmentAllocator;
import lombok.extern.slf4j.Slf4j;

/**
 * 链式分段ID生成器
 * 基于链表结构的高性能分段ID生成器，支持预取和无锁设计
 */
@Slf4j
public class SegmentChainIdGenerator implements IdGenerator {
    
    /**
     * 生成器名称
     */
    private final String name;
    
    /**
     * 链式分段ID缓存管理器
     */
    private final SegmentChainBufferManager bufferManager;
    
    /**
     * 创建链式分段ID生成器
     *
     * @param name 生成器名称
     * @param segmentAllocator ID段分配器
     * @param prefetchPeriod 预取周期（毫秒）
     * @param safeDistancePercent 安全距离百分比
     * @param maxChainLength 最大链长
     */
    public SegmentChainIdGenerator(String name, SegmentAllocator segmentAllocator,
                                 long prefetchPeriod, int safeDistancePercent, int maxChainLength) {
        this.name = name;
        this.bufferManager = new SegmentChainBufferManager(segmentAllocator, prefetchPeriod);
        
        // 获取并配置缓存器
        SegmentChainBuffer buffer = getBuffer();
        buffer.setSafeDistancePercent(safeDistancePercent);
        buffer.setMaxChainLength(maxChainLength);
        
        log.info("初始化链式分段ID生成器：{}，预取周期：{}ms，安全距离：{}%，最大链长：{}", 
                name, prefetchPeriod, safeDistancePercent, maxChainLength);
    }
    
    /**
     * 创建链式分段ID生成器（使用默认配置）
     *
     * @param name 生成器名称
     * @param segmentAllocator ID段分配器
     */
    public SegmentChainIdGenerator(String name, SegmentAllocator segmentAllocator) {
        this(name, segmentAllocator, 1000, 20, 3);
    }
    
    @Override
    public long generate() {
        return getBuffer().nextId();
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getType() {
        return "segment-chain";
    }
    
    /**
     * 获取当前生成器的缓存器
     *
     * @return 链式分段ID缓存器
     */
    private SegmentChainBuffer getBuffer() {
        return bufferManager.getBuffer(name);
    }
    
    /**
     * 关闭生成器，释放资源
     */
    public void shutdown() {
        bufferManager.shutdown();
    }
} 