package com.caoyixin.cyxid.segment.chain;

import com.caoyixin.cyxid.core.IdGenerator;
import com.caoyixin.cyxid.storage.api.SegmentAllocator;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 链式分段ID生成器工厂
 * 用于创建和管理多个链式分段ID生成器实例
 */
@Slf4j
public class SegmentChainIdGeneratorFactory {
    
    /**
     * 生成器缓存，按名称索引
     */
    private final Map<String, SegmentChainIdGenerator> generatorCache = new ConcurrentHashMap<>();
    
    /**
     * ID段分配器
     */
    private final SegmentAllocator segmentAllocator;
    
    /**
     * 预取周期（毫秒）
     */
    private final long prefetchPeriod;
    
    /**
     * 安全距离百分比
     */
    private final int safeDistancePercent;
    
    /**
     * 最大链长
     */
    private final int maxChainLength;
    
    /**
     * 创建链式分段ID生成器工厂
     *
     * @param segmentAllocator ID段分配器
     * @param prefetchPeriod 预取周期（毫秒）
     * @param safeDistancePercent 安全距离百分比
     * @param maxChainLength 最大链长
     */
    public SegmentChainIdGeneratorFactory(SegmentAllocator segmentAllocator, long prefetchPeriod, 
                                       int safeDistancePercent, int maxChainLength) {
        this.segmentAllocator = segmentAllocator;
        this.prefetchPeriod = prefetchPeriod;
        this.safeDistancePercent = safeDistancePercent;
        this.maxChainLength = maxChainLength;
    }
    
    /**
     * 创建链式分段ID生成器工厂（使用默认配置）
     *
     * @param segmentAllocator ID段分配器
     */
    public SegmentChainIdGeneratorFactory(SegmentAllocator segmentAllocator) {
        this(segmentAllocator, 1000, 20, 3);
    }
    
    /**
     * 获取或创建指定名称的链式分段ID生成器
     *
     * @param name 生成器名称
     * @return 链式分段ID生成器
     */
    public IdGenerator getGenerator(String name) {
        return generatorCache.computeIfAbsent(name, this::createGenerator);
    }
    
    /**
     * 创建新的链式分段ID生成器
     *
     * @param name 生成器名称
     * @return 新创建的链式分段ID生成器
     */
    private SegmentChainIdGenerator createGenerator(String name) {
        log.info("创建链式分段ID生成器：{}", name);
        return new SegmentChainIdGenerator(name, segmentAllocator, 
                prefetchPeriod, safeDistancePercent, maxChainLength);
    }
    
    /**
     * 关闭工厂，释放资源
     */
    public void shutdown() {
        for (SegmentChainIdGenerator generator : generatorCache.values()) {
            try {
                generator.shutdown();
            } catch (Exception e) {
                log.error("关闭生成器异常：{}", generator.getName(), e);
            }
        }
        generatorCache.clear();
    }
} 